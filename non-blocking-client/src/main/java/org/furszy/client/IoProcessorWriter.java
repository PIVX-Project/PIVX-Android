package org.furszy.client;

import org.furszy.client.basic.IoSessionImp;
import org.furszy.client.interfaces.write.WriteRequest;
import org.furszy.client.interfaces.write.WriteRequestQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by mati on 14/05/17.
 */

public class IoProcessorWriter {

    private Logger log = LoggerFactory.getLogger(IoProcessorWriter.class);

    private IoProcessorImp processor;

    public IoProcessorWriter(IoProcessorImp ioProcessorImp) {
        this.processor = ioProcessorImp;
    }

    boolean flushNow(IoSessionImp session, long currentTime) {
        if (!session.isConnected()) {
            processor.scheduleRemove(session);
            return false;
        }

        final WriteRequestQueue writeRequestQueue = session.getWriteRequestQueue();

        // Set limitation for the number of written bytes for read-write
        // fairness. I used maxReadBufferSize * 3 / 2, which yields best
        // performance in my experience while not breaking fairness much.
        final int maxWrittenBytes = session.getSessionConf().getMaxReadBufferSize() + (session.getSessionConf().getMaxReadBufferSize() >>> 1);
        int writtenBytes = 0;
        WriteRequest req = null;

        try {
            // Clear OP_WRITE
            processor.setInterestedInWrite(session, false);

            do {
                // Check for pending writes.
                req = session.getCurrentWriteRequest();

                if (req == null) {
                    req = writeRequestQueue.poll();

                    if (req == null) {
                        break;
                    }

                    session.setCurrentWriteRequest(req);
                }

                int localWrittenBytes = 0;
                Object message = req.getMessageFiltered();
                // if the filtered message is not encoded yet
                if (message==null){
                    ByteBuffer byteBuffer = session.getProtocolEncoder().encode(req.getMessage());
                    byteBuffer.flip();
                    message = byteBuffer;
                    req.setFilteredMessage((ByteBuffer) message);
                }

                if (message instanceof ByteBuffer) {
                    localWrittenBytes = writeBuffer(session, req, maxWrittenBytes - writtenBytes,
                            currentTime);

                    if ((localWrittenBytes > 0) && ((ByteBuffer) message).hasRemaining()) {
                        // the buffer isn't empty, we re-interest it in writing
                        writtenBytes += localWrittenBytes;
                        processor.setInterestedInWrite(session, true);
                        return false;
                    }
                } else {
                    throw new IllegalStateException("Don't know how to handle message of type '"
                            + message.getClass().getName() + "'.  Are you missing a protocol encoder?");
                }

                if (localWrittenBytes == 0) {
                    // Kernel buffer is full.
                    processor.setInterestedInWrite(session, true);
                    return false;
                }

                writtenBytes += localWrittenBytes;

                if (writtenBytes >= maxWrittenBytes) {
                    // Wrote too much
                    processor.scheduleFlush(session);
                    return false;
                }

                if (message instanceof ByteBuffer) {
                    ((ByteBuffer) message).clear();
                }
            } while (writtenBytes < maxWrittenBytes);
        } catch (Exception e) {
            e.printStackTrace();
            if (req != null) {
                req.getFuture().setException(e);
            }
            try {
                session.getIoHandler().exceptionCaught(session,e);
            } catch (Exception e1) {
                log.info("Exception on exceptionCaught",e1);
            }

            return false;
        }
        return true;
    }

    private int writeBuffer(IoSessionImp session, WriteRequest req, int maxLength, long currentTime)
            throws Exception {
        ByteBuffer buf = (ByteBuffer) req.getMessageFiltered();
        int localWrittenBytes = 0;

        if (buf.hasRemaining()) {
            int length;


            length = buf.remaining();


            try {
                localWrittenBytes = write(session, buf, length);
            } catch (IOException ioe) {
                // We have had an issue while trying to send data to the
                // peer : let's close the session.
                buf.clear();
                session.close();
                processor.destroy(session);

                return 0;
            }

        }

//        session.increaseWrittenBytes(localWrittenBytes, currentTime);

        if (!buf.hasRemaining() || ((localWrittenBytes != 0))) {
            // Buffer has been sent, clear the current request.
            int pos = buf.position();
            buf.clear();

            fireMessageSent(session, req);

            // And set it back to its position
            buf.position(pos);
        }

        return localWrittenBytes;
    }

    private int write(IoSessionImp session, ByteBuffer buf, int length) throws Exception {
        if (buf.remaining() <= length) {
            return ((SocketChannel)session.getChannel()).write(buf);
        }

        int oldLimit = buf.limit();
        buf.limit(buf.position() + length);
        try {
            return ((SocketChannel)session.getChannel()).write(buf);
        } finally {
            buf.limit(oldLimit);
        }
    }

    private void fireMessageSent(IoSessionImp session, WriteRequest req) {

        // first notity future:
        req.getFuture().notifySend();

        try {
            session.getIoHandler().messageSent(session,req);
        } catch (Exception e) {
            try {
                session.getIoHandler().exceptionCaught(session,e);
            } catch (Exception e1) {
                log.info("Exception caught",e1);
            }
        }
    }

}
