package pivx.org.pivxwallet.ui.upgrade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import pivx.org.pivxwallet.R;
import global.exceptions.UpgradeException;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.utils.DialogsUtil;

/**
 * Created by furszy on 10/8/17.
 */

public class UpgradeWalletActivity extends BaseActivity {

    public static final String INTENT_EXTRA_TITLE = "intent_title";
    public static final String INTENT_EXTRA_MESSAGE = "intent_message";
    public static final String INTENT_EXTRA_UPGRADE_CODE = "intent_upgrade_code";

    private View root;
    private TextView txt_message,txt_title;
    private ProgressBar progress;

    private SimpleTextDialog noConnectionDialog;

    private AtomicBoolean flag = new AtomicBoolean(false);

    public static Intent createStartIntent(Context context, String title, String message, String upgradeCode){
        Intent intent = new Intent(context,UpgradeWalletActivity.class);
        intent.putExtra(INTENT_EXTRA_TITLE,title);
        intent.putExtra(INTENT_EXTRA_MESSAGE,message);
        intent.putExtra(INTENT_EXTRA_UPGRADE_CODE,upgradeCode);
        return intent;
    }

    @Override
    public boolean isFullScreen() {
        return true;
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        super.onCreateView(savedInstanceState, container);

        Intent intent = getIntent();
        String title = intent.getStringExtra(INTENT_EXTRA_TITLE);
        String message = intent.getStringExtra(INTENT_EXTRA_MESSAGE);
        final String upgradeCode = intent.getStringExtra(INTENT_EXTRA_UPGRADE_CODE);

        root = getLayoutInflater().inflate(R.layout.upgrade_wallet_main,container);

        txt_message = (TextView) root.findViewById(R.id.txt_message);
        txt_title = (TextView) root.findViewById(R.id.txt_title);
        progress = (ProgressBar) root.findViewById(R.id.progress);
        txt_message.setText(message);
        txt_title.setText(title);
        root.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pivxModule.isAnyPeerConnected()){
                    if (noConnectionDialog==null)
                        noConnectionDialog = DialogsUtil.buildSimpleTextDialog(
                                UpgradeWalletActivity.this,
                                getString(R.string.not_connection),
                                getString(R.string.message_connection_is_needed)
                        );
                    noConnectionDialog.show(getFragmentManager(),"no_connection");
                    return;
                }
                if(!flag.getAndSet(true)) {
                    progress.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // upgrade wallet
                            Log.i("UpgradeWallet", "Upgrading wallet..");
                            boolean succed = false;
                            String message = null;
                            try {
                                succed = pivxModule.upgradeWallet(upgradeCode);
                                Log.i("UpgradeWallet", "wallet upgrade result: " + succed);
                            } catch (UpgradeException e) {
                                e.printStackTrace();
                                message = e.getMessage();
                            }
                            final boolean finalSucced = succed;
                            final String finalMessage = message;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (finalSucced) {
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                pivxApplication.startPivxService();
                                            }
                                        }, TimeUnit.SECONDS.toMillis(3));
                                        Toast.makeText(UpgradeWalletActivity.this,
                                                R.string.upgrade_succesfull,
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(UpgradeWalletActivity.this, finalMessage, Toast.LENGTH_LONG).show();
                                    }
                                    progress.setVisibility(View.INVISIBLE);
                                    finish();
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }
}
