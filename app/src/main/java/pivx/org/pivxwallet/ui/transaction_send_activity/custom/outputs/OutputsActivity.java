package pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import org.pivxj.core.Coin;

import java.io.Serializable;
import java.util.List;

import pivx.org.pivxwallet.R;
import global.AddressLabel;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;

/**
 * Created by furszy on 8/4/17.
 */

public class OutputsActivity extends BaseActivity {

    public static final String INTENT_EXTRA_OUTPUTS_WRAPPERS = "output_wrappers";
    public static final String INTENT_EXTRA_OUTPUTS_CLEAR= "clear_outputs";

    private View root;
    private MultipleOutputsFragment multiple_addresses_fragment;
    private TextView txt_add_address;
    private TextView txt_total_amount;

    private List<OutputWrapper> outputWrappers;
    private Coin totalAmount;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle(R.string.multi_send_activity_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        totalAmount = Coin.ZERO;

        root = getLayoutInflater().inflate(R.layout.outputs_main,container);
        multiple_addresses_fragment = (MultipleOutputsFragment) getSupportFragmentManager().findFragmentById(R.id.multiple_addresses_fragment);
        txt_add_address = (TextView) root.findViewById(R.id.txt_add_address);
        txt_total_amount = (TextView) root.findViewById(R.id.txt_total_amount);

        updateTotalAmount();

        txt_add_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                multiple_addresses_fragment.addOutput();
                if(getCurrentFocus()!=null) {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
            }
        });

        Intent intent = getIntent();
        if (intent!=null){
            if (intent.hasExtra(INTENT_EXTRA_OUTPUTS_WRAPPERS)){
                outputWrappers = (List<OutputWrapper>) intent.getSerializableExtra(INTENT_EXTRA_OUTPUTS_WRAPPERS);
                multiple_addresses_fragment.setOutputsWrappers(outputWrappers);
            }
        }

    }

    private void updateTotalAmount() {
        txt_total_amount.setText(totalAmount.toFriendlyString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu_default,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.option_ok){
            try {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                outputWrappers = multiple_addresses_fragment.getList();
                if (outputWrappers.isEmpty()){
                    Toast.makeText(this,R.string.invalid_inputs,Toast.LENGTH_SHORT).show();
                    return true;
                }
                // save addresses labels in db
                saveAddressesLabels(outputWrappers);
                bundle.putSerializable(INTENT_EXTRA_OUTPUTS_WRAPPERS, (Serializable) outputWrappers);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            } catch (InvalidFieldException e) {
                e.printStackTrace();
                DialogsUtil.buildSimpleErrorTextDialog(this,getString(R.string.invalid_inputs),e.getMessage())
                        .show(getFragmentManager(),"invalid_fields_outputs");
            } catch (Exception e){
                e.printStackTrace();
                DialogsUtil.buildSimpleErrorTextDialog(this,getString(R.string.invalid_inputs),e.getMessage())
                        .show(getFragmentManager(),"invalid_fields_outputs");
            }
            return true;
        }else if (id == R.id.option_default){
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_OUTPUTS_CLEAR,true);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAddressesLabels(List<OutputWrapper> outputWrappers){
        for (OutputWrapper outputWrapper : outputWrappers) {
            if (outputWrapper.getAddressLabel()!=null && !outputWrapper.getAddressLabel().equals("")) {
                AddressLabel addressLabel = new AddressLabel(
                        outputWrapper.getAddressLabel()
                );
                addressLabel.addAddress(outputWrapper.getAddress());
                pivxModule.saveContactIfNotExist(
                        addressLabel
                );
            }
        }
    }

    public void setAmount(Coin amount) {
        totalAmount = amount;
        updateTotalAmount();
    }

    public void substractAmount(OutputWrapper outputWrapper) {
        if (outputWrapper.getAmount()!=null) {
            totalAmount = totalAmount.subtract(outputWrapper.getAmount());
            updateTotalAmount();
        }
    }
}
