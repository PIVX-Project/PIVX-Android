package chain;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.ListenableFuture;

import org.pivxj.core.BlockChain;
import org.pivxj.core.CheckpointManager;
import org.pivxj.core.Peer;
import org.pivxj.core.PeerGroup;
import org.pivxj.core.Sha256Hash;
import org.pivxj.core.StoredBlock;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionBroadcast;
import org.pivxj.core.listeners.PeerConnectedEventListener;
import org.pivxj.core.listeners.PeerDataEventListener;
import org.pivxj.core.listeners.PeerDisconnectedEventListener;
import org.pivxj.net.discovery.MultiplexingDiscovery;
import org.pivxj.net.discovery.PeerDiscovery;
import org.pivxj.net.discovery.PeerDiscoveryException;
import org.pivxj.params.MainNetParams;
import org.pivxj.params.RegTestParams;
import org.pivxj.params.TestNet3Params;
import org.pivxj.store.BlockStore;
import org.pivxj.store.BlockStoreException;
import org.pivxj.store.LevelDBBlockStore;
import org.pivxj.store.SPVBlockStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import global.ContextWrapper;
import global.PivtrumGlobalData;
import global.WalletConfiguration;
import pivtrum.PivtrumPeerData;
import wallet.WalletManager;

import static global.PivtrumGlobalData.FURSZY_TESTNET_SERVER;

public class BlockchainManager {

    private static final Logger LOG = LoggerFactory.getLogger(BlockchainManager.class);

    public static final int BLOCKCHAIN_STATE_OFF = 10;
    public static final int BLOCKCHAIN_STATE_ON = 11;

    /** User-agent to use for network access. */
    public final String USER_AGENT;

    // system
    private ContextWrapper context;

    // wallet files..
    private WalletManager walletManager;
    private WalletConfiguration conf;

    private BlockStore blockStore;
    private File blockChainFile;
    private BlockChain blockChain;
    private PeerGroup peerGroup;

    private List<BlockchainManagerListener> blockchainManagerListeners;


    public BlockchainManager(ContextWrapper contextWrapper,WalletManager walletManager, WalletConfiguration conf) {
        this.walletManager = walletManager;
        this.conf = conf;
        this.context = contextWrapper;
        this.USER_AGENT = context.getPackageName()+"_AGENT";
        this.blockchainManagerListeners = new ArrayList<>();
    }

    public void init(BlockStore blockStore,File blockStoreDir,String blockStoreFilename,boolean blockStoreFileExists){
        synchronized (this) {

            // todo: en vez de que el service este maneje el blockchain deberia crear una clase que lo haga..
            blockChainFile = new File(blockStoreDir, blockStoreFilename);
            final boolean blockChainFileExists = blockChainFile.exists();

            if (!blockChainFileExists) {
                LOG.info("blockchain does not exist, resetting wallet");
                walletManager.reset();
            }

            // Create the blockstore
            try {
                this.blockStore = (blockStore!=null) ? blockStore : new LevelDBBlockStore(conf.getWalletContext(), blockChainFile);
                blockStore.getChainHead(); // detect corruptions as early as possible

                final long earliestKeyCreationTime = walletManager.getEarliestKeyCreationTime();

                if (!blockStoreFileExists && earliestKeyCreationTime > 0 && !(conf.getNetworkParams() instanceof RegTestParams)) {
                    try {
                        String filename = conf.getCheckpointFilename();
                        String suffix = conf.getNetworkParams() instanceof MainNetParams ? "":"-testnet";
                        final Stopwatch watch = Stopwatch.createStarted();
                        final InputStream checkpointsInputStream =  context.openAssestsStream(filename+suffix);
                        CheckpointManager.checkpoint(conf.getNetworkParams(), checkpointsInputStream, blockStore, earliestKeyCreationTime);
                        watch.stop();
                        LOG.info("checkpoints loaded from '{}', took {}", conf.getCheckpointFilename(), watch);
                    }catch (final IOException x) {
                        LOG.error("problem reading checkpoints, continuing without", x);
                    }catch (Exception e){
                        LOG.error("problem reading checkpoints, continuing without", e);
                    }
                }

            } catch (final BlockStoreException x) {
                blockChainFile.delete();

                final String msg = "blockstore cannot be created";
                LOG.error(msg, x);
                throw new Error(msg, x);
            }

            // create the blockchain
            try {
                blockChain = new BlockChain(conf.getNetworkParams(), blockStore);
                walletManager.addWalletFrom(blockChain);
            } catch (final BlockStoreException x) {
                throw new Error("blockchain cannot be created", x);
            }

        }

    }

