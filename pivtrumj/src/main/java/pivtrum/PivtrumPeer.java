package pivtrum;

import com.google.protobuf.ByteString;

import org.pivxj.core.Sha256Hash;
import org.furszy.client.IoManager;
import org.furszy.client.basic.BaseMsgFuture;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import pivtrum.exceptions.InvalidPeerVersion;
import pivtrum.listeners.PeerDataListener;
import pivtrum.listeners.PeerListener;
import pivtrum.messages.BaseMsg;
import pivtrum.messages.GetBalanceMsg;
import pivtrum.messages.GetHeader;
import pivtrum.messages.GetHistoryMsg;
import pivtrum.messages.GetTxMsg;
import pivtrum.messages.ListUnspentMsg;
import pivtrum.messages.Method;
import pivtrum.messages.SubscribeAddressMsg;
import pivtrum.messages.VersionMsg;
import pivtrum.messages.responses.StatusHistory;
import pivtrum.messages.responses.Unspent;
import pivtrum.utility.TxHashHeightWrapper;

/**
 * Created by furszy on 6/12/17.
 *
 * Class in charge of connect to a single peer on the network
 *
 * todo: create batch message builder and bathMessage
 */

public class PivtrumPeer implements IoHandler{

    private final Logger log;

    /** Peer data */
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
    private ConcurrentMap<Long,BaseMsg> waitingRequests = new ConcurrentHashMap<>();
    /** Peer height */
    private long height;

    /** Listeners */
    private CopyOnWriteArrayList<PeerListener> peerListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<PeerDataListener> peerDataListeners = new CopyOnWriteArrayList<>();

    public PivtrumPeer(PivtrumPeerData peerData,IoManager ioManager,VersionMsg versionMsg) {
        this.peerData = peerData;
        this.ioManager = ioManager;
        this.versionMsg = versionMsg;
        this.log = LoggerFactory.getLogger(PivtrumPeer.class.getName()+"-"+peerData.getHost());
    }

    public void addPeerListener(PeerListener peerListener){
        peerListeners.add(peerListener);
    }

    public void addPeerDataListener(PeerDataListener peerDataListener){
        peerDataListeners.add(peerDataListener);
    }

