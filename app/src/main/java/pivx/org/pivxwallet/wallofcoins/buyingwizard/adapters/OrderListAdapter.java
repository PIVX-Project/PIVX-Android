package pivx.org.pivxwallet.wallofcoins.buyingwizard.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.databinding.LayoutItemOrderListBinding;
import pivx.org.pivxwallet.wallofcoins.BuyDashPref;
import pivx.org.pivxwallet.wallofcoins.WOCConstants;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.models.AccountJson;
import pivx.org.pivxwallet.wallofcoins.buyingwizard.order_history.OrderHistoryFragment;
import pivx.org.pivxwallet.wallofcoins.response.OrderListResp;


/**
 * Created on 08-Mar-18.
 */

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.VHolder> {

    private Context mContext;
    private final List<OrderListResp> orderList;
    private OrderHistoryFragment historyFragment;
    private BuyDashPref buyDashPref;

    public OrderListAdapter(Context mContext, List<OrderListResp> orderList, OrderHistoryFragment historyFragment, BuyDashPref buyDashPref) {
        this.mContext = mContext;
        this.orderList = orderList;
        this.historyFragment = historyFragment;
        this.buyDashPref = buyDashPref;
    }

    @Override
    public OrderListAdapter.VHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        LayoutItemOrderListBinding itemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_item_order_list, parent, false);
        return new OrderListAdapter.VHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(OrderListAdapter.VHolder holder, int position) {
        final OrderListResp orderListResp = orderList.get(position);
        if (orderListResp.id != -1 && orderListResp.id != -2 && orderListResp.id != -3) {

            holder.itemBinding.layLogout.setVisibility(View.GONE);
            holder.itemBinding.layHelpInstruction.setVisibility(View.GONE);
            holder.itemBinding.layOrderHistory.setVisibility(View.GONE);
            holder.itemBinding.layoutCompletionDetail.setVisibility(View.VISIBLE);
            holder.itemBinding.setItem(orderListResp);


            Type listType = new TypeToken<ArrayList<AccountJson>>() {
            }.getType();

            holder.itemBinding.linearAccountDetail.removeAllViews();
            try {
                ArrayList<AccountJson> accountList = new Gson().fromJson(orderListResp.account, listType);

                if (accountList != null && orderListResp.status.equals("WD")) {
                    for (int i = 0; i < accountList.size(); i++) {
                        TextView textView = new TextView(mContext);
                        textView.setTextSize(16);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams.topMargin = 0;
                        textView.setLayoutParams(layoutParams);
                        textView.setText(accountList.get(i).getLabel() + ": " + accountList.get(i).getValue());
                        holder.itemBinding.linearAccountDetail.addView(textView);
                    }
                    holder.itemBinding.textAccountNo.setVisibility(View.GONE);
                    holder.itemBinding.textNameAccount.setVisibility(View.GONE);

                } else {
                    holder.itemBinding.linearAccountDetail.setVisibility(View.GONE);
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }

            if (orderListResp.account == null
                    || orderListResp.account.equals("")) {
                holder.itemBinding.textAccountNo.setVisibility(View.GONE);
            }
            if (orderListResp.nameOnAccount == null
                    || orderListResp.nameOnAccount.equals("")) {
                holder.itemBinding.textNameAccount.setVisibility(View.GONE);
            }

            holder.itemBinding.btnDepositFinished.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    historyFragment.depositFinished(orderListResp);
                }
            });

            holder.itemBinding.btnCancelOrder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    historyFragment.cancelHoldOrder(orderListResp);
                }
            });


            if (orderListResp.nearestBranch != null) {
                if ((orderListResp.nearestBranch.address == null
                        || orderListResp.nearestBranch.address.equals(""))
                        && orderListResp.status.equals("WD")
                        ) {
                    holder.itemBinding.buttonBuyDashItemLocation.setVisibility(View.VISIBLE);
                    holder.itemBinding.tvItrmOffer4.setVisibility(View.VISIBLE);
                } else {
                    holder.itemBinding.buttonBuyDashItemLocation.setVisibility(View.GONE);
                    holder.itemBinding.tvItrmOffer4.setVisibility(View.GONE);
                }
            }

            holder.itemBinding.buttonBuyDashItemLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    historyFragment.goToGivenUrl(orderListResp.bankUrl);
                }
            });

