package pivx.org.pivxwallet.ui.wallet_activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.bitcoinj.uri.BitcoinURI;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.qr_activity.QrActivity;
import pivx.org.pivxwallet.ui.start_activity.StartActivity;
import pivx.org.pivxwallet.ui.transaction_request_activity.RequestActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;
import pivx.org.pivxwallet.utils.scanner.ScanActivity;

import static android.Manifest.permission.CAMERA;
import static pivx.org.pivxwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/11/17.
 */

public class WalletActivity extends BaseDrawerActivity {

    private static final int SCANNER_RESULT = 122;

    RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private Button buttonSend;
    private Button buttonRequest;

    @Override
    protected void beforeCreate(){
        if (!pivxApplication.getAppConf().isAppInit()){
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_wallet, container);
        setTitle("My Wallet");
        // Recicler view
        List<TransactionData> data = fill_with_data();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        adapter = new RecyclerViewAdapter(data, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Open Send
        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SendActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Request
        buttonRequest = (Button) findViewById(R.id.btnRequest);
        buttonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), RequestActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(0);
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
                    //Toast.makeText(this,address,Toast.LENGTH_LONG).show();
                    BitcoinURI pivxUri = new BitcoinURI(address);
                    final DialogBuilder dialog = DialogBuilder.warn(this, R.string.scan_result_title);
                    dialog.setMessage("Hey!, new address found: \n"+pivxUri.getAddress());
                    final String tempPubKey = pivxUri.getAddress().toBase58();
                    DialogInterface.OnClickListener rightListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
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
}
