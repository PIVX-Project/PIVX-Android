package pivx.org.pivxwallet.wallofcoins.buyingwizard;

import android.app.LoaderManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.bitcoinj.core.Coin;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.wallofcoins.BuyDashPref;
import pivx.org.pivxwallet.wallofcoins.WOCConstants;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.buy_dash_location.BuyDashLocationFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.email_phone.EmailAndPhoneFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.offer_amount.BuyDashOfferAmountFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.order_history.OrderHistoryFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.utils.FragmentUtils;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.verification_otp.VerifycationOtpFragment;
import pivx.org.pivxwallet.wallofcoins.response.CreateHoldResp;
import pivx.org.pivxwallet.wallofcoins.ui.CurrencyTextView;
import pivx.org.pivxwallet.wallofcoins.utils.Configuration;
import pivx.org.pivxwallet.wallofcoins.utils.Constants;


/**
 * Created on 6/3/18.
 */

public class BuyDashBaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {


    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    public BuyDashPref buyDashPref;
    private ImageView imgViewToolbarBack;
    // private Wallet wallet;
    // private WalletApplication application;
    private TextView appBarMessageView;
    private ProgressBar progressView;
    private LinearLayout viewBalance;
    private CurrencyTextView viewBalanceBtc, viewBalanceLocal;
    private ImageView viewBalanceTooMuch;
    private static final Coin TOO_MUCH_BALANCE_THRESHOLD = Coin.COIN.multiply(30);
    private static final int ID_BALANCE_LOADER = 0;
    private static final int ID_RATE_LOADER = 1;
    private static final int ID_BLOCKCHAIN_STATE_LOADER = 2;
    private static final int ID_MASTERNODE_SYNC_LOADER = 3;

