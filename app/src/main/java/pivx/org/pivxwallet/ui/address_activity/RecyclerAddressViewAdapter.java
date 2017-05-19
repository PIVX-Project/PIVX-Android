package pivx.org.pivxwallet.ui.address_activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/18/17.
 */

public class RecyclerAddressViewAdapter extends RecyclerView.Adapter<AddressViewHolder>{

    List<AddressData> list = Collections.emptyList();
    Context context;

    public RecyclerAddressViewAdapter(List<AddressData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public AddressViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_row, parent, false);
        AddressViewHolder holder = new AddressViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(AddressViewHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.name.setText(list.get(position).name);
        holder.address.setText(list.get(position).address);


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
    public void insert(int position, AddressData data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(AddressData data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }

}
