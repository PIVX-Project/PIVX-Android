package pivx.org.pivxwallet.ui.address_add_activity;

import android.os.Bundle;
import android.view.ViewGroup;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 6/8/17.
 */

public class AddressAddActivity extends BaseActivity {
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_new_address, container);
        setTitle("New Address");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }
}
