package pivx.org.pivxwallet.ui.privacy.privacy_coin_control;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.pivxj.core.Coin;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutput;

import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

public class PrivacyCoinControlActivity extends BaseActivity {
    private static final int MENU_ITEM_ITEM1 = 1;

    private TextView text_balance;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        View root = getLayoutInflater().inflate(R.layout.activity_zercoin_control, container);
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

        text_balance = (TextView) root.findViewById(R.id.text_balance);
        updateBalance(pivxModule.getZpivAvailableBalanceCoin());
    }

    public void updateBalance(Coin totalBalance){
        text_balance.setText(totalBalance.toPlainString() + " zPIV");
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

    public static class ZunspentWrapper{

        private TransactionOutput output;

    }


    public static class CoinControlFragment extends BaseRecyclerFragment<TransactionOutput>{

        @Override
        protected List<TransactionOutput> onLoading() {
            return pivxModule.listZpivUnspents();
        }

        @Override
        protected BaseRecyclerAdapter<TransactionOutput, ? extends BaseRecyclerViewHolder> initAdapter() {
            return new BaseRecyclerAdapter<TransactionOutput, BaseRecyclerViewHolder>(getActivity()) {
                @Override
                protected BaseRecyclerViewHolder createHolder(View itemView, int type) {
                    return null;
                }

                @Override
                protected int getCardViewResource(int type) {
                    return R.layout.zerocoin_coin_control_row;
                }

                @Override
                protected void bindHolder(BaseRecyclerViewHolder holder, TransactionOutput data, int position) {

                }
            };
        }
    }
}
