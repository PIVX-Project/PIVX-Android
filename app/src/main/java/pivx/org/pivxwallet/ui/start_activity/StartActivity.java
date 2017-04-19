package pivx.org.pivxwallet.ui.start_activity;

import android.os.Bundle;

import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by mati on 18/04/17.
 */

public class StartActivity extends BaseActivity {


    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);

        // example..
        pivxModule.createWallet();

    }
}
