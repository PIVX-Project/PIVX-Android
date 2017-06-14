package pivx.org.pivxwallet.module.pivtrum;

import org.bitcoinj.core.Address;
import org.furszy.client.IoManager;
import org.furszy.client.basic.ConnectionId;
import org.furszy.client.basic.IoSessionConfImp;
import org.furszy.client.exceptions.InvalidProtocolViolationException;
import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoHandler;
import org.furszy.client.interfaces.IoSession;
import org.furszy.client.interfaces.ProtocolDecoder;
import org.furszy.client.interfaces.ProtocolEncoder;
import org.furszy.client.interfaces.write.WriteRequest;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import pivx.org.pivxwallet.module.pivtrum.messages.BaseMsg;
import pivx.org.pivxwallet.module.pivtrum.messages.Method;

/**
 * Created by furszy on 6/12/17.
 *
 * Class in charge of connect to a single peer on the network
 */

public class PivtrumPeer implements IoHandler{

    private static final Logger log = LoggerFactory.getLogger(PivtrumPeer.class);

    /**  */
    private PivtrumPeerData peerData;
    /**  */
    private IoManager ioManager;
    /** Session connection id */
    private IoSession session;
    private ConnectionId connectionId;
    private AtomicLong msgIdGenerator = new AtomicLong(0);


    public PivtrumPeer(PivtrumPeerData peerData,IoManager ioManager) {
        this.peerData = peerData;
        this.ioManager = ioManager;
    }

    /**
     * Connect synchronized
     */
    public void connect() {
        try {
            IoSessionConfImp ioSessionConfImp = new IoSessionConfImp();
            ioSessionConfImp.setProtocolDecoder(new StringDecoder());
            ioSessionConfImp.setProtocolEncoder(new StringEncoder());
            ConnectFuture future = ioManager.connect(new InetSocketAddress(peerData.getHost(),peerData.getTcpPort()),null,this,ioSessionConfImp);
            future.get(TimeUnit.SECONDS.toNanos(30));
            connectionId = future.getConnectionId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send a getPeers messages to the server.
     * @return
     */
    public void getPeers() {
        try {
            BaseMsg getPeers = new BaseMsg(Method.GET_PEERS.getMethod());
            WriteRequest writeRequest = ioManager.send(buildMsg(getPeers,true),connectionId);
            writeRequest.getFuture().get(TimeUnit.SECONDS.toNanos(30));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param addresses
     */
    public void subscribeAddresses(List<Address> addresses) {

    }

    private String buildMsg(BaseMsg msg,boolean isSingleMsg) throws JSONException {
        msg.setId(msgIdGenerator.incrementAndGet());
        String msgStr = msg.toJson().toString();
        if (isSingleMsg){
            msgStr+="\n";
        }
        return msgStr;
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        log.info("Session created: "+ioSession.getId());
        System.out.println("session created: " + ioSession.getId());
        session = ioSession;
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {
        log.info("Session opened: "+ioSession.getId());
        System.out.println("session opened: "+ioSession.getId());
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        log.info("Session closed: "+ioSession.getId());
        System.out.println("session closed: "+ioSession.getId());
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        log.error("exceptionCaught: "+ioSession.getId());
        throwable.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession ioSession, Object s) throws Exception {
        log.info("messageReceived: "+s.toString()+", session id:"+ioSession.getId());
        System.out.println("messageReceived: "+s.toString()+", session id:"+ioSession.getId());
    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
        log.info("messageSent: "+o+", session id:"+ioSession.getId());
        System.out.println("messageSent: "+o.toString()+", session id:"+ioSession.getId());

    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {
        log.error("input closed session id:"+ioSession.getId());
    }


    static class StringDecoder extends ProtocolDecoder<String> {

        @Override
        public String decode(ByteBuffer byteBuffer) throws InvalidProtocolViolationException {
            try {
                return new String(byteBuffer.array(),"UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new InvalidProtocolViolationException("error decoder",e);
            }
        }
    }

    static class StringEncoder extends ProtocolEncoder<String> {

        @Override
        public ByteBuffer encode(String message) throws InvalidProtocolViolationException {
            try {
                byte[] bytes =  message.getBytes("UTF-8");
                ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
                byteBuffer.put(bytes);
                return byteBuffer;
            } catch (UnsupportedEncodingException e) {
                throw new InvalidProtocolViolationException("error encoder",e);
            }
        }
    }
}
