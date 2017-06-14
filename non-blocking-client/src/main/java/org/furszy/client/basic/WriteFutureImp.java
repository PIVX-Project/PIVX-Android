package org.furszy.client.basic;

import org.furszy.client.interfaces.write.WriteFuture;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mati on 14/05/17.
 */

public class WriteFutureImp implements WriteFuture {

    private Exception exception;

    private AtomicBoolean isSent = new AtomicBoolean(false);

    @Override
    public synchronized void setException(Exception e) {
        this.exception = e;
        notifyAll();
    }

    @Override
    public synchronized void get(long millis) throws InterruptedException {
        if (isSent.get()==true){
            return;
        }
        wait(millis);
    }

    @Override
    public synchronized void notifySend() {
        isSent.set(true);
        notifyAll();
    }

    @Override
    public boolean isSent() {
        return isSent.get();
    }
}
