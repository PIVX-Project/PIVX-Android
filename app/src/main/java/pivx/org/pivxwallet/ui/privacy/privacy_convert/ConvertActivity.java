package pivx.org.pivxwallet.ui.privacy.privacy_convert;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.pivxj.core.Address;
import org.pivxj.core.Coin;
import org.pivxj.core.InsufficientMoneyException;
import org.pivxj.core.Transaction;
import org.pivxj.wallet.SendRequest;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

import global.PivxModuleImp;
import global.PivxRate;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.service.PivxWalletService;
import pivx.org.pivxwallet.ui.backup_mnemonic_activity.MnemonicActivity;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.privacy.privacy_coin_control.PrivacyCoinControlActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;

import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_BROADCAST_TRANSACTION;
import static pivx.org.pivxwallet.service.IntentsConstants.DATA_TRANSACTION_HASH;

public class ConvertActivity extends BaseActivity {
    private FrameLayout header_container;
    private LinearLayout layout_blocked;
    private RadioGroup radio_convert_type;
    private RelativeLayout bg_balance;
    private RadioButton radio_zpiv, radio_piv;
    private Button btn_convert;
    private EditText edit_amount;
    private TextView txt_amount_local;

    // header
    private TextView txt_value, text_value_bottom, text_value_bottom_local, txt_local_total, txt_unnavailable, txt_local_currency, txt_watch_only;

    private PivxRate pivxRate;

