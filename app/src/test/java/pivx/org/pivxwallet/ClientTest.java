package pivx.org.pivxwallet;

import com.google.protobuf.ByteString;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import pivtrum.messages.BaseMsg;
import pivtrum.messages.GetBalanceMsg;
import pivtrum.messages.GetHistoryMsg;
import pivtrum.messages.Method;
import pivtrum.messages.SubscribeAddressMsg;
import pivtrum.messages.VersionMsg;

/**
 * Created by furszy on 6/5/17.
 */

public class ClientTest {


    @Test
    public void connectClientTest() throws JSONException, IOException {
        // Msg version
        VersionMsg versionMsg = new VersionMsg("mobile","2.9.5","0.6");
        JSONObject jsonObject = versionMsg.toJson();
        jsonObject.put("id",1);
        // Send
        final Socket socket = getFactory().createSocket(); //new Socket("localhost", 50001);
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress("localhost", 50002));
        System.out.println("socket connected");
        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(jsonObject.toString()+'\n');
        bw.flush();
        System.out.println("server version sent");
        byte[] readBuffer = new byte[8064];
        int read = socket.getInputStream().read(readBuffer);
        if (read>1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(read);
            byteBuffer.put(readBuffer,0,read);
            String str = ByteString.copyFrom(byteBuffer.array()).toStringUtf8();
            System.out.println("str: "+str);
            JSONObject returnJson = new JSONObject(str);
        }
        osw.close();
        bw.close();
    }

    /**
     *
     * Return the confirmed and unconfirmed balances of a bitcoin address.
     * method -> blockchain.address.get_balance(**address**)
     * //address//
     * The address as a Base58 string.
     * //Response//
     * A dictionary with keys *confirmed* and *unconfirmed*.  The value of
     * each is the appropriate balance in coin units as a string.
     * //Response Example//::
     * {
     * "confirmed": "1.03873966",
     * "unconfirmed": "0.236844"
     * }
     *
     * @throws JSONException
     * @throws IOException
     */
    @Test
    public void getAddressBalanceTest() throws JSONException, IOException {
        // Msg version
        GetBalanceMsg getBalanceMsg = new GetBalanceMsg("DDjju8xCtGczPCrdz2LG683xj8j8hz1XpU");
        JSONObject jsonObject = getBalanceMsg.toJson();
        jsonObject.put("id",2);
        // Send
        final Socket socket = new Socket("localhost", 50001);
        socket.setReuseAddress(true);
        System.out.println("socket connected");
        System.out.println("sending: "+jsonObject.toString()+"\n");
        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(jsonObject.toString()+'\n');
        bw.flush();
        System.out.println("server getBalance sent");
        byte[] readBuffer = new byte[8064];
        int read = socket.getInputStream().read(readBuffer);
        if (read>1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(read);
            byteBuffer.put(readBuffer,0,read);
            String str = ByteString.copyFrom(byteBuffer.array()).toStringUtf8();
            System.out.println("str: "+str);
            JSONObject returnJson = new JSONObject(str);
        }
        osw.close();
        bw.close();
    }

    @Test
    public void getPeersTest() throws IOException, JSONException {
        // Send
        final Socket socket = new Socket("localhost", 50001);
        socket.setReuseAddress(true);
        System.out.println("socket connected");

        // Msg version
        VersionMsg versionMsg = new VersionMsg("mobile","2.9.5","0.6");
        JSONObject jsonVersion = versionMsg.toJson();
        jsonVersion.put("id",1);
        sendAndWaitReceive(socket,jsonVersion);
        // get peers
        BaseMsg baseMsg = new BaseMsg(Method.GET_PEERS.getMethod());
        baseMsg.setId(3);
        JSONObject jsonObject = baseMsg.toJson();
        sendAndWaitReceive(socket,jsonObject);
    }

    /**
     * Todo: make this
     */
    @Test
    public void subscribeAddresTest() throws IOException, JSONException {
        // Send
        final Socket socket = new Socket("localhost", 50001);
        socket.setReuseAddress(true);
        System.out.println("socket connected");

        SubscribeAddressMsg subscribeAddressMsg = new SubscribeAddressMsg("yChC1VQS5zET5pDxXgcc4bFye3Q9nurccG");
        subscribeAddressMsg.setId(4);
        sendAndWaitReceive(socket,subscribeAddressMsg.toJson());
    }

    @Test
    public void batchRequestTest() throws IOException, JSONException {
        GetHistoryMsg getBalanceMsg1 = new GetHistoryMsg("yCRaSQvLd5a9VFFv9dzns2zNMJhWyymtAd");
        getBalanceMsg1.setId(5);
        GetHistoryMsg getBalanceMsg2 = new GetHistoryMsg("yCRaSQvLd5a9VFFv9dzns2zNMJhWyymtAd");
        getBalanceMsg2.setId(6);
        GetHistoryMsg getBalanceMsg3 = new GetHistoryMsg("yCRaSQvLd5a9VFFv9dzns2zNMJhWyymtAd");
        getBalanceMsg3.setId(7);
        // Send
        final Socket socket1 = new Socket("localhost", 50001);
        socket1.setReuseAddress(true);
        System.out.println("socket1 connected");

        JSONObject jsonObject1 = getBalanceMsg1.toJson();
        JSONObject jsonObject2 = getBalanceMsg2.toJson();
        JSONObject jsonObject3 = getBalanceMsg3.toJson();

        OutputStreamWriter osw = new OutputStreamWriter(socket1.getOutputStream());
        BufferedWriter bw = new BufferedWriter(osw);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject1);
        jsonArray.put(jsonObject2);
        jsonArray.put(jsonObject3);
        bw.write(jsonArray.toString()+'\n');
        bw.flush();
        System.out.println("server batchRequest sent");
        byte[] readBuffer = new byte[8064];

        int read = socket1.getInputStream().read(readBuffer);
        if (read > 1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(read);
            byteBuffer.put(readBuffer, 0, read);
            String str = ByteString.copyFrom(byteBuffer.array()).toStringUtf8();
            System.out.println("str: " + str);
            JSONArray returnJson = new JSONArray(str);
        }


    }

    private void sendAndWaitReceive(Socket socket,JSONObject jsonObject) throws IOException, JSONException {
        System.out.println("sending: "+jsonObject.toString()+"\n");
        OutputStreamWriter osw = new OutputStreamWriter(socket.getOutputStream());
        BufferedWriter bw = new BufferedWriter(osw);
        bw.write(jsonObject.toString()+'\n');
        bw.flush();
        System.out.println("server getPeers sent");
        byte[] readBuffer = new byte[8064];
        int read = socket.getInputStream().read(readBuffer);
        if (read>1) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(read);
            byteBuffer.put(readBuffer,0,read);
            String str = ByteString.copyFrom(byteBuffer.array()).toStringUtf8();
            System.out.println("str: "+str);
            JSONObject returnJson = new JSONObject(str);
        }
        //osw.close();
        //bw.close();
    }



    private SSLSocketFactory getFactory(){
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) { return true; }
        };

        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
             return sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
