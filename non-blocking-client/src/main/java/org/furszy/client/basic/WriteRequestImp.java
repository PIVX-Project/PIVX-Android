package org.furszy.client.basic;

import org.furszy.client.interfaces.write.WriteFuture;
import org.furszy.client.interfaces.write.WriteRequest;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

/**
 * Created by mati on 14/05/17.
 */

public class WriteRequestImp implements WriteRequest {

    private Object msg;
    private Object filteredMessage;

    private WriteFuture writeFuture;

    public WriteRequestImp(Object msg,WriteFuture writeFuture) {
        this.msg = msg;
        this.writeFuture = writeFuture;
    }

    @Override
    public WriteFuture getFuture() {
        return writeFuture;
    }

    @Override
    public Object getMessage() {
        return msg;
    }

    @Override
    public Object getMessageFiltered() {
        return filteredMessage;
    }

    @Override
    public SocketAddress getDestination() {
        return null;
    }

    @Override
    public boolean isEncoded() {
        return false;
    }

    @Override
    public void setFilteredMessage(ByteBuffer byteBuffer) {
        this.filteredMessage = byteBuffer;
    }
}
