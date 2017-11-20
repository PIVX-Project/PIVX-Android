package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import org.pivxj.core.Coin;
import org.pivxj.core.Transaction;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseFragment;

/**
 * Created by furszy on 8/3/17.
 */

public class CustomFeeFragment extends BaseFragment {

    public static String INTENT_EXTRA_CLEAR = "clear_custom_fee";
    public static String INTENT_EXTRA_IS_FEE_PER_KB = "is_fee_per_kb";
    public static String INTENT_EXTRA_IS_TOTAL_FEE = "is_total_fee";
    public static String INTENT_EXTRA_IS_MINIMUM_FEE = "is_minimum_fee";
    public static String INTENT_EXTRA_FEE = "fee";

    private View root;
    private EditText edit_fee;
    private CheckBox check_pay_minimum;
    private RadioGroup radio_group;
    private RadioButton radio_per_kb;
    private RadioButton radio_total_at_least;
    private TextView txt_fee_explanation;

    private boolean isFeePerKbSelected = true;
    private boolean payMinimum = false;

    private Coin fee = Transaction.DEFAULT_TX_FEE;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Intent intent = getActivity().getIntent();
        if (intent!=null){
            if (intent.hasExtra(INTENT_EXTRA_IS_FEE_PER_KB)){
                isFeePerKbSelected = intent.getBooleanExtra(INTENT_EXTRA_IS_FEE_PER_KB,true);
            }
            if (intent.hasExtra(INTENT_EXTRA_IS_TOTAL_FEE)){
                isFeePerKbSelected = intent.getBooleanExtra(INTENT_EXTRA_IS_TOTAL_FEE,false);
            }
            if (intent.hasExtra(INTENT_EXTRA_IS_MINIMUM_FEE)){
                payMinimum = intent.getBooleanExtra(INTENT_EXTRA_IS_MINIMUM_FEE,false);
            }
            if (intent.hasExtra(INTENT_EXTRA_FEE)) {
                fee = (Coin) intent.getSerializableExtra(INTENT_EXTRA_FEE);
            }
        }

        root = inflater.inflate(R.layout.custom_fee_fragment,container,false);
        edit_fee = (EditText) root.findViewById(R.id.edit_fee);
        check_pay_minimum = (CheckBox) root.findViewById(R.id.check_pay_minimum);
        radio_group = (RadioGroup) root.findViewById(R.id.radio_group);
        radio_per_kb = (RadioButton) root.findViewById(R.id.radio_per_kb);
        radio_total_at_least = (RadioButton) root.findViewById(R.id.radio_total_at_least);
        radio_per_kb.setChecked(true);
        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.radio_total_at_least){
                    isFeePerKbSelected = false;
                }else {
                    isFeePerKbSelected = true;
                }
            }
        });
        check_pay_minimum.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                payMinimum = isChecked;
                edit_fee.setEnabled(!isChecked);
                radio_group.setEnabled(!isChecked);
                radio_per_kb.setEnabled(!isChecked);
                radio_total_at_least.setEnabled(!isChecked);
            }
        });
        setDefaultValues();
        return root;
    }

    public void setDefaultValues() {
        edit_fee.setText(fee.toPlainString());
        if (isFeePerKbSelected){
            radio_per_kb.setChecked(true);
        }else {
            radio_total_at_least.setChecked(true);
        }
        check_pay_minimum.setChecked(payMinimum);
        if (payMinimum){
            edit_fee.setEnabled(!payMinimum);
            radio_group.setEnabled(!payMinimum);
            radio_per_kb.setEnabled(!payMinimum);
            radio_total_at_least.setEnabled(!payMinimum);
        }
    }

    public void clearValues() {
        fee = Transaction.DEFAULT_TX_FEE;
        isFeePerKbSelected = true;
        payMinimum = false;
        setDefaultValues();
    }

    public FeeSelector getFee() throws InvalidFeeException {
        if (!payMinimum){
            String feeStr = edit_fee.getText().toString();
            if (feeStr.length()>0){
                Coin fee = Coin.parseCoin(feeStr);
                if (fee.getValue()==Coin.ZERO.getValue()){
                    throw new InvalidFeeException(getString(R.string.invalid_fee_amount));
                }
                return new FeeSelector(isFeePerKbSelected,fee,payMinimum);
            }else {
                throw new InvalidFeeException(getString(R.string.invalid_fee_amount));
            }
        }else {
            return new FeeSelector(isFeePerKbSelected,fee,payMinimum);
        }
    }

    public static class FeeSelector{

        private boolean isFeePerKbSelected;
        private Coin amount;
        private boolean payMinimum = false;

        public FeeSelector(boolean isFeePerKbSelected, Coin amount, boolean payMinimum) {
            this.isFeePerKbSelected = isFeePerKbSelected;
            this.amount = amount;
            this.payMinimum = payMinimum;
        }

        public boolean isFeePerKbSelected() {
            return isFeePerKbSelected;
        }

        public Coin getAmount() {
            return amount;
        }

        public boolean isPayMinimum() {
            return payMinimum;
        }
    }
}
