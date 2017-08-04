package pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs;

import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

/**
 * Created by furszy on 8/4/17.
 */

public class OutputHolder extends BaseRecyclerViewHolder{

    TextView txt_address_number;
    EditText edit_address;
    EditText edit_amount;
    EditText edit_address_label;
    ImageView img_cancel;
    ImageView img_qr;

    public OutputHolder(View itemView, int holderType) {
        super(itemView, holderType);
        txt_address_number = (TextView) itemView.findViewById(R.id.txt_address_number);
        edit_address = (EditText) itemView.findViewById(R.id.edit_address);
        edit_amount = (EditText) itemView.findViewById(R.id.edit_amount);
        edit_address_label = (EditText) itemView.findViewById(R.id.edit_address_label);
        img_cancel = (ImageView) itemView.findViewById(R.id.img_cancel);
        img_qr = (ImageView) itemView.findViewById(R.id.img_qr);
    }
}
