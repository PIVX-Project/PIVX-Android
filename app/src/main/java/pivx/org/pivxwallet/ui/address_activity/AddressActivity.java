package pivx.org.pivxwallet.ui.address_activity;

import android.os.Bundle;
import android.view.ViewGroup;

import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/11/17.
 */

public class AddressActivity extends BaseDrawerActivity {

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_address, container);
        setTitle("Address Book");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check current activity in the navigation drawer
        setNavigationMenuItemChecked(1);
    }
}
