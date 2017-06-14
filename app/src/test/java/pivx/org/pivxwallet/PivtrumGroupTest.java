package pivx.org.pivxwallet;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.android.BasicLogcatConfigurator;
import pivx.org.pivxwallet.module.pivtrum.NetworkConf;
import pivx.org.pivxwallet.module.pivtrum.PivtrumPeergroup;

/**
 * Created by furszy on 6/13/17.
 */

public class PivtrumGroupTest {


    @Test
    public void connectionTest() throws IOException {

        NetworkConf networkConf = new NetworkConf(new InetSocketAddress("localhost",50001));
        PivtrumPeergroup pivtrumPeergroup = new PivtrumPeergroup(networkConf);
        pivtrumPeergroup.start();

        assert pivtrumPeergroup.isRunning():"Peergroup is not running";
        System.out.println("PivtrumGroup connected!");

    }

    @Test
    public void serverVersionTest() throws IOException, InterruptedException {

        final Logger logger = LoggerFactory.getLogger("serverVersionTest");

        logger.info("Starting");

        NetworkConf networkConf = new NetworkConf(new InetSocketAddress("localhost",50001));
        PivtrumPeergroup pivtrumPeergroup = new PivtrumPeergroup(networkConf);
        pivtrumPeergroup.start();
        assert pivtrumPeergroup.isRunning():"Peergroup is not running";

        // wait for send message
        while (true){
            TimeUnit.SECONDS.sleep(5);
        }


    }

}
