package pivx.org.pivxwallet.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.module.PivxModule;

/**
 * Created by furszy on 6/12/17.
 */

public class PivxWalletService extends Service{

    private PivxModule module;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        module = PivxApplication.getInstance().getModule();

        // connect to pivtrum servers




    }
}
