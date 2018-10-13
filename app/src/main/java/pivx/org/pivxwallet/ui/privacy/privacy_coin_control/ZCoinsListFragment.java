package pivx.org.pivxwallet.ui.privacy.privacy_coin_control;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zerocoinj.core.CoinDenomination;

import org.pivxj.core.TransactionOutput;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import host.furszy.zerocoinj.wallet.AmountPerDen;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class ZCoinsListFragment extends BaseRecyclerFragment<AmountPerDen>{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setSwipeRefresh(false);
        View view = super.onCreateView(inflater, container, savedInstanceState);
        getRecycler().getLayoutParams().height = WRAP_CONTENT;
        getRecycler().setHasFixedSize(true);
        return view;
    }

    @Override
    protected List<AmountPerDen> onLoading() {
        List<AmountPerDen> amountPerDens = pivxModule.listAmountPerDen();
        Collections.sort(amountPerDens, (o1, o2) -> {
            CoinDenomination first = o1.getDen();
            CoinDenomination second = o2.getDen();
            if (first.getDenomination() > second.getDenomination()) return 1;
            else if (first.getDenomination() < second.getDenomination()) return -1;
            else return 0;
        });
        return amountPerDens;
    }

    @Override
    protected BaseRecyclerAdapter<AmountPerDen, ? extends BaseRecyclerViewHolder> initAdapter() {
        return new BaseRecyclerAdapter<AmountPerDen, DenHolder>(getActivity()) {
            @Override
            protected DenHolder createHolder(View itemView, int type) {
                return new DenHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.row_zerocoin_coin_balance;
            }

            @Override
            protected void bindHolder(DenHolder holder, AmountPerDen data, int position) {
                holder.text_amount.setText(data.getAmount().toPlainString() + " zPIV" );
                String s = getString(R.string.denomination) + " = " + String.valueOf(data.getDen().getDenomination());
                holder.text_demon.setText(s);
                holder.text_demon_amount.setText(data.getCoinsCont() + " x ");
            }
        };
    }

    private class DenHolder extends BaseRecyclerViewHolder{

        TextView text_demon_amount, text_demon, text_amount;

        protected DenHolder(View itemView, int holderType) {
            super(itemView, holderType);
            text_amount = (TextView) itemView.findViewById(R.id.text_amount);
            text_demon_amount = (TextView) itemView.findViewById(R.id.text_demon_amount);
            text_demon = (TextView) itemView.findViewById(R.id.text_demon);
        }
    }
}
