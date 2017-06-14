package org.furszy.client;

import org.furszy.client.basic.IoSessionImp;
import org.furszy.client.interfaces.IoSessionConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Created by mati on 14/05/17.
 */

public class IoProcessorReader {

    private Logger log = LoggerFactory.getLogger(IoProcessorReader.class);

    private IoProcessorImp processor;

    public IoProcessorReader(IoProcessorImp processor) {
        this.processor = processor;
    }

    /**
     * Read operation
     * The read is complete, there is no fragmentation.
     * @param session
     */
    public void read(IoSessionImp session) {
        IoSessionConf config = session.getSessionConf();
        int bufferSize = config.getReadBufferSize();
        ByteBuffer buf = ByteBuffer.allocate(bufferSize);
//        IoBuffer buf = IoBuffer.allocate(bufferSize);

        try {
            int readBytes = 0;
            int ret;

            try {
                ret = read(session, buf);

                if (ret > 0) {
                    readBytes = ret;
                }
            } finally {
                buf.flip();
            }

            if (readBytes > 0) {
                // todo: here i can add the ssl engine before the decoder.
                // first i decode the message
                Object o = session.getProtocolDecoder().decode(buf);
                // notify user message arrived
                session.getIoHandler().messageReceived(session,o);

                buf = null;
            }

            if (ret < 0) {
                // scheduleRemove(session);
                session.getIoHandler().inputClosed(session);
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                if (!(e instanceof PortUnreachableException)) {
                    processor.scheduleRemove(session);
                }
            }

            try {
                session.getIoHandler().exceptionCaught(session,e);
            } catch (Exception e1) {
                log.error("Exception on read",e1);
            }
        }
    }

    int read(IoSessionImp session, ByteBuffer buf) throws Exception {
        ByteChannel channel = (ByteChannel) session.getChannel();
        return channel.read(buf);
    }

}
