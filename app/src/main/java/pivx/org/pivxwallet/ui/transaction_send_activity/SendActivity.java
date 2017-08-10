package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.uri.PivxURI;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.module.ContactAlreadyExistException;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.service.PivxWalletService;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputWrapper;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputsActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputWrapper;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputsActivity;
import pivx.org.pivxwallet.ui.wallet_activity.TransactionWrapper;
import pivx.org.pivxwallet.utils.DialogsUtil;
import pivx.org.pivxwallet.utils.scanner.ScanActivity;
import wallet.InsufficientInputsException;

import static android.Manifest.permission_group.CAMERA;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION;
import static pivx.org.pivxwallet.service.IntentsConstants.DATA_TRANSACTION_HASH;
import static pivx.org.pivxwallet.ui.transaction_detail_activity.FragmentTxDetail.TX;
import static pivx.org.pivxwallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_CLEAR;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_FEE_PER_KB;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_MINIMUM_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_TOTAL_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputsFragment.INTENT_EXTRA_UNSPENT_WRAPPERS;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputsActivity.INTENT_EXTRA_OUTPUTS_CLEAR;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputsActivity.INTENT_EXTRA_OUTPUTS_WRAPPERS;
import static pivx.org.pivxwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by Neoperol on 5/4/17.
 */

public class SendActivity extends BaseActivity implements View.OnClickListener {

    public static final String INTENT_EXTRA_TOTAL_AMOUNT = "total_amount";

    private static final int PIN_RESULT = 121;
    private static final int SCANNER_RESULT = 122;
    private static final int CUSTOM_FEE_RESULT = 123;
    private static final int MULTIPLE_ADDRESSES_SEND_RESULT = 124;
    private static final int CUSTOM_INPUTS = 125;
    private static final int SEND_DETAIL = 126;

    private View root;
    private Button buttonSend, addAllPiv;
    private AutoCompleteTextView edit_address;
    private TextView txt_local_currency , txt_coin_selection, txt_custom_fee, txtShowPiv;
    private TextView txt_multiple_outputs, txt_currency_amount;
    private View container_address;
    private EditText edit_amount, editCurrency;
    private EditText edit_memo;
    private MyFilterableAdapter filterableAdapter;
    private String addressStr;
    private PivxRate pivxRate;
    private SimpleTextDialog errorDialog;
    private ImageButton btnSwap;
    private ViewFlipper amountSwap;

    private boolean inPivs = true;
    private Transaction transaction;
    private String contactName;
    /** Several outputs */
    private List<OutputWrapper> outputWrappers;
    /** Custom inputs */
    private List<InputWrapper> unspent;
    /** Custom fee selector */
    private CustomFeeFragment.FeeSelector customFee;


    @Override
    protected void onCreateView(Bundle savedInstanceState,ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_transaction_send, container);
        setTitle("Send");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        edit_address = (AutoCompleteTextView) findViewById(R.id.edit_address);
        edit_amount = (EditText) findViewById(R.id.edit_amount);
        edit_memo = (EditText) findViewById(R.id.edit_memo);
        container_address = root.findViewById(R.id.container_address);
        txt_local_currency = (TextView) findViewById(R.id.txt_local_currency);
        txt_multiple_outputs = (TextView) root.findViewById(R.id.txt_multiple_outputs);
        txt_multiple_outputs.setOnClickListener(this);
        txt_coin_selection = (TextView) root.findViewById(R.id.txt_coin_selection);
        txt_coin_selection.setOnClickListener(this);
        txt_custom_fee = (TextView) root.findViewById(R.id.txt_custom_fee);
        txt_custom_fee.setOnClickListener(this);
        findViewById(R.id.button_qr).setOnClickListener(this);
        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSend.setOnClickListener(this);

