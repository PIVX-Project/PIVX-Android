package pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.bitcoinj.core.Coin;

import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

import static pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity.INTENT_EXTRA_TOTAL_AMOUNT;

/**
 * Created by furszy on 8/4/17.
 */

public class OutputsActivity extends BaseActivity {

    public static final String INTENT_EXTRA_OUTPUTS_WRAPPERS = "output_wrappers";

    private View root;
    private MultipleOutputsFragment multiple_addresses_fragment;
    private TextView txt_add_address;
    private TextView txt_total_amount;

    private List<OutputWrapper> outputWrappers;

    private Coin totalAmount;
    private Coin selectedAmount;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
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
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.option_ok){
            Intent intent = new Intent();
            // todo: complete
            //intent.putExtra(INTENT_EXTRA_OUTPUTS_WRAPPERS,outputWrappers);
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
