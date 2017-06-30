package pivx.org.pivxwallet.ui.base.tools.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * TransactionViewHolderBase Base Class
 *
 */
public abstract class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {

    private int holderId = 0;
    private int holderType;
    private int holderLayoutRes;

    /**
     * Constructor
     *
     * @param itemView
     */
    @Deprecated
    protected BaseRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    /**
     * @param itemView
     * @param holderType
     */
    protected BaseRecyclerViewHolder(View itemView, int holderType) {
        super(itemView);
        this.holderType = holderType;
    }

    protected BaseRecyclerViewHolder(View itemView, int holderId, int holderType) {
        super(itemView);
        this.holderId = holderId;
        this.holderType = holderType;
    }

    public BaseRecyclerViewHolder(View itemView, int holderId, int holderType, int holderLayoutRes) {
        super(itemView);
        this.holderId = holderId;
        this.holderType = holderType;
        this.holderLayoutRes = holderLayoutRes;
    }

    public int getHolderId() {
        return holderId;
    }

    public int getHolderType() {
        return holderType;
    }

    public int getHolderLayoutRes() {
        return holderLayoutRes;
    }

}