    public void addDiscuonnectedEventListener(PeerDisconnectedEventListener listener){
        peerGroup.addDisconnectedEventListener(listener);
    }

    public void addConnectivityListener(PeerConnectedEventListener listener){
        peerGroup.addConnectedEventListener(listener);
    }

    public void removeDisconnectedEventListener(PeerDisconnectedEventListener listener){
        if (peerGroup!=null)
            peerGroup.removeDisconnectedEventListener(listener);
    }

    public void removeConnectivityListener(PeerConnectedEventListener listener){
        if (peerGroup!=null)
            peerGroup.removeConnectedEventListener(listener);
    }

    public void addBlockchainManagerListener(BlockchainManagerListener listener){
        if (blockchainManagerListeners==null) blockchainManagerListeners = new ArrayList<>();
        blockchainManagerListeners.add(listener);
    }

    public void removeBlockchainManagerListener(BlockchainManagerListener listener){
        if (blockchainManagerListeners!=null){
            blockchainManagerListeners.remove(listener);
        }
    }

    /**
     *
     * @param transactionHash
     */
    public ListenableFuture<Transaction> broadcastTransaction(byte[] transactionHash) {
        final Sha256Hash hash = Sha256Hash.wrap(transactionHash);
        final Transaction tx = walletManager.getTransaction(hash);
        return broadcastTransaction(tx);
    }
    public ListenableFuture<Transaction> broadcastTransaction(Transaction tx){
        if (peerGroup != null) {
            LOG.info("broadcasting transaction " + tx.getHashAsString());
            boolean onlyTrustedNode =
                    (conf.getNetworkParams() instanceof RegTestParams || conf.getNetworkParams() instanceof TestNet3Params)
                            ||
                            conf.getTrustedNodeHost()!=null;
            TransactionBroadcast transactionBroadcast = peerGroup.broadcastTransaction(
                    tx,
                    onlyTrustedNode?1:2,
                    false);
            return transactionBroadcast.broadcast();
        } else {
            LOG.info("peergroup not available, not broadcasting transaction " + tx.getHashAsString());
            return null;
        }
    }

    public void destroy(boolean resetBlockchainOnShutdown) {
        if (peerGroup != null) {
//            peerGroup.removeDisconnectedEventListener(peerConnectivityListener);
//            peerGroup.removeConnectedEventListener(peerConnectivityListener);
            walletManager.removeWalletFrom(peerGroup);
            if (peerGroup.isRunning())
                peerGroup.stopAsync();
            peerGroup = null;
            LOG.info("peergroup stopped");
        }

        try {
            blockStore.close();
        } catch (final BlockStoreException x) {
            throw new RuntimeException(x);
        }

        // save the wallet
        walletManager.saveWallet();

        if (resetBlockchainOnShutdown) {
            LOG.info("removing blockchain");
            blockChainFile.delete();
            blockChain=null;
            blockStore=null;
        }
    }

