package org.furszy.client.interfaces;


import org.furszy.client.basic.IdleStatus;
import org.furszy.client.interfaces.write.WriteRequest;
import org.furszy.client.interfaces.write.WriteRequestQueue;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

/**
 * Created by mati on 11/05/17.
 *
 * todo: chequear tema de isScheduledForFlush y unscheduleForFlush..
 */

public interface IoSession<M> {

    /** session id */
    long getId();
    /** Get the session configuration */
    IoSessionConf getSessionConf();
    /** session events handler */
    IoHandler getIoHandler();
    /** Session messages decoder */
    ProtocolDecoder getProtocolDecoder();
    /** Session message encoder */
    ProtocolEncoder getProtocolEncoder();
    /** Queue of messages to send */
    WriteRequestQueue getWriteRequestQueue();
    /** Offer a write request to the queue */
    void addWriteRequest(WriteRequest writeRequest);
    /** Session channel */
    SelectableChannel getChannel();
    /** Add an attribute to the attributes map */
    void addAttribute(String id, Object attribute);
    Object getAttribute(String id);
    Object removeAttribute(String id);
    /** If this session has TLS engine */
    void setSecure(boolean isSecure);
    /** Session selectionKey */
    void setSelectionKey(SelectionKey key);
    /** Obtains the selection key */
    SelectionKey getSelectionKey();
    /** Check read is not more possible */
    boolean isReadSuspended();
    /** Check if write is not more possible */
    boolean isWriteSuspended();
    /** The last time that the session sent something */
    long getLastWriteTime();
    /** The last time that the session read something */
    long getLastReadTime();
    /** Increase the idle status */
    void increaseIdleCount(IdleStatus status, long currentTimeMillis);
    /** close method */
    void close();

    boolean setScheduledForFlush();
    void unscheduledForFlush();
}
