package org.furszy.client;

import org.furszy.client.basic.DefaultConnectFuture;
import org.furszy.client.basic.IoSessionImp;
import org.furszy.client.exceptions.ConnectionFailureException;
import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoHandler;
import org.furszy.client.interfaces.IoSessionConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by mati on 12/05/17.
 *
 * This class has the main looper
 */

public class IoLooper {

    private Logger log = LoggerFactory.getLogger(IoLooper.class);
    /** Connection selector */
    private SelectorWrapper selectorWrapper;
    /** Main flag to continue working */
    private AtomicBoolean isSelectable = new AtomicBoolean(false);
    /**  */
    private AtomicBoolean disposed = new AtomicBoolean(false);
    /** Connection timeout */
    private long connectTimeoutInMillis = 60 * 1000L; // 1 minute by default
    /** Session constructor and initilizer helper */
    private SessionHelper sessionHelper;
    /** loop reference */
    private AtomicReference<Looper> looperRef = new AtomicReference<>();
    /** In charge of processing connected sessions */
    private IoProcessorImp processor;
    /** Main executor */
    private ExecutorService executor;

    /** Queue */
    private ConcurrentLinkedQueue<ConnectionRequest> connectionQueue = new ConcurrentLinkedQueue<>();
    private ConcurrentLinkedQueue<ConnectionRequest> cancelQueue = new ConcurrentLinkedQueue<>();



    public IoLooper(ExecutorService executor,IoProcessorImp ioProcessorImp) {
        try {
            this.executor = executor;
            sessionHelper = new SessionHelper();
            this.processor = ioProcessorImp;
            selectorWrapper = new SelectorWrapper(SelectorProvider.provider());
            isSelectable.set(true);
        } catch (IOException e) {
            log.error("IoLooper init",e);
        } catch (Exception e){
            log.error("IoLooper init",e);
        }
    }



