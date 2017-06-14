package org.furszy.client.interfaces.write;

/**
 * Created by mati on 12/05/17.
 */

public interface WriteFuture {

    void setException(Exception e);
    /**
     * Wait until the operation finish
     *
     */
    void get(long millis) throws InterruptedException;

    void notifySend();

    /**
     *
     * @return true if the message was sent
     */
    boolean isSent();
}