        //Swap type of ammounts
        amountSwap = (ViewFlipper) findViewById( R.id.viewFlipper );
        amountSwap.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_in_left));
        amountSwap.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.slide_out_right));
        btnSwap = (ImageButton) findViewById(R.id.btn_swap);
        btnSwap.setOnClickListener(this);

        //Sending amount currency
        editCurrency = (EditText) findViewById(R.id.edit_amount_currency);
        txt_currency_amount = (TextView) root.findViewById(R.id.txt_currency_amount);
        txtShowPiv = (TextView) findViewById(R.id.txt_show_piv) ;

        //Sending amount piv
        addAllPiv =  (Button) findViewById(R.id.btn_add_all);
        addAllPiv.setOnClickListener(this);
        pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());

        edit_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length()>0) {
                    String valueStr = s.toString();
                    if (valueStr.charAt(0)=='.'){
                        valueStr = "0"+valueStr;
                    }
                    Coin coin = Coin.parseCoin(valueStr);
                    txt_local_currency.setText(
                            pivxApplication.getCentralFormats().getNumberFormat().format(
                                    new BigDecimal(coin.getValue() * pivxRate.getValue().doubleValue()).movePointLeft(8)
                            )
                                    + " "+pivxRate.getCoin()
                    );
                }else {
                    txt_local_currency.setText("0 "+pivxRate.getCoin());
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.option_fee){
            startCustomFeeActivity(customFee);
            return true;
        }else if(id == R.id.option_multiple_addresses){
            startMultiAddressSendActivity(outputWrappers);
            return true;
        }else if(id == R.id.option_select_inputs){
            startCoinControlActivity(unspent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCustomFeeActivity(CustomFeeFragment.FeeSelector customFee) {
        Intent intent = new Intent(this, CustomFeeActivity.class);
        if (customFee != null) {
            intent.putExtra(INTENT_EXTRA_IS_FEE_PER_KB, customFee.isFeePerKbSelected());
            intent.putExtra(INTENT_EXTRA_IS_TOTAL_FEE, !customFee.isFeePerKbSelected());
            intent.putExtra(INTENT_EXTRA_IS_MINIMUM_FEE, customFee.isPayMinimum());
            intent.putExtra(INTENT_EXTRA_FEE, customFee.getAmount());
        }
        startActivityForResult(intent,CUSTOM_FEE_RESULT);
    }

    private void startMultiAddressSendActivity(List<OutputWrapper> outputWrappers) {
        String amountStr = edit_amount.getText().toString();
        if (amountStr.length()>0){
            Intent intent = new Intent(this, OutputsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(INTENT_EXTRA_TOTAL_AMOUNT,edit_amount.getText().toString());
            if (outputWrappers!=null)
                bundle.putSerializable(INTENT_EXTRA_OUTPUTS_WRAPPERS, (Serializable) outputWrappers);
            intent.putExtras(bundle);
            startActivityForResult(intent,MULTIPLE_ADDRESSES_SEND_RESULT);
        }else {
            Toast.makeText(this,R.string.send_amount_address_error,Toast.LENGTH_LONG).show();
        }
    }

    private void startCoinControlActivity(List<InputWrapper> unspent) {
        String amountStr = edit_amount.getText().toString();
        if (amountStr.length()>0){
            Intent intent = new Intent(this, InputsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(INTENT_EXTRA_TOTAL_AMOUNT,edit_amount.getText().toString());
            if (unspent!=null)
                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, (Serializable) unspent);
            intent.putExtras(bundle);
            startActivityForResult(intent,CUSTOM_INPUTS);
        }else {
            Toast.makeText(this,R.string.send_amount_input_error,Toast.LENGTH_LONG).show();
        }
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
                if (checkConnectivity()){
                    send();
                }
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
        }else if(id == R.id.btn_add_all){
            Coin coin = pivxModule.getAvailableBalanceCoin();
            if (inPivs){
                edit_amount.setText(coin.toPlainString());
                txt_local_currency.setText(
                        pivxApplication.getCentralFormats().getNumberFormat().format(
                                new BigDecimal(coin.getValue() * pivxRate.getValue().doubleValue()).movePointLeft(8)
                        )
                                + " "+pivxRate.getCoin()
                );
            }else {
                editCurrency.setText(
                        pivxApplication.getCentralFormats().getNumberFormat().format(
                                new BigDecimal(coin.getValue() * pivxRate.getValue().doubleValue()).movePointLeft(8)
                        )
                                + " "+pivxRate.getCoin()
                );
                txtShowPiv.setText(coin.toFriendlyString());
            }
        }else if(id == R.id.btn_swap){
            inPivs = !inPivs;
            amountSwap.showNext();
        }else if (id == R.id.txt_coin_selection){
            startCoinControlActivity(unspent);
        }else if(id == R.id.txt_multiple_outputs){
            startMultiAddressSendActivity(outputWrappers);
        }else if(id == R.id.txt_custom_fee){
            startCustomFeeActivity(customFee);
        }
    }

    private boolean checkConnectivity() {
        if (!isOnline()){
            SimpleTwoButtonsDialog noConnectivityDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                    this,
                    getString(R.string.error_no_connectivity_title),
                    getString(R.string.error_no_connectivity_body),
                    new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                        @Override
                        public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                            try {
                                send();
                            }catch (Exception e){
                                showErrorDialog(e.getMessage());
                            }
                            dialog.dismiss();

                        }

                        @Override
                        public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                            dialog.dismiss();
                        }
                    }
            );
            noConnectivityDialog.setLeftBtnTextColor(Color.WHITE)
                    .setLeftBtnBackgroundColor(getColor(R.color.lightGreen))
                    .setRightBtnTextColor(Color.BLACK)
                    .setRightBtnBackgroundColor(Color.WHITE)
                    .setLeftBtnText(getString(R.string.button_cancel))
                    .setRightBtnText(getString(R.string.button_ok))
                    .show();

            return false;
        }
        return true;
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
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
        }else if(requestCode == SEND_DETAIL){
            if (resultCode==RESULT_OK) {
                // pin ok, send the tx now
                sendConfirmed();
            }
        }else if(requestCode == MULTIPLE_ADDRESSES_SEND_RESULT){
            if (resultCode == RESULT_OK){
                if (data.hasExtra(INTENT_EXTRA_OUTPUTS_CLEAR)){
                    outputWrappers = null;
                    txt_multiple_outputs.setVisibility(View.GONE);
                    container_address.setVisibility(View.VISIBLE);
                }else {
                    outputWrappers = (List<OutputWrapper>) data.getSerializableExtra(INTENT_EXTRA_OUTPUTS_WRAPPERS);
                    txt_multiple_outputs.setText(getString(R.string.multiple_address_send, outputWrappers.size()));
                    txt_multiple_outputs.setVisibility(View.VISIBLE);
                    container_address.setVisibility(View.GONE);
                }
            }
        }else if (requestCode == CUSTOM_INPUTS){
            if (resultCode == RESULT_OK) {
                List<InputWrapper> unspents = (List<InputWrapper>) data.getSerializableExtra(INTENT_EXTRA_UNSPENT_WRAPPERS);
                for (InputWrapper inputWrapper : unspents) {
                    inputWrapper.setUnspent(pivxModule.getUnspent(inputWrapper.getParentTxHash(),inputWrapper.getIndex()));
                }
                unspent = unspents;
                txt_coin_selection.setVisibility(View.VISIBLE);
            }
        }else if (requestCode == CUSTOM_FEE_RESULT){
            if (resultCode == RESULT_OK){
                if (data.hasExtra(INTENT_EXTRA_CLEAR)){
                    customFee = null;
                    txt_custom_fee.setVisibility(View.GONE);
                }else {
                    boolean isPerKb = data.getBooleanExtra(INTENT_EXTRA_IS_FEE_PER_KB, false);
                    boolean isTotal = data.getBooleanExtra(INTENT_EXTRA_IS_TOTAL_FEE, false);
                    boolean isMinimum = data.getBooleanExtra(INTENT_EXTRA_IS_MINIMUM_FEE, false);
                    Coin feeAmount = (Coin) data.getSerializableExtra(INTENT_EXTRA_FEE);
                    customFee = new CustomFeeFragment.FeeSelector(isPerKb, feeAmount, isMinimum);
                    txt_custom_fee.setVisibility(View.VISIBLE);
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showErrorDialog(String message) {
        if (errorDialog==null){
            errorDialog = DialogsUtil.buildSimpleErrorTextDialog(this,getResources().getString(R.string.invalid_inputs),message);
        }else {
            errorDialog.setBody(message);
        }
        errorDialog.show(getFragmentManager(),getResources().getString(R.string.send_error_dialog_tag));
    }

    private void send() {
        try {

            // first check amount
            String amountStr = edit_amount.getText().toString();
            if (amountStr.length() < 1) throw new IllegalArgumentException("Amount not valid");
            if (amountStr.length()==1 && amountStr.equals(".")) throw new IllegalArgumentException("Amount not valid");
            if (amountStr.charAt(0)=='.'){
                amountStr = "0"+amountStr;
            }
            Coin amount = Coin.parseCoin(amountStr);
            if (amount.isZero()) throw new IllegalArgumentException("Amount zero, please correct it");
            if (amount.isLessThan(Transaction.MIN_NONDUST_OUTPUT)) throw new IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, "+Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());
            if (amount.isGreaterThan(Coin.valueOf(pivxModule.getAvailableBalance())))
                throw new IllegalArgumentException("Insuficient balance");


            NetworkParameters params = pivxModule.getConf().getNetworkParams();
            Transaction transaction = new Transaction(params);

            // then outputs
            if (outputWrappers!=null && !outputWrappers.isEmpty()){
                for (OutputWrapper outputWrapper : outputWrappers) {
                    transaction.addOutput(
                            outputWrapper.getAmount(),
                            Address.fromBase58(params,outputWrapper.getAddress())
                    );
                }
            }else {
                addressStr = edit_address.getText().toString();
                if (!pivxModule.chechAddress(addressStr))
                    throw new IllegalArgumentException("Address not valid");
                transaction.addOutput(amount,Address.fromBase58(pivxModule.getConf().getNetworkParams(),addressStr));
            }

            // then check custom inputs if there is any
            if (unspent!=null && !unspent.isEmpty()){
                for (InputWrapper inputWrapper : unspent) {
                    transaction.addInput(inputWrapper.getUnspent());
                }
            }
            // satisfy output with inputs if it's neccesary
            Coin ouputsSum = transaction.getOutputSum();
            Coin inputsSum = transaction.getInputSum();

            if (ouputsSum.isGreaterThan(inputsSum)){
                List<TransactionOutput> unspent = pivxModule.getRandomUnspentNotInListToFullCoins(transaction.getInputs(),ouputsSum);
                for (TransactionOutput transactionOutput : unspent) {
                    transaction.addInput(transactionOutput);
                }
                // update the input amount
                inputsSum = transaction.getInputSum();
            }

            Coin fee;
            // then fee and change address

            // tx size calculation -> (148*inputs)+(34*outputs)+10
            long txSize = 148 * transaction.getInputs().size() + 34 * transaction.getOutputs().size() + 10;

            if (customFee!=null){
                if (customFee.isPayMinimum()){
                    fee = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
                }else {
                    if (customFee.isFeePerKbSelected()){
                        // fee per kB
                        Coin feePerByte = customFee.getAmount().shiftRight(3);
                        fee = feePerByte.multiply(txSize);
                    }else {
                        // total fee
                        fee = customFee.getAmount();
                    }
                }
            }else {
                Coin feePerByte = Transaction.DEFAULT_TX_FEE.shiftRight(3);
                fee = feePerByte.multiply(txSize);
            }

            // check if something left and add it on a change address output
            Coin coinsLeft = inputsSum.minus(ouputsSum).minus(fee);
            if (coinsLeft.isGreaterThan(Coin.ZERO)){
                transaction.addOutput(coinsLeft,pivxModule.getAddress());
            }
            String memo = edit_memo.getText().toString();
            if (memo.length()>0)
                transaction.setMemo(memo);

            transaction = pivxModule.completeTx(transaction);
            // build a tx with the default fee
            //transaction = pivxModule.buildSendTx(addressStr, amount, memo);

            Log.i("APP","tx: "+transaction.toString());

            TransactionWrapper transactionWrapper = new TransactionWrapper(transaction,null,null,amount, TransactionWrapper.TransactionUse.SENT_SINGLE);

            // Confirmation screen
            Intent intent = new Intent(this,SendTxDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(TX_WRAPPER,transactionWrapper);
            bundle.putSerializable(TX,transaction.bitcoinSerialize());
            intent.putExtras(bundle);
            startActivityForResult(intent,SEND_DETAIL);
            //launchDialog(transaction);

        } catch (InsufficientMoneyException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Insuficient balance");
        } catch (InsufficientInputsException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Insuficient balance");
        }
    }

    private void sendConfirmed(){
        if (contactName.length()>0){
            Contact contact = new Contact(contactName);
            contact.addAddress(addressStr);
            contact.addTx(transaction.getHash());
            try {
                pivxModule.saveContact(contact);
            } catch (ContactAlreadyExistException e) {
                e.printStackTrace();
                Toast.makeText(SendActivity.this,R.string.contact_already_exist,Toast.LENGTH_LONG).show();
                return;
            }
        }
        pivxModule.commitTx(transaction);
        Intent intent = new Intent(SendActivity.this, PivxWalletService.class);
        intent.setAction(ACTION_BROADCAST_TRANSACTION);
        intent.putExtra(DATA_TRANSACTION_HASH,transaction.getHash().getBytes());
        startService(intent);
        Toast.makeText(SendActivity.this,R.string.sending_tx,Toast.LENGTH_LONG).show();
        onBackPressed();
    }

}
