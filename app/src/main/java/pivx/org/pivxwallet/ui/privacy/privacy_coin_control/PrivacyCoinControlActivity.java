package pivx.org.pivxwallet.ui.privacy.privacy_coin_control;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

public class PrivacyCoinControlActivity extends BaseActivity {
    private static final int MENU_ITEM_ITEM1 = 1;
    private TextView text_balance, text_selected;
    private EditText edit_count_demon1, edit_count_demon5, edit_count_demon10, edit_count_demon50, edit_count_demon100, edit_count_demon500, edit_count_demon1000, edit_count_demon5000;
    private TextView text_demon1, text_demon5, text_demon10, text_demon50, text_demon100, text_demon500, text_demon1000, text_demon5000;
    private LinearLayout layout_demon1, layout_demon5, layout_demon10, layout_demon50, layout_demon100, layout_demon500, layout_demon1000, layout_demon5000;
    private ImageView btn_minus_demon1, btn_minus_demon5, btn_minus_demon10, btn_minus_demon50, btn_minus_demon100, btn_minus_demon500, btn_minus_demon1000, btn_minus_demon5000;
    private ImageView btn_add_demon1, btn_add_demon5, btn_add_demon10, btn_add_demon50, btn_add_demon100, btn_add_demon500, btn_add_demon1000, btn_add_demon5000;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.activity_zercoin_control, container);
        setTitle(R.string.coin_control);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.darkPurple));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.darkPurple)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ITEM_ITEM1, Menu.NONE, R.string.save).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ITEM_ITEM1:
                return true;

            default:
                return false;
        }
    }
}
