package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseFragment;

/**
 * Created by furszy on 8/3/17.
 */

public class CustomFeeFragment extends BaseFragment {

    private View root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.custom_fee_fragment,container,false);
        return root;
    }
}
