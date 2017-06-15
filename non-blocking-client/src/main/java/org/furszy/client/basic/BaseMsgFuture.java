package org.furszy.client.basic;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class BaseMsgFuture<O> implements Future<O> {

    protected int messageId;
    protected String msgName;
    protected O object;
    protected Listener listener;

    protected final Object reentrantLock = new Object();

    protected int status;
    protected String statusDetail;

    public BaseMsgFuture() {
    }

    @Override
    public boolean cancel(boolean b) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return messageId!=0;
    }

    @Override
    public O get() throws InterruptedException, ExecutionException {
        synchronized(reentrantLock) {
            if (object == null) {
                reentrantLock.wait();
            }
        }
        return object;
    }

    @Override
    public O get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized(reentrantLock) {
            if (object == null) {
                reentrantLock.wait(timeUnit.toMillis(l));
            }
        }
        return object;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusDetail() {
        return statusDetail;
    }

    public void setListener(Listener<O> listener) {
        this.listener = listener;
    }

    public String getMessageName() {
        return msgName;
    }

    public void setMsgName(String msgName) {
        this.msgName = msgName;
    }

    public interface Listener<O>{

        void onAction(int messageId,O object);

        void onFail(int messageId,int status,String statusDetail);

    }
}