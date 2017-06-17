package org.pivtrum;

import org.junit.Test;
import org.pivtrum.imp.AddressStoreImp;
import org.pivtrum.imp.ContextWrapperImp;
import org.pivtrum.imp.WalletConfigurationsImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import global.WalletConfiguration;
import pivtrum.NetworkConf;
import pivtrum.PivtrumPeergroup;
import store.AddressStore;
import wallet.WalletManager;

/**
 * Created by furszy on 6/15/17.
 * todo: probar qué id tienen los push del servidor del subscribe address o del subscribe height... necesito saber eso para decodificarlo.
 */

public class PeergroupTest {


    @Test
    public void connectPivtrumPeergroupTest() throws IOException {
        ContextWrapperImp contextWrapperImp = new ContextWrapperImp();
        WalletConfiguration walletConfiguration = new WalletConfigurationsImp();
        NetworkConf networkConf = new NetworkConf(new InetSocketAddress("localhost",50001));
        WalletManager walletManager = new WalletManager(contextWrapperImp,walletConfiguration);
        walletManager.init();
        AddressStore addressStore = new AddressStoreImp();
        PivtrumPeergroup pivtrumPeergroup = new PivtrumPeergroup(networkConf,walletManager,addressStore);
        pivtrumPeergroup.start();
        while (true){
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //assert pivtrumPeergroup.isRunning():"PivtrumPeergroup is not running..";
    }


}