    public void check(Set<Impediment> impediments, PeerConnectedEventListener peerConnectivityListener, PeerDisconnectedEventListener peerDisconnectedEventListener , PeerDataEventListener blockchainDownloadListener){
        synchronized (this) {
            //final Wallet wallet = walletManager.getWallet();

            if (impediments.isEmpty() && peerGroup == null) {

                for (BlockchainManagerListener blockchainManagerListener : blockchainManagerListeners) {
                    blockchainManagerListener.checkStart();
                }

                // consistency check
                final int walletLastBlockSeenHeight = walletManager.getLastBlockSeenHeight();
                final int bestChainHeight = blockChain.getBestChainHeight();
                if (walletLastBlockSeenHeight != -1 && walletLastBlockSeenHeight != bestChainHeight) {
                    final String message = "wallet/blockchain out of sync: " + walletLastBlockSeenHeight + "/" + bestChainHeight;
                    LOG.error(message);
//                CrashReporter.saveBackgroundTrace(new RuntimeException(message), application.packageInfoWrapper());
                }
                LOG.info("starting peergroup");
                peerGroup = new PeerGroup(conf.getNetworkParams(), blockChain);
                peerGroup.setDownloadTxDependencies(0); // recursive implementation causes StackOverflowError
                walletManager.addWalletFrom(peerGroup);
                peerGroup.setUserAgent(USER_AGENT, context.getVersionName());
                peerGroup.addConnectedEventListener(peerConnectivityListener);
                peerGroup.addDisconnectedEventListener(peerDisconnectedEventListener);

                // Memory check
                final int maxConnectedPeers = context.isMemoryLow() ? 4 : 6 ;

                final String trustedPeerHost = conf.getTrustedNodeHost();
                final boolean hasTrustedPeer = trustedPeerHost != null;

                final boolean connectTrustedPeerOnly = true;//hasTrustedPeer && config.getTrustedPeerOnly();
                peerGroup.setMaxConnections(connectTrustedPeerOnly ? 1 : maxConnectedPeers);
                peerGroup.setConnectTimeoutMillis(conf.getPeerTimeoutMs());
                peerGroup.setPeerDiscoveryTimeoutMillis(conf.getPeerDiscoveryTimeoutMs());

                if (conf.isTest()){
                    peerGroup.setMinBroadcastConnections(1);
                }

                if (conf.getNetworkParams().equals(RegTestParams.get())) {
                    peerGroup.addPeerDiscovery(new PeerDiscovery() {
                        @Override
                        public InetSocketAddress[] getPeers(long services, long timeoutValue, TimeUnit timeUnit) throws PeerDiscoveryException {
                            // No regtest in pivx yet..
                            return null; //RegtestUtil.getPeersToConnect(conf.getNetworkParams(),conf.getNode());
                        }

                        @Override
                        public void shutdown() {

                        }
                    });
                } else {
                    peerGroup.addPeerDiscovery(new PeerDiscovery() {

                        private final PeerDiscovery normalPeerDiscovery = MultiplexingDiscovery.forServices(conf.getNetworkParams(), 0);

                        @Override
                        public InetSocketAddress[] getPeers(final long services, final long timeoutValue, final TimeUnit timeoutUnit)
                                throws PeerDiscoveryException {
                            final List<InetSocketAddress> peers = new LinkedList<InetSocketAddress>();

                            boolean needsTrimPeersWorkaround = false;

                            if (hasTrustedPeer) {
                                LOG.info("trusted peer '" + trustedPeerHost + "'" + (connectTrustedPeerOnly ? " only" : ""));
                                final InetSocketAddress addr;
                                if (trustedPeerHost.equals(FURSZY_TESTNET_SERVER) && !conf.isTest()){
                                    addr = new InetSocketAddress(trustedPeerHost, 8443);
                                }else {
                                    addr = new InetSocketAddress(trustedPeerHost, conf.getNetworkParams().getPort());
                                }

                                if (addr.getAddress() != null) {
                                    peers.add(addr);
                                    needsTrimPeersWorkaround = true;
                                }
                                /*if (conf.isTest()){
                                    // add one more peer to validate tx
                                    peers.add(new InetSocketAddress(FURSZY_TESTNET_SERVER,6444));
                                    needsTrimPeersWorkaround = false;
                                }*/
                            }else {
                                for (PivtrumPeerData pivtrumPeerData : PivtrumGlobalData.listTrustedHosts()) {
                                    peers.add(new InetSocketAddress(pivtrumPeerData.getHost(), pivtrumPeerData.getTcpPort()));
                                }
                            }

                            if (!connectTrustedPeerOnly)
                                peers.addAll(Arrays.asList(normalPeerDiscovery.getPeers(services, timeoutValue, timeoutUnit)));

                            // workaround because PeerGroup will shuffle peers
                            if (needsTrimPeersWorkaround)
                                while (peers.size() >= maxConnectedPeers)
                                    peers.remove(peers.size() - 1);

                            return peers.toArray(new InetSocketAddress[0]);
                        }

                        @Override
                        public void shutdown() {
                            normalPeerDiscovery.shutdown();
                        }
                    });
                }

                // notify that the peergroup was initialized
                if (blockchainManagerListeners != null) {
                    for (BlockchainManagerListener blockchainManagerListener : blockchainManagerListeners) {
                        blockchainManagerListener.peerGroupInitialized(peerGroup);
                    }
                }

                // init peergroup
                //peerGroup.addBlocksDownloadedEventListener(blockchainDownloadListener);
                peerGroup.startAsync();
                peerGroup.startBlockChainDownload(blockchainDownloadListener);

            } else if (!impediments.isEmpty() && peerGroup != null) {
                LOG.info("stopping peergroup");
                peerGroup.removeDisconnectedEventListener(peerDisconnectedEventListener);
                peerGroup.removeConnectedEventListener(peerConnectivityListener);
                walletManager.removeWalletFrom(peerGroup);
                peerGroup.stopAsync();
                peerGroup = null;

                for (BlockchainManagerListener blockchainManagerListener : blockchainManagerListeners) {
                    blockchainManagerListener.checkEnd();
                }

                notifyBlockchainStateOff(impediments);
            }
        }

        //todo: falta hacer el tema de la memoria, hoy en día si se queda sin memoria no dice nada..
        //todo: ver si conviene esto..
//        broadcastBlockchainState();

    }

