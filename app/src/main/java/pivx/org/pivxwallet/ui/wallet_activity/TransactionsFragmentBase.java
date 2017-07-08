package pivx.org.pivxwallet.ui.wallet_activity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmptyView(R.drawable.img_transaction_empty);
        setEmptyText("You don't have any transfers yet.");
        setEmptyTextColor(Color.parseColor("#cccccc"));
        return view;
    }

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

                if (data.isTxMine()){
                    //holder.cv.setBackgroundColor(Color.RED);Color.GREEN
                    holder.imageView.setImageResource(R.mipmap.ic_transaction_send);
                }else {
                    holder.imageView.setImageResource(R.mipmap.ic_transaction_receive);
                }

                if (data.getContact()!=null){
                    holder.title.setText(data.getContact().getName());
                }else {
                    holder.title.setText(data.getAddress().toBase58());
                }
                String memo = data.getTransaction().getMemo();
                holder.description.setText(memo!=null?memo:"No description");
            }
        };
    }
}