    // Convert tx.
    private Transaction transaction;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.convert_zpiv);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.darkPurple));
        }
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.darkPurple)));

        View view = getLayoutInflater().inflate(R.layout.activity_convert, container);
        header_container = (FrameLayout) view.findViewById(R.id.header_container);
        header_container.setVisibility(View.VISIBLE);
        View headerView = getLayoutInflater().inflate(R.layout.fragment_pivx_amount, header_container);

        edit_amount = (EditText) view.findViewById(R.id.edit_amount);
        txt_amount_local = (TextView) view.findViewById(R.id.txt_amount_local);
        bg_balance = (RelativeLayout) headerView.findViewById(R.id.bg_balance);
        bg_balance.setBackgroundColor(ContextCompat.getColor(this, R.color.darkPurple));
        layout_blocked = (LinearLayout) headerView.findViewById(R.id.layout_blocked);
        layout_blocked.setVisibility(View.GONE);

        // Convert Selection
        radio_convert_type = (RadioGroup) findViewById(R.id.radio_convert_type);

        radio_zpiv = (RadioButton) findViewById(R.id.radio_zpiv);
        radio_piv = (RadioButton) findViewById(R.id.radio_piv);

        radio_piv.setOnClickListener(v -> {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                    .getColor(R.color.bgPurple)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.bgPurple));
            }
            setTitle(R.string.convert_piv);
            bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.bgPurple));
            btn_convert.setBackgroundResource(R.drawable.bg_button_border);
            btn_convert.setText(R.string.convert_piv);
            btn_convert.setTextColor(getResources().getColor(R.color.mainText));
        });

        radio_zpiv.setChecked(true);

        radio_zpiv.setOnClickListener(v -> {
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                    .getColor(R.color.darkPurple)));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.darkPurple));
            }
            setTitle(R.string.convert_zpiv);
            bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.darkPurple));
            btn_convert.setBackgroundResource(R.drawable.bg_button_purple);
            btn_convert.setTextColor(getResources().getColor(R.color.white));
            btn_convert.setText(R.string.convert_zpiv);

        });

        // Convert
        btn_convert = (Button) findViewById(R.id.btn_convert);
        btn_convert.setOnClickListener(v -> {
            String mint = edit_amount.getText().toString();
            if (mint.length() < 1){
                Toast.makeText(v.getContext(), R.string.invalid_amount_value, Toast.LENGTH_SHORT).show();
                return;
            }
            try {

                Coin coin = Coin.parseCoin(mint);

                // Check if it's a mint or a spend to re convert them to PIV
                if (radio_piv.isChecked()){
                    // Spend
                    reconvertProcess(coin);
                }else {
                    // Mint
                    mintProcess(coin);
                }
            } catch (InsufficientMoneyException e) {
                Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                LoggerFactory.getLogger(ConvertActivity.class).error("Exception on mint/convert", e);
                Toast.makeText(v.getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Header section
        text_value_bottom =  (TextView) findViewById(R.id.text_value_bottom);
        text_value_bottom_local =  (TextView) findViewById(R.id.text_value_bottom_local);
        txt_local_total = (TextView) header_container.findViewById(R.id.txt_local_total);
        txt_value = (TextView) headerView.findViewById(R.id.pivValue);
        txt_unnavailable = (TextView) headerView.findViewById(R.id.txt_unnavailable);
        txt_local_currency = (TextView) headerView.findViewById(R.id.txt_local_currency);
        txt_watch_only = (TextView) headerView.findViewById(R.id.txt_watch_only);
        bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.darkPurple));

        updateBalance();
    }

    private void reconvertProcess(Coin coin) throws InsufficientMoneyException {
        // Internal address to receive the PIV
        Address address = pivxModule.getReceiveAddress();
        SendRequest sendRequest = pivxModule.createSpend(address, coin);
        SimpleTwoButtonsDialog simpleTwoButtonsDialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                this,
                "zPIV Spend",
                String.format("You are just about to convert %s zPIV to PIV again\n\nThis process will take a while, please be patient", coin.toPlainString()),
                new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                    @Override
                    public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                        transaction = sendRequest.tx;
                        connectToService();
                        clearFields();
                        Toast.makeText(ConvertActivity.this,"Starting the spend process..", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
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
    }

    private void clearFields() {
        edit_amount.setText("");
    }

    private void mintProcess(Coin coin) throws InsufficientMoneyException {
        final SendRequest sendRequest = pivxModule.createMint(coin);
        DialogsUtil.buildSimpleTwoBtnsDialog(
                this,
                "Mint process",
                String.format("You are just about to convert %s to zPIV", coin.toFriendlyString()),
                new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                    @Override
                    public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                        new Thread(() -> {
                            String message;
                            try {
                                Transaction tx = sendRequest.tx;
                                pivxModule.commitTx(tx);
                                Intent intent = new Intent(ConvertActivity.this, PivxWalletService.class);
                                intent.setAction(ACTION_BROADCAST_TRANSACTION);
                                intent.putExtra(DATA_TRANSACTION_HASH, tx.getHash().getBytes());
                                startService(intent);
                                message = "Converting";
                            }catch (Exception e){
                                e.printStackTrace();
                                message = e.getMessage();
                            }
                            String finalMessage = message;
                            runOnUiThread(() -> {
                                Toast.makeText(ConvertActivity.this, finalMessage, Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            });
                        }).start();
                        edit_amount.setText("");
                        dialog.dismiss();
                    }

                    @Override
                    public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                        dialog.dismiss();
                    }
                }
        )
                .setImgAlertRes(R.drawable.ic_zero_coin)
                .setRightBtnTextColor(ContextCompat.getColor(this,R.color.white))
                .setLeftBtnTextColor(ContextCompat.getColor(this, R.color.white))
                .setContainerBtnsBackgroundColor(ContextCompat.getColor(this,R.color.bgPurple))
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0, R.string.coin_control);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                Intent myIntent = new Intent(getApplicationContext(), PrivacyCoinControlActivity.class);
                startActivity(myIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }







    private void updateBalance() {
        Coin availableBalance = pivxModule.getAvailableBalanceCoin();
        Coin unnavailableBalance = pivxModule.getUnnavailableBalanceCoin();
        Coin zAvailableBalance = pivxModule.getZpivAvailableBalanceCoin();
        Coin zUnspendable = pivxModule.getZpivUnnavailableBalanceCoin();
        updateBalanceViews(
                zAvailableBalance,
                zUnspendable,
                "zPIV",
                availableBalance,
                unnavailableBalance,
                "PIV"
        );

        Coin sum = availableBalance.add(zAvailableBalance);
        if (pivxRate != null) {
            txt_local_total.setText(
                    pivxApplication.getCentralFormats().format(
                            new BigDecimal(sum.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + pivxRate.getCode()
            );
        } else {
            txt_local_total.setText("0.00 USD");
        }
    }

    private void updateBalanceViews(Coin topBalance,Coin topUnspendableBalance, String topDen, Coin bottomBalance, Coin bottomUnspendable, String bottomDen){
        txt_value.setText(!topBalance.isZero() ? topBalance.toPlainString() + " " + topDen : "0 " + topDen);
        txt_unnavailable.setText(!topUnspendableBalance.isZero() ? topUnspendableBalance.toPlainString() + " " + topDen : "0 " + topDen);

        text_value_bottom.setText(!bottomBalance.isZero() ? bottomBalance.toPlainString() + " " + bottomDen : "0 " + bottomDen);

        if (pivxRate == null)
            pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());
        if (pivxRate != null) {
            txt_local_currency.setText(
                    pivxApplication.getCentralFormats().format(
                            new BigDecimal(topBalance.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + pivxRate.getCode()
            );
            text_value_bottom_local.setText(
                    pivxApplication.getCentralFormats().format(
                            new BigDecimal(bottomBalance.getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + pivxRate.getCode()
            );
        } else {
            txt_local_currency.setText("0");
        }
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
                transaction = null;
                msg = "Sending transaction..";
            } catch (Exception e){
                e.printStackTrace();
                msg = "Cannot Spend coins, " + e.getMessage();
            }
            String finalMsg = msg;
            runOnUiThread(() -> {
                Toast.makeText(ConvertActivity.this, finalMsg, Toast.LENGTH_SHORT).show();
                disconnectFromService();
                new Handler().postDelayed(ConvertActivity.this::onBackPressed,4000);
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
