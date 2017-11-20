package pivtrum;

import org.pivxj.core.Address;
import org.pivxj.core.CoinDefinition;
import org.furszy.client.IoManager;
import org.furszy.client.exceptions.ConnectionFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import pivtrum.exceptions.InvalidPeerVersion;
import pivtrum.listeners.AddressListener;
import pivtrum.listeners.PeerDataListener;
import pivtrum.listeners.PeerListener;
import pivtrum.messages.VersionMsg;
import pivtrum.messages.responses.StatusHistory;
import pivtrum.messages.responses.Unspent;
import pivtrum.utility.TxHashHeightWrapper;
import store.AddressBalance;
import store.AddressNotFoundException;
import store.AddressStore;
import store.CantInsertAddressException;
import store.DbException;
import wallet.WalletManager;

/**
 * Created by furszy on 6/12/17.
 *
 * Class in charge of manage the connection with pivtrum servers.
 *
 * todo: para el look-a-head del bip 32 tengo que estar observando addresses de la account que compartí, 10 addresses despues de la última marcada(vista en la blockhain).
 */

public class PivtrumPeergroup implements PeerListener, PeerDataListener {

    private static final Logger log = LoggerFactory.getLogger(PivtrumPeergroup.class);

    /**
     * Default number of connections
     */
    public static final int DEFAULT_CONNECTIONS = 1;

    /** Network configurations */
    private NetworkConf networkConf;
    /** Connection manager */
    private IoManager ioManager;
    /** Trusted peer */
    private PivtrumPeer trustedPeer;
    // Currently active peers.
    private final CopyOnWriteArrayList<PivtrumPeer> peers;
    // Currently connecting peers.
    private final CopyOnWriteArrayList<PivtrumPeer> pendingPeers;
    // The version message to use for new connections
    private VersionMsg versionMsg;
    // How many connections we want to have open at the current time. If we lose connections, we'll try opening more
    // until we reach this count.
    private AtomicInteger maxConnections = new AtomicInteger(1);
    // Whether the peer group is currently running. Once shut down it cannot be restarted.
    private volatile boolean isRunning;
    /** Whether the peer group was active. */
    private volatile boolean isActive;
    /** How many milliseconds to wait after receiving a pong before sending another ping. */
    public static final long DEFAULT_PING_INTERVAL_MSEC = 2000;
    private long pingIntervalMsec = DEFAULT_PING_INTERVAL_MSEC;
    /** Wallet manager */
    private WalletManager walletManager;
    /** Address-status store */
    private AddressStore addressStore;
    private CopyOnWriteArrayList<String> watchedAddresses = new CopyOnWriteArrayList<>();
    /** Addresses waiting for an update, address-  */
    //private List<String> waitingAddressses = new ConcurrentHashMap();
    /** Minumum amount of server in which the app is going to broadcast a tx */
    private int minBroadcastConnections = CoinDefinition.minBroadcastConnections;
    /** Address balance listener */
    private CopyOnWriteArrayList<AddressListener> addressListeners = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<PeerListener> peerConnectionListeners = new CopyOnWriteArrayList<>();

    public PivtrumPeergroup(NetworkConf networkConf, WalletManager walletManager, AddressStore addressStore) throws IOException {
        this.peers = new CopyOnWriteArrayList<>();
        this.pendingPeers = new CopyOnWriteArrayList<>();
        this.networkConf = networkConf;
        this.walletManager = walletManager;
        this.addressStore = addressStore;
        this.ioManager = new IoManager(1,1);
        // create the version message that the manager will always use
        versionMsg = new VersionMsg(networkConf.getClientName(),networkConf.getMaxProtocolVersion(),networkConf.getMinProtocolVersion());
    }

    public PivtrumPeergroup(NetworkConf networkConf) throws IOException {
        this.peers = new CopyOnWriteArrayList<>();
        this.pendingPeers = new CopyOnWriteArrayList<>();
        this.networkConf = networkConf;
        this.ioManager = new IoManager(1,1);
        // create the version message that the manager will always use
        versionMsg = new VersionMsg(networkConf.getClientName(),networkConf.getMaxProtocolVersion(),networkConf.getMinProtocolVersion());
    }

    public void setWalletManager(WalletManager walletManager) {
        this.walletManager = walletManager;
    }

