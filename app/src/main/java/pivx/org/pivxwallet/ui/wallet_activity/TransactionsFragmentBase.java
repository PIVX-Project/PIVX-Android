package pivx.org.pivxwallet.ui.wallet_activity;

import android.graphics.Color;
import android.view.View;

import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

/**
 * Created by furszy on 6/29/17.
 */

public class TransactionsFragmentBase extends BaseRecyclerFragment<TransactionWrapper> {

    @Override
    protected List<TransactionWrapper> onLoading() {
        return pivxModule.listTx();
    }

    @Override
    protected BaseRecyclerAdapter<TransactionWrapper, ? extends BaseRecyclerViewHolder> initAdapter() {
        return new BaseRecyclerAdapter<TransactionWrapper, TransactionViewHolderBase>(getActivity()) {
            @Override
            protected TransactionViewHolderBase createHolder(View itemView, int type) {
                return new TransactionViewHolderBase(itemView);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.transaction_row;
            }

            @Override
            protected void bindHolder(TransactionViewHolderBase holder, TransactionWrapper data, int position) {
                //todo: fill this..
                holder.amount.setText(data.getAmount().toFriendlyString());
                holder.description.setText(data.getTransaction().getMemo());
                holder.cv.setBackgroundColor(data.isTxMine()? Color.RED:Color.GREEN);

            }
        };
    }
}
