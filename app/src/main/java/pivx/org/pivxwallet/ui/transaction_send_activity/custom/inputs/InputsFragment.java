package pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import global.wrappers.InputWrapper;
import wallet.exceptions.TxNotFoundException;

/**
 * Created by furszy on 8/4/17.
 */

public class InputsFragment extends BaseRecyclerFragment<InputsFragment.InputSelectionWrapper> {

    private static final int TYPE_INIT = 0;
    private static final int TYPE_NORMAL = 1;

    public static final String INTENT_EXTRA_UNSPENT_WRAPPERS = "unspent_wrappers";


    private List<InputSelectionWrapper> list;
    private BaseRecyclerAdapter adapter;

    private Set<InputWrapper> selectedList = new HashSet<>();

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
                    selectedList = (Set<InputWrapper>) intent.getSerializableExtra(INTENT_EXTRA_UNSPENT_WRAPPERS);
                    for (InputWrapper inputWrapper : selectedList) {
                        inputWrapper.setUnspent(pivxModule.getUnspent(inputWrapper.getParentTxHash(), inputWrapper.getIndex()));
                    }
                }
            }
            setSwipeRefresh(false);
            setEmptyText(getString(R.string.no_available_inputs));
            setEmptyTextColor(Color.parseColor("#444444"));
            setEmptyView(R.drawable.img_coins_empty);
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
            Set<InputWrapper> unspent = listSelected();
            if (unspent!=null && !unspent.isEmpty()) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, (Serializable) unspent);
                intent.putExtras(bundle);
                getActivity().setResult(Activity.RESULT_OK,intent);
                getActivity().finish();
                return true;
            }else {
                Toast.makeText(getActivity(),R.string.no_coin_selected,Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private Set<InputWrapper> listSelected() {
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
    protected List<InputSelectionWrapper> onLoading() {

        list.clear();

        list.add(
                new InputSelectionWrapper(
                    new InputWrapper(
                    null,
                    null
                    )
        ));

        for (InputWrapper inputWrapper : pivxModule.listUnspentWrappers()) {
            InputSelectionWrapper inputSelectionWrapper = new InputSelectionWrapper(inputWrapper);
            list.add(inputSelectionWrapper);
        }

        return list;
    }

    @Override
    protected BaseRecyclerAdapter<InputSelectionWrapper, ? extends BaseRecyclerViewHolder> initAdapter() {
        adapter = new BaseRecyclerAdapter<InputSelectionWrapper, BaseRecyclerViewHolder>(getActivity()) {

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm");

            @Override
            protected BaseRecyclerViewHolder createHolder(View itemView, int type) {
                return type==TYPE_NORMAL?new InputHolder(itemView,type):new SelectorHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return type==TYPE_NORMAL? R.layout.input_row : R.layout.layout_input_row_first;
            }

            @Override
            protected void bindHolder(final BaseRecyclerViewHolder holder, final InputSelectionWrapper data, int position) {
                if (position==0){
                    final SelectorHolder selectorHolder = (SelectorHolder) holder;
                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            setSelectorImg(selectorHolder.radio_select,!data.isSelected);
                            data.isSelected = !data.isSelected;
                            selectAll();
                        }
                    };
                    selectorHolder.itemView.setOnClickListener(onClickListener);
                }else {
                    final InputHolder inputHolder = (InputHolder) holder;
                    boolean found = false;
                    for (InputWrapper inputWrapper : selectedList) {
                        if (inputWrapper.getParentTxHash().equals(data.getInputWrapper().getParentTxHash())
                                &&
                                inputWrapper.getIndex() == data.getInputWrapper().getIndex()){
                            found = true;
                            break;
                        }
                    }

                    // img
                    setSelectorImg(inputHolder.radio_select,found);

                    data.setSelected(found);

                    inputHolder.txt_address.setText(data.getInputWrapper().getLabel(PivxContext.NETWORK_PARAMETERS));
                    inputHolder.txt_amount.setText(data.getInputWrapper().getUnspent().getValue().toFriendlyString());
                    inputHolder.txt_confirmations_amount.setText(data.getInputWrapper().getUnspent().getParentTransactionDepthInBlocks()+" "+getString(R.string.confimations));
                    inputHolder.txt_date.setText(simpleDateFormat.format(data.getInputWrapper().getUnspent().getParentTransaction().getUpdateTime()));

                    View.OnClickListener onClickListener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            boolean selected = !data.isSelected;
                            setSelectorImg(inputHolder.radio_select,selected);
                            data.setSelected(selected);
                            if (selected){
                                selectedList.add(data.getInputWrapper());
                            }else {
                                selectedList.remove(data.getInputWrapper());
                            }
                        }
                    };
                    inputHolder.itemView.setOnClickListener(onClickListener);

                }
            }
            @Override
            public int getItemViewType(int position) {
                return position==0?TYPE_INIT:TYPE_NORMAL;
            }
        };
        return adapter;
    }

    private void setSelectorImg(ImageView img,boolean selected){
        if (selected){
            img.setImageResource(R.drawable.ic_selector_on);
        }else {
            img.setImageResource(R.drawable.ic_selector_off);
        }
    }

    private void selectAll(){
        selectAll = !selectAll;
        for (int i=1;i<list.size();i++){
            InputHolder inputHolder = (InputHolder) getRecycler().findViewHolderForAdapterPosition(i);
            if (inputHolder!=null) {
                setSelectorImg(inputHolder.radio_select,selectAll);
            }
            InputSelectionWrapper inputSelectionWrapper = list.get(i);
            inputSelectionWrapper.setSelected(selectAll);
            if (selectAll) {
                selectedList.add(inputSelectionWrapper.getInputWrapper());
            }else {
                selectedList.remove(inputSelectionWrapper.getInputWrapper());
            }
        }
    }

    private class SelectorHolder extends BaseRecyclerViewHolder{

        ImageView radio_select;

        protected SelectorHolder(View itemView, int holderType) {
            super(itemView, holderType);
            this.radio_select = (ImageView) itemView.findViewById(R.id.radio_select);
        }
    }

    public static class InputSelectionWrapper{

        private InputWrapper inputWrapper;
        private boolean isSelected;

        public InputSelectionWrapper(InputWrapper inputWrapper) {
            this.inputWrapper = inputWrapper;
        }

        public InputWrapper getInputWrapper() {
            return inputWrapper;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public boolean isSelected() {
            return isSelected;
        }
    }

}
