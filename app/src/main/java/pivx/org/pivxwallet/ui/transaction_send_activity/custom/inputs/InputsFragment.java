package pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import org.bitcoinj.core.Coin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import wallet.TxNotFoundException;

/**
 * Created by furszy on 8/4/17.
 */

public class InputsFragment extends BaseRecyclerFragment<InputWrapper> {

    private static final int TYPE_INIT = 0;
    private static final int TYPE_NORMAL = 1;

    public static final String INTENT_EXTRA_UNSPENT_WRAPPERS = "unspent_wrappers";


    private List<InputWrapper> list;
    private BaseRecyclerAdapter adapter;

    private List<InputWrapper> selectedList = new ArrayList<>();

    private boolean selectAll = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            list = new ArrayList<>();
            setHasOptionsMenu(true);
            Intent intent = getActivity().getIntent();
            if (intent != null) {
                if (intent.hasExtra(INTENT_EXTRA_UNSPENT_WRAPPERS)) {
                    selectedList = (List<InputWrapper>) intent.getSerializableExtra(INTENT_EXTRA_UNSPENT_WRAPPERS);
                    for (InputWrapper inputWrapper : selectedList) {
                        inputWrapper.setUnspent(pivxModule.getUnspent(inputWrapper.getParentTxHash(), inputWrapper.getIndex()));
                    }
                }
            }
            setSwipeRefresh(false);
        } catch (TxNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),R.string.invalid_inputs,Toast.LENGTH_SHORT).show();
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.save_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.option_ok){
            List<InputWrapper> unspent = listSelected();
            if (unspent!=null && !unspent.isEmpty()) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, (Serializable) unspent);
                intent.putExtras(bundle);
                getActivity().setResult(Activity.RESULT_OK,intent);
                getActivity().finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private List<InputWrapper> listSelected() {
        /*List<InputWrapper> unspent = new ArrayList<>();
        for (int i=1;i<adapter.getItemCount();i++){
            InputHolder inputHolder = (InputHolder) getRecycler().findViewHolderForAdapterPosition(i);
            if (inputHolder!=null) {
                if (inputHolder.radio_select.isChecked()) {
                    unspent.add(list.get(i));
                }
            }
        }*/
        return selectedList;
    }

    @Override
    protected List<InputWrapper> onLoading() {

        list.clear();

        list.add(new InputWrapper(
                null,
                null
        ));

        list.addAll(pivxModule.listUnspentWrappers());

        return list;
    }

    @Override
    protected BaseRecyclerAdapter<InputWrapper, ? extends BaseRecyclerViewHolder> initAdapter() {
        adapter = new BaseRecyclerAdapter<InputWrapper, BaseRecyclerViewHolder>(getActivity()) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat();

            @Override
            protected BaseRecyclerViewHolder createHolder(View itemView, int type) {
                return type==TYPE_NORMAL?new InputHolder(itemView,type):new SelectorHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return type==TYPE_NORMAL? R.layout.input_row : R.layout.layout_input_row_first;
            }

            @Override
            protected void bindHolder(final BaseRecyclerViewHolder holder, final InputWrapper data, int position) {
                if (position==0){
                    SelectorHolder selectorHolder = (SelectorHolder) holder;
                    selectorHolder.radio_select.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectAll();
                        }
                    });
                }else {
                    final InputHolder inputHolder = (InputHolder) holder;
                    inputHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean selected = !inputHolder.radio_select.isChecked();
                            inputHolder.radio_select.setChecked(selected);
                            if (selected){
                                selectedList.add(data);
                            }else {
                                selectedList.remove(data);
                            }
                        }
                    });

                    for (InputWrapper inputWrapper : selectedList) {
                        if (inputWrapper.getParentTxHash().equals(data.getParentTxHash())
                                &&
                                inputWrapper.getIndex() == data.getIndex()){
                            inputHolder.radio_select.setChecked(true);
                            break;
                        }
                    }
                    inputHolder.txt_address.setText(data.getLabel());
                    inputHolder.txt_amount.setText(data.getUnspent().getValue().toFriendlyString());
                    inputHolder.txt_confirmations_amount.setText(data.getUnspent().getParentTransactionDepthInBlocks()+" "+getString(R.string.confimations));
                    inputHolder.txt_date.setText(simpleDateFormat.format(data.getUnspent().getParentTransaction().getUpdateTime()));
                }
            }
            @Override
            public int getItemViewType(int position) {
                return position==0?TYPE_INIT:TYPE_NORMAL;
            }
        };
        return adapter;
    }

    private void selectAll(){
        selectAll = !selectAll;
        for (int i=1;i<list.size();i++){
            InputHolder inputHolder = (InputHolder) getRecycler().findViewHolderForAdapterPosition(i);
            if (inputHolder!=null)
                inputHolder.radio_select.setChecked(selectAll);
        }
    }

    private class SelectorHolder extends BaseRecyclerViewHolder{

        CheckBox radio_select;

        protected SelectorHolder(View itemView, int holderType) {
            super(itemView, holderType);
            this.radio_select = (CheckBox) itemView.findViewById(R.id.radio_select);
        }


    }

}
