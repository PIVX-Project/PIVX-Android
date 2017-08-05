package pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import org.bitcoinj.core.Coin;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;

/**
 * Created by furszy on 8/4/17.
 */

public class InputsFragment extends BaseRecyclerFragment<InputWrapper> {

    private List<InputWrapper> list;
    private BaseRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();

        if (list.isEmpty()){
            list.add(new InputWrapper(
                    null,
                    null
            ));
            list.add(new InputWrapper(
                    null,
                    null
            ));
        }

    }

    @Override
    protected List<InputWrapper> onLoading() {
        return list;
    }

    @Override
    protected BaseRecyclerAdapter<InputWrapper, ? extends BaseRecyclerViewHolder> initAdapter() {
        adapter = new BaseRecyclerAdapter<InputWrapper, InputHolder>(getActivity()) {
            @Override
            protected InputHolder createHolder(View itemView, int type) {
                return new InputHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.input_row;
            }

            @Override
            protected void bindHolder(final InputHolder holder, InputWrapper data, int position) {

            }
        };
        return adapter;
    }

}
