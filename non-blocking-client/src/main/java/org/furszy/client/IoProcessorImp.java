package org.furszy.client;

import org.furszy.client.basic.ConnectionId;
import org.furszy.client.basic.IoSessionImp;
import org.furszy.client.basic.SessionState;
import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.furszy.client.basic.IoSessionImp.ATTR_CONNECT_FUTURE;

/**
 * Created by mati on 14/05/17.
 */

public class IoProcessorImp implements IoProcessor {

    private Logger log = LoggerFactory.getLogger(IoProcessorImp.class);

    private static final long SELECT_TIMEOUT = 1000L;
    /** Sessions references */
    private ConcurrentMap<ConnectionId,IoSessionImp> sessions = new ConcurrentHashMap<>();

    /** The processor thread : it handles the incoming messages */
    private final AtomicReference<ProcessorLoop> processorRef = new AtomicReference<ProcessorLoop>();

    private ConcurrentLinkedQueue<IoSessionImp> newSessions = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<IoSessionImp> removingSessions = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<IoSessionImp> flushingSessions = new ConcurrentLinkedQueue<>();
    /** Selector */
    private SelectorWrapper selectorWrapper;
    /** Processor channel reader */
    private IoProcessorReader ioProcessorReader;
    private IoProcessorWriter ioProcessorWriter;
    private boolean disposing;
    private AtomicBoolean disposed = new AtomicBoolean(false);

    private ExecutorService executor;

    public IoProcessorImp(SelectorProvider selectorProvider, ExecutorService executorService) throws IOException {
        ioProcessorReader = new IoProcessorReader(this);
        ioProcessorWriter = new IoProcessorWriter(this);
        selectorWrapper = new SelectorWrapper(selectorProvider);
        executor = executorService;
    }


    /**
     * Starts the inner Processor, asking the executor to pick a thread in its
     * pool. The Runnable will be renamed
     */
    private void startupProcessor() {
        ProcessorLoop processor = processorRef.get();

        if (processor == null) {
            processor = new ProcessorLoop();

            if (processorRef.compareAndSet(null, processor)) {
                executor.execute(new NamePreservingRunnable(processor, "IoProcessorImp"));
            }
        }

        // Just stop the select() and start it again, so that the processor
        // can be activated immediately.
        selectorWrapper.wakeup();
    }

    /**
     * Add session
     *
     * @param ioSessionImp
     * @throws Exception
     */
    public void add(IoSessionImp ioSessionImp) throws Exception {
        if (disposed.get() || disposing) {
            throw new IllegalStateException("Already disposed.");
        }
        log.info("Adding session to processor",ioSessionImp);
        newSessions.add(ioSessionImp);
        ioSessionImp.setProcessor(this);
        startupProcessor();
    }

    @Override
    public IoSessionImp getActiveSession(ConnectionId connectionId) {
        return sessions.get(connectionId);
    }

    @Override
    public <M> void scheduleForFlush(IoSessionImp mIoSessionImp) {
        if (!flushingSessions.contains(mIoSessionImp)){
            flushingSessions.add(mIoSessionImp);
        }
    }

    /**
     * Add session to remove queue
     * @param session
     */
    public void scheduleRemove(IoSessionImp session) {
        if (!removingSessions.contains(session)) {
            removingSessions.add(session);
        }
    }

    public void setInterestedInWrite(IoSessionImp session, boolean flag) throws Exception {
        selectorWrapper.setInterestedInWrite(session.getSelectionKey(),flag);
    }

    public boolean isDisposing() {
        return disposing;
    }

    public void setDisposing(boolean disposing) {
        this.disposing = disposing;
    }


