package pivx.org.pivxwallet.ui.privacy.privacy_convert;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.backup_mnemonic_activity.MnemonicActivity;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.privacy.privacy_coin_control.PrivacyCoinControlActivity;

public class ConvertActivity extends BaseActivity {
    private FrameLayout header_container;
    private LinearLayout layout_blocked;
    private RadioGroup radio_convert_type;
    private RelativeLayout bg_balance;
    private RadioButton radio_zpiv, radio_piv;
    private Button btn_convert;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.activity_convert, container);

        header_container = (FrameLayout) findViewById(R.id.header_container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(R.string.convert_zpiv);
        View headerView = getLayoutInflater().inflate(R.layout.fragment_pivx_amount, header_container);

        header_container.setVisibility(View.VISIBLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.darkPurple));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkPurple)));

        bg_balance = (RelativeLayout) headerView.findViewById(R.id.bg_balance);
        bg_balance.setBackgroundColor(ContextCompat.getColor(this, R.color.darkPurple));
        layout_blocked = (LinearLayout) headerView.findViewById(R.id.layout_blocked);
        layout_blocked.setVisibility(View.GONE);
        // Convert Selection

        radio_convert_type = (RadioGroup) findViewById(R.id.radio_convert_type);

        radio_zpiv = (RadioButton) findViewById(R.id.radio_zpiv);
        radio_piv = (RadioButton) findViewById(R.id.radio_piv);

        radio_piv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                        .getColor(R.color.bgPurple)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.bgPurple));
                }
                setTitle(R.string.convert_piv);
                bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.bgPurple));
                btn_convert.setBackgroundResource(R.drawable.bg_button_border);
                btn_convert.setText(R.string.convert_piv);
                btn_convert.setTextColor(getResources().getColor(R.color.mainText));

            }
        });

        radio_zpiv.isChecked();

        radio_zpiv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources()
                        .getColor(R.color.darkPurple)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Window window = getWindow();
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    window.setStatusBarColor(getResources().getColor(R.color.darkPurple));
                }
                setTitle(R.string.convert_zpiv);
                bg_balance.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.darkPurple));
                btn_convert.setBackgroundResource(R.drawable.bg_button_purple);
                btn_convert.setTextColor(getResources().getColor(R.color.white));
                btn_convert.setText(R.string.convert_zpiv);

            }
        });
/*
        radio_zpiv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radio_zpiv.setTextColor(getResources().getColor(R.color.mainText));
                    radio_piv.setTextColor(getResources().getColor(R.color.white));
                } else {
                    radio_zpiv.setTextColor(getResources().getColor(R.color.white));
                    radio_piv.setTextColor(getResources().getColor(R.color.mainText));
                }
            }
        });

        radio_piv.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    radio_piv.setTextColor(getResources().getColor(R.color.mainText));
                } else {
                    radio_piv.setTextColor(getResources().getColor(R.color.white));
                }
            }
        });
*/

        // Convert

        btn_convert = (Button) findViewById(R.id.btn_convert);
        btn_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0,0,0, R.string.coin_control);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case 0:
                Intent myIntent = new Intent(getApplicationContext(), PrivacyCoinControlActivity.class);
                startActivity(myIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
