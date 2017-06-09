package pivx.org.pivxwallet.ui.qr_activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxModule;

/**
 * Created by furszy on 6/8/17.
 */

public class MyAddressFragment extends Fragment implements PagerFragment{

    private PivxModule module;

    private View root;

    public static MyAddressFragment newInstance(PivxModule pivxModule) {
        MyAddressFragment f = new MyAddressFragment();
        f.setModule(pivxModule);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.my_address,null);
        return root;
    }

    public void setModule(PivxModule module) {
        this.module = module;
    }

    @Override
    public void onPageVisible() {

    }

    @Override
    public void onPageInvisible() {

    }
}
