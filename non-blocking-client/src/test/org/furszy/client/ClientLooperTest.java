package org.furszy.client;

import org.furszy.client.basic.ConnectionId;
import org.furszy.client.basic.IoSessionConfImp;
import org.furszy.client.exceptions.ConnectionFailureException;
import org.furszy.client.exceptions.InvalidProtocolViolationException;
import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoHandler;
import org.furszy.client.interfaces.ProtocolDecoder;
import org.furszy.client.interfaces.ProtocolEncoder;
import org.furszy.client.interfaces.write.WriteFuture;
import org.furszy.client.interfaces.write.WriteRequest;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

/**
 * Created by mati on 12/05/17.
 */

public class ClientLooperTest {


    @Test
    public void runServerTest(){

        try {
            ServerSocket serverSocket = new ServerSocket(9999);
            while (true){

                Socket socket = serverSocket.accept();

                System.out.println("socket conectado: "+socket.getRemoteSocketAddress());

                byte[] bytes = new byte[8048];
                socket.getInputStream().read(bytes);
                System.out.println(new String(bytes,"UTF-8"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void connectServerTest() throws Throwable {

        IoManager ioManager = new IoManager(1,1);

        SocketAddress remoteAddress = new InetSocketAddress(9999);
        SocketAddress localAddress = new InetSocketAddress(9443);
        IoSessionConfImp ioSessionConfImp = new IoSessionConfImp();
        ioSessionConfImp.setProtocolDecoder(new StringDecoder());
        ioSessionConfImp.setProtocolEncoder(new StringEncoder());
        ConnectFuture connectFuture = ioManager.connect(remoteAddress,localAddress,new IoHandlerImpTest(),ioSessionConfImp);
        connectFuture.get(TimeUnit.SECONDS.toMillis(30));
        ConnectionId connectionId = connectFuture.getConnectionId();

        if (connectionId==null){
            throw connectFuture.getException();
        }

        WriteRequest writeRequest = ioManager.send("holaa",connectionId);

        WriteFuture writeFuture = writeRequest.getFuture();
        writeFuture.get(TimeUnit.SECONDS.toMillis(30));

        assert writeFuture.isSent();
    }

    @Test
    public void sendMessageTest() throws IOException, ConnectionFailureException, InterruptedException {





    }


    static class IoHandlerImpTest implements IoHandler {

        @Override
        public void sessionCreated(org.furszy.client.interfaces.IoSession session) throws Exception {
            System.out.println("Session created! "+session.getId());
        }

        @Override
        public void sessionOpened(org.furszy.client.interfaces.IoSession session) throws Exception {
            System.out.println("Session created!"+session.getId());
        }

        @Override
        public void sessionClosed(org.furszy.client.interfaces.IoSession session) throws Exception {
            System.out.println("Session closed!"+session.getId());
        }

        @Override
        public void exceptionCaught(org.furszy.client.interfaces.IoSession session, Throwable cause) throws Exception {
            System.out.println("Session exception caught!"+session.getId()+", "+cause.getMessage());
        }

        @Override
        public void messageReceived(org.furszy.client.interfaces.IoSession session, Object message) throws Exception {

        }

        @Override
        public void messageSent(org.furszy.client.interfaces.IoSession session, Object message) throws Exception {

        }

        @Override
        public void inputClosed(org.furszy.client.interfaces.IoSession session) throws Exception {

        }
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
