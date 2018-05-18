package pivx.org.pivxwallet.wallofcoins.buyingwizard.offer_amount;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.bitcoinj.wallet.Wallet;
import org.pivxj.core.Address;

import java.util.HashMap;
import java.util.List;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.adapters.BuyDashOffersAdapter;
import pivx.org.pivxwallet.wallofcoins.WOCConstants;
import pivx.org.pivxwallet.wallofcoins.api.WallofCoins;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.BuyDashBaseActivity;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.BuyDashBaseFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.email_phone.EmailAndPhoneFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.order_history.OrderHistoryFragment;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.utils.BuyDashAddressPref;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.utils.FragmentUtils;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.verification_otp.VerifycationOtpFragment;
import pivx.org.pivxwallet.wallofcoins.response.BuyDashErrorResp;
import pivx.org.pivxwallet.wallofcoins.response.CreateHoldResp;
import pivx.org.pivxwallet.wallofcoins.response.DiscoveryInputsResp;
import pivx.org.pivxwallet.wallofcoins.response.GetHoldsResp;
import pivx.org.pivxwallet.wallofcoins.response.GetOffersResp;
import pivx.org.pivxwallet.wallofcoins.ui.CurrencyAmountView;
import pivx.org.pivxwallet.wallofcoins.utils.NetworkUtil;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created on 07-Mar-18.
 */

