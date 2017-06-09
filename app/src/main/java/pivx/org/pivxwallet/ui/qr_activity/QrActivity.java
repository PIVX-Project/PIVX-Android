package pivx.org.pivxwallet.ui.qr_activity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by furszy on 6/8/17.
 */

public class QrActivity extends BaseActivity {

    private View root;


    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        super.onCreateView(savedInstanceState, container);
        getSupportActionBar().hide();
        root = getLayoutInflater().inflate(R.layout.qr_activity,container,true);

    }

}
