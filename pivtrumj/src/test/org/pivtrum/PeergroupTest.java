/*
package org.pivtrum;

import org.pivxj.core.Address;
import org.pivxj.core.Coin;
import org.pivxj.utils.MonetaryFormat;
import org.furszy.client.exceptions.ConnectionFailureException;
import org.junit.Test;
import org.pivtrum.imp.AddressStoreImp;
import org.pivtrum.imp.ContextWrapperImp;
import org.pivtrum.imp.WalletConfigurationsImp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import global.WalletConfiguration;
import pivtrum.NetworkConf;
import pivtrum.PivtrumPeer;
import pivtrum.PivtrumPeergroup;
import pivtrum.listeners.AddressListener;
import store.AddressStore;
import store.CantInsertAddressException;
import wallet.WalletManager;

*
 * Created by furszy on 6/15/17.
 * todo: probar qué id tienen los push del servidor del subscribe address o del subscribe height... necesito saber eso para decodificarlo.



public class PeergroupTest {


    @Test
    public void connectPivtrumPeergroupTest() throws IOException, ConnectionFailureException, InterruptedException {
        ContextWrapperImp contextWrapperImp = new ContextWrapperImp();
        WalletConfiguration walletConfiguration = new WalletConfigurationsImp();
        NetworkConf networkConf = new NetworkConf("localhost",50001);
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

    @Test
    public void pushAddressPivtrumPeergroupTest() throws IOException, CantInsertAddressException, ConnectionFailureException, InterruptedException {
        ContextWrapperImp contextWrapperImp = new ContextWrapperImp();
        WalletConfiguration walletConfiguration = new WalletConfigurationsImp();
        NetworkConf networkConf = new NetworkConf("localhost",50001);
        // fake non trusted peers to start testing..
        networkConf.addPeers(fakePeers());
        WalletManager walletManager = new WalletManager(contextWrapperImp,walletConfiguration);
        walletManager.init();
        AddressStore addressStore = new AddressStoreImp();
        PivtrumPeergroup pivtrumPeergroup = new PivtrumPeergroup(networkConf,walletManager,addressStore);
        pivtrumPeergroup.addAddressListener(new AddressListener() {
            @Override
            public void onBalanceChange(String address, long confirmed, long unconfirmed,int numConfirmations) {
                System.out.println("onBalanceChange, address: "+address+
                        ", confirmed amount: "+Coin.valueOf(confirmed).toFriendlyString()+
                        " ,unconfirmed amount: "+Coin.valueOf(unconfirmed).toFriendlyString()+
                        "\n Amount of confirmations: "+numConfirmations);
            }
        });
        pivtrumPeergroup.start();
        while (!pivtrumPeergroup.isRunning()){
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        assert pivtrumPeergroup.isRunning():"PivtrumPeergroup is not running..";
        // subscribe address
        Address address = Address.fromBase58(walletConfiguration.getNetworkParams(),"yCRaSQvLd5a9VFFv9dzns2zNMJhWyymtAd");
        pivtrumPeergroup.addWatchedAddress(address);
        while (true){
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private List<InetSocketAddress> fakePeers(){
        List<InetSocketAddress> peers = new ArrayList<>();
        peers.add(new InetSocketAddress("localhost",50001));
        peers.add(new InetSocketAddress("localhost",50001));
        peers.add(new InetSocketAddress("localhost",50001));
        peers.add(new InetSocketAddress("localhost",50001));
        return peers;
    }


}
*/
