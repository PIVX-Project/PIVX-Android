package pivx.org.pivxwallet.ui.wallet_activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import pivx.org.pivxwallet.ui.transaction_detail_activity.TransactionDetailActivity;

/**
 * Created by Neoperol on 5/3/17.
 */


public class TransactionViewHolderBase extends BaseRecyclerViewHolder {

    CardView cv;
    TextView title;
    TextView description;
    TextView amount;
    TextView amountLocal;
    ImageView imageView;
    TextView txt_scale;
    private final Context context;

    public TransactionViewHolderBase(View itemView) {
        super(itemView);
        context = itemView.getContext();
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, TransactionDetailActivity.class);
                context.startActivity(intent);
            }
        });
        cv = (CardView) itemView.findViewById(R.id.cardView);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        amount = (TextView) itemView.findViewById(R.id.amount);
        txt_scale = (TextView) itemView.findViewById(R.id.txt_scale);
        amountLocal = (TextView) itemView.findViewById(R.id.txt_local_currency);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }

}
