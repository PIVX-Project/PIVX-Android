package pivx.org.pivxwallet.ui.transaction_detail_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.pivxj.core.Coin;
import org.pivxj.core.Transaction;
import org.pivxj.core.TransactionInput;
import org.pivxj.core.TransactionOutPoint;
import org.pivxj.core.TransactionOutput;
import org.pivxj.script.Script;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import global.PivxRate;
import global.wrappers.InputWrapper;
import host.furszy.zerocoinj.wallet.MultiWallet;
import pivx.org.pivxwallet.R;
import global.AddressLabel;
import pivx.org.pivxwallet.ui.base.BaseFragment;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerAdapter;
import pivx.org.pivxwallet.ui.base.tools.adapter.BaseRecyclerViewHolder;
import pivx.org.pivxwallet.ui.base.tools.adapter.ListItemListeners;
import global.wrappers.TransactionWrapper;
import pivx.org.pivxwallet.utils.DialogsUtil;
import wallet.exceptions.TxNotFoundException;

import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputsActivity.INTENT_NO_TOTAL_AMOUNT;
import static pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputsFragment.INTENT_EXTRA_UNSPENT_WRAPPERS;

/**
 * Created by furszy on 8/7/17.
 */

public class FragmentTxDetail extends BaseFragment implements View.OnClickListener {

    public static final String TX = "tx";
    public static final String TX_WRAPPER = "tx_wrapper";
    public static final String IS_DETAIL = "is_detail";
    public static final String IS_ZPIV_WALLET = "is_zpiv";
    public static final String TX_MEMO = "tx_memo";

    private View root;
    private TextView txt_transaction_id;
    private TextView txt_amount;
    private TextView txt_date;
    private RecyclerView recycler_outputs;
    private TextView txt_memo, txt_fee, txt_inputs, txt_date_title, txt_confirmations, container_confirmations, txt_tx_weight, txt_is_tx_spent, txt_amount_local;

