package pivx.org.pivxwallet.ui.settings_network_activity;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

/**
 * Created by Neoperol on 6/8/17.
 */

public class NetworkViewHolder extends BaseRecyclerViewHolder {

    CardView cv;
    TextView address;
    TextView network_ip;
    TextView protocol;
    TextView blocks;
    TextView speed;


    NetworkViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        address = (TextView) itemView.findViewById(R.id.address);
        network_ip = (TextView) itemView.findViewById(R.id.network_ip);
        protocol = (TextView) itemView.findViewById(R.id.protocol);
        blocks = (TextView) itemView.findViewById(R.id.blocks);
        speed = (TextView) itemView.findViewById(R.id.speed);

    }
}