    public void setAddressStore(AddressStore addressStore) {
        this.addressStore = addressStore;
    }

    public void addAddressListener(AddressListener addressListener) {
        this.addressListeners.add(addressListener);
    }

    public void addPeerConnectionListener(PeerListener peerListener){
        this.peerConnectionListeners.add(peerListener);
    }

    public void removePeerConnectionListener(PeerListener peerListener){
        this.peerConnectionListeners.remove(peerListener);
    }

    /**
     *
     * La conexión no deberia ser sincrona, lo único que necesito es agregar una variable de "isRunning", una de "isConnecting" y un listener de conexion.
     * todo: return future..
     */
    public synchronized void start() throws InterruptedException, ConnectionFailureException {
        try {
            log.info("Starting PivtrumPeergroup");
            isActive = true;
            // todo: first part discovery..
            /*
            * Connect to the trusted node and get servers from it.
            */
            trustedPeer = new PivtrumPeer(networkConf.getTrustedServer(), ioManager,versionMsg);
            trustedPeer.addPeerListener(this);
            trustedPeer.addPeerDataListener(this);
            trustedPeer.connect();

        }catch (Exception e){
            isRunning = false;
            isActive = false;
            log.error("PivtrumPeerGroup start",e);
            throw e;
        }
    }


    public boolean isRunning(){
        return isRunning;
    }


