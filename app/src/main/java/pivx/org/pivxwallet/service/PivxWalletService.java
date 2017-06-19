package pivx.org.pivxwallet.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.snappydb.SnappydbException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pivtrum.PivtrumPeergroup;
import pivtrum.listeners.AddressListener;
import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.module.PivxModuleImp;
import pivx.org.pivxwallet.module.store.SnappyStore;
import store.AddressStore;

import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_ADDRESS_BALANCE_CHANGE;

/**
 * Created by furszy on 6/12/17.
 */

public class PivxWalletService extends Service{

    private Logger log = LoggerFactory.getLogger(PivxWalletService.class);

    private PivxApplication pivxApplication;
    private PivxModuleImp module;
    private PivtrumPeergroup peergroup;
    private LocalBroadcastManager broadcastManager;


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
        public void onCoinReceived(String address, long confirmed, long unconfirmed) {
            Intent intent = new Intent(ACTION_ADDRESS_BALANCE_CHANGE);
            broadcastManager.sendBroadcast(intent);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            log.info("Pivx service started");
            pivxApplication = PivxApplication.getInstance();
            module = (PivxModuleImp) pivxApplication.getModule();
            broadcastManager = LocalBroadcastManager.getInstance(this);
            // connect to pivtrum servers
            peergroup = new PivtrumPeergroup(pivxApplication.getNetworkConf());
            peergroup.addAddressListener(addressListener);
            module.setPeergroup(peergroup);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        peergroup.start();
                    }catch (Exception e){
                        // todo: I have to handle the connection refused..
                        e.printStackTrace();
                        // for now i just launch a notification
                        Intent intent = new Intent(IntentsConstants.ACTION_TRUSTED_PEER_CONNECTION_FAIL);
                        broadcastManager.sendBroadcast(intent);
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log.info("Pivx service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // todo: notify module about this shutdown...
        if (peergroup.isRunning()){
            peergroup.shutdown();
        }
    }
}
