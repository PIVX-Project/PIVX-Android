package pivtrum;

import org.bitcoinj.core.Address;
import org.furszy.client.IoManager;
import org.furszy.client.basic.BaseMsgFuture;
import org.furszy.client.basic.ConnectionId;
import org.furszy.client.basic.IoSessionConfImp;
import org.furszy.client.basic.WriteFutureImp;
import org.furszy.client.basic.WriteRequestImp;
import org.furszy.client.exceptions.ConnectionFailureException;
import org.furszy.client.exceptions.InvalidProtocolViolationException;
import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoHandler;
import org.furszy.client.interfaces.IoSession;
import org.furszy.client.interfaces.ProtocolDecoder;
import org.furszy.client.interfaces.ProtocolEncoder;
import org.furszy.client.interfaces.write.WriteFuture;
import org.furszy.client.interfaces.write.WriteRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import pivtrum.exceptions.InvalidPeerVersion;
import pivtrum.listeners.PeerListener;
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
    /** Connection flag */
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private AtomicBoolean isInitilizing = new AtomicBoolean(false);
    /** Client version */
    private VersionMsg versionMsg;
    private AtomicLong msgIdGenerator = new AtomicLong(0);
    /** Messages sent by type */
    private ConcurrentMap<Long,String> waitingRequests = new ConcurrentHashMap<>();

    /** Listeners */
    private CopyOnWriteArrayList<PeerListener> peerListeners = new CopyOnWriteArrayList<>();

    public PivtrumPeer(PivtrumPeerData peerData,IoManager ioManager,VersionMsg versionMsg) {
        this.peerData = peerData;
        this.ioManager = ioManager;
        this.versionMsg = versionMsg;
    }

    public void addPeerListener(PeerListener peerListener){
        peerListeners.add(peerListener);
    }

    /**
     * Connect synchronized
     */
    public void connect() throws ConnectionFailureException, InterruptedException {
        if (isInitilizing.compareAndSet(false,true) && !isRunning.get()) {
            IoSessionConfImp ioSessionConfImp = new IoSessionConfImp();
            ioSessionConfImp.setProtocolDecoder(new JsonDecoder());
            ioSessionConfImp.setProtocolEncoder(new StringEncoder());
            ConnectFuture future = ioManager.connect(new InetSocketAddress(peerData.getHost(), peerData.getTcpPort()), null, this, ioSessionConfImp);
            future.get(TimeUnit.SECONDS.toNanos(30));
            session = future.getSession();
            log.info("Peer connected");
            // Send version
            sendVersion();
        }else {
            throw new IllegalStateException("PivtrumPeer already initializing");
        }
    }

    /**
     * Send version message
     */
    public void sendVersion(){
        try{
            WriteFuture writeFuture = new WriteFutureImp();
            WriteRequest writeRequest = sendMsg(versionMsg,true,writeFuture);
            writeRequest.getFuture().get(TimeUnit.SECONDS.toNanos(30));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
            WriteFuture writeFuture = new WriteFutureImp();
            WriteRequest writeRequest = sendMsg(getPeers,true,writeFuture);
            writeRequest.getFuture().get(TimeUnit.SECONDS.toNanos(30));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private WriteRequest sendMsg(BaseMsg baseMsg, boolean singleRequest, WriteFuture writeFuture){
        WriteRequest writeRequest = new WriteRequestImp(buildMsg(baseMsg,singleRequest),writeFuture);
        waitingRequests.put(versionMsg.getId(),versionMsg.getMethod());
        session.addWriteRequest(writeRequest);
        return writeRequest;
    }

    /**
     *
     * @param addresses
     */
    public void subscribeAddresses(List<Address> addresses) {
        log.info("suscribe addresses: " + Arrays.toString(addresses.toArray()));
    }

    // -----------------------  Receive -------------------------------

    private void receiveVersion(JSONObject serverVersion){
        // todo: check version message and create a Version class to parse and compare versions.
        // Version version = new version(versionMsg.getMax(),versionMsg.getMin());
        // if(!version.fitInto(this.version)) -> notify error and close the connection with this peer
        // for now i just need to do a lazy check
        String peerVersion = serverVersion.getString("result");
        if (peerVersion.equals("ElectrumX 1.0.10")) {
            if (isInitilizing.get()) {
                isRunning.set(true);
                isInitilizing.set(false);
                log.info("Peer initilized, " + peerData.getHost());
                for (PeerListener peerListener : peerListeners) {
                    peerListener.onConnected(this);
                }
            }
        }else {
            // server version not valid
            isInitilizing.set(false);
            session.close();
            for (PeerListener peerListener : peerListeners) {
                peerListener.onExceptionCaught(this,new InvalidPeerVersion(peerVersion));
            }
        }
    }


    private String buildMsg(BaseMsg msg,boolean isSingleMsg) throws JSONException {
        msg.setId(msgIdGenerator.incrementAndGet());
        String msgStr = msg.toJson().toString();
        if (isSingleMsg){
            msgStr+="\n";
        }
        return msgStr;
    }

    private void msgArrived(JSONObject jsonObject){
        long id = jsonObject.getLong("id");
        if (waitingRequests.containsKey(id)){
            String method = waitingRequests.get(id);
            switch (Method.getMethodByName(method)){
                case VERSION:
                    receiveVersion(jsonObject);
                    break;
                case GET_PEERS:
                    log.info("method not implemented");
                    break;
                case ADDRESS_SUBSCRIBE:
                    log.info("method not implemented");
                    break;
                default:
                    log.info("dispatch method "+method+" not implemented");
                    break;
            }
        }else {
            log.info("Message arrive without a waiting request type..");
        }

    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        log.info("Session created: "+ioSession.getId());
        session = ioSession;
    }

    @Override
    public void sessionOpened(IoSession ioSession) throws Exception {
        log.info("Session opened: "+ioSession.getId());
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        log.info("Session closed: "+ioSession.getId());
    }

    @Override
    public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
        log.error("exceptionCaught: "+ioSession.getId());
        throwable.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession ioSession, Object s) throws Exception {
        log.info("messageReceived: "+s.toString()+", session id:"+ioSession.getId());
        msgArrived((JSONObject) s);

    }

    @Override
    public void messageSent(IoSession ioSession, Object o) throws Exception {
        log.info("messageSent: "+o+", session id:"+ioSession.getId());

    }

    @Override
    public void inputClosed(IoSession ioSession) throws Exception {
        log.error("input closed session id:"+ioSession.getId());
    }


    static class JsonDecoder extends ProtocolDecoder<JSONObject> {

        @Override
        public JSONObject decode(ByteBuffer byteBuffer) throws InvalidProtocolViolationException {
            try {
                return new JSONObject(new String(byteBuffer.array(),"UTF-8"));
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






















    private interface MsgListener{
        void onMsgReceived(String jsonStr);
        void onMsgFail(String jsonStr);
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
