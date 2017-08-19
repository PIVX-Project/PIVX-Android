package pivx.org.pivxwallet.service;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.listeners.AbstractPeerDataEventListener;
import org.bitcoinj.core.listeners.PeerConnectedEventListener;
import org.bitcoinj.core.listeners.PeerDataEventListener;
import org.bitcoinj.core.listeners.PeerDisconnectedEventListener;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.utils.BtcFormat;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import chain.BlockchainManager;
import chain.BlockchainState;
import chain.Impediment;
import pivtrum.PivtrumPeergroup;
import pivtrum.listeners.AddressListener;
import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.module.PivxModuleImp;
import pivx.org.pivxwallet.rate.CoinMarketCapApiClient;
import pivx.org.pivxwallet.rate.CoinTypes;
import pivx.org.pivxwallet.rate.RequestPivxRateException;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;
import pivx.org.pivxwallet.utils.AppConf;

import static pivx.org.pivxwallet.module.PivxContext.CONTEXT;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_ADDRESS_BALANCE_CHANGE;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_CANCEL_COINS_RECEIVED;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_NOTIFICATION;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_RESET_BLOCKCHAIN;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_SCHEDULE_SERVICE;
import static pivx.org.pivxwallet.service.IntentsConstants.DATA_TRANSACTION_HASH;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_PEER_CONNECTED;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_EXTRA_BLOCKCHAIN_STATE;
import static pivx.org.pivxwallet.service.IntentsConstants.NOT_BLOCKCHAIN_ALERT;
import static pivx.org.pivxwallet.service.IntentsConstants.NOT_COINS_RECEIVED;

/**
 * Created by furszy on 6/12/17.
 */

public class PivxWalletService extends Service{

    private Logger log = LoggerFactory.getLogger(PivxWalletService.class);

    private PivxApplication pivxApplication;
    private PivxModuleImp module;
    //private PivtrumPeergroup pivtrumPeergroup;
    private BlockchainManager blockchainManager;

    private PeerConnectivityListener peerConnectivityListener;

    private PowerManager.WakeLock wakeLock;
    private NotificationManager nm;
    private LocalBroadcastManager broadcastManager;

    private boolean resetBlockchainOnShutdown = false;
    /** Created service time (just for checks) */
    private long serviceCreatedAt;
    /** Cached amount to notify balance */
    private Coin notificationAccumulatedAmount = Coin.ZERO;
    /**  */
    private final Set<Impediment> impediments = EnumSet.noneOf(Impediment.class);

    private BlockchainState blockchainState = BlockchainState.NOT_CONNECTION;

    private volatile long lastUpdateTime = System.currentTimeMillis();
    private volatile long lastMessageTime = System.currentTimeMillis();

    public class PivxBinder extends Binder {
        public PivxWalletService getService() {
            return PivxWalletService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PivxBinder();
    }

    private AddressListener addressListener = new AddressListener() {
        @Override
        public void onBalanceChange(String address, long confirmed, long unconfirmed,int numConfirmations) {
            Intent intent = new Intent(ACTION_ADDRESS_BALANCE_CHANGE);
            broadcastManager.sendBroadcast(intent);
        }
    };

    private final class PeerConnectivityListener implements PeerConnectedEventListener, PeerDisconnectedEventListener{

        @Override
        public void onPeerConnected(Peer peer, int i) {
            //todo: notify peer connected
            log.info("Peer connected: "+peer.getAddress());
            broadcastPeerConnected();
        }

        @Override
        public void onPeerDisconnected(Peer peer, int i) {
            //todo: notify peer disconnected
            log.info("Peer disconnected: "+peer.getAddress());
        }
    }

    private final PeerDataEventListener blockchainDownloadListener = new AbstractPeerDataEventListener() {

        @Override
        public void onBlocksDownloaded(final Peer peer, final Block block, final FilteredBlock filteredBlock, final int blocksLeft) {
            //log.info("Peer: " + peer + ", Block: " + block + ", left: " + blocksLeft);
            /*log.info("############# on Blockcs downloaded ###########");
            log.info("Peer: " + peer + ", Block: " + block + ", left: " + blocksLeft);*/


            /*if (PivxContext.IS_TEST)
                showBlockchainSyncNotification(blocksLeft);*/

            //delayHandler.removeCallbacksAndMessages(null);


            final long now = System.currentTimeMillis();
            if (now-lastMessageTime > TimeUnit.SECONDS.toMillis(15)) {
                if (blocksLeft<6){
                    blockchainState = BlockchainState.SYNC;
                }else {
                    blockchainState = BlockchainState.SYNCING;
                }
                pivxApplication.getAppConf().setLastBestChainBlockTime(block.getTime().getTime());
                broadcastBlockchainState(true);
            }
        }
    };

