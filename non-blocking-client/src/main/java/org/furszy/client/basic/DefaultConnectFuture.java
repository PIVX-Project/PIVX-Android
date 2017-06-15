package org.furszy.client.basic;


import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoSession;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by mati on 12/05/17.
 */

public class DefaultConnectFuture implements ConnectFuture {

    /** Connection identifier on the client */
    private IoSession session;
    /** Exception occured */
    private Throwable exception;

    @Override
    public IoSession getSession() {
        return session;
    }

    @Override
    public Throwable getException() {
        return exception;
    }

    @Override
    public boolean isConnected() {
        return session!=null;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public synchronized void setSession(IoSession session) {
        this.session = session;
        notifyAll();
    }

    @Override
    public synchronized void setException(Throwable exception) {
        this.exception = exception;
        notifyAll();
    }

    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public synchronized ConnectFuture get(long timeout) throws InterruptedException {
        if (session==null && exception==null){
            wait(timeout);
        }
        return this;
    }

}
