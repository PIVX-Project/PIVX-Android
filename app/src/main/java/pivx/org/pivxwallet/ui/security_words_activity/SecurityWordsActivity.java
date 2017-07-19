package pivx.org.pivxwallet.ui.security_words_activity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 7/17/17.
 */

public class SecurityWordsActivity extends BaseActivity {
    private TextView text_words;
    private ImageButton btn_show;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.security_words_show, container);
        setTitle("Security Words");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        text_words = (TextView) findViewById(R.id.text_words);
        btn_show = (ImageButton) findViewById(R.id.btn_show);
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Words will show for 45 seconds!", Toast.LENGTH_LONG).show();
                btn_show.setEnabled(false);
                btn_show.setImageResource(R.drawable.ic_show_words_disable);
            }
        });
    }

}