    private class RunnableBlockChecker implements Runnable{

        private Block block;

        public RunnableBlockChecker(Block block) {
            this.block = block;
        }

        @Override
        public void run() {
            org.bitcoinj.core.Context.propagate(PivxContext.CONTEXT);
            lastMessageTime = System.currentTimeMillis();
            broadcastBlockchainState(false);
        }
    }

    private final BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                final NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                final boolean hasConnectivity = networkInfo.isConnected();
                log.info("network is {}, state {}/{}", hasConnectivity ? "up" : "down", networkInfo.getState(), networkInfo.getDetailedState());
                if (hasConnectivity)
                    impediments.remove(Impediment.NETWORK);
                else
                    impediments.add(Impediment.NETWORK);
                check();
                // try to request coin rate
                requestRateCoin();
            } else if (Intent.ACTION_DEVICE_STORAGE_LOW.equals(action)) {
                log.info("device storage low");

                impediments.add(Impediment.STORAGE);
                check();
            } else if (Intent.ACTION_DEVICE_STORAGE_OK.equals(action)) {
                log.info("device storage ok");
                impediments.remove(Impediment.STORAGE);
                check();
            }
        }
    };

    private WalletCoinsReceivedEventListener coinReceiverListener = new WalletCoinsReceivedEventListener() {

        android.support.v4.app.NotificationCompat.Builder mBuilder;
        PendingIntent deleteIntent;
        PendingIntent openPendingIntent;

        @Override
        public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
            //todo: acá falta una validación para saber si la transaccion es mia.
            org.bitcoinj.core.Context.propagate(CONTEXT);

            int depthInBlocks = transaction.getConfidence().getDepthInBlocks();

            long now = System.currentTimeMillis();
            if (lastUpdateTime+3000<now) {
                lastUpdateTime=now;
                Intent intent = new Intent(ACTION_NOTIFICATION);
                intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_ON_COIN_RECEIVED);
                broadcastManager.sendBroadcast(intent);
            }

            //final Address address = WalletUtils.getWalletAddressOfReceived(WalletConstants.NETWORK_PARAMETERS,transaction, wallet);
            final Coin amount = transaction.getValue(wallet);
            final TransactionConfidence.ConfidenceType confidenceType = transaction.getConfidence().getConfidenceType();

            if (amount.isGreaterThan(Coin.ZERO)) {
                //notificationCount++;
                notificationAccumulatedAmount = notificationAccumulatedAmount.add(amount);
                Intent openIntent = new Intent(getApplicationContext(), WalletActivity.class);
                openPendingIntent = PendingIntent.getActivity(getApplicationContext(),0,openIntent,0);
                Intent resultIntent = new Intent(getApplicationContext(), PivxWalletService.this.getClass());
                resultIntent.setAction(ACTION_CANCEL_COINS_RECEIVED);
                deleteIntent = PendingIntent.getService(PivxWalletService.this, 0, resultIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle("Pivs received!")
                                .setContentText("Coins received for a value of " + notificationAccumulatedAmount.toFriendlyString())
                                .setAutoCancel(true)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setColor(
                                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                                                getResources().getColor(R.color.bgPurple,null)
                                                :
                                                ContextCompat.getColor(PivxWalletService.this,R.color.bgPurple))
                                .setDeleteIntent(deleteIntent)
                                .setContentIntent(openPendingIntent);
                nm.notify(NOT_COINS_RECEIVED, mBuilder.build());
            }else {
                log.error("transaction with a value lesser than zero arrives..");
            }

        }
    };

    private TransactionConfidenceEventListener transactionConfidenceEventListener = new TransactionConfidenceEventListener() {
        @Override
        public void onTransactionConfidenceChanged(Wallet wallet, Transaction transaction) {
            org.bitcoinj.core.Context.propagate(CONTEXT);
            if (transaction != null) {
                if (transaction.getConfidence().getDepthInBlocks() > 1) {
                    long now = System.currentTimeMillis();
                    if (lastUpdateTime+3000<now) {
                        lastUpdateTime=now;
                        // update balance state
                        Intent intent = new Intent(ACTION_NOTIFICATION);
                        intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_ON_COIN_RECEIVED);
                        broadcastManager.sendBroadcast(intent);
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        serviceCreatedAt = System.currentTimeMillis();
        super.onCreate();
        try {
            log.info("Pivx service started");
            // Android stuff
            final String lockName = getPackageName() + " blockchain sync";
            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, lockName);
            nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            broadcastManager = LocalBroadcastManager.getInstance(this);
            // Pivx
            pivxApplication = PivxApplication.getInstance();
            module = (PivxModuleImp) pivxApplication.getModule();
            blockchainManager = module.getBlockchainManager();
            // connect to pivtrum node
            /*pivtrumPeergroup = new PivtrumPeergroup(pivxApplication.getNetworkConf());
            pivtrumPeergroup.addAddressListener(addressListener);
            module.setPivtrumPeergroup(pivtrumPeergroup);*/

            // Schedule service
            tryScheduleService();

            peerConnectivityListener = new PeerConnectivityListener();

            blockchainManager.init();

            module.addCoinsReceivedEventListener(coinReceiverListener);
            module.addOnTransactionConfidenceChange(transactionConfidenceEventListener);

            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_LOW);
            intentFilter.addAction(Intent.ACTION_DEVICE_STORAGE_OK);
            registerReceiver(connectivityReceiver, intentFilter); // implicitly init PeerGroup

            // initilizing trusted node.
            //pivtrumPeergroup.start();


        } catch (Exception e){
            // todo: I have to handle the connection refused..
            e.printStackTrace();
            // for now i just launch a notification
            Intent intent = new Intent(IntentsConstants.ACTION_TRUSTED_PEER_CONNECTION_FAIL);
            broadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("Pivx service onStartCommand");
        if (intent != null) {
            try {
                log.info("service init command: " + intent
                        + (intent.hasExtra(Intent.EXTRA_ALARM_COUNT) ? " (alarm count: " + intent.getIntExtra(Intent.EXTRA_ALARM_COUNT, 0) + ")" : ""));
            }catch (Exception e){
                e.printStackTrace();
                log.info("service init command: " + intent
                        + (intent.hasExtra(Intent.EXTRA_ALARM_COUNT) ? " (alarm count: " + intent.getLongArrayExtra(Intent.EXTRA_ALARM_COUNT) + ")" : ""));
            }
            final String action = intent.getAction();
            if (ACTION_SCHEDULE_SERVICE.equals(action)){
                check();
            }else if (ACTION_CANCEL_COINS_RECEIVED.equals(action)) {
                notificationAccumulatedAmount = Coin.ZERO;
                nm.cancel(NOT_COINS_RECEIVED);
            }else if (ACTION_RESET_BLOCKCHAIN.equals(action)) {
                log.info("will remove blockchain on service shutdown");
                resetBlockchainOnShutdown = true;
                stopSelf();
            }else if (ACTION_BROADCAST_TRANSACTION.equals(action)) {
                blockchainManager.broadcastTransaction(intent.getByteArrayExtra(DATA_TRANSACTION_HASH));
            }
        }else {
            log.warn("service restart, although it was started as non-sticky");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.info(".onDestroy()");
        try {
            // todo: notify module about this shutdown...
            unregisterReceiver(connectivityReceiver);

            // remove listeners
            module.removeCoinsReceivedEventListener(coinReceiverListener);
            module.removeTransactionsConfidenceChange(transactionConfidenceEventListener);
            blockchainManager.removeBlockchainDownloadListener(blockchainDownloadListener);
            // destroy the blockchain
            blockchainManager.destroy(resetBlockchainOnShutdown);

            /*if (pivtrumPeergroup.isRunning()) {
                pivtrumPeergroup.shutdown();
            }*/

            if (wakeLock.isHeld()) {
                log.debug("wakelock still held, releasing");
                wakeLock.release();
            }

            log.info("service was up for " + ((System.currentTimeMillis() - serviceCreatedAt) / 1000 / 60) + " minutes");
            // schedule service it is not scheduled yet
            tryScheduleService();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Schedule service for later
     */
    private void tryScheduleService() {
        boolean isSchedule = System.currentTimeMillis()<module.getConf().getScheduledBLockchainService();

        if (!isSchedule){
            log.info("scheduling service");
            AlarmManager alarm = (AlarmManager)getSystemService(ALARM_SERVICE);
            long scheduleTime = System.currentTimeMillis() + 2000*60;//(1000 * 60 * 60); // One hour from now

            Intent intent = new Intent(this, PivxWalletService.class);
            intent.setAction(ACTION_SCHEDULE_SERVICE);
            alarm.set(
                    // This alarm will wake up the device when System.currentTimeMillis()
                    // equals the second argument value
                    alarm.RTC_WAKEUP,
                    scheduleTime,
                    // PendingIntent.getService creates an Intent that will start a service
                    // when it is called. The first argument is the Context that will be used
                    // when delivering this intent. Using this has worked for me. The second
                    // argument is a request code. You can use this code to cancel the
                    // pending intent if you need to. Third is the intent you want to
                    // trigger. In this case I want to create an intent that will start my
                    // service. Lastly you can optionally pass flags.
                    PendingIntent.getService(this, 0,intent , 0)
            );
            // save
            module.getConf().saveScheduleBlockchainService(scheduleTime);
        }
    }

    private void requestRateCoin(){
        final AppConf appConf = pivxApplication.getAppConf();
        PivxRate pivxRate = module.getRate(appConf.getSelectedRateCoin());
        if (pivxRate==null || pivxRate.getTimestamp()+PivxContext.RATE_UPDATE_TIME<System.currentTimeMillis()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        CoinMarketCapApiClient c = new CoinMarketCapApiClient();
                        BigDecimal usdValue = c.getPivxPrice();
                        PivxRate pivxRate = new PivxRate(CoinTypes.USD.name(),usdValue,System.currentTimeMillis(),"coinmarketcap");
                        module.saveRate(pivxRate);
                    } catch (RequestPivxRateException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    private AtomicBoolean isChecking = new AtomicBoolean(false);

    /**
     * Check and download the blockchain if it needed
     */
    private void check() {
        log.info("check");
        try {
            if (!isChecking.getAndSet(true)) {
                blockchainManager.check(
                        impediments,
                        peerConnectivityListener,
                        peerConnectivityListener,
                        blockchainDownloadListener);
                //todo: ver si conviene esto..
                broadcastBlockchainState(true);
                isChecking.set(false);
            }
        }catch (Exception e){
            e.printStackTrace();
            isChecking.set(false);
            broadcastBlockchainState(false);
        }
    }

    private void broadcastBlockchainState(boolean isCheckOk) {
        boolean showNotif = false;
        if (!impediments.isEmpty()) {

            StringBuilder stringBuilder = new StringBuilder();
            for (Impediment impediment : impediments) {
                if (stringBuilder.length()!=0){
                    stringBuilder.append("\n");
                }
                if (impediment == Impediment.NETWORK){
                    blockchainState = BlockchainState.NOT_CONNECTION;
                    stringBuilder.append("No peer connection");
                }else if(impediment == Impediment.STORAGE){
                    stringBuilder.append("No available storage");
                    showNotif = true;
                }
            }

            if(showNotif) {
                android.support.v4.app.NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(getApplicationContext())
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("Alert")
                                .setContentText(stringBuilder.toString())
                                .setAutoCancel(true)
                                .setColor(
                                        (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
                                                getResources().getColor(R.color.bgPurple,null)
                                                :
                                                ContextCompat.getColor(PivxWalletService.this,R.color.bgPurple))
                        ;

                nm.notify(NOT_BLOCKCHAIN_ALERT, mBuilder.build());
            }
        }

        if (isCheckOk){
            broadcastBlockchainStateIntent();
        }
    }

    private void broadcastBlockchainStateIntent(){
        final long now = System.currentTimeMillis();
        if (now-lastMessageTime> TimeUnit.SECONDS.toMillis(10)) {
            Intent intent = new Intent(ACTION_NOTIFICATION);
            intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE);
            intent.putExtra(INTENT_EXTRA_BLOCKCHAIN_STATE,blockchainState);
            broadcastManager.sendBroadcast(intent);
        }
    }

    private void broadcastPeerConnected() {
        Intent intent = new Intent(ACTION_NOTIFICATION);
        intent.putExtra(INTENT_BROADCAST_DATA_TYPE, INTENT_BROADCAST_DATA_PEER_CONNECTED);
        broadcastManager.sendBroadcast(intent);
    }

}