    /**
     * Connect
     * todo: add future.
     */
    public void connect() throws ConnectionFailureException, InterruptedException {
        if (isInitilizing.compareAndSet(false,true) && !isRunning.get()) {
            IoSessionConfImp ioSessionConfImp = new IoSessionConfImp();
            ioSessionConfImp.setProtocolDecoder(new JsonDecoder());
            ioSessionConfImp.setProtocolEncoder(new StringEncoder());
            ConnectFuture future = ioManager.connect(new InetSocketAddress(peerData.getHost(), peerData.getTcpPort()), null, this, ioSessionConfImp);
            //future = future.get(TimeUnit.SECONDS.toNanos(30));
            /*if(future.isConnected()){
                session = future.getSession();
            }else {
                log.info("Connection fail",future.getException());
                throw new ConnectionFailureException(future.getException());
            }*/
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
            //writeRequest.getFuture().get(TimeUnit.SECONDS.toNanos(30));
        } catch (JSONException e) {
            e.printStackTrace();
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
            log.info("getPeers");
            BaseMsg getPeers = new BaseMsg(Method.GET_PEERS.getMethod());
            WriteFuture writeFuture = new WriteFutureImp();
            sendMsg(getPeers,true,writeFuture);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private void subscribeHeight() {
        log.info("subscribeHeight");
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(new BaseMsg(Method.HEIGHT_SUBSCRIBE.getMethod()),true,writeFuture);
    }

    /**
     *
     * @param addresses
     */
    public void subscribeAddresses(Set<String> addresses) {
        log.info("suscribe addresses: " + Arrays.toString(addresses.toArray()));
        for (String address : addresses) {
            subscribeAddress(address);
        }
    }

    public void subscribeAddress(String address){
        log.info("subscribe address: "+address);
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(
                new SubscribeAddressMsg(address),
                true,
                writeFuture
        );
    }



    /**
     *
     * @param address
     */
    public void listUnspent(String address){
        log.info("list unspent");
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(
                new ListUnspentMsg(address),
                true,
                writeFuture
        );
    }

    public void getBalance(String address) {
        log.info("getBalance");
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(
                new GetBalanceMsg(address),
                true,
                writeFuture
        );
    }

    /**
     *
     * @param height
     */
    public void getHeader(long height){
        log.info("getHeader");
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(
                new GetHeader(height),
                true,
                writeFuture
        );
    }

    /**
     *
     * @param address
     */
    public void getHistory(String address) {
        log.info("onGetHistory");
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(
                new GetHistoryMsg(address),
                true,
                writeFuture
        );
    }

    /**
     *
     * @param txHash
     */
    public void getTx(String txHash){
        log.info("onGetTx");
        WriteFuture writeFuture = new WriteFutureImp();
        sendMsg(
                new GetTxMsg(txHash),
                true,
                writeFuture
        );
    }

    private WriteRequest sendMsg(BaseMsg baseMsg, boolean singleRequest, WriteFuture writeFuture){
        if (session==null) throw new IllegalStateException("Not connected peer");
        if (session.isConnected()) {
            WriteRequest writeRequest = new WriteRequestImp(buildMsg(baseMsg, singleRequest), writeFuture);
            waitingRequests.put(baseMsg.getId(), baseMsg);
            session.addWriteRequest(writeRequest);
            return writeRequest;
        }else {
            throw new IllegalStateException("Session not connected");
        }
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
                // subscribe height before init
                subscribeHeight();
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

    private void receivePeers(JSONObject jsonObject){
        log.info("receive peers");
        //todo: save peers
        jsonObject.getJSONArray("result");
    }

    private void receiveAddress(JSONObject jsonObject, String address){
        log.info("receive address: "+jsonObject.toString());
        String result = jsonObject.getString("result");
        log.info("result: "+result);
        for (PeerDataListener peerDataListener : peerDataListeners) {
            peerDataListener.onSubscribedAddressChange(this,address,result);
        }
    }

    private void receiveUnspents(JSONObject jsonObject, ListUnspentMsg msg) {
        log.info("receive unspents: "+jsonObject.toString());
        List<Unspent> unspents = new ArrayList<>();
        JSONArray unspentArray = jsonObject.getJSONArray("result");
        for (int i=0;i<unspentArray.length();i++){
            JSONObject unspent = unspentArray.getJSONObject(0);
            int txPos = unspent.getInt("tx_pos");
            String txHash = unspent.getString("tx_hash");
            long value = unspent.getLong("value");
            long height = unspent.getLong("height");
            unspents.add(new Unspent(txPos,txHash,value,height));
        }
        for (PeerDataListener peerDataListener : peerDataListeners) {
            peerDataListener.onListUnpent(this,msg.getAddress(),unspents);
        }
    }

    private void receiveHeaders(JSONObject jsonObject,GetHeader getHeader){
        log.info("receive getHeader, "+jsonObject.toString());

    }

    private void receiveSubscribeHeight(JSONObject jsonObject){
        log.info("receive receiveSubscribeHeight, "+jsonObject.toString());
        if (jsonObject.has("result")){
            this.height = jsonObject.getLong("result");
        }else
            this.height = jsonObject.getJSONArray("params").getLong(0);
    }

    private void receiveGetBalance(JSONObject jsonObject,GetBalanceMsg msg){
        log.info("receive receiveGetBalance, "+jsonObject.toString());
        JSONObject jsonObj = jsonObject.getJSONObject("result");
        long confirmed = jsonObj.getLong("confirmed");
        long unconfirmed = jsonObj.getLong("unconfirmed");
        for (PeerDataListener peerDataListener : peerDataListeners) {
            peerDataListener.onBalanceReceive(this,msg.getAddress(),confirmed,unconfirmed);
        }
    }

    // {"result":[{"tx_hash":"d2b6046de1febf450f416eef820ecdfee30112d7522bc9470fb0ae44fc704e02","height":131213},{"tx_hash":"a79c6eefb61e544303e7e4c6d12150018d253ed92a7538ceddd38add228942cd","height":132939}],"id":3,"jsonrpc":"2.0"},
    private void receiveHistory(JSONObject jsonObject,String address){
        log.info("receiveHistory, "+jsonObject.toString());
        JSONArray jsonArray = jsonObject.getJSONArray("result");
        List<TxHashHeightWrapper> list = new ArrayList<>();
        // server hash status
        StringBuilder stringBuilder = new StringBuilder();
        for (int i =0;i<jsonArray.length();i++){
            JSONObject txAndHeightJson = jsonArray.getJSONObject(i);
            String txHash = txAndHeightJson.getString("tx_hash");
            long height = txAndHeightJson.getLong("height");
            stringBuilder.append(txHash)
                    .append(":")
                    .append(height)
                    .append(":");
            list.add(new TxHashHeightWrapper(txHash,height));
        }
        byte[] hash = Sha256Hash.hash(ByteString.copyFromUtf8(stringBuilder.toString()).toByteArray());
        String hashHex = Hex.toHexString(hash);
        for (PeerDataListener peerDataListener : peerDataListeners) {
            peerDataListener.onGetHistory(this,new StatusHistory(address,list,hashHex));
        }
    }

    private void receiveTx(JSONObject jsonObject,GetTxMsg getTxMsg){
        log.info("receive receiveTx, "+jsonObject.toString());
        //JSONArray jsonArray = jsonObject.getJSONArray("result");


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
        if (jsonObject.has("id")) {
            long id = jsonObject.getLong("id");
            if (waitingRequests.containsKey(id)) {
                BaseMsg baseMsg = waitingRequests.get(id);
                String method = baseMsg.getMethod();
                switch (Method.getMethodByName(method)) {
                    case VERSION:
                        receiveVersion(jsonObject);
                        break;
                    case GET_PEERS:
                        receivePeers(jsonObject);
                        break;
                    case ADDRESS_SUBSCRIBE:
                        receiveAddress(jsonObject, ((SubscribeAddressMsg) baseMsg).getAddress());
                        break;
                    case LIST_UNSPENT:
                        receiveUnspents(jsonObject, (ListUnspentMsg) baseMsg);
                        break;
                    case GET_HEADER:
                        receiveHeaders(jsonObject, (GetHeader) baseMsg);
                        break;
                    case HEIGHT_SUBSCRIBE:
                        receiveSubscribeHeight(jsonObject);
                        break;
                    case GET_BALANCE:
                        receiveGetBalance(jsonObject,(GetBalanceMsg)baseMsg);
                        break;
                    case GET_ADDRESS_HISTORY:
                        receiveHistory(jsonObject,((GetHistoryMsg)baseMsg).getAddress());
                        break;
                    case GET_TX:
                        receiveTx(jsonObject, (GetTxMsg) baseMsg);
                        break;
                    default:
                        log.info("dispatch method " + method + " not implemented");
                        break;
                }
                waitingRequests.remove(id);
            } else {
                log.info("Message arrive without a waiting request type.., "+jsonObject.toString());
            }
        }else {
            // Is a notification
            String method = jsonObject.getString("method");
            switch (Method.getMethodByName(method)){
                case HEIGHT_SUBSCRIBE:
                    receiveSubscribeHeight(jsonObject);
                    break;
                case ADDRESS_SUBSCRIBE:
                    // todo: get address from json object
                    receiveAddress(jsonObject,null);
                    break;
                default:
                    log.info("Message notification arrive without a waiting request type..");
                    break;
            }
        }
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        log.info("Session created: "+ioSession.getId());
        session = ioSession;
        log.info("Peer connected");
        // Send version
        sendVersion();
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

    public PivtrumPeerData getPeerData() {
        return peerData;
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
