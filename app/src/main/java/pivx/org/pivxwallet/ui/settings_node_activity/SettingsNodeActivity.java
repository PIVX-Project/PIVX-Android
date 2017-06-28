package pivx.org.pivxwallet.ui.settings_node_activity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 6/27/17.
 */

public class SettingsNodeActivity extends BaseActivity  {
    Button btnSelectNode;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_start_node, container);
        setTitle("Node preferences");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


}
