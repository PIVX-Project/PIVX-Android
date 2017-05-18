package pivx.org.pivxwallet.ui.transaction_request_activity;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 5/11/17.
 */

public class RequestActivity extends BaseActivity{
    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_transaction_request, frameLayout);
        setTitle("Request");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }
}
