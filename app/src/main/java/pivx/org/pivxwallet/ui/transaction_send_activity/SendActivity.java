package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.pivxj.core.Address;
import org.pivxj.core.Coin;
import org.pivxj.core.InsufficientMoneyException;
import org.pivxj.core.NetworkParameters;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutput;
import org.pivxj.uri.PivxURI;
import org.pivxj.wallet.SendRequest;
import org.pivxj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import de.schildbach.wallet.ui.scan.ScanActivity;
import host.furszy.zerocoinj.wallet.CannotSpendCoinsException;
import pivx.org.pivxwallet.R;
import global.AddressLabel;
import global.exceptions.NoPeerConnectedException;
import global.PivxRate;
import pivx.org.pivxwallet.service.PivxWalletService;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.pincode_activity.PincodeActivity;
import pivx.org.pivxwallet.ui.privacy.privacy_coin_control.PrivacyCoinControlActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.ChangeAddressActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment;
import global.wrappers.InputWrapper;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputsActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputWrapper;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputsActivity;
import global.wrappers.TransactionWrapper;
import pivx.org.pivxwallet.utils.CrashReporter;
import pivx.org.pivxwallet.utils.DialogsUtil;
import pivx.org.pivxwallet.utils.NavigationUtils;
import wallet.exceptions.InsufficientInputsException;
import wallet.exceptions.TxNotFoundException;

import static android.Manifest.permission_group.CAMERA;
import static de.schildbach.wallet.ui.scan.ScanActivity.INTENT_EXTRA_RESULT;
import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION;
import static pivx.org.pivxwallet.service.IntentsConstants.DATA_TRANSACTION_HASH;
import static pivx.org.pivxwallet.ui.transaction_detail_activity.FragmentTxDetail.TX;
import static pivx.org.pivxwallet.ui.transaction_detail_activity.FragmentTxDetail.TX_MEMO;
import static pivx.org.pivxwallet.ui.transaction_detail_activity.FragmentTxDetail.TX_WRAPPER;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.ChangeAddressActivity.INTENT_EXTRA_CHANGE_ADDRESS;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.ChangeAddressActivity.INTENT_EXTRA_CHANGE_SEND_ORIGIN;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_CLEAR;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_FEE_PER_KB;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_MINIMUM_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_TOTAL_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputsFragment.INTENT_EXTRA_UNSPENT_WRAPPERS;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputsActivity.INTENT_EXTRA_OUTPUTS_CLEAR;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs.OutputsActivity.INTENT_EXTRA_OUTPUTS_WRAPPERS;

/**
 * Created by Neoperol on 5/4/17.
 */

public class SendActivity extends BaseActivity implements View.OnClickListener {

    private Logger logger = LoggerFactory.getLogger(SendActivity.class);

    public static final String INTENT_EXTRA_TOTAL_AMOUNT = "total_amount";
    public static final String INTENT_ADDRESS = "intent_address";
    public static final String INTENT_MEMO = "intent_memo";

    private static final int PIN_RESULT = 121;
    private static final int SCANNER_RESULT = 122;
    private static final int CUSTOM_FEE_RESULT = 123;
    private static final int MULTIPLE_ADDRESSES_SEND_RESULT = 124;
    private static final int CUSTOM_INPUTS = 125;
    private static final int SEND_DETAIL = 126;
    private static final int CUSTOM_CHANGE_ADDRESS = 127;
    private static final int PIN_RESULT_ZPIV = 128;

    private Boolean isPrivate = false;
    private View root;
    private Button buttonSend, addAllPiv, btn_clear;
    private AutoCompleteTextView edit_address;
    private TextView txt_local_currency , txt_coin_selection, txt_custom_fee, txt_change_address, txtShowPiv;
    private TextView txt_multiple_outputs, txt_currency_amount, text_fee_message, title_amount_piv, title_amount_local, title_address, title_description;
    private View container_address, layout_qr_button;
    private EditText edit_amount, editCurrency;
    private EditText edit_memo;
    private MyFilterableAdapter filterableAdapter;
    private String addressStr;
    private PivxRate pivxRate;
    private SimpleTextDialog errorDialog;
    private ImageButton btnSwap, button_qr;
    private ViewFlipper amountSwap;
    private ScrollView layout_scroll;
    private CheckBox check_mint_change;