    public ConnectFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, IoHandler ioHandler, IoSessionConf ioSessionConf) throws ConnectionFailureException {
        SocketChannel ch = null;
        boolean success = false;
        try{
            ch = newSocketChannel(localAddress,ioSessionConf);
            if(ch.connect(remoteAddress)) {
                ConnectFuture connectFuture = new DefaultConnectFuture();
                IoSessionImp ioSessionImp = sessionHelper.newSession(ch, ioSessionConf);
                sessionHelper.initSession(ioSessionImp, ioHandler, connectFuture);
                processor.add(ioSessionImp);
                success = true;
                return connectFuture;
            }
            success = true;
        } catch (IOException e) {
            throw new ConnectionFailureException(e);
        } catch (Exception e) {
            throw new ConnectionFailureException(e);
        }finally {
            if (!success && ch!=null){
                try{
                    close(ch);
                }catch (Exception e) {
                    log.error("connect exception",e);
                }
            }
        }
        // todo: acá quizas tengo que armar un connectionRequest y agregarlo a la lista.
        ConnectionRequest connectionRequest = new ConnectionRequest(ch,ioSessionConf,ioHandler);
        connectionQueue.offer(connectionRequest);
        startupWorker();
        selectorWrapper.wakeup();
        return connectionRequest;
    }

    private void startupWorker() {
        if (!isSelectable.get()) {
            connectionQueue.clear();
            cancelQueue.clear();
        }

        Looper ioLooper = looperRef.get();

        if (ioLooper == null) {
            ioLooper = new Looper();

            if (looperRef.compareAndSet(null, ioLooper)) {
                executeWorker(ioLooper);
            }
        }
    }

    private void executeWorker(Runnable ioLooper) {
        executor.submit(ioLooper);
    }

    private SocketChannel newSocketChannel(SocketAddress localAddress, IoSessionConf ioSessionConf) throws Exception {
        SocketChannel ch = SocketChannel.open();

        int receiveBufferSize = ioSessionConf.getReadBufferSize();

        if (receiveBufferSize > 65535) {
            ch.socket().setReceiveBufferSize(receiveBufferSize);
        }

        if (localAddress != null) {
            try {
                ch.socket().setReuseAddress(true);
                ch.socket().bind(localAddress);
            } catch (IOException ioe) {
                // Add some info regarding the address we try to bind to the
                // message
                String newMessage = "Error while binding on " + localAddress + "\n" + "original message : "
                        + ioe.getMessage();
                Exception e = new IOException(newMessage);
                e.initCause(ioe.getCause());

                // Preemptively close the channel
                ch.close();
                throw e;
            }
        }
        ch.configureBlocking(false);
        return ch;
    }


    /**
     * Register channels on selector
     * @return
     */
    private int registerNew(){
        int nHandles = 0;
        for (;;){
            ConnectionRequest req = connectionQueue.poll();
            if (req==null){
                break;
            }

            SocketChannel socketChannel = req.socketChannel;
            try{
                selectorWrapper.registerConnect(socketChannel,req);
                nHandles++;
            } catch (Exception e) {
                log.error("registerNew",e);
                req.setException(e);
                try{
                    close(socketChannel);
                } catch (Exception e2){
                    log.error("registerNew exception",e2);
                }
            }
        }
        return nHandles;
    }


    /**
     * Process the incoming connections, creating a new session for each valid
     * connection.
     */
    private int processConnections(Iterator<ConnectionRequest> handlers){
        int nHandles = 0;

        while (handlers.hasNext()){
            SocketChannel channel = handlers.next().socketChannel;
            handlers.remove();
            ConnectionRequest connectionRequest = getConnectionRequest(channel);
            if (connectionRequest == null) {
                continue;
            }
            boolean success = false;
            try {
                if (selectorWrapper.finishConnect(channel)) {
                    IoSessionImp ioSessionImp = sessionHelper.newSession(channel, connectionRequest.ioSessionConf);
                    sessionHelper.initSession(ioSessionImp, connectionRequest.ioHandler, connectionRequest);
                    // add the session to the processor
                    processor.add(ioSessionImp);
                    nHandles++;
                }
                success = true;
            } catch (Exception e) {
                connectionRequest.setException(e);
            }finally {
                if (!success) {
                    // The connection failed, we have to cancel it.
                    cancelQueue.offer(connectionRequest);
                }
            }
        }
        return nHandles;
    }

    /**
     * Close the socket channel registered on the selector
     * @param socketChannel
     * @return
     * @throws IOException
     */
    private boolean close(SocketChannel socketChannel) throws IOException {
        if (socketChannel.finishConnect()){
            selectorWrapper.close(socketChannel);
            return true;
        }
        return false;
    }


    private void processTimedOutSessions(Iterator<ConnectionRequest> handles) {
        long currentTime = System.currentTimeMillis();

        while (handles.hasNext()) {
            SocketChannel handle = handles.next().socketChannel;
            ConnectionRequest connectionRequest = getConnectionRequest(handle);

            if ((connectionRequest != null) && (currentTime >= connectionRequest.deadline)) {
                connectionRequest.setException(new ConnectException("Connection timed out."));
                cancelQueue.offer(connectionRequest);
            }
        }
    }

    private int cancelKeys() {
        int nHandles = 0;

        for (;;) {
            ConnectionRequest req = cancelQueue.poll();

            if (req == null) {
                break;
            }

            SocketChannel handle = req.socketChannel;

            try {
                close(handle);
            } catch (Exception e) {
                log.error("cancelKeys",e);
            } finally {
                nHandles++;
            }
        }

        if (nHandles > 0) {
            selectorWrapper.wakeup();
        }

        return nHandles;
    }

    public final void dispose(boolean awaitTermination){
        if (disposed.get()) {
            return;
        }

        if (executor!=null){
            if (!executor.isShutdown()){
                executor.shutdownNow();
                if (awaitTermination){
                    try {
                        log.debug("awaitTermination on {} called by thread=[{}]", this, Thread.currentThread().getName());
                        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
                        log.debug("awaitTermination on {} finished", this);
                    } catch (InterruptedException e1) {
                        log.warn("awaitTermination on [{}] was interrupted", this);
                        // Restore the interrupted status
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        disposed.set(true);
    }

    IoLooper.ConnectionRequest getConnectionRequest(SocketChannel handle) {
        SelectionKey key = selectorWrapper.getSelectionKeyForChannel(handle);

        if ((key == null) || (!key.isValid())) {
            return null;
        }

        return (IoLooper.ConnectionRequest) key.attachment();
    }

    private class Looper implements Runnable{

        @Override
        public void run() {

            int nHandles = 0;

            while (isSelectable.get()) {
                try {
                    // the timeout for select shall be smaller of the connect
                    // timeout or 1 second...
                    int timeout = (int) Math.min(connectTimeoutInMillis, 1000L);
                    int selected = selectorWrapper.select(timeout);
                    // registerConnect queued connections on selector.
                    nHandles += registerNew();

                    // get a chance to get out of the connector loop, if we
                    // don't have any more handles
                    if (nHandles == 0) {
                        // todo: i can stop the thread here if i not have more sockets to handle

                    }
                    if (selected > 0) {
                        nHandles -= processConnections(selectorWrapper.selectedHandles());
                    }
                    // process idle connections
                    processTimedOutSessions(selectorWrapper.allHandles());
                    // canceled queue
                    nHandles -= cancelKeys();

                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Main looper exception",e);
                }
            }


        }
    }

    class ConnectionRequest extends DefaultConnectFuture{

        private SocketChannel socketChannel;
        private IoSessionConf ioSessionConf;
        private IoHandler ioHandler;
        private long deadline;

        public ConnectionRequest(SocketChannel handle, IoSessionConf ioSessionConf, IoHandler ioHandler) {
            this.socketChannel = handle;
            this.ioSessionConf = ioSessionConf;
            this.ioHandler = ioHandler;
            long timeout = connectTimeoutInMillis;
            if (timeout <= 0L) {
                this.deadline = Long.MAX_VALUE;
            } else {
                this.deadline = System.currentTimeMillis() + timeout;
            }
        }
    }




}
