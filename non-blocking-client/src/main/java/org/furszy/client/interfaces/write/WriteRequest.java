package org.furszy.client.interfaces.write;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by mati on 12/05/17.
 */

public interface WriteRequest {

    /**
     * @return {@link WriteFuture} that is associated with this write request.
     */
    WriteFuture getFuture();

    /**
     * @return a message object to be written.
     */
    Object getMessage();

    /**
     * MessageFiltered
     */
    Object getMessageFiltered();

    /**
     * Returns the destination of this write request.
     *
     * @return <tt>null</tt> for the default destination
     */
    SocketAddress getDestination();

    /**
     * Tells if the current message has been encoded
     *
     * @return true if the message has already been encoded
     */
    boolean isEncoded();

    void setFilteredMessage(ByteBuffer byteBuffer);

}
