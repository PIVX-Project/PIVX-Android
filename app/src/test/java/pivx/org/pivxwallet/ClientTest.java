package pivx.org.pivxwallet;

import com.google.protobuf.ByteString;

import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by furszy on 6/5/17.
 */

public class ClientTest {


    @Test
    public void connectClientTest() throws JSONException, IOException {
        // Msg version
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",1);
        jsonObject.put("method","server.version");
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("2.9.5");
        jsonArray.put("0.6");
        jsonObject.put("params",jsonArray);
        // Send
        final Socket socket = new Socket("localhost", 50001);
        socket.setReuseAddress(true);
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
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id",2);
        jsonObject.put("method","blockchain.address.get_balance");
        JSONObject balanceJson = new JSONObject();
        balanceJson.put("address","DDjju8xCtGczPCrdz2LG683xj8j8hz1XpU");
        jsonObject.put("params",balanceJson);
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






}
