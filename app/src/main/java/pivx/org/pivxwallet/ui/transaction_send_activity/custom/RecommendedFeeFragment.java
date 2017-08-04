package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseFragment;
import pivx.org.pivxwallet.ui.restore_activity.FileAdapter;

/**
 * Created by furszy on 8/3/17.
 */

public class RecommendedFeeFragment extends BaseFragment {

    private View root;
    private SeekBar seekBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.recommended_fee_fragment,container, false);
        seekBar = (SeekBar) root.findViewById(R.id.seekbar);
        return root;
    }

    public int getProgressPosition(){
        return seekBar.getProgress();
    }
}
