package pivx.org.pivxwallet;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Test;

import com.snappydb.SnappydbException;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.net.InetSocketAddress;

import pivx.org.pivxwallet.module.WalletConfImp;
import global.WalletConfiguration;
import pivtrum.NetworkConf;
import pivtrum.PivtrumPeergroup;
import pivx.org.pivxwallet.module.store.AddressStore;
import pivx.org.pivxwallet.module.store.SnappyStore;
import wallet.WalletManager;

/**
 * Created by furszy on 6/13/17.
 */

public class PivtrumGroupTest {

    Context context;
    PivxApplication contextWrapper;

    @Before
    public void beforeTest(){
        context = InstrumentationRegistry.getTargetContext();
        contextWrapper = PivxApplication.getInstance();
    }



    @Test
    public void connectionTest() throws IOException, SnappydbException {
        NetworkConf networkConf = new NetworkConf(new InetSocketAddress("localhost",50001));
        WalletConfiguration walletConfiguration = new WalletConfImp();
        WalletManager walletManager = new WalletManager(contextWrapper,walletConfiguration);
        AddressStore addressStore = new SnappyStore(context);
        PivtrumPeergroup pivtrumPeergroup = new PivtrumPeergroup(networkConf,walletManager,addressStore);
        pivtrumPeergroup.start();

        assert pivtrumPeergroup.isRunning():"Peergroup is not running";
        System.out.println("PivtrumGroup connected!");

    }

    @Test
    public void serverVersionTest() throws IOException, InterruptedException, SnappydbException {

        final Logger logger = LoggerFactory.getLogger("serverVersionTest");

        /*logger.info("Starting");
        NetworkConf networkConf = new NetworkConf(new InetSocketAddress("localhost",50001));
        WalletManager walletManager = new WalletManager(contextWrapper,new WalletConfImp());
        store.AddressStore addressStore = Mockito.mock(store.AddressStore.class);
        PivtrumPeergroup pivtrumPeergroup = new PivtrumPeergroup(networkConf,walletManager,addressStore);
        pivtrumPeergroup.start();
        assert pivtrumPeergroup.isRunning():"Peergroup is not running";

        // wait for send message
        while (true){
            TimeUnit.SECONDS.sleep(5);
        }*/
    }


}