    private static final long BLOCKCHAIN_UPTODATE_THRESHOLD_MS = DateUtils.HOUR_IN_MILLIS;
    @javax.annotation.Nullable
    private Coin balance = null;
    private LoaderManager loaderManager;
    private boolean showLocalBalance;
    private boolean initComplete = false;
    @javax.annotation.Nullable
    // private BlockchainState blockchainState = null;
    private String progressMessage;
    private Configuration config;
    @javax.annotation.Nullable
    // private ExchangeRate exchangeRate = null;
    private Context mContext;
    private boolean isPopBack;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_buy_dash_base);
        init();
        setListners();
        if (!TextUtils.isEmpty(buyDashPref.getAuthToken())) {
            if (!TextUtils.isEmpty(buyDashPref.getHoldId())) {
                CreateHoldResp createHoldResp = buyDashPref.getCreateHoldResp();
                //binding.etOtp.setText(createHoldResp.__PURCHASE_CODE);
                // hideViewExcept(binding.layoutVerifyOtp);
                Bundle bundle = new Bundle();
                bundle.putString(WOCConstants.VERIFICATION_OTP, createHoldResp.__PURCHASE_CODE);
                VerifycationOtpFragment otpFragment = new VerifycationOtpFragment();
                otpFragment.setArguments(bundle);
                replaceFragment(otpFragment, true, false);
                //navigateToVerifyOtp(createHoldResp.__PURCHASE_CODE);
            } else {
                //hideViewExcept(binding.rvOrderList);
                //getOrderList(false);
                //navigateToOrderList(false);
                /*OrderHistoryFragment historyFragment = new OrderHistoryFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean("isFromCreateHold", false);
                historyFragment.setArguments(bundle);
                replaceFragment(historyFragment, true, false);*/
                replaceFragment(new BuyDashLocationFragment(), true, true);
            }
        } else
            replaceFragment(new BuyDashLocationFragment(), true, true);
    }

    private void init() {
        mContext = this;
        this.loaderManager = getLoaderManager();
    /*    this.application = (WalletApplication) this.getApplication();
        this.wallet = application.getWallet();
        this.config = application.getConfiguration();*/
        this.buyDashPref = new BuyDashPref(PreferenceManager.getDefaultSharedPreferences(this));
        buyDashPref.registerOnSharedPreferenceChangeListener(this);
        fragmentManager = getSupportFragmentManager();
        imgViewToolbarBack = (ImageView) findViewById(R.id.imgViewToolbarBack);
        appBarMessageView = (TextView) findViewById(R.id.toolbar_message);
        progressView = (ProgressBar) findViewById(R.id.progress);
        viewBalance = (LinearLayout) findViewById(R.id.wallet_balance);
        viewBalanceBtc = (CurrencyTextView) findViewById(R.id.wallet_balance_btc);
        viewBalanceLocal = (CurrencyTextView) findViewById(R.id.wallet_balance_local);
        viewBalanceTooMuch = (ImageView) findViewById(R.id.wallet_balance_too_much_warning);


        viewBalanceBtc.setPrefixScaleX(0.9f);
        viewBalanceLocal.setInsignificantRelativeSize(1);
        viewBalanceLocal.setStrikeThru(Constants.TEST);

        // loaderManager.initLoader(ID_BALANCE_LOADER, null, balanceLoaderCallbacks);

        // loaderManager.initLoader(ID_RATE_LOADER, null, rateLoaderCallbacks);

   /*     if (!initComplete) {
            loaderManager.initLoader(ID_BLOCKCHAIN_STATE_LOADER, null, blockchainStateLoaderCallbacks);
            initComplete = true;
        } else
            loaderManager.restartLoader(ID_BLOCKCHAIN_STATE_LOADER, null, blockchainStateLoaderCallbacks);*/

        //updateView();

    }

    private void setListners() {
        imgViewToolbarBack.setOnClickListener(this);
        viewBalance.setOnClickListener(this);
    }

    public void replaceFragment(Fragment fragment, boolean withAnimation, boolean withBackStack) {
        fragmentTransaction = fragmentManager.beginTransaction();
        Log.e("Fragment name", fragment.getClass().getName());
        if (withAnimation)
            fragmentTransaction.setCustomAnimations(R.anim.activity_in, R.anim.activity_out, R.anim.activity_backin, R.anim.activity_back_out);
        if (withBackStack)
            fragmentTransaction.replace(R.id.containerBuyDashBase, fragment).addToBackStack(fragment.getClass().getName());
        else
            fragmentTransaction.replace(R.id.containerBuyDashBase, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void finishBaseActivity() {
        this.finish();
    }

    public void popbackFragment() {

        Log.e("CurrentFragment", fragmentManager.findFragmentById(R.id.containerBuyDashBase).toString());
        if (fragmentManager.getBackStackEntryCount() > 0) {
            Fragment fragment = fragmentManager.findFragmentById(R.id.containerBuyDashBase);
            if (fragment instanceof EmailAndPhoneFragment)
                ((EmailAndPhoneFragment) fragment).changeView();

            else if (fragment instanceof BuyDashOfferAmountFragment)
                ((BuyDashOfferAmountFragment) fragment).changeView();
            else if (fragment instanceof BuyDashLocationFragment)
                this.finish();
            else if (fragment instanceof OrderHistoryFragment)
                ((OrderHistoryFragment)fragment).changeView();
            else
                fragmentManager.popBackStack();
        } else
            this.finish();
    }

    public void popBackDirect() {
        if (fragmentManager.getBackStackEntryCount() > 0)
            fragmentManager.popBackStack();
        else
            this.finish();

    }

    @Override
    public void onBackPressed() {
        popbackFragment();
        //super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        buyDashPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.imgViewToolbarBack:
                popbackFragment();
                break;

            case R.id.wallet_balance:
                showWarningIfBalanceTooMuch();
                //showExchangeRatesActivity();
                break;
        }


    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void showWarningIfBalanceTooMuch() {
        if (balance != null && balance.isGreaterThan(TOO_MUCH_BALANCE_THRESHOLD)) {
            Toast.makeText(this, getString(R.string.wallet_balance_fragment_too_much),
                    Toast.LENGTH_LONG).show();
        }
    }


    private void showAppBarMessage(CharSequence message) {
        if (message != null) {
            appBarMessageView.setVisibility(View.VISIBLE);
            appBarMessageView.setText(message);
        } else {
            appBarMessageView.setVisibility(View.GONE);
        }
    }

    private void updateBalanceTooMuchWarning() {
        if (balance == null)
            return;

        boolean tooMuch = balance.isGreaterThan(TOO_MUCH_BALANCE_THRESHOLD);
        viewBalanceTooMuch.setVisibility(tooMuch ? View.VISIBLE : View.GONE);
    }

    public void popBackAllFragmentsExcept(String tag) {
        fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    public void removeAllFragmentFromStack() {

        if (fragmentManager.getBackStackEntryCount() > 0) {
            FragmentUtils.sDisableFragmentAnimations = true;
            fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            FragmentUtils.sDisableFragmentAnimations = false;
        }
    }
}
