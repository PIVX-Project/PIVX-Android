package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import org.pivxj.core.Transaction;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseFragment;

/**
 * Created by furszy on 8/3/17.
 */

public class RecommendedFeeFragment extends BaseFragment {

    private View root;
    private SeekBar seekBar;
    private TextView txt_amount_per_kb;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.recommended_fee_fragment,container, false);
        seekBar = (SeekBar) root.findViewById(R.id.seekbar);
        txt_amount_per_kb = (TextView) root.findViewById(R.id.txt_amount_per_kb);
        txt_amount_per_kb.setText(getString(R.string.fee_per_kb,Transaction.REFERENCE_DEFAULT_MIN_TX_FEE.toPlainString()));
        return root;
    }

    public int getProgressPosition(){
        return seekBar.getProgress();
    }
}
