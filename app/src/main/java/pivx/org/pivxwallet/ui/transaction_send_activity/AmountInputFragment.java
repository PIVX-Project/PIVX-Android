package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.pivxj.core.Coin;

import java.math.BigDecimal;
import java.math.RoundingMode;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.ui.base.BaseFragment;

/**
 * Created by furszy on 2/9/18.
 */

public class AmountInputFragment extends BaseFragment implements View.OnClickListener {

    private View root;

    private EditText edit_amount, editCurrency;
    private TextView txt_currency_amount, txtShowPiv,txt_local_currency;
    private ImageButton btnSwap;
    private ViewFlipper amountSwap;
    private PivxRate pivxRate;
    private boolean inPivs = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.amount_input,container,false);
        edit_amount = (EditText) root.findViewById(R.id.edit_amount);
        //Sending amount currency
        editCurrency = (EditText) root.findViewById(R.id.edit_amount_currency);
        txt_currency_amount = (TextView) root.findViewById(R.id.txt_currency_amount);
        txt_local_currency = (TextView) root.findViewById(R.id.txt_local_currency);
        txtShowPiv = (TextView) root.findViewById(R.id.txt_show_piv) ;
        //Swap type of ammounts
        amountSwap = (ViewFlipper) root.findViewById( R.id.viewFlipper );
        amountSwap.setInAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_in_left));
        amountSwap.setOutAnimation(AnimationUtils.loadAnimation(getActivity(),
                android.R.anim.slide_out_right));
        btnSwap = (ImageButton) root.findViewById(R.id.btn_swap);
        btnSwap.setOnClickListener(this);

        pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());

        if (pivxRate != null)
            txt_local_currency.setText("0 " + pivxRate.getCode());
        else
            txt_local_currency.setText("0");

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
                if (s.length()>0) {
                    if (pivxRate != null) {
                        String valueStr = s.toString();
                        if (valueStr.charAt(0) == '.') {
                            valueStr = "0" + valueStr;
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
            }
        });

        return root;
    }

    public String getAmountStr() throws Exception {
        if (edit_amount == null && editCurrency == null){
            throw new Exception("Fragment is not attached");
        }
        String amountStr = "0";
        if (inPivs) {
            amountStr = edit_amount.getText().toString();
        }else {
            String valueStr = editCurrency.getText().toString();
            if(valueStr.length()>0) {
                if (valueStr.charAt(0) == '.') {
                    valueStr = "0" + valueStr;
                }
                BigDecimal result = new BigDecimal(valueStr).multiply(pivxRate.getRate());
                amountStr = result.setScale(6, RoundingMode.FLOOR).toPlainString();
            }
        }
        return amountStr;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.btn_swap) {
            inPivs = !inPivs;
            amountSwap.showNext();
        }
    }
}
