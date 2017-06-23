package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;

/**
 * Created by Neoperol on 5/4/17.
 */

public class SendActivity extends BaseActivity implements View.OnClickListener {
    final Context context = this;
    private Button buttonSend;
    private AutoCompleteTextView edit_address;
    private EditText edit_amount;
    private EditText edit_memo;
    private MyFilterableAdapter filterableAdapter;
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
        }


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
            String addressStr = edit_address.getText().toString();
            if (!pivxModule.chechAddress(addressStr))
                throw new IllegalArgumentException("Address not valid");
            String amountStr = edit_amount.getText().toString();
            if (amountStr.length() < 1) throw new IllegalArgumentException("Amount not valid");
            Coin amount = Coin.parseCoin(amountStr);
            if (amount.isLessThan(Coin.valueOf(pivxModule.getAvailableBalance())))
                throw new IllegalArgumentException("Insuficient balance");
            String memo = edit_memo.getText().toString();
            // build a tx with the default fee
            Transaction transaction = pivxModule.buildSendTx(addressStr, amount, memo);
            // dialog
            launchSendDialog(transaction);
        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Insuficient balance");
        }
    }

    private void launchSendDialog(Transaction transaction){
        // create a Dialog component
        final Dialog dialog = new Dialog(context);

        //tell the Dialog to use the dialog.xml as it's layout description
        dialog.setContentView(R.layout.transaction_dialog);
        dialog.setTitle("Send");
        TextView valuePivx = (TextView) dialog.findViewById(R.id.valuePivx);
        valuePivx.setText(transaction.getOutput(0).getValue().toFriendlyString());
        Button dialogButton = (Button) dialog.findViewById(R.id.btnConfirm);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