    @Override
    public void onConnected(PivtrumPeer pivtrumPeer) {
        try {
            if (pivtrumPeer == trustedPeer) {
                log.info("trusted peer connected");
                // trusted peer connected.
                isRunning = true;

                // notify -> todo: this should be on other thread.
                for (PeerListener peerConnectionListener : peerConnectionListeners) {
                    peerConnectionListener.onConnected(pivtrumPeer);
                }

                // Get more peers from the trusted server to use it later
                trustedPeer.getPeers();
                // Suscribe watched addresses to the trusted server
                Map<String,AddressBalance> map = addressStore.map();
                watchedAddresses.addAll(map.keySet());
                if (!map.isEmpty()) {
                    trustedPeer.subscribeAddresses(map.keySet());
                }

                // connect to non trusted peers
                for (InetSocketAddress inetSocketAddress : networkConf.getNetworkServers()) {
                    PivtrumPeerData peerData = new PivtrumPeerData(inetSocketAddress.getHostName(),inetSocketAddress.getPort(),0);
                    PivtrumPeer peer = new PivtrumPeer(peerData,ioManager,versionMsg);
                    peer.addPeerListener(this);
                    peer.addPeerDataListener(this);
                    pendingPeers.add(peer);
                    peer.connect();
                }
            }else {
                log.info("Non trusted peer connected, "+pivtrumPeer.getPeerData());
                pendingPeers.remove(pivtrumPeer);
                peers.add(pivtrumPeer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDisconnected(PivtrumPeer pivtrumPeer) {

    }

    @Override
    public void onExceptionCaught(PivtrumPeer pivtrumPeer, Exception e) {
        if (e instanceof InvalidPeerVersion){
            if (pivtrumPeer == trustedPeer){
                // We are fuck. Invalid trusted peer version..
                isActive = false;
                isRunning = false;
                // notify error..

            }
        }
    }

    @Override
    public void onSubscribedAddressChange(PivtrumPeer pivtrumPeer, String address, String status) {
        try {
            if (status==null)return;
            AddressBalance statusDb = null;
            try {
                statusDb = addressStore.getAddressStatus(address);
            } catch (AddressNotFoundException e) {
                // nothing
            }
            if (statusDb == null) statusDb = new AddressBalance();
            if (statusDb.getStatus()==null || !status.equals(statusDb.getStatus())){

                // this should done be when the balance is updated
                log.info("inserting new address-status");
                statusDb.setStatus(status);
                statusDb.addStatusConfirmation();
                addressStore.insert(address,statusDb);

                // first request balance
                // notify
                // todo: here i should request the tx for that address and recalculate the balance.
                // request unspent of address change
                trustedPeer.getBalance(address);
                trustedPeer.getHistory(address);
                // todo: mejorar esto con request en batch -> simplificaria la cantidad de request a la mitad.
                // request status to other peers - 4 max
                for (PivtrumPeer peer : peers) {
                    peer.getHistory(address);
                }
                // request balance to other peers - 4 max
                for (PivtrumPeer peer : peers) {
                    peer.getBalance(address);
                }
            }
        } catch (CantInsertAddressException e) {
            e.printStackTrace();
            log.error("onReceiveAddress error",e);
        }
    }

    @Override
    public void onListUnpent(PivtrumPeer pivtrumPeer,String address, List<Unspent> unspents) {
        log.info("onListUnspent: "+address);
        // now i should check this unspent tx requesting the merkle root.

        // then request the header to multiple peers and validate that with the txHash.
        List<Long> heightOfHeadersToRequest = new ArrayList<>();
        for (Unspent unspent : unspents) {
            if (!heightOfHeadersToRequest.contains(unspent.getBlockHeight())){
                heightOfHeadersToRequest.add(unspent.getBlockHeight());
            }
        }
        trustedPeer.getHeader(heightOfHeadersToRequest.get(0));
    }

    public void addWatchedAddress(Address address) {
        try {
            String addressStr = address.toBase58();
            if (watchedAddresses.contains(addressStr))return;
            if (!addressStore.contains(addressStr)) {
                addressStore.insert(addressStr,new AddressBalance());
            }
            trustedPeer.subscribeAddress(address.toBase58());
        } catch (DbException e) {
            e.printStackTrace();
            throw new IllegalStateException("Db problem",e);
        } catch (CantInsertAddressException e) {
            e.printStackTrace();
            throw new IllegalStateException("Db problem",e);
        }
    }

    @Override
    public void onBalanceReceive(PivtrumPeer pivtrumPeer, String address, long confirmed, long unconfirmed) {
        try {
            if (pivtrumPeer == trustedPeer) {
                AddressBalance addressBalance = addressStore.getAddressStatus(address);

                long prevConfirmedBalance = addressBalance.getConfirmedBalance();
                long prevUnConfirmedBalance = addressBalance.getUnconfirmedBalance();

                addressBalance.setConfirmedBalance(confirmed);
                addressBalance.setUnconfirmedBalance(unconfirmed);
                addressBalance.addBalanceConfirmation();
                addressStore.insert(address,addressBalance);

                // notify
                notifyBalance(address,confirmed-prevConfirmedBalance,unconfirmed-prevUnConfirmedBalance,addressBalance.getAmountOfBalanceConfirmations());
            }else {
                AddressBalance addressBalance = addressStore.getAddressStatus(address);
                if (addressBalance.getConfirmedBalance() == confirmed && addressBalance.getUnconfirmedBalance()==unconfirmed){
                    addressBalance.addBalanceConfirmation();
                    // Notify
                    notifyBalance(address,addressBalance.getConfirmedBalance(),addressBalance.getUnconfirmedBalance(),addressBalance.getAmountOfBalanceConfirmations());
                }else {
                    log.info("AddressBalance different in peer than in the db, requesting again status from the trusted peer",addressBalance,address);
                }

            }
        } catch (AddressNotFoundException e) {
            e.printStackTrace();
        } catch (CantInsertAddressException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onGetHistory(PivtrumPeer pivtrumPeer, StatusHistory statusHistory) {
        try {
            log.info("onGetHistory, address: "+statusHistory.getAddress()+", status: "+statusHistory.getStatus());
            AddressBalance addressBalance = addressStore.getAddressStatus(statusHistory.getAddress());
            if (addressBalance.getStatus().equals(statusHistory.getStatus())){
                addressBalance.addStatusConfirmation();
                if(pivtrumPeer == trustedPeer){
                    addressBalance.addAllTx(statusHistory.getTxHashHeight());
                }
                addressStore.insert(statusHistory.getAddress(),addressBalance);
            }
        } catch (AddressNotFoundException e) {
            e.printStackTrace();
        } catch (CantInsertAddressException e) {
            e.printStackTrace();
        }
    }

    private void notifyBalance(String address,long confirmed,long unconfirmed,int confirmationsAmount){
        for (AddressListener addressListener : addressListeners) {
            addressListener.onBalanceChange(address,confirmed,unconfirmed,confirmationsAmount);
        }
    }

    public void shutdown() {
        //todo: check if this is fine.. i have to let every single listener know about this action.
        ioManager.shutdown();
    }
}
