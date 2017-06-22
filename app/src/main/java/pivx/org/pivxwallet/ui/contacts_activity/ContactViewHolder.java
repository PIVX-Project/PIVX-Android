package pivx.org.pivxwallet.ui.contacts_activity;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.tools.adapter.RecyclerViewHolder;

/**
 * Created by Neoperol on 5/18/17.
 */

public class ContactViewHolder extends RecyclerViewHolder {

    CardView cv;
    TextView name;
    TextView address;

    ContactViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        name = (TextView) itemView.findViewById(R.id.name);
        address = (TextView) itemView.findViewById(R.id.address);
    }
}
