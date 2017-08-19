package pivx.org.pivxwallet.ui.base.tools.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Adapter
 * Use with RecyclerView Widgets
 *
 */
public abstract class BaseRecyclerAdapter<M, H extends BaseRecyclerViewHolder> extends RecyclerView.Adapter<H> {

    protected List<M> dataSet;
    protected Context context;
    protected ListItemListeners<M> eventListeners;

    protected BaseRecyclerAdapter(Context context) {
        this.context = context;
    }

    protected BaseRecyclerAdapter(Context context, List<M> dataSet) {
        this.context = context;
        this.dataSet = dataSet;
    }

    protected BaseRecyclerAdapter(Context context, List<M> dataSet,ListItemListeners<M> itemListeners) {
        this(context,dataSet);
        this.eventListeners = itemListeners;
    }

    @Override
    public H onCreateViewHolder(ViewGroup viewGroup, int type) {
        return createHolder(LayoutInflater.from(context).inflate(getCardViewResource(type), viewGroup, false), type);
    }

    @Override
    public void onBindViewHolder(H holder, final int position) {
        holder.itemView.setTag((holder.getHolderId() != 0) ? holder.getHolderId() : position);
        try {
            // setting up custom listeners
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (eventListeners != null) {
                        eventListeners.onItemClickListener(getItem(position), position);
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (eventListeners != null) {
                        eventListeners.onLongItemClickListener(getItem(position), position);
                        return true;
                    }
                    return false;
                }
            });
            bindHolder(holder, getItem(position), position);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    /**
     * Get item
     *
     * @param position int position to get
     * @return Model object
     */
    public M getItem(final int position) {
        return dataSet != null ? (!dataSet.isEmpty() && position < dataSet.size()) ? dataSet.get(position) : null : null;
    }

    /**
     * Change whole dataSet and notify the adapter
     *
     * @param dataSet new ArrayList of model to change
     */
    public void changeDataSet(List<M> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    /**
     * Add an item to current dataSet
     *
     * @param item Item to insert into the dataSet
     */
    public void addItem(M item) {
        if (dataSet == null)
            return;
        int position = dataSet.size();
        dataSet.add(item);
        notifyItemInserted(position);
    }

    /**
     * Add an item to current dataSet
     *
     * @param item Item to insert into the dataSet
     */
    public void addItem(M item,int position) {
        if (dataSet == null)
            return;
        dataSet.add(position,item);
        notifyItemInserted(position);
    }

    /**
     * Remove an item to current dataSet
     *
     * @param pos Item to remove into the dataSet
     */
    public M removeItem(int pos) {
        if (dataSet == null)
            return null;
        M item = dataSet.remove(pos);
        notifyItemRemoved(pos);
        return item;
    }

    /**
     * Remove an item to current dataSet
     *
     * @param rItem Item to remove into the dataSet
     */
    public boolean removeItem(M rItem) {
        if (dataSet == null)
            return false;
        int pos = dataSet.indexOf(rItem);
        boolean item = dataSet.remove(rItem);
        if (item)
            notifyItemRemoved(pos);
        return item;
    }

    public void setListEventListener(ListItemListeners<M> onEventListeners) {
        this.eventListeners = onEventListeners;
    }

    /**
     * Create a new holder instance
     *
     * @param itemView View object
     * @param type     int type
     * @return TransactionViewHolderBase
     */
    protected abstract H createHolder(View itemView, int type);

    /**
     * Get custom layout to use it.
     *
     * @return int Layout Resource id: Example: R.layout.row_item
     */
    protected abstract int getCardViewResource(int type);

    /**
     * Bind TransactionViewHolderBase
     *
     * @param holder   TransactionViewHolderBase object
     * @param data     Object data to render
     * @param position position to render
     */
    protected abstract void bindHolder(H holder, M data, int position);


    public Context getContext() {
        return context;
    }

}