    /**
     * The main loop. This is the place in charge to poll the Selector, and to
     * process the active sessions. It's done in - handle the newly created
     * sessions -
     */
    private class ProcessorLoop implements Runnable {
        public void run() {

            int nSessions = 0;
//            lastIdleCheckTime = System.currentTimeMillis();


            for (; ; )
                try {
                    // This select has a timeout so that we can manage
                    // idle session when we get out of the select every
                    // second. (note : this is a hack to avoid creating
                    // a dedicated thread).
                    long t0 = System.currentTimeMillis();
                    int selected = selectorWrapper.select(SELECT_TIMEOUT);
                    long t1 = System.currentTimeMillis();
                    long delta = (t1 - t0);
                    if (!selectorWrapper.getWakeUpAndSet(false) && (selected == 0) && (delta < 100)) {
                        // Last chance : the select() may have been
                        // interrupted because we have had an closed channel.
                        if (selectorWrapper.isBrokenConnection()) {
                            log.warn("Broken connection");
                        } else {
                            log.warn("Create a new selector. Selected is 0, delta = " + (t1 - t0));
                            // Ok, we are hit by the nasty epoll
                            // spinning.
                            // Basically, there is a race condition
                            // which causes a closing file descriptor not to be
                            // considered as available as a selected channel,
                            // but
                            // it stopped the select. The next time we will
                            // call select(), it will exit immediately for the
                            // same
                            // reason, and do so forever, consuming 100%
                            // CPU.
                            // We have to destroy the selector, and
                            // registerConnect all the socket on a new one.
                            selectorWrapper.registerNewSelector();
                        }
                    }
                    // Manage newly created session first
                    nSessions += handleNewSessions();


                    // Now, if we have had some incoming or outgoing events,
                    // deal with them
                    if (selected > 0) {
                        // LOG.debug("Processing ..."); // This log hurts one of
                        // the MDCFilter test...
                        process();
                    }
                    // Write the pending requests
                    long currentTime = System.currentTimeMillis();
                    flush(currentTime);

                    // And manage removed sessions
                    nSessions -= removeSessions();

                    // Last, not least, send Idle events to the idle sessions
//                    notifyIdleSessions(currentTime);


                    // Get a chance to exit the infinite loop if there are no
                    // more sessions on this Processor
                    if (nSessions == 0) {
                        processorRef.set(null);

                        ProcessorLoop processorLoop = this;

                        if (newSessions.isEmpty() && selectorWrapper.isSelectorEmpty()) {

                            // newSessions.add() precedes startupProcessor
                            assert (processorRef.get() != processorLoop);
                            break;
                        }

                        assert (processorRef.get() != this);

                        if (!processorRef.compareAndSet(null, this)) {
                            // startupProcessor won race, so must exit processor
                            assert (processorRef.get() != this);
                            break;
                        }

                        assert (processorRef.get() == this);
                    }

                    // Disconnect all sessions immediately if disposal has been
                    // requested so that we exit this loop eventually.
                    if (isDisposing()) {
                        boolean hasKeys = false;

                        for (Iterator<IoSessionImp> i = selectorWrapper.allSessions(); i.hasNext(); ) {
                            IoSessionImp session = i.next();

                            if (session.isActive()) {
                                scheduleRemove((IoSessionImp) session);
                                hasKeys = true;
                            }
                        }

                        if (hasKeys) {
                            selectorWrapper.wakeup();
                        }
                    }

                } catch (ClosedSelectorException cse) {
                    // If the selector has been closed, we can exit the loop
                    // But first, dump a stack trace
                    log.error("Main loop",cse);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Main loop",e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        log.error("Main loop",e1);
                    }
                }
        }
    }

    /**
     * Loops over the new sessions blocking queue and returns the number of
     * sessions which are effectively created
     *
     * @return The number of new sessions
     */
    private int handleNewSessions() {
        int addedSessions = 0;
        for (IoSessionImp session = newSessions.poll(); session != null; session = newSessions.poll()) {
            if (addNow(session)) {
                // A new session has been created
                addedSessions++;
            }
        }
        return addedSessions;
    }

    /**
     * Process a new session : - initialize it - create its chain - fire the
     * CREATED listeners if any
     *
     * @param session The session to create
     * @return <tt>true</tt> if the session has been registered
     */
    private boolean addNow(IoSessionImp session) {
        boolean registered = false;

        try {
            init(session);
            registered = true;
            // todo: acá deberia agregar el sslEngine y demás cosas.

            // raise creation notification
            try {
                session.getIoHandler().sessionCreated(session);
            } catch (Exception e) {
                log.error("add fail",e,session);
                session.getIoHandler().exceptionCaught(session,e);
            }
            // get and remove the connection future from the session
            ConnectFuture connectFuture = (ConnectFuture) session.removeAttribute(ATTR_CONNECT_FUTURE);
            ConnectionId connectionId = new ConnectionId(session.getId());
            // add session to reference map
            sessions.put(connectionId,session);
            // release the future setting the id
            connectFuture.setConnectionId(connectionId);

        } catch (Exception e) {
            e.printStackTrace();
            log.error("addNow",e,session);

            try {
                destroy(session);
            } catch (Exception e1) {
                log.error("Exception destroying session",e1,session);
            } finally {
                registered = false;
            }
        }

        return registered;
    }

    /**
     * Init session to read in selector
     * @param session
     * @throws Exception
     */
    private void init(IoSessionImp session) throws Exception {
        SelectableChannel ch = session.getChannel();
        ch.configureBlocking(false);
        session.setSelectionKey(selectorWrapper.registerRead((SocketChannel) ch,session));
    }

    /**
     * Destroy session
     * @param session
     * @throws Exception
     */
    void destroy(IoSessionImp session) throws Exception {
        ByteChannel ch = (ByteChannel) session.getChannel();
        SelectionKey key = session.getSelectionKey();
        if (key != null) {
            key.cancel();
        }
        ch.close();
    }

    /**
     * Loop every single open session
     * @throws Exception
     */
    private void process() throws Exception {
        for (Iterator<IoSessionImp> i = selectorWrapper.selectedSessions(); i.hasNext();) {
            IoSessionImp session = i.next();
            process(session);
            i.remove();
        }
    }

    /**
     * Deal with session ready for the read or write operations, or both.
     */
    private void process(IoSessionImp session) {
        // Process Reads
        if (selectorWrapper.isReadable(session) && !session.isReadSuspended()) {
            ioProcessorReader.read(session);
        }

        // Process writes
        if (selectorWrapper.isWritable(session) && !session.isWriteSuspended()) {
            // add the session to the queue, if it's not already there
            if (session.setScheduledForFlush(true)) {
                flushingSessions.add(session);
            }
        }
    }

    private SessionState getState(IoSessionImp session) {
        SelectionKey key = session.getSelectionKey();

        if (key == null) {
            // The channel is not yet registred to a selector
            return SessionState.OPENING;
        }

        if (key.isValid()) {
            // The session is opened
            return SessionState.OPENED;
        } else {
            // The session still as to be closed
            return SessionState.CLOSING;
        }
    }

    public void scheduleFlush(IoSessionImp session) {
        // add the session to the queue if it's not already
        // in the queue
        if (session.setScheduledForFlush(true)) {
            flushingSessions.add(session);
        }
    }
    /**
     * Write all the pending messages
     */
    private void flush(long currentTime) {
        if (flushingSessions.isEmpty()) {
            return;
        }

        do {
            IoSessionImp session = flushingSessions.poll(); // the same one with
            // firstSession

            if (session == null) {
                // Just in case ... It should not happen.
                break;
            }

            // Reset the Schedule for flush flag for this session,
            // as we are flushing it now
            session.unscheduledForFlush();

            SessionState state = getState(session);

            switch (state) {
                case OPENED:
                    try {
                        boolean flushedAll = ioProcessorWriter.flushNow(session, currentTime);

                        if (flushedAll && !session.getWriteRequestQueue().isEmpty() && !session.isScheduledForFlush()) {
                            scheduleFlush(session);
                        }
                    } catch (Exception e) {
                        scheduleRemove(session);
                        session.close();
                        try {
                            session.getIoHandler().exceptionCaught(session,e);
                        } catch (Exception e1) {
                            log.info("Exception on exceptionCaught",e,session);
                        }
                    }

                    break;

                case CLOSING:
                    // Skip if the channel is already closed.
                    break;

                case OPENING:
                    // Retry later if session is not yet fully initialized.
                    // (In case that Session.write() is called before addSession()
                    // is processed)
                    scheduleFlush(session);
                    return;

                default:
                    throw new IllegalStateException(String.valueOf(state));
            }

        } while (!flushingSessions.isEmpty());
    }

    private boolean removeNow(IoSessionImp session) {
        // todo: this clearWriteRequest is to notify users that their messages are not sent
//        clearWriteRequestQueue(session);

        try {
            destroy(session);
            return true;
        } catch (Exception e) {
            try {
                session.getIoHandler().exceptionCaught(session,e);
            } catch (Exception e1) {
                log.info("exception exceptionCaught",e1,session);
            }
        } finally {
            try {
                //clearWriteRequestQueue(session);
                session.getIoHandler().sessionClosed(session);
            } catch (Exception e) {
                // The session was either destroyed or not at this point.
                // We do not want any exception thrown from this "cleanup" code to change
                // the return value by bubbling up.
                try {
                    session.getIoHandler().exceptionCaught(session,e);
                } catch (Exception e1) {
                    log.info("exception exceptionCaught",e1,session);
                }
            }
        }

        return false;
    }

    private int removeSessions() {
        int removedSessions = 0;

        for (IoSessionImp session = removingSessions.poll(); session != null;session = removingSessions.poll()) {
            SessionState state = getState(session);

            // Now deal with the removal accordingly to the session's state
            switch (state) {
                case OPENED:
                    // Try to remove this session
                    if (removeNow(session)) {
                        removedSessions++;
                    }

                    break;

                case CLOSING:
                    // Skip if channel is already closed
                    // In any case, remove the session from the queue
                    removedSessions++;
                    break;

                case OPENING:
                    // Remove session from the newSessions queue and
                    // remove it
                    newSessions.remove(session);

                    if (removeNow(session)) {
                        removedSessions++;
                    }

                    break;

                default:
                    throw new IllegalStateException(String.valueOf(state));
            }
        }

        return removedSessions;
    }


}