public class BuyDashOfferAmountFragment extends BuyDashBaseFragment implements View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private View rootView;
    private String zipCode;
    private double latitude, longitude;
    private Button button_buy_dash_get_offers;
    private EditText request_coins_amount_btc_edittext, request_coins_amount_local_edittext, edtViewDollar;
    private LinearLayout linearProgress, layout_create_hold;
    private final String TAG = "BuyDashOfferFragment";
    private Wallet wallet;
    private String keyAddress, offerId, dashAmount = "", bankId;
    // private Configuration config;
    //private WalletApplication application;
    private RecyclerView rv_offers;
    private CurrencyAmountView request_coins_amount_local, request_coins_amount_btc;
    //private CurrencyCalculatorLink amountCalculatorLink;
    private LoaderManager loaderManager;
    private final int ID_RATE_LOADER = 1;
    private CreateHoldResp createHoldResp;
    private BuyDashAddressPref dashAddressPref;
    private PivxModule module;
    private Address address;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_buy_dash_offer_amount, container, false);
            init();
            setListeners();
            handleArgs();
            return rootView;
        } else
            return rootView;
    }


    private void init() {
        module = PivxApplication.getInstance().getModule();
        address = module.getReceiveAddress();
        this.loaderManager = getLoaderManager();
        dashAddressPref = new BuyDashAddressPref(PreferenceManager.getDefaultSharedPreferences(mContext));

        button_buy_dash_get_offers = (Button) rootView.findViewById(R.id.button_buy_dash_get_offers);
        //request_coins_amount_btc_edittext = (EditText) rootView.findViewById(R.id.request_coins_amount_btc_edittext);
        //request_coins_amount_local_edittext = (EditText) rootView.findViewById(R.id.request_coins_amount_local_edittext);
        linearProgress = (LinearLayout) rootView.findViewById(R.id.linear_progress);
        layout_create_hold = (LinearLayout) rootView.findViewById(R.id.layout_create_hold);
        //request_coins_amount_local = (CurrencyAmountView) rootView.findViewById(R.id.request_coins_amount_local);
        //request_coins_amount_btc = (CurrencyAmountView) rootView.findViewById(R.id.request_coins_amount_btc);
        edtViewDollar = (EditText) rootView.findViewById(R.id.edtViewDollar);
        // request_coins_amount_btc.setCurrencySymbol(config.getFormat().code());
        // request_coins_amount_btc.setInputFormat(config.getMaxPrecisionFormat());
        //request_coins_amount_btc.setHintFormat(config.getFormat());

        //request_coins_amount_local.setInputFormat(Constants.LOCAL_FORMAT);
        // request_coins_amount_local.setHintFormat(Constants.LOCAL_FORMAT);

        //amountCalculatorLink = new CurrencyCalculatorLink(request_coins_amount_btc, request_coins_amount_local);

        rv_offers = (RecyclerView) rootView.findViewById(R.id.rv_offers);
        rv_offers.setLayoutManager(new LinearLayoutManager(mContext));
    }

    private void setListeners() {
        button_buy_dash_get_offers.setOnClickListener(this);
    }

    /**
     * handle the arguments according to user come from previos screen
     */
    private void handleArgs() {
        if (getArguments() != null) {
            if (getArguments().containsKey(WOCConstants.LATITUDE)) { //user come from my location
                latitude = getArguments().getDouble(WOCConstants.LATITUDE);
                longitude = getArguments().getDouble(WOCConstants.LONGITUDE);
            }
            if (getArguments().containsKey(WOCConstants.ZIP)) { // user come with only zip
                zipCode = getArguments().getString(WOCConstants.ZIP);
            }
            if (getArguments().containsKey(WOCConstants.BANK_ID)) {// user come from bank list
                bankId = getArguments().getString(WOCConstants.BANK_ID);
            }
        }
    }

    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //amountCalculatorLink.setExchangeDirection(config.getLastExchangeDirection());
        //amountCalculatorLink.requestFocus();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        loaderManager.destroyLoader(ID_RATE_LOADER);
        super.onDestroy();
    }

    /**
     * hide show view
     */
    public void changeView() {
        if (layout_create_hold.getVisibility() == View.GONE) {
            layout_create_hold.setVisibility(View.VISIBLE);
            rv_offers.setVisibility(View.GONE);
        } else
            ((BuyDashBaseActivity) mContext).popBackDirect();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_buy_dash_get_offers:
                hideKeyBoard();
                if (isValidAmount())
                    callDiscoveryInputs();
                break;


        }
    }

    private boolean isValidAmount() {
       String amt=edtViewDollar.getText().toString().trim();

        if (edtViewDollar.getText().toString().trim().isEmpty()) {
            showToast(mContext.getString(R.string.alert_amount));
            return false;
        } else if (amt.matches("^\\.$")) {
            showToast(mContext.getString(R.string.enter_valid_amt));
            return false;
        } else if (Double.parseDouble(edtViewDollar.getText().toString().trim()) < 5) {
            showToast(mContext.getString(R.string.alert_puchase_amout));
            return false;
        } else if (Double.parseDouble(edtViewDollar.getText().toString().trim()) > 1000000) {
            showToast(mContext.getString(R.string.amount_less_than_1000000));
            return false;
        }
        return true;
    }


    private void callDiscoveryInputs() {
        if (NetworkUtil.isOnline(mContext)) {
            HashMap<String, String> discoveryInputsReq = new HashMap<String, String>();
            discoveryInputsReq.put(WOCConstants.KEY_PUBLISHER_ID, getString(R.string.WALLOFCOINS_PUBLISHER_ID));
            keyAddress = address.toBase58();
            dashAddressPref.setBuyDashAddress(keyAddress);

            discoveryInputsReq.put(WOCConstants.KEY_CRYPTO_ADDRESS, keyAddress);
            String offerAmount = "0";

            discoveryInputsReq.put(WOCConstants.KEY_USD_AMOUNT, "" + edtViewDollar.getText().toString());
            offerAmount = "" + edtViewDollar.getText().toString();
        /*try {
            if (Float.valueOf(request_coins_amount_local.getTextView().getHint().toString()) > 0f) {
                discoveryInputsReq.put(WOCConstants.KEY_USD_AMOUNT, "" + request_coins_amount_local.getTextView().getHint());
                offerAmount = "" + request_coins_amount_local_edittext.getHint();
            } else {
                discoveryInputsReq.put(WOCConstants.KEY_USD_AMOUNT, "" + request_coins_amount_local.getTextView().getText());
                offerAmount = "" + request_coins_amount_local_edittext.getText();
            }
            Log.d(TAG, "callDiscoveryInputs: usdAmount==>>" + request_coins_amount_local.getTextView().getHint());
        } catch (Exception e) {
            discoveryInputsReq.put(WOCConstants.KEY_USD_AMOUNT, "0");
            e.printStackTrace();
        }*/

            if (latitude > 0.0)
                discoveryInputsReq.put(WOCConstants.KEY_COUNTRY, getCountryCode(latitude, longitude).toLowerCase());

            //discoveryInputsReq.put(WOCConstants.KEY_CRYPTO, config.getFormat().code());
            discoveryInputsReq.put(WOCConstants.KEY_CRYPTO, WOCConstants.CRYPTO);
            if (bankId != null)
                discoveryInputsReq.put(WOCConstants.KEY_BANK, bankId);

            if (zipCode != null)
                discoveryInputsReq.put(WOCConstants.KEY_ZIP_CODE, zipCode);

            if (latitude > 0.0) {
                JsonObject jObj = new JsonObject();
                jObj.addProperty(WOCConstants.KEY_LATITUDE, latitude + "");
                jObj.addProperty(WOCConstants.KEY_LONGITUDE, longitude + "");

                discoveryInputsReq.put(WOCConstants.KEY_BROWSE_LOCATION, jObj.toString());
            }
            discoveryInputsReq.put(WOCConstants.KEY_CRYPTO_AMOUNT, "0");
            linearProgress.setVisibility(View.VISIBLE);

            final String finalOfferAmount = offerAmount;
            WallofCoins.createService(interceptor, getActivity())
                    .discoveryInputs(discoveryInputsReq)
                    .enqueue(new Callback<DiscoveryInputsResp>() {
                        @Override
                        public void onResponse(Call<DiscoveryInputsResp> call, Response<DiscoveryInputsResp> response) {

                            if (null != response && null != response.body()) {
                                if (null != response.body().id) {
                                    updateAddressBookValue(keyAddress, WOCConstants.WOC_ADDRESS);// Update Address Book for Order

                                    WallofCoins.createService(null, getActivity()).getOffers(response.body().id, getString(R.string.WALLOFCOINS_PUBLISHER_ID)).enqueue(new Callback<GetOffersResp>() {
                                        @Override
                                        public void onResponse(Call<GetOffersResp> call, final Response<GetOffersResp> response) {

                                            if (null != response && null != response.body()) {

                                                linearProgress.setVisibility(View.GONE);

                                                if (null != response.body().singleDeposit && !response.body().singleDeposit.isEmpty()) {
                                                    rv_offers.setVisibility(View.VISIBLE);
                                                    layout_create_hold.setVisibility(View.GONE);

                                                    BuyDashOffersAdapter buyDashOffersAdapter = new BuyDashOffersAdapter(mContext, response.body(), finalOfferAmount, new AdapterView.OnItemSelectedListener() {
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                            hideKeyBoard();
                                                            if (position < response.body().singleDeposit.size() + 1) {
                                                                offerId = response.body().singleDeposit.get(position - 1).id;
                                                                dashAmount = response.body().singleDeposit.get(position - 1).amount.DASH;
                                                            } else {
                                                                offerId = response.body().doubleDeposit.get(position - response.body().singleDeposit.size() - 2).id;
                                                                dashAmount = response.body().doubleDeposit.get(position - response.body().singleDeposit.size() - 2).totalAmount.DASH;
                                                            }
                                                            if (!TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getAuthToken())) {
                                                                createHold();
                                                            } else {
                                                                Bundle bundle = new Bundle();
                                                                bundle.putString(WOCConstants.OFFER_ID, offerId);
                                                                EmailAndPhoneFragment fragment = new EmailAndPhoneFragment();
                                                                fragment.setArguments(bundle);

                                                                ((BuyDashBaseActivity) mContext).replaceFragment(fragment, true,
                                                                        true);
                                                            }
                                                        }

                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> parent) {
                                                            linearProgress.setVisibility(View.GONE);
                                                        }
                                                    });
                                                    rv_offers.setAdapter(buyDashOffersAdapter);
                                                } else {
                                                    showToast(mContext.getString(R.string.alert_no_offers));
                                                }
                                            } else if (null != response && null != response.errorBody()) {
                                                linearProgress.setVisibility(View.GONE);
                                                try {
                                                    BuyDashErrorResp buyDashErrorResp = new Gson().fromJson(response.errorBody().string(), BuyDashErrorResp.class);
                                                    Toast.makeText(getContext(), buyDashErrorResp.detail, Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    showToast(mContext.getString(R.string.try_again));
                                                }

                                            } else {
                                                linearProgress.setVisibility(View.GONE);
                                                showToast(mContext.getString(R.string.try_again));
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<GetOffersResp> call, Throwable t) {
                                            linearProgress.setVisibility(View.GONE);
                                            showToast(mContext.getString(R.string.try_again));
                                        }
                                    });
                                } else {
                                    linearProgress.setVisibility(View.GONE);
                                    showToast(mContext.getString(R.string.try_again));
                                }
                            } else if (null != response && null != response.errorBody()) {

                                linearProgress.setVisibility(View.GONE);

                                try {
                                    BuyDashErrorResp buyDashErrorResp = new Gson().fromJson(response.errorBody().string(), BuyDashErrorResp.class);
                                    if (buyDashErrorResp.detail != null && !TextUtils.isEmpty(buyDashErrorResp.detail)) {
                                        Toast.makeText(getContext(), buyDashErrorResp.detail, Toast.LENGTH_LONG).show();
                                    } else {
                                        showToast(mContext.getString(R.string.try_again));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast(mContext.getString(R.string.try_again));
                                }

                            } else {
                                linearProgress.setVisibility(View.GONE);
                                showToast(mContext.getString(R.string.try_again));
                            }
                        }

                        @Override
                        public void onFailure(Call<DiscoveryInputsResp> call, Throwable t) {
                            linearProgress.setVisibility(View.GONE);
                            String message = t.getMessage();
                            Log.d("failure", message);
                            showToast(mContext.getString(R.string.try_again));
                        }
                    });
        } else
            showToast(mContext.getString(R.string.network_not_avaialable));

    }

    /**
     * Method for create new hold
     */
    public void createHold() {
        if (NetworkUtil.isOnline(mContext)) {
            String phone = ((BuyDashBaseActivity) mContext).buyDashPref.getPhone();

            final HashMap<String, String> createHoldPassReq = new HashMap<String, String>();

            if (TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getAuthToken())) {
                createHoldPassReq.put(WOCConstants.KEY_PHONE, phone);
                createHoldPassReq.put(WOCConstants.KEY_PUBLISHER_ID, getString(R.string.WALLOFCOINS_PUBLISHER_ID));
                //createHoldPassReq.put(WOCConstants.KEY_EMAIL, email);
                createHoldPassReq.put(WOCConstants.KEY_deviceName, WOCConstants.KEY_DEVICE_NAME_VALUE);
                createHoldPassReq.put(WOCConstants.KEY_DEVICECODE, getDeviceCode(mContext, ((BuyDashBaseActivity) mContext).buyDashPref));
            }
            createHoldPassReq.put(WOCConstants.KEY_OFFER, offerId);

            linearProgress.setVisibility(View.VISIBLE);

            WallofCoins.createService(interceptor, getActivity()).createHold(createHoldPassReq).enqueue(new Callback<CreateHoldResp>() {
                @Override
                public void onResponse(Call<CreateHoldResp> call, Response<CreateHoldResp> response) {
                    linearProgress.setVisibility(View.GONE);

                    if (null != response.body() && response.code() < 299) {

                        createHoldResp = response.body();
                        ((BuyDashBaseActivity) mContext).buyDashPref.setHoldId(createHoldResp.id);
                        ((BuyDashBaseActivity) mContext).buyDashPref.setCreateHoldResp(createHoldResp);
                        if (TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getDeviceId())
                                && !TextUtils.isEmpty(createHoldResp.deviceId)) {
                            ((BuyDashBaseActivity) mContext).buyDashPref.setDeviceId(createHoldResp.deviceId);
                        }
                        if (!TextUtils.isEmpty(response.body().token)) {
                            ((BuyDashBaseActivity) mContext).buyDashPref.setAuthToken(createHoldResp.token);
                        }
                        navigateToVerifyOtp(createHoldResp.__PURCHASE_CODE);

                    } else if (null != response.errorBody()) {
                        if (response.code() == 403 && TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getAuthToken())) {
                        } else if (response.code() == 403 && !TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getAuthToken())) {
                            getHolds();
                        } else if (response.code() == 400) {
                            if (!TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getAuthToken())) {
                                navigateToOrderList(false);
                            }
                        } else {
                            try {
                                if (!TextUtils.isEmpty(((BuyDashBaseActivity) mContext).buyDashPref.getAuthToken())) {
                                    navigateToOrderList(false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<CreateHoldResp> call, Throwable t) {
                    linearProgress.setVisibility(View.GONE);
                    showToast(mContext.getString(R.string.try_again));
                }
            });
        } else
            showToast(mContext.getString(R.string.network_not_avaialable));


    }

    /**
     * Get all holds for delete active hold
     */
    private void getHolds() {
        if (NetworkUtil.isOnline(mContext)) {
            linearProgress.setVisibility(View.VISIBLE);
            WallofCoins.createService(interceptor, getActivity()).getHolds().enqueue(new Callback<List<GetHoldsResp>>() {
                @Override
                public void onResponse(Call<List<GetHoldsResp>> call, Response<List<GetHoldsResp>> response) {
                    linearProgress.setVisibility(View.GONE);
                    if (response.code() == 200 && response.body() != null) {
                        List<GetHoldsResp> holdsList = response.body();
                        int holdCount = 0;
                        if (holdsList.size() > 0) {
                            for (int i = 0; i < holdsList.size(); i++) {
                                if (null != holdsList.get(i).status && holdsList.get(i).status.equals("AC")) {
                                    deleteHold(holdsList.get(i).id);
                                    holdCount++;
                                }
                            }
                            if (holdCount == 0) {
                                navigateToOrderList(false);
                            }
                        } else {
                            navigateToOrderList(false);
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<GetHoldsResp>> call, Throwable t) {
                    linearProgress.setVisibility(View.GONE);
                    Log.e(TAG, "onFailure: ", t);
                    showToast(mContext.getString(R.string.try_again));
                }
            });
        } else
            showToast(mContext.getString(R.string.network_not_avaialable));

    }

    /**
     * Method call for delete for provide holdId
     *
     * @param holdId
     */
    private void deleteHold(String holdId) {
        if (NetworkUtil.isOnline(mContext)) {
            linearProgress.setVisibility(View.VISIBLE);
            WallofCoins.createService(interceptor, getActivity()).deleteHold(holdId).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    createHold();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    linearProgress.setVisibility(View.GONE);
                    Log.e(TAG, "onFailure: ", t);
                    showToast(mContext.getString(R.string.try_again));
                }
            });
        } else
            showToast(mContext.getString(R.string.network_not_avaialable));

    }

    private void navigateToOrderList(boolean isFromCreateHold) {
        OrderHistoryFragment historyFragment = new OrderHistoryFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isFromCreateHold", isFromCreateHold);
        historyFragment.setArguments(bundle);
        ((BuyDashBaseActivity) mContext).replaceFragment(historyFragment, true, true);
    }

    private void navigateToVerifyOtp(String otp) {
        Bundle bundle = new Bundle();
        bundle.putString(WOCConstants.VERIFICATION_OTP, otp);
        VerifycationOtpFragment otpFragment = new VerifycationOtpFragment();
        otpFragment.setArguments(bundle);

        ((BuyDashBaseActivity) mContext).replaceFragment(otpFragment, true, true);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    //this method remove animation when user want to clear back stack
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (FragmentUtils.sDisableFragmentAnimations) {
            Animation a = new Animation() {
            };
            a.setDuration(0);
            return a;
        }
        return super.onCreateAnimation(transit, enter, nextAnim);
    }
}
