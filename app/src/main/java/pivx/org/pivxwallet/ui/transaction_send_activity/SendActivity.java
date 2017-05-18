package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 5/4/17.
 */

public class SendActivity extends BaseActivity {
    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_transaction_send, frameLayout);
        setTitle("Send");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }
}
