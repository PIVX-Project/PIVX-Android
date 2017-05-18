package pivx.org.pivxwallet.ui.settings_backup_activity;

import android.os.Bundle;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 5/18/17.
 */

public class SettingsBackupActivity extends BaseActivity {

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_settings_backup, frameLayout);
        setTitle("Backup Wallet");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }
}
