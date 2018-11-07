package pivx.org.pivxwallet.ui.privacy.privacy_coin_control;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
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

import com.zerocoinj.core.CoinDenomination;
import com.zerocoinj.core.ZCoin;

import org.pivxj.core.Coin;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutput;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

public class PrivacyCoinControlActivity extends BaseActivity {
    private static final int MENU_ITEM_ITEM1 = 1;

    private TextView text_balance, text_selected;
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
        text_selected = (TextView) root.findViewById(R.id.text_selected);
        updateBalance(pivxModule.getZpivAvailableBalanceCoin());

        ((BaseRecyclerFragment)getSupportFragmentManager().findFragmentById(R.id.privacy_coin_control)).getRecycler().addItemDecoration(new SeparatorDecoration(this, Color.BLACK, 1));
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


    private static class SelectZCoinWrapper{
        ZCoin zCoin;
        boolean isSelected;

        public SelectZCoinWrapper(ZCoin zCoin, boolean isSelected) {
            this.zCoin = zCoin;
            this.isSelected = isSelected;
        }
    }

    public static class CoinControlFragment extends BaseRecyclerFragment<SelectZCoinWrapper>{

        @Override
        protected List<SelectZCoinWrapper> onLoading() {
            List<ZCoin> list = new ArrayList<>();
            for (Map.Entry<CoinDenomination, HashSet<ZCoin>> entry : pivxModule.getAllMintedZCoins().entrySet()) {
                list.addAll(entry.getValue());
            }
            List<SelectZCoinWrapper> list2 = new ArrayList<>();
            for (ZCoin zCoin : list) {
                list2.add(new SelectZCoinWrapper(zCoin,false));
            }
            return list2;
        }

        @Override
        protected BaseRecyclerAdapter<SelectZCoinWrapper, Holder> initAdapter() {
            return new BaseRecyclerAdapter<SelectZCoinWrapper, Holder>(getActivity()) {
                @Override
                protected Holder createHolder(View itemView, int type) {
                    return new Holder(itemView,type);
                }

                @Override
                protected int getCardViewResource(int type) {
                    return R.layout.zerocoin_coin_control_row2;//zerocoin_coin_control_row;
                }

                @Override
                protected void bindHolder(Holder holder, SelectZCoinWrapper data, int position) {
                    holder.txt_id.setText("Comm: " + data.zCoin.getCommitment().getCommitmentValue().toString(16));
                    holder.txt_date.setText(new SimpleDateFormat("dd/MM/yyyy").format(pivxModule.getTx(data.zCoin.getParentTxId()).getUpdateTime()));
                    holder.txt_demon.setText("Denom: " + String.valueOf(data.zCoin.getCoinDenomination().getDenomination()));
                    holder.txt_minted_height.setText("Minted height: " + data.zCoin.getHeight());
                    holder.itemView.setOnClickListener(view -> {
                        data.isSelected = !data.isSelected;
                        view.setBackgroundColor(data.isSelected ? getResources().getColor(R.color.lightPurple) : Color.WHITE);
                    });
                }
            };
        }
    }

    private static class Holder extends BaseRecyclerViewHolder{

        private TextView txt_id, txt_minted_height, txt_demon, txt_date;

        protected Holder(View itemView, int holderType) {
            super(itemView, holderType);
            txt_id = (TextView) itemView.findViewById(R.id.txt_id);
            txt_minted_height = (TextView) itemView.findViewById(R.id.txt_minted_height);
            txt_demon = (TextView) itemView.findViewById(R.id.txt_demon);
            txt_date = (TextView) itemView.findViewById(R.id.txt_date);
        }
    }

    public class SeparatorDecoration extends RecyclerView.ItemDecoration {

        private final Paint mPaint;

        /**
         * Create a decoration that draws a line in the given color and width between the items in the view.
         *
         * @param context  a context to access the resources.
         * @param color    the color of the separator to draw.
         * @param heightDp the height of the separator in dp.
         */
        public SeparatorDecoration(@NonNull Context context, @ColorInt int color,
                                   @FloatRange(from = 0, fromInclusive = false) float heightDp) {
            mPaint = new Paint();
            mPaint.setColor(color);
            final float thickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    heightDp, context.getResources().getDisplayMetrics());
            mPaint.setStrokeWidth(thickness);
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

            // we want to retrieve the position in the list
            final int position = params.getViewAdapterPosition();

            // and add a separator to any view but the last one
            if (position < state.getItemCount()) {
                outRect.set(0, 0, 0, (int) mPaint.getStrokeWidth()); // left, top, right, bottom
            } else {
                outRect.setEmpty(); // 0, 0, 0, 0
            }
        }

        @Override
        public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
            // we set the stroke width before, so as to correctly draw the line we have to offset by width / 2
            final int offset = (int) (mPaint.getStrokeWidth() / 2);

            // this will iterate over every visible view
            for (int i = 0; i < parent.getChildCount(); i++) {
                // get the view
                final View view = parent.getChildAt(i);
                final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) view.getLayoutParams();

                // get the position
                final int position = params.getViewAdapterPosition();

                // and finally draw the separator
                if (position < state.getItemCount()) {
                    c.drawLine(view.getLeft(), view.getBottom() + offset, view.getRight(), view.getBottom() + offset, mPaint);
                }
            }
        }
    }

}
