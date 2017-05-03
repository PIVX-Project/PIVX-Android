package pivx.org.pivxwallet;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * Created by Neoperol on 5/3/17.
 */


public class RecyclerViewAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<TransactionData> list = Collections.emptyList();
    Context context;

    public RecyclerViewAdapter(List<TransactionData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_row, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.setText(list.get(position).title);
        holder.description.setText(list.get(position).description);
        holder.imageView.setImageResource(list.get(position).imageId);
        holder.amount.setText(list.get(position).amount);
        holder.amountLocal.setText(list.get(position).amountLocal);

        //animate(holder);

    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    // Insert a new item to the RecyclerView on a predefined position
    public void insert(int position, TransactionData data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(TransactionData data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }

}