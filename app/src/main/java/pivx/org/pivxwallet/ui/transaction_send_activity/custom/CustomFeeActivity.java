package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;

import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_CLEAR;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_FEE_PER_KB;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_MINIMUM_FEE;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.CustomFeeFragment.INTENT_EXTRA_IS_TOTAL_FEE;

/**
 * Created by furszy on 8/3/17.
 */

public class CustomFeeActivity extends BaseActivity implements View.OnClickListener {

    private View root;
    private TextView txt_default;
    private CustomFeeFragment customFeeFragment;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.custom_fee_main, container);
        setTitle("Custom fee");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txt_default = (TextView) root.findViewById(R.id.txt_default);
        customFeeFragment = (CustomFeeFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_custom_fee);

        txt_default.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.option_ok){
            try {
                Intent intent = new Intent();
                CustomFeeFragment.FeeSelector feeSelector = customFeeFragment.getFee();
                intent.putExtra(INTENT_EXTRA_IS_FEE_PER_KB,feeSelector.isFeePerKbSelected());
                intent.putExtra(INTENT_EXTRA_IS_TOTAL_FEE,!feeSelector.isFeePerKbSelected());
                intent.putExtra(INTENT_EXTRA_IS_MINIMUM_FEE,feeSelector.isPayMinimum());
                intent.putExtra(INTENT_EXTRA_FEE,feeSelector.getAmunt());
                setResult(RESULT_OK,intent);
                finish();
            } catch (InvalidFeeException e) {
                e.printStackTrace();
                DialogsUtil.buildSimpleErrorTextDialog(this,getString(R.string.invalid_inputs),e.getMessage()).show(getFragmentManager(),"custom_fee_invalid_inputs");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_default){
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_CLEAR,true);
            setResult(RESULT_OK,intent);
            finish();
        }

    }

}