//              you must deposit cash
            double dots = Double.parseDouble(orderListResp.total) * 1000000;
            DecimalFormat formatter = new DecimalFormat("#,###,###.##");
            String yourFormattedDots = formatter.format(dots);

            if (orderListResp.bankLogo != null
                    && !orderListResp.bankLogo.equals("")) {
                Glide.with(mContext)
                        .load(orderListResp.bankLogo)
                        .placeholder(R.drawable.ic_account_balance_black_24dp)
                        .error(R.drawable.ic_account_balance_black_24dp)
                        .into(holder.itemBinding.imageBank);
            } else {
                holder.itemBinding.imageBank.setImageResource(R.drawable.ic_account_balance_black_24dp);
            }
            //Log.e(TAG, "onBindViewHolder: " + orderListResp.status);

            if (orderListResp.status.equals("WD")) {

                holder.itemBinding.orderDash.setText("Total PIV: " + orderListResp.total + " (" + yourFormattedDots + " μⱣiv)\n"
                        + "You must deposit cash at the above Payment Center. Additional fees may apply. Paying in another method other than cash may delay your order.");
                holder.itemBinding.orderDashInstruction.setVisibility(View.VISIBLE);
                holder.itemBinding.btnCancelOrder.setVisibility(View.VISIBLE);
                holder.itemBinding.btnDepositFinished.setVisibility(View.VISIBLE);
                holder.itemBinding.layoutDueDate.setVisibility(View.VISIBLE);

                holder.itemBinding.textPaymentDueDate.setVisibility(View.VISIBLE);
                historyFragment.countDownStart(orderListResp.paymentDue, holder.itemBinding.textPaymentDueDate);


                holder.itemBinding.textContactInstruction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // goToUrl(WOCConstants.KEY_WEB_URL);
                        historyFragment.goToGivenUrl(WOCConstants.KEY_WEB_URL);
                    }
                });

            } else {

                holder.itemBinding.orderDash.setText("Total PIV: " + orderListResp.total + " (" + yourFormattedDots + " μⱣiv)");
                holder.itemBinding.layoutDueDate.setVisibility(View.GONE);
                holder.itemBinding.textPaymentDueDate.setVisibility(View.GONE);
                holder.itemBinding.orderDashInstruction.setVisibility(View.GONE);
                holder.itemBinding.btnCancelOrder.setVisibility(View.GONE);
                holder.itemBinding.btnDepositFinished.setVisibility(View.GONE);
                holder.itemBinding.textContactInstruction.setVisibility(View.GONE);
                holder.itemBinding.textAccountNo.setVisibility(View.GONE);
                holder.itemBinding.textNameAccount.setVisibility(View.GONE);
            }

            if (orderListResp.status.equals("WD")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_waiting_deposit));
            } else if (orderListResp.status.equals("WDV")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_waiting_deposit_verification));
            } else if (orderListResp.status.equals("RERR")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_issue_with_receipt));
            } else if (orderListResp.status.equals("DERR")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_issue_with_deposit));
            } else if (orderListResp.status.equals("RSD")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_reserved_for_deposit));
            } else if (orderListResp.status.equals("RMIT")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_remit_address_missing));
            } else if (orderListResp.status.equals("UCRV")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_under_review));
            } else if (orderListResp.status.equals("PAYP")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_done_pending_delivery));
            } else if (orderListResp.status.equals("SENT")) {
                holder.itemBinding.textTransactionStatus.setText(mContext.getString(R.string.status_done_units_delivered));
            }

        } else if (orderListResp.id == -3) {

            holder.itemBinding.layoutCompletionDetail.setVisibility(View.GONE);
            holder.itemBinding.layHelpInstruction.setVisibility(View.GONE);
            holder.itemBinding.layLogout.setVisibility(View.GONE);
            holder.itemBinding.layOrderHistory.setVisibility(View.VISIBLE);

        } else if (orderListResp.id == -2) {

            holder.itemBinding.layoutCompletionDetail.setVisibility(View.GONE);
            holder.itemBinding.layHelpInstruction.setVisibility(View.GONE);
            holder.itemBinding.layLogout.setVisibility(View.VISIBLE);
            holder.itemBinding.layOrderHistory.setVisibility(View.GONE);

            holder.itemBinding.textMessage.setText(mContext.getString(R.string.wallet_is_signed) + " " +
                    buyDashPref.getPhone());

            holder.itemBinding.btnSignout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    historyFragment.deleteAuthCall(false);
                }
            });


        } else if (orderListResp.id == -1) {

            holder.itemBinding.layoutCompletionDetail.setVisibility(View.GONE);
            holder.itemBinding.layHelpInstruction.setVisibility(View.VISIBLE);
            holder.itemBinding.layLogout.setVisibility(View.GONE);
            holder.itemBinding.layOrderHistory.setVisibility(View.GONE);

            holder.itemBinding.textHelpMessage.setText(R.string.call_for_help);
            holder.itemBinding.btnWebLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //goToUrl(WOCConstants.KEY_WEB_URL);
                    historyFragment.goToGivenUrl(WOCConstants.KEY_WEB_URL);
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class VHolder extends RecyclerView.ViewHolder {
        private LayoutItemOrderListBinding itemBinding;


        public VHolder(LayoutItemOrderListBinding itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }
}
