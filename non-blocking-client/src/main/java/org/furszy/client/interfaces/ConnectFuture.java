package org.furszy.client.interfaces;


import org.furszy.client.basic.ConnectionId;

/**
 * Created by mati on 12/05/17.
 */

public interface ConnectFuture {

    /**
     * Returns {@link IoSession} which is the result of connect operation.
     *
     * @return The {link IoSession} instance that has been associated with the connection,
     * if the connection was successful, {@code null} otherwise
     */
    ConnectionId getConnectionId();

    /**
     * Returns the cause of the connection failure.
     *
     * @return <tt>null</tt> if the connect operation is not finished yet,
     *         or if the connection attempt is successful, otherwise returns
     *         teh cause of the exception
     */
    Throwable getException();

    /**
     * @return {@code true} if the connect operation is finished successfully.
     */
    boolean isConnected();

    /**
     * @return {@code true} if the connect operation has been canceled by
     * {@link #cancel()} method.
     */
    boolean isCanceled();

    /**
     * Sets the newly connected session and notifies all threads waiting for
     * this future.  This method is invoked by MINA internally.  Please do not
     * call this method directly.
     *
     * @param session The created session to store in the ConnectFuture insteance
     */
    void setConnectionId(ConnectionId connectionId);

    /**
     * Sets the exception caught due to connection failure and notifies all
     * threads waiting for this future.  This method is invoked by MINA
     * internally.  Please do not call this method directly.
     *
     * @param exception The exception to store in the ConnectFuture instance
     */
    void setException(Throwable exception);

    /**
     * Cancels the connection attempt and notifies all threads waiting for
     * this future.
     *
     * @return {@code true} if the future has been cancelled by this call, {@code false}
     * if the future was already cancelled.
     */
    boolean cancel();

    /**
     * {@inheritDoc}
     */
    ConnectFuture get(long timeout) throws InterruptedException;


//    /**
//     * {@inheritDoc}
//     */
//    ConnectFuture addListener(IoFutureListener<?> listener);
//
//    /**
//     * {@inheritDoc}
//     */
//    ConnectFuture removeListener(IoFutureListener<?> listener);


}
