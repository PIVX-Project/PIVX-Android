package pivx.org.pivxwallet.ui.settings_activity;

import android.os.Bundle;

import pivx.org.pivxwallet.MainActivity;
import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/11/17.
 */

public class SettingsActivity extends MainActivity {

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_settings, frameLayout);
        setTitle("Settings");


    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        navigationView.getMenu().getItem(2).setChecked(true);
    }
}
