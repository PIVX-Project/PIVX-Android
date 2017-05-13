package pivx.org.pivxwallet.ui.address_activity;

import android.os.Bundle;

import pivx.org.pivxwallet.MainActivity;
import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/11/17.
 */

public class AddressActivity extends MainActivity {

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_address, frameLayout);
        setTitle("Address Book");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        navigationView.getMenu().getItem(1).setChecked(true);
    }
}
