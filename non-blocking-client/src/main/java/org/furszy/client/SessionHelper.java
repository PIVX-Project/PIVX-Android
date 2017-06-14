package org.furszy.client;

import org.furszy.client.basic.IoSessionImp;
import org.furszy.client.interfaces.ConnectFuture;
import org.furszy.client.interfaces.IoHandler;
import org.furszy.client.interfaces.IoSessionConf;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicLong;

import static org.furszy.client.basic.IoSessionImp.ATTR_CONNECT_FUTURE;

/**
 * Created by mati on 12/05/17.
 */

public class SessionHelper {

    public AtomicLong sessionIdDistributor = new AtomicLong(0);

    public IoSessionImp newSession(SocketChannel socketChannel, IoSessionConf ioSessionConf) throws Exception {
        return new IoSessionImp(sessionIdDistributor.incrementAndGet(),socketChannel,ioSessionConf){

        };
//        try {
//            Constructor<? extends IoSessionImp> ioSessionImpConstructor = ioSessionConf.getIoSessionClass().getDeclaredConstructor(Long.class,Channel.class);
//            return ioSessionImpConstructor.newInstance(sessionIdDistributor.incrementAndGet(),socketChannel);

//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        throw new Exception("see problems above..");
    }


    public void initSession(IoSessionImp session, IoHandler ioHandler, ConnectFuture connectFuture) {
        // nothing yet..
        session.setHandler(ioHandler);
        session.addAttribute(ATTR_CONNECT_FUTURE,connectFuture);
    }
}
