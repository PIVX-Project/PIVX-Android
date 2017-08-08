package pivx.org.pivxwallet.ui.security_words_activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 7/17/17.
 */

public class MnemonicActivity extends BaseActivity {
    private FlexboxLayout txt_words;
    private ImageButton btn_show;
    private int margin = 100;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.security_words_show, container);
        setTitle("Mnemonic code");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        btn_show = (ImageButton) findViewById(R.id.btn_show);
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Words will show for 45 seconds!", Toast.LENGTH_LONG).show();
                btn_show.setEnabled(false);
                btn_show.setImageResource(R.drawable.ic_show_words_disable);
            }
        });



        List<String> textArray = pivxModule.getMnemonic();
        txt_words = ( FlexboxLayout ) findViewById(R.id.securityWords);

        for (String word : textArray) {
            TextView textView = new TextView(this);
            FlexboxLayout.LayoutParams llp = new FlexboxLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(0, 40, 20, 0);
            textView.setLayoutParams(llp);
            textView.setTextColor(Color.BLACK);
            textView.setBackgroundResource(R.drawable.bg_button_grey);
            textView.setPadding(10,8,10,8);

            textView.setText(word);
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
