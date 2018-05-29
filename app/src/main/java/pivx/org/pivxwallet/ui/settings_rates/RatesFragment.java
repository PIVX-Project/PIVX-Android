package pivx.org.pivxwallet.ui.settings_rates;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import pivx.org.pivxwallet.R;
import global.PivxRate;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import pivx.org.pivxwallet.ui.base.tools.adapter.ListItemListeners;

/**
 * Created by furszy on 7/2/17.
 */

public class RatesFragment extends BaseRecyclerFragment<PivxRate> implements ListItemListeners<PivxRate> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setEmptyText("No rate available");
        setEmptyTextColor(Color.parseColor("#cccccc"));
        return view;
    }

    @Override
    protected List<PivxRate> onLoading() {
        return pivxModule.listRates();
    }

    @Override
    protected BaseRecyclerAdapter<PivxRate, ? extends PivxRateHolder> initAdapter() {
        BaseRecyclerAdapter<PivxRate, PivxRateHolder> adapter = new BaseRecyclerAdapter<PivxRate, PivxRateHolder>(getActivity()) {
            @Override
            protected PivxRateHolder createHolder(View itemView, int type) {
                return new PivxRateHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.rate_row;
            }

            @Override
            protected void bindHolder(PivxRateHolder holder, PivxRate data, int position) {
                holder.txt_name.setText(data.getCode());
                if (list.get(0).getCode().equals(data.getCode()))
                    holder.view_line.setVisibility(View.GONE);
            }
        };
        adapter.setListEventListener(this);
        return adapter;
    }

    @Override
    public void onItemClickListener(PivxRate data, int position) {
        pivxApplication.getAppConf().setSelectedRateCoin(data.getCode());
        Toast.makeText(getActivity(),R.string.rate_selected,Toast.LENGTH_SHORT).show();
        getActivity().onBackPressed();
    }

    @Override
    public void onLongItemClickListener(PivxRate data, int position) {

    }

    private  class PivxRateHolder extends BaseRecyclerViewHolder{

        private TextView txt_name;
        private View view_line;

        protected PivxRateHolder(View itemView, int holderType) {
            super(itemView, holderType);
            txt_name = (TextView) itemView.findViewById(R.id.txt_name);
            view_line = itemView.findViewById(R.id.view_line);
        }
    }
}
