package pivx.org.pivxwallet.ui.backup_mnemonic_activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.Arrays;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.loading.LoadingActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 7/17/17.
 */

public class MnemonicActivity extends BaseActivity {

    public static final String INTENT_EXTRA_INIT_VIEW = "init_view";

    private FlexboxLayout txt_words;
    private Button btn_continue;
    private int margin = 100;
    private boolean isInit = false;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        isInit = getIntent().hasExtra(INTENT_EXTRA_INIT_VIEW);

        if (!isInit) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        getLayoutInflater().inflate(R.layout.security_words_show, container);
        setTitle(R.string.title_mnemonic_code);

        if (!isInit){
            findViewById(R.id.btn_continue).setVisibility(View.GONE);
        }else {
            btn_continue = (Button) findViewById(R.id.btn_continue);
            btn_continue.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), WalletActivity.class);
                startActivity(intent);
                finish();
            });
        }

        List<String> textArray = pivxModule.getMnemonic();

        txt_words = (FlexboxLayout) findViewById(R.id.securityWords);

        for (String word : textArray) {
            TextView textView = new TextView(this);
            FlexboxLayout.LayoutParams llp = new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 40, 20, 0);
            textView.setLayoutParams(llp);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.bg_inputs_primary);
            textView.setPadding(18,8,18,8);
            textView.setText(word);
            textView.setTextSize(15);
            txt_words.addView(textView);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        /*MenuItem menuItem = menu.add(0,0,0, R.string.share);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*switch (item.getItemId()) {
            case 0:

                return true;
        }*/
        return super.onOptionsItemSelected(item);
    }

}
