package pivx.org.pivxwallet.module.pivtrum;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.CoinDefinition;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.net.ClientConnectionManager;
import org.bitcoinj.net.NioClientManager;
import org.furszy.client.IoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import pivx.org.pivxwallet.module.pivtrum.messages.VersionMsg;
import pivx.org.pivxwallet.module.wallet.WalletManager;

/**
 * Created by furszy on 6/12/17.
 *
 * Class in charge of manage the connection with pivtrum servers.
 */

public class PivtrumPeergroup {

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
    /** How many milliseconds to wait after receiving a pong before sending another ping. */
    public static final long DEFAULT_PING_INTERVAL_MSEC = 2000;
    private long pingIntervalMsec = DEFAULT_PING_INTERVAL_MSEC;
    /** Wallet manager */
    private WalletManager walletManager;
    /** Minumum amount of server in which the app is going to broadcast a tx */
    private int minBroadcastConnections = CoinDefinition.minBroadcastConnections;

    public PivtrumPeergroup(NetworkConf networkConf) throws IOException {
        this.peers = new CopyOnWriteArrayList<>();
        this.pendingPeers = new CopyOnWriteArrayList<>();
        this.networkConf = networkConf;
        this.ioManager = new IoManager(1,1);
        // create the version message that the manager will always use
        versionMsg = new VersionMsg(networkConf.getClientName(),networkConf.getMaxProtocolVersion(),networkConf.getMinProtocolVersion());
    }

    /**
     *
     * First synchronized start
     *
     */
    public synchronized void start(){
        try {
            log.info("Starting PivtrumPeergroup");
            isRunning = true;
            // todo: first part discovery..
            /*
            * Connect to the trusted node and get servers from it.
            */
            trustedPeer = new PivtrumPeer(networkConf.getTrustedServer(), ioManager);
            trustedPeer.connect();
            // Get more peers from the trusted server to use it later
            trustedPeer.getPeers();
            // now i suscribe my watched addresses to the trusted server
            List<Address> addresses = walletManager.getWatchedAddresses();
            trustedPeer.subscribeAddresses(addresses);

        }catch (Exception e){
            isRunning = false;
            e.printStackTrace();
        }
    }


    public boolean isRunning(){
        return isRunning;
    }


}