    private TransactionWrapper transactionWrapper;
    private boolean isTxDetail = true;
    private boolean isZpivWallet = false;
    private int myPosition ;
    private PivxRate pivxRate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_transaction_detail,container,false);
        pivxRate = pivxModule.getRate(pivxApplication.getAppConf().getSelectedRateCoin());
        Intent intent = getActivity().getIntent();
        if (intent != null){
            transactionWrapper = (TransactionWrapper) intent.getSerializableExtra(TX_WRAPPER);
            if (intent.hasExtra(IS_DETAIL)){
                transactionWrapper.setTransaction(pivxModule.getTx(transactionWrapper.getTxId()));
                isTxDetail = true;
                if (intent.hasExtra(IS_ZPIV_WALLET)){
                    isZpivWallet = intent.getBooleanExtra(IS_ZPIV_WALLET, false);
                }
            }else {
                transactionWrapper.setTransaction(new Transaction(pivxModule.getConf().getNetworkParams(),intent.getByteArrayExtra(TX)));
                if (intent.hasExtra(TX_MEMO)){
                    transactionWrapper.getTransaction().setMemo(intent.getStringExtra(TX_MEMO));
                }
                isTxDetail = false;
                if (intent.hasExtra(IS_ZPIV_WALLET)){
                    isZpivWallet = intent.getBooleanExtra(IS_ZPIV_WALLET, false);
                }
            }
        }
        txt_transaction_id = (TextView) root.findViewById(R.id.txt_transaction_id);
        txt_amount = (TextView) root.findViewById(R.id.txt_amount);
        txt_amount_local = (TextView) root.findViewById(R.id.txt_amount_local);
        txt_date = (TextView) root.findViewById(R.id.txt_date);
        txt_memo = (TextView) root.findViewById(R.id.txt_memo);
        txt_fee = (TextView) root.findViewById(R.id.txt_fee);
        txt_inputs = (TextView) root.findViewById(R.id.txt_inputs);
        txt_date_title = (TextView) root.findViewById(R.id.txt_date_title);
        recycler_outputs = (RecyclerView) root.findViewById(R.id.recycler_outputs);
        txt_confirmations = (TextView) root.findViewById(R.id.txt_confirmations);
        container_confirmations = (TextView) root.findViewById(R.id.container_confirmations);
        txt_tx_weight = (TextView) root.findViewById(R.id.txt_tx_weight);
        txt_is_tx_spent = (TextView) root.findViewById(R.id.txt_is_tx_spent);

        txt_inputs.setOnClickListener(this);

        try {
            loadTx();
        }catch (Exception e){
            e.printStackTrace();
        }

        return root;
    }

    private void loadTx() {
        if (!isTxDetail){
            txt_date_title.setVisibility(View.GONE);
            txt_date.setVisibility(View.GONE);
            container_confirmations.setVisibility(View.GONE);
            txt_confirmations.setVisibility(View.GONE);
        }else {
            // set date
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");
            txt_date.setText(simpleDateFormat.format(transactionWrapper.getTransaction().getUpdateTime()));
        }
        txt_transaction_id.setText(transactionWrapper.getTransaction().getHashAsString());
        txt_amount.setText(transactionWrapper.getAmount().toFriendlyString());

        if (pivxRate != null) {
            txt_amount_local.setText(
                    pivxApplication.getCentralFormats().format(
                            new BigDecimal(transactionWrapper.getAmount().getValue() * pivxRate.getRate().doubleValue()).movePointLeft(8)
                    )
                            + " " + pivxRate.getCode()
            );
        } else {
            txt_amount_local.setText("0.00");
        }
        Coin fee = null;
        if (transactionWrapper.isStake()){
            fee = Coin.ZERO;
        }else if(transactionWrapper.getTransaction().getFee() != null) {
            fee = transactionWrapper.getTransaction().getFee();
        }else {
            try {
                // Fee calculation with low performance, have to check why the fee is null here..
                Coin inputsSum = Coin.ZERO;
                for (TransactionInput input : transactionWrapper.getTransaction().getInputs()) {
                    TransactionOutPoint unspent = input.getOutpoint();
                    Coin unspentValue = pivxModule.getUnspentValue(unspent.getHash(), (int) unspent.getIndex());
                    if (unspentValue != null)
                        inputsSum = inputsSum.plus(unspentValue);
                    else
                        // TODO: Improve this..
                        throw new Exception("Unspent value null");
                }
                fee = inputsSum.subtract(transactionWrapper.getTransaction().getOutputSum());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if (fee != null)
            txt_fee.setText(fee.toFriendlyString());
        else
            txt_fee.setText(R.string.no_data_available);

        if (transactionWrapper.getTransaction().getMemo()!=null && transactionWrapper.getTransaction().getMemo().length()>0){
            txt_memo.setText(transactionWrapper.getTransaction().getMemo());
        }else {
            txt_memo.setText(R.string.tx_detail_no_memo);
        }

        txt_confirmations.setText(String.valueOf(transactionWrapper.getTransaction().getConfidence().getDepthInBlocks()));

        int bytesSize = transactionWrapper.getTransaction().unsafeBitcoinSerialize().length;
        BigDecimal bytesSizeDecimal = new BigDecimal(bytesSize).movePointLeft(3);
        txt_tx_weight.setText(bytesSizeDecimal.toPlainString() + " Kb");

        txt_is_tx_spent.setText(
                pivxModule.isEveryOutputSpent(
                        transactionWrapper.getTransaction(),
                        isZpivWallet ? MultiWallet.WalletType.ZPIV : MultiWallet.WalletType.PIV
                ) ? R.string.yes : R.string.no);

        if (transactionWrapper.getTransaction().getInputs().size() > 1) {
            txt_inputs.setText(getString(R.string.tx_detail_inputs,transactionWrapper.getTransaction().getInputs().size()));
        } else {

            txt_inputs.setText(getString(R.string.tx_detail_input,transactionWrapper.getTransaction().getInputs().size()));
        }



        List<OutputUtil> list = new ArrayList<>();

        for (TransactionOutput transactionOutput : transactionWrapper.getTransaction().getOutputs()) {

            String label;
            if (transactionWrapper.getOutputLabels()!=null && transactionWrapper.getOutputLabels().containsKey(transactionOutput.getIndex())){
                AddressLabel addressLabel = transactionWrapper.getOutputLabels().get(transactionOutput.getIndex());
                if (addressLabel !=null) {
                    if (addressLabel.getName() != null) {
                        label = addressLabel.getName();
                    } else
                        //label = addressLabel.getAddresses().get(0);
                        label = transactionOutput.getScriptPubKey().getToAddress(pivxModule.getConf().getNetworkParams(),true).toBase58();
                }else {
                    label = transactionOutput.getScriptPubKey().getToAddress(pivxModule.getConf().getNetworkParams(),true).toBase58();
                }
            }else {
                Script script = transactionOutput.getScriptPubKey();
                if (script.isPayToScriptHash() || script.isSentToRawPubKey() || script.isSentToAddress()) {
                    label = script.getToAddress(pivxModule.getConf().getNetworkParams(), true).toBase58();
                }else {
                    label = script.toString();
                }
            }

            list.add(
                    new OutputUtil(
                            transactionOutput.getIndex(),
                            label, // for now.. //label,
                            transactionOutput.getValue()
                    )
            );
        }

        setupOutputs(list);

    }

    private void setupOutputs(List<OutputUtil> list) {
        recycler_outputs.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_outputs.setHasFixedSize(true);
        ListItemListeners<OutputUtil> listItemListener = new ListItemListeners<OutputUtil>() {
            @Override
            public void onItemClickListener(OutputUtil data, int position) {

            }

            @Override
            public void onLongItemClickListener(OutputUtil data, int position) {
                if (pivxModule.chechAddress(data.getLabel())) {
                    DialogsUtil.showCreateAddressLabelDialog(getActivity(),data.getLabel());
                }
            }
        };
        recycler_outputs.setAdapter(new BaseRecyclerAdapter<OutputUtil,DetailOutputHolder>(getActivity(),list,listItemListener) {
            String myOutputs = getResources().getString(R.string.output);
            @Override
            protected DetailOutputHolder createHolder(View itemView, int type) {
                return new DetailOutputHolder(itemView,type);
            }

            @Override
            protected int getCardViewResource(int type) {
                return R.layout.detail_output_row;
            }

            @Override
            protected void bindHolder(DetailOutputHolder holder, OutputUtil data, int position) {
                myPosition = position + 1;
                holder.txt_num.setText(myOutputs+ " "+myPosition);
                holder.txt_address.setText(data.getLabel());
                holder.txt_value.setText(data.getAmount().toFriendlyString());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_inputs){
            try {
                Set<InputWrapper> set = pivxModule.convertFrom(transactionWrapper.getTransaction().getInputs());
                if (set == null || set.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.detail_no_available_inputs, Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), InputsDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putBoolean(INTENT_NO_TOTAL_AMOUNT, true);
                bundle.putSerializable(INTENT_EXTRA_UNSPENT_WRAPPERS, (Serializable) set);
                intent.putExtras(bundle);
                startActivity(intent);
            } catch (TxNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(),R.string.detail_no_available_inputs,Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class OutputUtil{

        private int pos;
        private String label;
        private Coin amount;

        public OutputUtil(int pos, String label, Coin amount) {
            this.pos = pos;
            this.label = label;
            this.amount = amount;
        }

        public int getPos() {
            return pos;
        }

        public String getLabel() {
            return label;
        }

        public Coin getAmount() {
            return amount;
        }
    }

    public static class DetailOutputHolder extends BaseRecyclerViewHolder{

        TextView txt_num;
        TextView txt_address;
        TextView txt_value;

        protected DetailOutputHolder(View itemView, int holderType) {
            super(itemView, holderType);
            txt_num = (TextView) itemView.findViewById(R.id.txt_num);
            txt_address = (TextView) itemView.findViewById(R.id.txt_address);
            txt_value = (TextView) itemView.findViewById(R.id.txt_value);

        }
    }
}
