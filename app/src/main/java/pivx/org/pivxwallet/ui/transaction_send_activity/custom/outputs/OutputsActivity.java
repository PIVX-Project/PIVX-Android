package pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import org.bitcoinj.core.Coin;

import java.io.Serializable;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;

import static pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity.INTENT_EXTRA_TOTAL_AMOUNT;

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
    private Coin selectedAmount;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle("Address list");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        totalAmount = Coin.parseCoin(getIntent().getStringExtra(INTENT_EXTRA_TOTAL_AMOUNT));

        root = getLayoutInflater().inflate(R.layout.outputs_main,container);
        multiple_addresses_fragment = (MultipleOutputsFragment) getSupportFragmentManager().findFragmentById(R.id.multiple_addresses_fragment);
        txt_add_address = (TextView) root.findViewById(R.id.txt_add_address);
        txt_total_amount = (TextView) root.findViewById(R.id.txt_total_amount);

        txt_total_amount.setText(totalAmount.toFriendlyString());

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
                bundle.putSerializable(INTENT_EXTRA_OUTPUTS_WRAPPERS, (Serializable) outputWrappers);
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                finish();
            } catch (InvalidFieldException e) {
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
}
