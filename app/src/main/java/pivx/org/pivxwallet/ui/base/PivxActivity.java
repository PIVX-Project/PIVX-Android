package pivx.org.pivxwallet.ui.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.service.IntentsConstants;
import pivx.org.pivxwallet.utils.DialogBuilder;

import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_NOTIFICATION;

/**
 * Created by furszy on 6/8/17.
 */

public class PivxActivity extends AppCompatActivity {

    protected PivxApplication pivxApplication;
    protected PivxModule pivxModule;

    protected LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter = new IntentFilter(IntentsConstants.ACTION_TRUSTED_PEER_CONNECTION_FAIL);
    private BroadcastReceiver trustedPeerConnectionDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DialogBuilder dialogBuilder = DialogBuilder.warn(PivxActivity.this, R.string.title_no_trusted_peer_connection);
            dialogBuilder.setMessage(R.string.message_no_trusted_peer_connection);
            dialogBuilder.show();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pivxApplication = PivxApplication.getInstance();
        pivxModule = pivxApplication.getModule();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        localBroadcastManager.registerReceiver(trustedPeerConnectionDownReceiver,intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        localBroadcastManager.unregisterReceiver(trustedPeerConnectionDownReceiver);
    }
}
