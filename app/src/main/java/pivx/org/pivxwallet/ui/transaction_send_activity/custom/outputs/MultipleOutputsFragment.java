package pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;
import org.bitcoinj.core.Coin;
import org.bitcoinj.uri.PivxURI;

import java.util.ArrayList;
import java.util.List;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseRecyclerFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputHolder;
import pivx.org.pivxwallet.utils.scanner.ScanActivity;

import static android.Manifest.permission_group.CAMERA;
import static android.app.Activity.RESULT_OK;
import static pivx.org.pivxwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;

/**
 * Created by furszy on 8/4/17.
 * todo: agregar el change listener y pintar de rojo cuando un valor está mal..
 */

public class MultipleOutputsFragment extends BaseRecyclerFragment<OutputWrapper> {

    private static final int SCANNER_RESULT = 122;

    private List<OutputWrapper> list;
    private BaseRecyclerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        list = new ArrayList<>();

        if (list.isEmpty()){
            list.add(new OutputWrapper(
                    0,
                    null,
                    null,
                    null
            ));
        }
        setSwipeRefresh(false);

    }

    @Override
    protected List<OutputWrapper> onLoading() {
        return list;
    }

    @Override
    protected BaseRecyclerAdapter<OutputWrapper, ? extends BaseRecyclerViewHolder> initAdapter() {
        adapter = new BaseRecyclerAdapter<OutputWrapper, OutputHolder>(getActivity()) {
            @Override
            protected OutputHolder createHolder(View itemView, int type) {
                return new OutputHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.output_row;
            }

            @Override
            protected void bindHolder(final OutputHolder holder, final OutputWrapper data, int position) {
                holder.txt_address_number.setText(getString(R.string.address_num,position+1));
                if(position==0) {
                    holder.img_cancel.setVisibility(View.GONE);
                }else {
                    holder.img_cancel.setVisibility(View.VISIBLE);
                    holder.img_cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeItem(data.getId());
                        }
                    });
                }
                holder.img_qr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!checkPermission(CAMERA)) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                int permsRequestCode = 200;
                                String[] perms = {"android.permission.CAMERA"};
                                requestPermissions(perms, permsRequestCode);
                            }
                        }
                        startActivityForResult(new Intent(getActivity(), ScanActivity.class),SCANNER_RESULT);
                    }
                });
                if (data.getAddress()!=null){
                    holder.edit_address.setText(data.getAddress());
                }
                if (data.getAddressLabel()!=null){
                    holder.edit_address_label.setText(data.getAddressLabel());
                }
                if (data.getAmount()!=null){
                    holder.edit_amount.setText(data.getAmount().toPlainString());
                }
                holder.edit_address.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.length()>0){
                            if (holder.edit_address!=null) {
                                if (!pivxModule.chechAddress(s.toString())) {
                                    holder.edit_address.setTextColor(Color.RED);
                                } else {
                                    holder.edit_address.setTextColor(Color.GREEN);
                                }
                            }
                        }
                    }
                });
                holder.edit_amount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

            }

            @Override
            public OutputWrapper removeItem(int id) {
                int pos = -1;
                for (int i=0;i<dataSet.size();i++) {
                    OutputWrapper outputWrapper = dataSet.get(i);
                    if (outputWrapper.getId()==id){
                        pos = i;
                    }
                }
                if (pos==-1) throw new IllegalArgumentException("id not exist in the dataset, id:" +id);
                OutputWrapper outputWrapper = super.removeItem(pos);
                return outputWrapper;
            }
        };
        return adapter;
    }


    public void addOutput(){

        int pos = list.size()-1;
        OutputHolder outputHolder = (OutputHolder) getRecycler().findViewHolderForAdapterPosition(pos);
        String address = outputHolder.edit_address.getText().toString();
        String amountStr = outputHolder.edit_amount.getText().toString();
        if (amountStr.equals("")){
            Toast.makeText(getActivity(),R.string.invalid_last_amount,Toast.LENGTH_LONG).show();
            return;
        }
        Coin amount = Coin.parseCoin(amountStr);
        String addressLabel = outputHolder.edit_address_label.getText().toString();

        OutputWrapper lastOutput = (OutputWrapper) adapter.getItem(pos);
        lastOutput.setAddress(address);
        lastOutput.setAmount(amount);
        lastOutput.setAddressLabel(addressLabel);

        if (lastOutput.getAddress()==null || !pivxModule.chechAddress(lastOutput.getAddress())){
            // todo: mejorar esto
            Toast.makeText(getActivity(),R.string.invalid_input_address,Toast.LENGTH_LONG).show();
            return;
        }

        adapter.addItem(new OutputWrapper(
                pos,
                null,
                null,
                null
        ));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT){
            if (resultCode==RESULT_OK) {
                try {

                }catch (Exception e){
                    Toast.makeText(getActivity(),R.string.bad_address,Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void setOutputsWrappers(List<OutputWrapper> outputsWrappers) {
        this.list = outputsWrappers;
        adapter.changeDataSet(list);
    }

    public void addOutputsWrappers(List<OutputWrapper> outputWrappers){
        this.list.addAll(outputWrappers);
    }

    public List<OutputWrapper> getList() throws InvalidFieldException {
        List<OutputWrapper> ret = new ArrayList<>();
        for (int i=0;i<list.size();i++){
            OutputHolder outputHolder = (OutputHolder) getRecycler().findViewHolderForAdapterPosition(i);
            if (outputHolder!=null){
                String address;
                String amountStr;
                address = outputHolder.edit_address.getText().toString();
                amountStr = outputHolder.edit_amount.getText().toString();
                boolean checkAddress = !pivxModule.chechAddress(address);
                boolean checkAmount = amountStr.length() == 0;
                if (i!=list.size()-1) {
                    if (checkAddress)
                        throw new InvalidFieldException("Address not valid");
                    if (checkAmount)
                        throw new InvalidFieldException("Amount not valid");
                }else {
                    if (checkAddress || checkAmount){
                        break;
                    }
                }
                Coin amount = Coin.parseCoin(amountStr);
                String addressLabel = outputHolder.edit_address_label.getText().toString();
                OutputWrapper outputWrapper = new OutputWrapper(i,address,amount,addressLabel);
                ret.add(outputWrapper);
            }
        }
        return ret;
    }
}
