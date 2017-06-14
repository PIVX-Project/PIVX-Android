/*
package pivx.org.pivxwallet.module.pivtrum;

import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Message;
import org.bitcoinj.core.PeerAddress;
import org.bitcoinj.core.ProtocolException;
import org.bitcoinj.net.AbstractTimeoutHandler;
import org.bitcoinj.net.MessageWriteTarget;
import org.bitcoinj.net.StreamConnection;
import org.bitcoinj.utils.Threading;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.locks.Lock;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

*
 * Created by furszy on 6/13/17.



public class PivtrumSocketHandler extends AbstractTimeoutHandler implements StreamConnection {

    private static final Logger log = LoggerFactory.getLogger(PivtrumSocketHandler.class);

    private PivtrumPeerData peerData;
    private MessageWriteTarget writeTarget;
    private Lock lock = Threading.lock("PivtrumSocketHandler");
    // If we close() before we know our writeTarget, set this to true to call writeTarget.closeConnection() right away.
    private boolean closePending = false;
    // The ByteBuffers passed to us from the writeTarget are static in size, and usually smaller than some messages we
    // will receive. For SPV clients, this should be rare (ie we're mostly dealing with small transactions), but for
    // messages which are larger than the read buffer, we have to keep a temporary buffer with its bytes.
    private byte[] largeReadBuffer;
    private int largeReadBufferPos;
*
     * Sends the given message to the peer. Due to the asynchronousness of network programming, there is no guarantee
     * the peer will have received it. Throws NotYetConnectedException if we are not yet connected to the remote peer.
     * TODO: Maybe use something other than the unchecked NotYetConnectedException here


    public void sendMessage(JSONObject jsonObject,boolean isSingleMessage) throws NotYetConnectedException {
        lock.lock();
        try {
            if (writeTarget == null)
                throw new NotYetConnectedException();
        } finally {
            lock.unlock();
        }
        // TODO: Some round-tripping could be avoided here
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            OutputStreamWriter osw = new OutputStreamWriter(out);
            BufferedWriter bw = new BufferedWriter(osw);
            String msgToWrite = jsonObject.toString();
            if (isSingleMessage){
                msgToWrite+='\n';
            }
            bw.write(msgToWrite);
            writeTarget.writeBytes(out.toByteArray());
            bw.close();
            osw.close();
            out.close();
        } catch (IOException e) {
            exceptionCaught(e);
        }
    }

    @Override
    public int receiveBytes(ByteBuffer buff) {
        checkArgument(buff.position() == 0);
        try {
            // Repeatedly try to deserialize messages until we hit a BufferUnderflowException
            boolean firstMessage = true;
            while (true) {
                // If we are in the middle of reading a message, try to fill that one first, before we expect another
                if (largeReadBuffer != null) {
                    // This can only happen in the first iteration
                    checkState(firstMessage);
                    // Read new bytes into the largeReadBuffer
                    int bytesToGet = Math.min(buff.remaining(), largeReadBuffer.length - largeReadBufferPos);
                    buff.get(largeReadBuffer, largeReadBufferPos, bytesToGet);
                    largeReadBufferPos += bytesToGet;
                    // Check the largeReadBuffer's status
                    if (largeReadBufferPos == largeReadBuffer.length) {
                        // ...processing a message if one is available
                        processMessage(serializer.deserializePayload(header, ByteBuffer.wrap(largeReadBuffer)));
                        largeReadBuffer = null;
                        header = null;
                        firstMessage = false;
                    } else // ...or just returning if we don't have enough bytes yet
                        return buff.position();
                }
                // Now try to deserialize any messages left in buff
                Message message;
                int preSerializePosition = buff.position();
                try {
                    message = serializer.deserialize(buff);
                } catch (BufferUnderflowException e) {
                    // If we went through the whole buffer without a full message, we need to use the largeReadBuffer
                    if (firstMessage && buff.limit() == buff.capacity()) {
                        // ...so reposition the buffer to 0 and read the next message header
                        buff.position(0);
                        try {
                            serializer.seekPastMagicBytes(buff);
                            header = serializer.deserializeHeader(buff);
                            // Initialize the largeReadBuffer with the next message's size and fill it with any bytes
                            // left in buff
                            largeReadBuffer = new byte[header.size];
                            largeReadBufferPos = buff.remaining();
                            buff.get(largeReadBuffer, 0, largeReadBufferPos);
                        } catch (BufferUnderflowException e1) {
                            // If we went through a whole buffer's worth of bytes without getting a header, give up
                            // In cases where the buff is just really small, we could create a second largeReadBuffer
                            // that we use to deserialize the magic+header, but that is rather complicated when the buff
                            // should probably be at least that big anyway (for efficiency)
                            throw new ProtocolException("No magic bytes+header after reading " + buff.capacity() + " bytes");
                        }
                    } else {
                        // Reposition the buffer to its original position, which saves us from skipping messages by
                        // seeking past part of the magic bytes before all of them are in the buffer
                        buff.position(preSerializePosition);
                    }
                    return buff.position();
                }
                // Process our freshly deserialized message
                processMessage(message);
                firstMessage = false;
            }
        } catch (Exception e) {
            exceptionCaught(e);
            return -1; // Returning -1 also throws an IllegalStateException upstream and kills the connection
        }
    }

    @Override
    protected void timeoutOccurred() {
        log.info("{}: Timed out", getAddress());
        close();
    }

* Catch any exceptions, logging them and then closing the channel.

    private void exceptionCaught(Exception e) {
        PivtrumPeerData addr = getAddress();
        String s = addr == null ? "?" : addr.toString();
        if (e instanceof ConnectException || e instanceof IOException) {
            // Short message for network errors
            log.info(s + " - " + e.getMessage());
        } else {
            log.warn(s + " - ", e);
            Thread.UncaughtExceptionHandler handler = Threading.uncaughtExceptionHandler;
            if (handler != null)
                handler.uncaughtException(Thread.currentThread(), e);
        }

        close();
    }

*
     * Closes the connection to the peer if one exists, or immediately closes the connection as soon as it opens


    public void close() {
        lock.lock();
        try {
            if (writeTarget == null) {
                closePending = true;
                return;
            }
        } finally {
            lock.unlock();
        }
        writeTarget.closeConnection();
    }

*
     * @return pivtrum peer data


    public PivtrumPeerData getAddress() {
        return peerData;
    }

    abstract
}
*/
