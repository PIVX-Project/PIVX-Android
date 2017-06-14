package org.furszy.client.basic;

import org.furszy.client.interfaces.IoHandler;
import org.furszy.client.interfaces.IoProcessor;
import org.furszy.client.interfaces.IoSession;
import org.furszy.client.interfaces.IoSessionConf;
import org.furszy.client.interfaces.ProtocolDecoder;
import org.furszy.client.interfaces.ProtocolEncoder;
import org.furszy.client.interfaces.write.WriteRequest;
import org.furszy.client.interfaces.write.WriteRequestQueue;

import java.nio.channels.Channel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mati on 12/05/17.
 */

public abstract class IoSessionImp<M> implements IoSession<M> {

    public static final String ATTR_CONNECT_FUTURE = "attr_connect_future";

    private final long id;
    private SelectionKey selectionKey;
    /** The communication channel */
    protected final Channel channel;
    /** Session configuration */
    private IoSessionConf sessionConf;
    /** Session handler */
    private IoHandler<M> handler;
    /** Write request queue */
    private WriteRequestQueue writeRequestQueue;
    /** Processor in charge of manage this session */
    private IoProcessor processor;
    /** lock object for operations */
    private final Object lock = new Object();
    /** Attributes to use and discard, like ConnectFuture */
    private HashMap<String,Object> attributes;

    private boolean isSecure;
    private AtomicBoolean isReadSuspended = new AtomicBoolean(false);
    private AtomicBoolean isWriteSuspended = new AtomicBoolean(false);

    private long lastWriteTime;
    private long lastReadTime;

    private AtomicBoolean isClosing = new AtomicBoolean(false);
    /** Flush flag */
    private AtomicBoolean scheduledForFlush = new AtomicBoolean(false);
    private WriteRequest currentWriteRequest;

    protected IoSessionImp(long id, Channel channel, IoSessionConf ioSessionConf) {
        this.id = id;
        this.channel = channel;
        this.sessionConf = ioSessionConf;
        this.writeRequestQueue = new WriteRequestQueueImp();
    }


    @Override
    public long getId() {
        return id;
    }

    @Override
    public IoSessionConf getSessionConf() {
        return sessionConf;
    }

    @Override
    public IoHandler getIoHandler() {
        return handler;
    }

    @Override
    public WriteRequestQueue getWriteRequestQueue() {
        return writeRequestQueue;
    }

    @Override
    public void addWriteRequest(WriteRequest writeRequest) {
        writeRequestQueue.offer(writeRequest);
        setScheduledForFlush();
    }

    @Override
    public SelectableChannel getChannel() {
        return (SelectableChannel) channel;
    }

    @Override
    public void setSecure(boolean isSecure) {
        this.isSecure = isSecure;
    }

    @Override
    public void setSelectionKey(SelectionKey key) {
        this.selectionKey = key;
    }

    @Override
    public SelectionKey getSelectionKey() {
        return selectionKey;
    }

    @Override
    public boolean isReadSuspended() {
        return isReadSuspended.get();
    }

    @Override
    public boolean isWriteSuspended() {
        return isWriteSuspended.get();
    }

    @Override
    public long getLastWriteTime() {
        return lastWriteTime;
    }

    @Override
    public long getLastReadTime() {
        return lastReadTime;
    }

    @Override
    public void increaseIdleCount(IdleStatus status, long currentTimeMillis) {

    }

    @Override
    public void addAttribute(String id, Object attribute) {
        if (attributes==null){
            attributes = new HashMap<>();
        }
        attributes.put(id,attribute);
    }

    @Override
    public Object getAttribute(String id) {
        return attributes.get(id);
    }

    @Override
    public Object removeAttribute(String id) {
        return attributes.remove(id);
    }

    @Override
    public void close() {
        synchronized (lock) {
            if (!isClosing()) {
                isClosing.set(true);
            }
        }
        // todo: here i call the close method.
        //getFilterChain().fireFilterClose();
    }

    public boolean isClosing() {
        return isClosing.get();
    }

    public void setHandler(IoHandler handler) {
        this.handler = handler;
    }

    public boolean setScheduledForFlush(boolean schedule) {
        if (schedule) {
            // If the current tag is set to false, switch it to true,
            // otherwise, we do nothing but return false : the session
            // is already scheduled for flush
            return scheduledForFlush.compareAndSet(false, schedule);
        }

        scheduledForFlush.set(schedule);
        return true;
    }

    @Override
    public void unscheduledForFlush() {
        scheduledForFlush.set(false);
    }

    public boolean isScheduledForFlush() {
        return scheduledForFlush.get();
    }

    public boolean isConnected() {
        return channel.isOpen();
    }

    public WriteRequest getCurrentWriteRequest() {
        return currentWriteRequest;
    }

    public void setCurrentWriteRequest(WriteRequest currentWriteRequest) {
        this.currentWriteRequest = currentWriteRequest;
    }

    public boolean isActive() {
        return selectionKey.isValid();
    }

    public void setProcessor(IoProcessor processor) {
        this.processor = processor;
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return sessionConf.getProtocolDecoder();
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return sessionConf.getProtocolEncoder();
    }

    @Override
    public boolean setScheduledForFlush() {
        boolean result = setScheduledForFlush(true);
        if (result){
            processor.scheduleForFlush(this);
        }
        return result;
    }
}