    private boolean inPivs = true;
    private Transaction transaction;
    /** Several outputs */
    private List<OutputWrapper> outputWrappers;
    /** Custom inputs */
    private Set<InputWrapper> unspent;
    /** Custom fee selector */
    private CustomFeeFragment.FeeSelector customFee;
    /** Clean wallet flag */
    private boolean cleanWallet;
    /** Is multi send */
    private boolean isMultiSend;
    /** Change address */
    private boolean changeToOrigin;
    private Address changeAddress;


    @Override
    protected void onCreateView(Bundle savedInstanceState,ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_transaction_send, container);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("Private")) {
            isPrivate = intent.getBooleanExtra("Private",false);
        }

        layout_scroll = (ScrollView) findViewById(R.id.layout_scroll);
        buttonSend = (Button) findViewById(R.id.btnSend);
        buttonSend.setOnClickListener(this);
        text_fee_message = (TextView) findViewById(R.id.text_fee_message);
        title_amount_piv = (TextView) findViewById(R.id.title_amount_piv);
        title_amount_piv.setText(getText(R.string.amount) + " "  + getText(R.string.set_amount_piv));
        title_amount_local = (TextView) findViewById(R.id.title_amount_local);
        title_address = (TextView) findViewById(R.id.title_address);
        title_description = (TextView) findViewById(R.id.title_description);
        addAllPiv =  (Button) findViewById(R.id.btn_add_all);
        btn_clear = (Button) findViewById(R.id.btn_clear);


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
        txt_change_address = (TextView) root.findViewById(R.id.txt_change_address);
        txt_change_address.setOnClickListener(this);
        button_qr = (ImageButton) root.findViewById(R.id.button_qr);
        findViewById(R.id.button_qr).setOnClickListener(this);
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

        check_mint_change = (CheckBox) findViewById(R.id.check_mint_change);

        //Sending amount piv
        addAllPiv.setOnClickListener(this);
        btn_clear.setOnClickListener(this);
        pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());
        if (pivxRate != null)
            txt_local_currency.setText("0 " + pivxRate.getCode());
        else {
            txt_local_currency.setText(R.string.no_rate);
        }

        editCurrency.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pivxRate != null) {
                    if (s.length() > 0) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        BigDecimal result = new BigDecimal(valueStr).divide(pivxRate.getRate(), 6, BigDecimal.ROUND_DOWN);
                        txtShowPiv.setText(result.toPlainString() + " PIV");
                    } else {
                        txtShowPiv.setText("0 " + pivxRate.getCode());
                    }
                }else {
                    txtShowPiv.setText(R.string.no_rate);
                }
                cleanWallet = false;
            }
        });

        edit_amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    if (pivxRate != null) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
                        }
                        if (valueStr.charAt(valueStr.length()-1) == '.'){
                            valueStr = valueStr.replace(".","");
                        }
                        Coin coin = Coin.parseCoin(valueStr);
                        txt_local_currency.setText(
                                pivxApplication.getCentralFormats().format(
                                        new BigDecimal(coin.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                                )
                                        + " " + pivxRate.getCode()
                        );
                    }else {
                        // rate null -> no connection.
                        txt_local_currency.setText(R.string.no_rate);
                    }
                }else {
                    if (pivxRate!=null)
                        txt_local_currency.setText("0 "+pivxRate.getCode());
                    else
                        txt_local_currency.setText(R.string.no_rate);
                }
                cleanWallet = false;

            }
        });
        if (pivxRate != null) {
            editCurrency.setHint(pivxRate.getCode() + " " + getText(R.string.title_equivalent));
            title_amount_local.setText(getText(R.string.amount) + "  (" + pivxRate.getCode() + " " + getText(R.string.title_equivalent) + ")");
        } else {
            editCurrency.setHint(R.string.title_equivalent);
        }
        // Load data if exists
        String address = intent.getStringExtra(INTENT_ADDRESS);
        if (address != null){
            edit_address.setText(address);
            Coin amount = (Coin) intent.getSerializableExtra(INTENT_EXTRA_TOTAL_AMOUNT);
            edit_amount.setText(amount.toPlainString());
            String memo = intent.getStringExtra(INTENT_MEMO);
            if (memo != null)
                edit_memo.setText(memo);
        }

        layout_qr_button = findViewById(R.id.layout_qr_button);

        // Layout changed zPIV

        if (isPrivate) {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.darkPurple)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkPurple));
            }
            setTitle(R.string.title_send_private);
            layout_scroll.setBackgroundResource(R.color.darkPurple);
            buttonSend.setText(R.string.btn_send_zpiv);
            buttonSend.setBackgroundResource(R.color.white);
            buttonSend.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.colorPurple));
            text_fee_message.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
            title_description.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white_a_60));
            title_address.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white_a_60));
            title_amount_local.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white_a_60));
            title_amount_piv.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white_a_60));
            addAllPiv.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
            btn_clear.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.white));
            edit_amount.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edit_text_white_selector));
            edit_amount.setHintTextColor(getResources().getColor(R.color.white_a_80));
            edit_amount.setTextColor(getResources().getColor(R.color.white));
            edit_amount.setPadding(convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12));
            editCurrency.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edit_text_white_selector));
            editCurrency.setPadding(convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12));
            editCurrency.setHintTextColor(getResources().getColor(R.color.white_a_80));
            editCurrency.setTextColor(getResources().getColor(R.color.white));
            edit_address.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edit_text_white_selector));
            edit_address.setPadding(convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12));
            edit_address.setHintTextColor(getResources().getColor(R.color.white_a_80));
            edit_address.setTextColor(getResources().getColor(R.color.white));
            edit_memo.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_edit_text_white_selector));
            edit_memo.setPadding(convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12), convertDpToPx(getResources(), 12));
            edit_memo.setHintTextColor(getResources().getColor(R.color.white_a_80));
            edit_memo.setTextColor(getResources().getColor(R.color.white));
            txt_local_currency.setTextColor(getResources().getColor(R.color.white));
            txtShowPiv.setTextColor(getResources().getColor(R.color.white));
            txt_multiple_outputs.setTextColor(getResources().getColor(R.color.black_a_60));
            txt_coin_selection.setTextColor(getResources().getColor(R.color.black_a_60));
            txt_custom_fee.setTextColor(getResources().getColor(R.color.black_a_60));
            txt_change_address.setTextColor(getResources().getColor(R.color.black_a_60));
            button_qr.setImageResource(R.drawable.ic_qr_code_white);
            btnSwap.setImageResource(R.drawable.ic_swap_white);
            edit_amount.setInputType(InputType.TYPE_CLASS_NUMBER);
            check_mint_change.setVisibility(View.VISIBLE);
        }
        else {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.bgPurple)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(ContextCompat.getColor(this, R.color.bgPurple));
            }
            setTitle(R.string.btn_send);
            layout_scroll.setBackgroundResource(R.color.white);
            buttonSend.setText(R.string.btn_send);
            buttonSend.setBackgroundResource(R.color.colorPurple);
            buttonSend.setTextColor(ContextCompat.getColor(this, R.color.white));
            text_fee_message.setTextColor(ContextCompat.getColor(this, R.color.subtitle));
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isPrivate)
            getMenuInflater().inflate(R.menu.send_menu,menu);
        else{
            getMenuInflater().inflate(R.menu.send_zpiv_menu,menu);
        }
        return true;
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
            if (isPrivate){
                Intent intent = new Intent(this, PrivacyCoinControlActivity.class);
                startActivityForResult(intent, CUSTOM_INPUTS);
            } else {
                startCoinControlActivity(unspent);
            }
        }else if (id == R.id.option_change_address){
            startChangeAddressActivity(changeAddress,changeToOrigin);
        }
        return super.onOptionsItemSelected(item);
    }

    private void startChangeAddressActivity(Address changeAddress,boolean changeToOrigin) {
        Intent intent = new Intent(this, ChangeAddressActivity.class);
        if (changeAddress!=null){
            intent.putExtra(INTENT_EXTRA_CHANGE_ADDRESS,changeAddress.toBase58());
        }
        intent.putExtra(INTENT_EXTRA_CHANGE_SEND_ORIGIN,changeToOrigin);
        startActivityForResult(intent,CUSTOM_CHANGE_ADDRESS);
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
        Intent intent = new Intent(this, OutputsActivity.class);
        Bundle bundle = new Bundle();
        if (outputWrappers!=null)
            bundle.putSerializable(INTENT_EXTRA_OUTPUTS_WRAPPERS, (Serializable) outputWrappers);
        intent.putExtras(bundle);
        startActivityForResult(intent,MULTIPLE_ADDRESSES_SEND_RESULT);
    }

    private void startCoinControlActivity(Set<InputWrapper> unspent) {
        String amountStr = getAmountStr();
        if (amountStr.length() > 0){
            Intent intent = new Intent(this, InputsActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(INTENT_EXTRA_TOTAL_AMOUNT,amountStr);
            if (unspent != null)
                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, (Serializable) unspent);
            intent.putExtras(bundle);
            startActivityForResult(intent,CUSTOM_INPUTS);
        }else {
            Toast.makeText(this,R.string.send_amount_input_error,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (transaction != null)
            outState.putSerializable(TX,transaction.unsafeBitcoinSerialize());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // todo: test this roting the screen..
        if (savedInstanceState.containsKey(TX)){
            transaction = new Transaction(pivxModule.getConf().getNetworkParams(),savedInstanceState.getByteArray(TX));
        }
    }

    @Override
    public void onBackPressed() {
        try {
            super.onBackPressed();
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent();
        intent.putExtra("Private",isPrivate);
        setResult(RESULT_OK, getIntent());
        finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        disconnectFromService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // todo: This is not updating the filter..
        if (filterableAdapter == null) {
            List<AddressLabel> list = new ArrayList<>(pivxModule.getContacts());
            filterableAdapter = new MyFilterableAdapter(this,list );
            edit_address.setAdapter(filterableAdapter);
        }

        if(getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btnSend){
            try {
                if (checkConnectivity()){
                    send(false);
                }
            }catch (IllegalArgumentException e){
                e.printStackTrace();
                showErrorDialog(e.getMessage());
            }catch (Exception e){
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
            if (!isMultiSend) {
                cleanWallet = true;
                Coin coin = (!isPrivate) ? pivxModule.getAvailableBalanceCoin() : pivxModule.getZpivAvailableBalanceCoin();
                if (inPivs) {
                    edit_amount.setText(coin.toPlainString());
                    txt_local_currency.setText(
                            pivxApplication.getCentralFormats().format(
                                    new BigDecimal(coin.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                            )
                                    + " " + pivxRate.getCode()
                    );
                } else {
                    editCurrency.setText(
                            pivxApplication.getCentralFormats().format(
                                    new BigDecimal(coin.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                            )
                    );
                    txtShowPiv.setText(coin.toFriendlyString());
                }
            } else {
                Toast.makeText(this,R.string.validate_multi_send_enabled,Toast.LENGTH_SHORT).show();
            }
        }else if(id == R.id.btn_swap){
            if (!isMultiSend){
                inPivs = !inPivs;
                amountSwap.showNext();
            }else {
                Toast.makeText(this,R.string.validate_multi_send_enabled,Toast.LENGTH_LONG).show();
            }
        }else if (id == R.id.txt_coin_selection){
            startCoinControlActivity(unspent);
        }else if(id == R.id.txt_multiple_outputs){
            startMultiAddressSendActivity(outputWrappers);
        }else if(id == R.id.txt_custom_fee){
            startCustomFeeActivity(customFee);
        }else if (id == R.id.txt_change_address){
            startChangeAddressActivity(changeAddress,changeToOrigin);
        } else if(id == R.id.btn_clear){
            clearFields();
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
                                send(true);
                            }catch (Exception e){
                                e.printStackTrace();
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                noConnectivityDialog.setRightBtnTextColor(getColor(R.color.lightGreen));
            }else {
                noConnectivityDialog.setRightBtnTextColor(ContextCompat.getColor(this, R.color.lightGreen));
            }
            noConnectivityDialog.setLeftBtnTextColor(Color.WHITE)
                    .setRightBtnTextColor(Color.BLACK)
                    .setRightBtnBackgroundColor(Color.WHITE)
                    .setLeftBtnTextColor(Color.BLACK)
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
            if (resultCode == RESULT_OK) {
                String address = "";
                try {
                    address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    String usedAddress;
                    if (pivxModule.chechAddress(address)){
                        usedAddress = address;
                    }else {
                        PivxURI pivxUri = new PivxURI(address);
                        usedAddress = pivxUri.getAddress().toBase58();
                    }
                    final String tempPubKey = usedAddress;
                    edit_address.setText(tempPubKey);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(this,"Bad address "+address,Toast.LENGTH_LONG).show();
                }
            }
        }else if(requestCode == SEND_DETAIL){
            if (resultCode == RESULT_OK) {
                try {
                    // pin ok, send the tx now
                    sendConfirmed();
                }catch (Exception e){
                    e.printStackTrace();
                    CrashReporter.saveBackgroundTrace(e,pivxApplication.getPackageInfo());
                    showErrorDialog(R.string.commit_tx_fail);
                }
            }
        }else if(requestCode == MULTIPLE_ADDRESSES_SEND_RESULT){
            if (resultCode == RESULT_OK){
                if (data.hasExtra(INTENT_EXTRA_OUTPUTS_CLEAR)){
                    outputWrappers = null;
                    txt_multiple_outputs.setVisibility(View.GONE);
                    container_address.setVisibility(View.VISIBLE);
                    unBlockAmount();
                    isMultiSend = false;
                }else {
                    outputWrappers = (List<OutputWrapper>) data.getSerializableExtra(INTENT_EXTRA_OUTPUTS_WRAPPERS);
                    Coin totalAmount = Coin.ZERO;
                    for (OutputWrapper outputWrapper : outputWrappers) {
                        totalAmount = outputWrapper.getAmount().plus(totalAmount);
                    }
                    setAmountAndBlock(totalAmount);
                    txt_multiple_outputs.setText(getString(R.string.multiple_address_send, outputWrappers.size()));
                    txt_multiple_outputs.setVisibility(View.VISIBLE);
                    container_address.setVisibility(View.GONE);
                    isMultiSend = true;
                }
            }
        }else if (requestCode == CUSTOM_INPUTS){
            if (resultCode == RESULT_OK) {
                try {
                    Set<InputWrapper> unspents = (Set<InputWrapper>) data.getSerializableExtra(INTENT_EXTRA_UNSPENT_WRAPPERS);
                    for (InputWrapper inputWrapper : unspents) {
                        inputWrapper.setUnspent(pivxModule.getUnspent(inputWrapper.getParentTxHash(), inputWrapper.getIndex()));
                    }
                    unspent = unspents;
                    txt_coin_selection.setVisibility(View.VISIBLE);
                } catch (TxNotFoundException e) {
                    e.printStackTrace();
                    CrashReporter.saveBackgroundTrace(e,pivxApplication.getPackageInfo());
                    Toast.makeText(this,R.string.load_inputs_fail,Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    CrashReporter.saveBackgroundTrace(e,pivxApplication.getPackageInfo());
                    Toast.makeText(this,R.string.load_inputs_fail,Toast.LENGTH_LONG).show();
                }
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
        }else if(requestCode == CUSTOM_CHANGE_ADDRESS){
            if (resultCode == RESULT_OK){
                if (data.hasExtra(ChangeAddressActivity.INTENT_EXTRA_CLEAR_CHANGE_ADDRESS)){
                    changeAddress = null;
                    changeToOrigin = false;
                    txt_change_address.setVisibility(View.GONE);
                }else {
                    if (data.hasExtra(INTENT_EXTRA_CHANGE_SEND_ORIGIN)){
                        changeAddress = null;
                        changeToOrigin = true;
                    }else {
                        if (data.hasExtra(INTENT_EXTRA_CHANGE_ADDRESS)) {
                            String address = data.getStringExtra(INTENT_EXTRA_CHANGE_ADDRESS);
                            changeAddress = Address.fromBase58(pivxModule.getConf().getNetworkParams(),address);
                        }
                    }
                    txt_change_address.setVisibility(View.VISIBLE);
                }
            }
        }else if (requestCode == PIN_RESULT_ZPIV){
            if (resultCode == RESULT_OK){
                connectToService();
                clearFields();
                Toast.makeText(SendActivity.this,R.string.starting_spend_process, Toast.LENGTH_SHORT).show();
            }else {
                // Spend cancelled
                Toast.makeText(SendActivity.this,R.string.invalid_pincode, Toast.LENGTH_SHORT).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showErrorDialog(int resStr){
        showErrorDialog(getString(resStr));
    }

    private void showErrorDialog(String message) {
        if (errorDialog==null){
            errorDialog = DialogsUtil.buildSimpleErrorTextDialog(this,getResources().getString(R.string.invalid_inputs),message);
        }else {
            errorDialog.setBody(message);
        }
        errorDialog.show(getFragmentManager(),getResources().getString(R.string.send_error_dialog_tag));
    }

    private String getAmountStr(){
        String amountStr = "0";
        if (inPivs) {
            amountStr = edit_amount.getText().toString();
        }else {
            // the value is already converted
            String valueStr = txtShowPiv.getText().toString();
            amountStr = valueStr.replace(" PIV","");
            if(valueStr.length() > 0) {
                if (valueStr.charAt(0) == '.') {
                    amountStr = "0" + valueStr;
                }
            }
        }
        return amountStr;
    }

    public void setAmountAndBlock(Coin amount) {
        if (inPivs) {
            edit_amount.setText(amount.toPlainString());
            edit_amount.setEnabled(false);
        }else {
            BigDecimal result = new BigDecimal(amount.toPlainString()).multiply(pivxRate.getRate()).setScale(6,RoundingMode.FLOOR);
            editCurrency.setText(result.toPlainString());
            edit_amount.setEnabled(false);
        }
    }

    public void unBlockAmount(){
        if (inPivs) {
            edit_amount.setEnabled(true);
        }else {
            edit_amount.setEnabled(true);
        }
    }

    private void send(boolean sendOffline) {
        try {

            // check if the wallet is still syncing
            try {
                if(!pivxModule.isSyncWithNode()){
                    throw new IllegalArgumentException(getString(R.string.wallet_is_not_sync));
                }
            } catch (NoPeerConnectedException e) {
                if (!sendOffline) {
                    e.printStackTrace();
                    throw new IllegalArgumentException(getString(R.string.no_peer_connection));
                }
            }

            // first check amount
            String amountStr = getAmountStr();
            if (amountStr.length() < 1) throw new IllegalArgumentException(String.valueOf(R.string.amount_error));
            if (amountStr.length()==1 && amountStr.equals(".")) throw new IllegalArgumentException(String.valueOf(R.string.amount_error));
            if (amountStr.charAt(0)=='.'){
                amountStr = "0"+amountStr;
            }

            Coin amount = Coin.parseCoin(amountStr);
            if (amount.isZero()) throw new IllegalArgumentException("Amount zero, please correct it");
            if (amount.isLessThan(Transaction.MIN_NONDUST_OUTPUT)) throw new IllegalArgumentException("Amount must be greater than the minimum amount accepted from miners, "+Transaction.MIN_NONDUST_OUTPUT.toFriendlyString());


            if (isPrivate){
                if (amount.isGreaterThan(pivxModule.getZpivAvailableBalanceCoin())){
                    throw new IllegalArgumentException("Insufficient balance");
                }
            }else if (amount.isGreaterThan(Coin.valueOf(pivxModule.getAvailableBalance()))) {
                throw new IllegalArgumentException("Insufficient balance");
            }

            // memo
            String memo = edit_memo.getText().toString();

            NetworkParameters params = pivxModule.getConf().getNetworkParams();


            if (isPrivate){
                addressStr = edit_address.getText().toString();
                if (!pivxModule.chechAddress(addressStr))
                    throw new IllegalArgumentException("Address not valid");
                Address address = Address.fromBase58(params, addressStr);

                boolean mintChange = false;
                if (check_mint_change != null){
                    mintChange = check_mint_change.isChecked();
                }

                // TODO: Add change address to an address in the piv wallet and not in the zpiv wallet
                SendRequest sendRequest = pivxModule.createSpend(address, amount, mintChange);

                SimpleTwoButtonsDialog simpleTwoButtonsDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                        this,
                        "zPIV Spend",
                        String.format("You are just about to spend %s to\n%s\n\nThis process will take a while, please be patient", amount.toFriendlyString(), addressStr),
                        new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                            @Override
                            public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                                transaction = sendRequest.tx;
                                dialog.dismiss();
                                // Now open pin code screen
                                // start pin
                                Intent intent = new Intent(SendActivity.this, PincodeActivity.class);
                                intent.putExtra(PincodeActivity.CHECK_PIN,true);
                                startActivityForResult(intent, PIN_RESULT_ZPIV);
                            }

                            @Override
                            public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                                dialog.dismiss();
                            }
                        }
                );
                simpleTwoButtonsDialog.setImgAlertRes(R.drawable.ic_zero_coin);
                simpleTwoButtonsDialog.setRightBtnTextColor(ContextCompat.getColor(this,R.color.white));
                simpleTwoButtonsDialog.setLeftBtnTextColor(ContextCompat.getColor(this, R.color.white));
                simpleTwoButtonsDialog.setContainerBtnsBackgroundColor(ContextCompat.getColor(this,R.color.bgPurple));
                simpleTwoButtonsDialog.show();
                return;
            }else {

                if ((outputWrappers == null || outputWrappers.isEmpty()) && (unspent == null || unspent.isEmpty())) {
                    addressStr = edit_address.getText().toString();
                    if (!pivxModule.chechAddress(addressStr))
                        throw new IllegalArgumentException("Address not valid");
                    Coin feePerKb = getFee();
                    Address changeAddressTemp = null;
                    if (changeAddress != null) {
                        changeAddressTemp = changeAddress;
                    } else {
                        changeAddressTemp = pivxModule.getReceiveAddress();
                    }
                    transaction = pivxModule.buildSendTx(addressStr, amount, feePerKb, memo, changeAddressTemp);

                    // check if there is a need to change the change address
                    if (changeToOrigin) {
                        transaction = changeChangeAddressToOriginAddress(transaction, changeAddressTemp);
                        transaction = pivxModule.completeTx(transaction);
                    }
                } else {
                    transaction = new Transaction(params);
                    // then outputs
                    if (outputWrappers != null && !outputWrappers.isEmpty()) {
                        for (OutputWrapper outputWrapper : outputWrappers) {
                            transaction.addOutput(
                                    outputWrapper.getAmount(),
                                    Address.fromBase58(params, outputWrapper.getAddress())
                            );
                        }
                    } else {
                        addressStr = edit_address.getText().toString();
                        if (!pivxModule.chechAddress(addressStr))
                            throw new IllegalArgumentException("Address not valid");
                        transaction.addOutput(amount, Address.fromBase58(pivxModule.getConf().getNetworkParams(), addressStr));
                    }

                    // then check custom inputs if there is any
                    if (unspent != null && !unspent.isEmpty()) {
                        for (InputWrapper inputWrapper : unspent) {
                            transaction.addInput(inputWrapper.getUnspent());
                        }
                    }
                    // satisfy output with inputs if it's neccesary
                    Coin ouputsSum = transaction.getOutputSum();
                    Coin inputsSum = transaction.getInputSum();

                    if (ouputsSum.isGreaterThan(inputsSum)) {
                        List<TransactionOutput> unspent = pivxModule.getRandomUnspentNotInListToFullCoins(transaction.getInputs(), ouputsSum);
                        for (TransactionOutput transactionOutput : unspent) {
                            transaction.addInput(transactionOutput);
                        }
                        // update the input amount
                        inputsSum = transaction.getInputSum();
                    }

                    // then fee and change address
                    Coin feePerKb = getFee();

                    if (memo.length() > 0)
                        transaction.setMemo(memo);

                    Address changeAddressTemp = null;
                    if (changeAddress == null) {
                        changeAddressTemp = changeAddress;
                    } else {
                        changeAddressTemp = pivxModule.getReceiveAddress();
                    }

                    transaction = pivxModule.completeTx(transaction, changeAddressTemp, feePerKb);

                    // check if there is a need to change the change address
                    // check if there is a need to change the change address
                    if (changeToOrigin) {
                        transaction = changeChangeAddressToOriginAddress(transaction, changeAddressTemp);
                        transaction = pivxModule.completeTx(transaction);
                    }
                }
            }

            Log.i("APP","tx: "+transaction.toString());

            TransactionWrapper transactionWrapper = new TransactionWrapper(transaction,null,null,amount, TransactionWrapper.TransactionUse.SENT_SINGLE, false);

            // Confirmation screen
            Intent intent = new Intent(this,SendTxDetailActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(TX_WRAPPER,transactionWrapper);
            bundle.putSerializable(TX,transaction.bitcoinSerialize());
            if (memo.length() > 0)
                bundle.putString(TX_MEMO,memo);
            intent.putExtras(bundle);
            startActivityForResult(intent,SEND_DETAIL);

        } catch (InsufficientMoneyException e) {
            String message;
            if (isPrivate){
                message = "Insufficient balance\nMissing coins "+e.missing.toFriendlyString() +
                        "\n\n Your zPIV needs at least 20 confirmations to be able to spend them";
            }else {
                message = "Insufficient balance\nMissing coins "+e.missing.toFriendlyString();
            }
            throw new IllegalArgumentException(message);
        } catch (InsufficientInputsException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Insufficient balance\nMissing coins "+e.getMissing().toFriendlyString());
        } catch (Wallet.DustySendRequested e){
            e.printStackTrace();
            throw new IllegalArgumentException("Dusty send output, please increase the value of your outputs");
        }
    }

    private Transaction changeChangeAddressToOriginAddress(Transaction transaction, Address currentChangeAddress) {
        NetworkParameters params = transaction.getParams();
        // origin address is the highest from the inputs.
        TransactionInput origin = null;
        for (TransactionInput input : transaction.getInputs()) {
            if (origin==null)
                origin = input;
            else {
                if (origin.getValue().isLessThan(input.getValue())){
                    origin = input;
                }
            }
        }
        Address originAddress = origin.getConnectedOutput().getScriptPubKey().getToAddress(params,true);
        // check if the address is mine just in case
        if (!pivxModule.isAddressUsed(originAddress)) throw new IllegalStateException("origin address is not on the wallet: "+originAddress);

        // Now i just have to re organize the outputs.
        TransactionOutput changeOutput = null;
        List<TransactionOutput> outputs = new ArrayList<>();
        for (TransactionOutput transactionOutput : transaction.getOutputs()) {
            if(transactionOutput.getScriptPubKey().getToAddress(params,true).equals(currentChangeAddress)){
                changeOutput = transactionOutput;
            }else {
                outputs.add(transactionOutput);
            }
        }
        transaction.clearOutputs();
        for (TransactionOutput output : outputs) {
            transaction.addOutput(output);
        }
        // now the new change address with the same value
        transaction.addOutput(changeOutput.getValue(),originAddress);
        return transaction;
    }

    public Coin getFee() {
        Coin feePerKb;
        // tx size calculation -> (148*inputs)+(34*outputs)+10
        //long txSize = 148 * transaction.getInputs().size() + 34 * transaction.getOutputs().size() + 10;

        if (customFee!=null){
            if (customFee.isPayMinimum()){
                feePerKb = Transaction.REFERENCE_DEFAULT_MIN_TX_FEE;
            }else {
                if (customFee.isFeePerKbSelected()){
                    // fee per kB
                    feePerKb = customFee.getAmount();
                }else {
                    // todo: total fee..
                    feePerKb = customFee.getAmount();
                }
            }
        }else {
            feePerKb = Transaction.DEFAULT_TX_FEE;
        }
        return feePerKb;
    }

    private void sendConfirmed(){
        if(transaction == null){
            logger.error("## trying to send a NULL transaction");
            try {
                CrashReporter.appendSavedBackgroundTraces(new StringBuilder().append("ERROR ### sendActivity - sendConfirmed - transaction NULL"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            showErrorDialog(R.string.commit_tx_fail);
            return;
        }
        pivxModule.commitTx(transaction);
        Intent intent = new Intent(SendActivity.this, PivxWalletService.class);
        intent.setAction(ACTION_BROADCAST_TRANSACTION);
        intent.putExtra(DATA_TRANSACTION_HASH,transaction.getHash().getBytes());
        startService(intent);
        Toast.makeText(SendActivity.this,R.string.sending_tx,Toast.LENGTH_LONG).show();

        Intent backIntent = new Intent();
        backIntent.putExtra("Private",isPrivate);
        setResult(RESULT_OK, backIntent);
        finish();

        //NavigationUtils.goBackToHome(this);
    }

    private void clearFields() {
        edit_amount.setText("");
        edit_address.setText("");
        edit_memo.setText("");
        check_mint_change.setChecked(false);
        // TODO: remove the other stuff too..
    }

    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }


    private PivxWalletService pivxWalletService;
    private AtomicBoolean isServiceConnected = new AtomicBoolean(false);

    // Service connection..
    protected ServiceConnection mServerConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d("APP", "onServiceConnected");
            pivxWalletService = ((PivxWalletService.PivxBinder)binder).getService();
            isServiceConnected.set(true);
            // Now that the service is connected, let's try to spend the coin
            String msg;
            try {
                pivxWalletService.broadcastCoinSpendTransactionSync(
                        SendRequest.forTx(transaction)
                );
                msg = "Sending transaction..";
            } catch (Exception e){
                e.printStackTrace();
                msg = "Cannot Spend coins, " + e.getMessage();
            }
            String finalMsg = msg;
            runOnUiThread(() -> {
                Toast.makeText(SendActivity.this, finalMsg, Toast.LENGTH_SHORT).show();
                disconnectFromService();
                new Handler().postDelayed(SendActivity.this::onBackPressed,4000);
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("APP", "onServiceDisconnected");
            isServiceConnected.set(false);
        }
    };

    public void connectToService() {
        // mContext is defined upper in code, I think it is not necessary to explain what is it
        Intent intent = new Intent(this, PivxWalletService.class);
        if(!bindService(intent, mServerConn, Context.BIND_IMPORTANT)){
            Toast.makeText(this, "Apparently the service is not running.." ,Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectFromService() {
        if (mServerConn != null && isServiceConnected.getAndSet(false)) {
            unbindService(mServerConn);
        }
    }


}
