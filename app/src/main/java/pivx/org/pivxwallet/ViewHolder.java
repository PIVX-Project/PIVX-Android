package pivx.org.pivxwallet;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Neoperol on 5/3/17.
 */


public class ViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView title;
    TextView description;
    TextView amount;
    TextView amountLocal;
    ImageView imageView;

    ViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        amount = (TextView) itemView.findViewById(R.id.amount);
        amountLocal = (TextView) itemView.findViewById(R.id.amountLocal);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }
}
