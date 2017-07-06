package pivx.org.pivxwallet.ui.wallet_activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.PivxURI;
import org.bitcoinj.utils.MonetaryFormat;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.service.IntentsConstants;
import pivx.org.pivxwallet.ui.address_add_activity.AddContactActivity;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.qr_activity.QrActivity;
import pivx.org.pivxwallet.ui.splash_activity.SplashActivity;
import pivx.org.pivxwallet.ui.transaction_request_activity.RequestActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;
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
    private PivxRate pivxRate;

    private TransactionsFragmentBase txsFragment;
    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance();

    // Receiver
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter addressBalanceIntent = new IntentFilter(IntentsConstants.ACTION_ADDRESS_BALANCE_CHANGE);

    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

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
        if (!pivxApplication.getAppConf().isAppInit()){
            Intent intent = new Intent(this, SplashActivity.class);
            startActivity(intent);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle("My Wallet");
        root = getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        View containerHeader = getLayoutInflater().inflate(R.layout.fragment_pivx_amount,header_container);
        header_container.setVisibility(View.VISIBLE);
        txt_value = (TextView) containerHeader.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) containerHeader.findViewById(R.id.txt_unnavailable);
        container_txs = root.findViewById(R.id.container_txs);
        txt_local_currency = (TextView) containerHeader.findViewById(R.id.txt_local_currency);


        // Open Send


        fab_add = (FloatingActionButton) root.findViewById(R.id.fab_add);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), SendActivity.class));
            }
        });


        txsFragment = (TransactionsFragmentBase) getSupportFragmentManager().findFragmentById(R.id.transactions_fragment);

        // number format
        numberFormat.setMaximumFractionDigits(3);
        numberFormat.setMinimumFractionDigits(3);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(0);
        // register
        localBroadcastManager.registerReceiver(localReceiver,addressBalanceIntent);
        localBroadcastManager.registerReceiver(pivxServiceReceiver,pivxServiceFilter);

        updateBalance();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // unregister
        localBroadcastManager.unregisterReceiver(localReceiver);
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
                    PivxURI pivxUri = new PivxURI(address);
                    final DialogBuilder dialog = DialogBuilder.warn(this, R.string.scan_result_address_title);
                    dialog.setMessage(pivxUri.getAddress()+"\n\nCreate contact?");
                    final String tempPubKey = pivxUri.getAddress().toBase58();
                    DialogInterface.OnClickListener rightListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            Intent intent = new Intent(WalletActivity.this, AddContactActivity.class);
                            intent.putExtra(AddContactActivity.ADDRESS_TO_ADD,tempPubKey);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    };
                    DialogInterface.OnClickListener lefttListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            // nothing yet
                            dialog.dismiss();
                        }
                    };
                    dialog.twoButtons(lefttListener,rightListener);
                    dialog.create().show();
                }catch (Exception e){
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
                    numberFormat.format(
                            new BigDecimal(availableBalance.getValue() * pivxRate.getValue().doubleValue()).movePointLeft(8)
                    )
                    + " "+pivxRate.getCoin()
            );
        }else {
            txt_local_currency.setText("0 USD");
        }
    }
}
