package pivx.org.pivxwallet.ui.security_words_activity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 7/17/17.
 */

public class SecurityWordsActivity extends BaseActivity {
    //private TextView text_words;
    //private Button btn_secure;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.security_words_show, container);
        setTitle("Security Words");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //text_words = (TextView) findViewById(R.id.text_words);
        //btn_secure = (Button) findViewById(R.id.btn_secure);
    }
}