    private void notifyBlockchainStateOff(Set<Impediment> impediments) {
        for (BlockchainManagerListener blockchainManagerListener : blockchainManagerListeners) {
            blockchainManagerListener.onBlockchainOff(impediments);
        }
    }


    /*public BlockchainState getBlockchainState(Set<Impediment> impediments)
    {
        final StoredBlock chainHead = blockChain.getChainHead();
        final Date bestChainDate = chainHead.getHeader().getTime();
        final int bestChainHeight = chainHead.getHeight();
        final boolean replaying = chainHead.getHeight() < conf.getBestChainHeightEver();

        return new BlockchainState(bestChainDate, bestChainHeight, replaying, impediments);
    }*/


    public List<Peer> getConnectedPeers() {
        if (peerGroup != null)
            return peerGroup.getConnectedPeers();
        else
            return null;
    }


    public List<StoredBlock> getRecentBlocks(final int maxBlocks) {
        final List<StoredBlock> blocks = new ArrayList<StoredBlock>(maxBlocks);
        try{
            StoredBlock block = blockChain.getChainHead();
            while (block != null) {
                blocks.add(block);
                if (blocks.size() >= maxBlocks)
                    break;
                block = block.getPrev(blockStore);
            }
        }
        catch (final BlockStoreException x) {
            // swallow
        }
        return blocks;
    }

    public int getChainHeadHeight() {
        return blockChain!=null? blockChain.getChainHead().getHeight():0;
    }


    public void removeBlockchainDownloadListener(PeerDataEventListener blockchainDownloadListener) {
        if (peerGroup!=null)
            peerGroup.removeBlocksDownloadedEventListener(blockchainDownloadListener);
    }

    public List<Peer> listConnectedPeers() {
        if (peerGroup!=null)
            return peerGroup.getConnectedPeers();
        return new ArrayList<>();
    }

    public int getProtocolVersion() {
        return conf.getProtocolVersion();
    }
}