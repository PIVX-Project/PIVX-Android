package pivx.org.pivxwallet.ui.address_activity;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/18/17.
 */

public class AddressViewHolder extends RecyclerView.ViewHolder {

    CardView cv;
    TextView name;
    TextView address;

    AddressViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        name = (TextView) itemView.findViewById(R.id.name);
        address = (TextView) itemView.findViewById(R.id.address);
    }
}
