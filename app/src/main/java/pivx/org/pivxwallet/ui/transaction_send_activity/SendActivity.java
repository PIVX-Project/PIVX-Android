package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.uri.PivxURI;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.service.PivxWalletService;
import pivx.org.pivxwallet.ui.address_add_activity.AddContactActivity;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.settings_activity.SettingsActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;
import pivx.org.pivxwallet.utils.scanner.ScanActivity;

import static android.Manifest.permission_group.CAMERA;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION;
import static pivx.org.pivxwallet.service.IntentsConstants.DATA_TRANSACTION_HASH;
import static pivx.org.pivxwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/4/17.
 */

public class SendActivity extends BaseActivity implements View.OnClickListener {
    private static final int SCANNER_RESULT = 122;
    final Context context = this;
    private Button buttonSend;
    private AutoCompleteTextView edit_address;
    private EditText edit_amount;
    private EditText edit_memo;
    private MyFilterableAdapter filterableAdapter;
    private String addressStr;
    @Override
    protected void onCreateView(Bundle savedInstanceState,ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_transaction_send, container);
        setTitle("Send");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        buttonSend = (Button) findViewById(R.id.btnSend);
        edit_address = (AutoCompleteTextView) findViewById(R.id.edit_address);
        edit_amount = (EditText) findViewById(R.id.edit_amount);
        edit_memo = (EditText) findViewById(R.id.edit_memo);
        findViewById(R.id.button_qr).setOnClickListener(this);
        buttonSend.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // todo: This is not updating the filter..
        if (filterableAdapter==null) {
            List<Contact> list = new ArrayList<>(pivxModule.getContacts());
            filterableAdapter = new MyFilterableAdapter(this,list );
            edit_address.setAdapter(filterableAdapter);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSend){
            try {
                send();
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            }
        }else if (id == R.id.button_qr){
            if (!checkPermission(CAMERA)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permsRequestCode = 200;
                    String[] perms = {"android.permission.CAMERA"};
                    requestPermissions(perms, permsRequestCode);
                }
            }
            startActivityForResult(new Intent(this, ScanActivity.class),SCANNER_RESULT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT){
            if (resultCode==RESULT_OK) {
                try {
                    String address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    PivxURI pivxUri = new PivxURI(address);
                    final String tempPubKey = pivxUri.getAddress().toBase58();
                    edit_address.setText(tempPubKey);
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

    private void showErrorDialog(String message) {
        DialogBuilder dialogBuilder = new DialogBuilder(this);
        dialogBuilder.setMessage(message);
        dialogBuilder.setTitle(R.string.error_sending_coins);
        dialogBuilder.show();
    }

    private void send() {
        try {
            // create the tx
            addressStr = edit_address.getText().toString();
            if (!pivxModule.chechAddress(addressStr))
                throw new IllegalArgumentException("Address not valid");
            String amountStr = edit_amount.getText().toString();
            if (amountStr.length() < 1) throw new IllegalArgumentException("Amount not valid");
            Coin amount = Coin.parseCoin(amountStr);
            if (amount.isGreaterThan(Coin.valueOf(pivxModule.getAvailableBalance())))
                throw new IllegalArgumentException("Insuficient balance");
            String memo = edit_memo.getText().toString();
            // build a tx with the default fee
            Transaction transaction = pivxModule.buildSendTx(addressStr, amount, memo);
            // dialog
            launchSendDialog(transaction);
            LayoutInflater content = LayoutInflater.from(SendActivity.this);
            View dialogView = content.inflate(R.layout.dialog_send_confirmation, null);
            DialogBuilder sendDialog = new DialogBuilder(SendActivity.this);
            sendDialog.setTitle("Transaction Information");
            sendDialog.setView(dialogView);
            sendDialog.setPositiveButton("OK", null);
            sendDialog.show();
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Insuficient balance");
        }
    }

    private void launchSendDialog(final Transaction transaction){
        // create a Dialog component
        final Dialog dialog = new Dialog(context);

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.transaction_dialog);
        final EditText edit_contact_name = (EditText) dialog.findViewById(R.id.edit_contact_name);
        dialog.setTitle("Send");
        TextView valuePivx = (TextView) dialog.findViewById(R.id.valuePivx);
        valuePivx.setText(pivxModule.getValueSentFromMe(transaction,true).toFriendlyString());
        Button dialogButton = (Button) dialog.findViewById(R.id.btnConfirm);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactName = edit_contact_name.getText().toString();
                if (contactName.length()>0){
                    Contact contact = new Contact(contactName);
                    contact.addAddress(addressStr);
                    contact.addTx(transaction.getHash());
                    pivxModule.saveContact(contact);
                }
                pivxModule.commitTx(transaction);
                Intent intent = new Intent(v.getContext(), PivxWalletService.class);
                intent.setAction(ACTION_BROADCAST_TRANSACTION);
                intent.putExtra(DATA_TRANSACTION_HASH,transaction.getHash().getBytes());
                startService(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }
}
