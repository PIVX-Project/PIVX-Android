package pivtrum;

import org.bitcoinj.core.Address;
import org.furszy.client.IoManager;
import org.furszy.client.basic.BaseMsgFuture;
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import pivtrum.messages.BaseMsg;
import pivtrum.messages.Method;
import pivtrum.messages.VersionMsg;

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
    /** Client version */
    private VersionMsg versionMsg;
    /** Message listeners */
    private ConcurrentMap<Long,MsgListener> msgListeners = new ConcurrentHashMap<>();

    private AtomicLong msgIdGenerator = new AtomicLong(0);

    private interface MsgListener{
        void onMsgReceived(String jsonStr);
        void onMsgFail(String jsonStr);
    }

    public PivtrumPeer(PivtrumPeerData peerData,IoManager ioManager,VersionMsg versionMsg) {
        this.peerData = peerData;
        this.ioManager = ioManager;
        this.versionMsg = versionMsg;
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
            session = future.getSession();
            log.info("Peer connected");
            // Check the version sync
            MsgFuture versionFuture = new MsgFuture();
            sendVersion(versionFuture);
            String version = versionFuture.get();
            log.info("version message arrive");
            VersionMsg versionMsg = new VersionMsg().fromJson(version);
            checkVersion(versionMsg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send version message
     */
    public void sendVersion(MsgListener msgListener){
        try{
            WriteRequest writeRequest = ioManager.send(buildMsg(versionMsg,true),new ConnectionId(session.getId()));
            writeRequest.getFuture().get(TimeUnit.SECONDS.toNanos(30));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkVersion(VersionMsg versionMsg){
        // todo: here make the check and throw an exception
    }

    /**
     * Send a getPeers messages to the server.
     * @return
     */
    public void getPeers() {
        try {
            BaseMsg getPeers = new BaseMsg(Method.GET_PEERS.getMethod());
            WriteRequest writeRequest = ioManager.send(buildMsg(getPeers,true),new ConnectionId(session.getId()));
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


    public class MsgFuture extends BaseMsgFuture<String> implements MsgListener {
        @Override
        public void onMsgReceived(String jsonStr) {
            try {
                synchronized (reentrantLock) {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    this.messageId = jsonObject.getInt("id");
                    this.status = 200;
                    object = jsonStr;
                    reentrantLock.notifyAll();
                }
                if (listener != null) {
                    listener.onAction(messageId, jsonStr);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMsgFail(String jsonStr) {
            try {
                synchronized (reentrantLock) {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    this.messageId = jsonObject.getInt("id");
                    this.status = 501;
                    this.statusDetail = jsonStr;
                    reentrantLock.notifyAll();
                }
                if (listener != null) {
                    listener.onFail(messageId, status, statusDetail);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
