package pivx.org.pivxwallet.ui.wallet_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.PivxURI;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.service.IntentsConstants;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.qr_activity.QrActivity;
import pivx.org.pivxwallet.ui.settings_backup_activity.SettingsBackupActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;
import pivx.org.pivxwallet.utils.scanner.ScanActivity;

import static android.Manifest.permission.CAMERA;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_NOTIFICATION;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_ON_COIN_RECEIVED;
import static pivx.org.pivxwallet.service.IntentsConstants.INTENT_BROADCAST_DATA_TYPE;
import static pivx.org.pivxwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/11/17.
 */

public class WalletActivity extends BaseDrawerActivity {

    private static final int SCANNER_RESULT = 122;

    private View root;
    private View container_txs;
    private FloatingActionButton fab_add;

    private TextView txt_value;
    private TextView txt_unnavailable;
    private TextView txt_local_currency;
    private TextView txt_watch_only;
    private PivxRate pivxRate;
    private TransactionsFragmentBase txsFragment;

    // Receiver
    private LocalBroadcastManager localBroadcastManager;

    private IntentFilter pivxServiceFilter = new IntentFilter(ACTION_NOTIFICATION);
    private BroadcastReceiver pivxServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_NOTIFICATION)){
                if(intent.getStringExtra(INTENT_BROADCAST_DATA_TYPE).equals(INTENT_BROADCAST_DATA_ON_COIN_RECEIVED)){
                    updateBalance();
                    txsFragment.refresh();
                }
            }

        }
    };

    @Override
    protected void beforeCreate(){
        /*
        if (!appConf.isAppInit()){
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
            finish();
        }
        // show report dialog if something happen with the previous process
        */
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle(R.string.my_wallet);
        root = getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        View containerHeader = getLayoutInflater().inflate(R.layout.fragment_pivx_amount,header_container);
        header_container.setVisibility(View.VISIBLE);
        txt_value = (TextView) containerHeader.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) containerHeader.findViewById(R.id.txt_unnavailable);
        container_txs = root.findViewById(R.id.container_txs);
        txt_local_currency = (TextView) containerHeader.findViewById(R.id.txt_local_currency);
        txt_watch_only = (TextView) containerHeader.findViewById(R.id.txt_watch_only);

        // Open Send
        fab_add = (FloatingActionButton) root.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pivxModule.isWalletWatchOnly()){
                    Toast.makeText(v.getContext(),R.string.error_watch_only_mode,Toast.LENGTH_SHORT).show();
                    return;
                }
                startActivity(new Intent(v.getContext(), SendActivity.class));
            }
        });

        txsFragment = (TransactionsFragmentBase) getSupportFragmentManager().findFragmentById(R.id.transactions_fragment);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(0);

        init();

        // register
        //localBroadcastManager.registerReceiver(localReceiver,addressBalanceIntent);
        localBroadcastManager.registerReceiver(pivxServiceReceiver,pivxServiceFilter);

        updateState();
        updateBalance();
    }

    private void updateState() {
        txt_watch_only.setVisibility(pivxModule.isWalletWatchOnly()?View.VISIBLE:View.GONE);
    }

    private void init() {
        // Start service if it's not started.
        pivxApplication.startPivxService();

        if (!pivxApplication.getAppConf().hasBackup()){
            long now = System.currentTimeMillis();
            if (pivxApplication.getLastTimeRequestedBackup()+1800000L<now) {
                pivxApplication.setLastTimeBackupRequested(now);
                SimpleTwoButtonsDialog reminderDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                        this,
                        getString(R.string.reminder_backup),
                        getString(R.string.reminder_backup_body),
                        new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                            @Override
                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                startActivity(new Intent(WalletActivity.this, SettingsBackupActivity.class));
                                dialog.dismiss();
                            }

                            @Override
                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                dialog.dismiss();
                            }
                        }
                );
                reminderDialog.setLeftBtnText(getString(R.string.button_dismiss));
                reminderDialog.setLeftBtnTextColor(Color.BLACK);
                reminderDialog.setRightBtnText(getString(R.string.button_ok));
                reminderDialog.show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unregister
        //localBroadcastManager.unregisterReceiver(localReceiver);
        localBroadcastManager.unregisterReceiver(pivxServiceReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.action_qr){
            startActivity(new Intent(this, QrActivity.class));
            return true;
        }else if (item.getItemId()==R.id.action_scan){
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            startActivityForResult(new Intent(this, ScanActivity.class),SCANNER_RESULT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Create a list of Data objects
    public List<TransactionData> fill_with_data() {

        List<TransactionData> data = new ArrayList<>();

        data.add(new TransactionData("Sent Pivx", "18:23", R.mipmap.ic_transaction_receive,"56.32", "701 USD" ));
        data.add(new TransactionData("Sent Pivx", "1 days ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "2 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "2 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "3 days ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "3 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));

        data.add(new TransactionData("Sent Pivx", "4 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "4 days ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "one week ago", R.mipmap.ic_transaction_send,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD"));
        data.add(new TransactionData("Sent Pivx", "one week ago", R.mipmap.ic_transaction_receive,"56.32", "701 USD" ));

        return data;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT){
            if (resultCode==RESULT_OK) {
                try {
                    String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    String usedAddress;
                    if (pivxModule.chechAddress(address)){
                        usedAddress = address;
                    }else {
                        PivxURI pivxUri = new PivxURI(address);
                        usedAddress = pivxUri.getAddress().toBase58();
                    }
                    DialogsUtil.showCreateAddressLabelDialog(this,usedAddress);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"Bad address",Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    private boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),permission);

        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void updateBalance() {
        Coin availableBalance = pivxModule.getAvailableBalanceCoin();
        txt_value.setText(!availableBalance.isZero()?availableBalance.toFriendlyString():"0 Pivs");
        Coin unnavailableBalance = pivxModule.getUnnavailableBalanceCoin();
        txt_unnavailable.setText(!unnavailableBalance.isZero()?unnavailableBalance.toFriendlyString():"0 Pivs");
        if (pivxRate == null)
            pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());
        if (pivxRate!=null) {
            txt_local_currency.setText(
                    pivxApplication.getCentralFormats().format(
                            new BigDecimal(availableBalance.getValue() * pivxRate.getValue().doubleValue()).movePointLeft(8)
                    )
                    + " "+pivxRate.getCoin()
            );
        }else {
            txt_local_currency.setText("0 USD");
        }
    }
}
