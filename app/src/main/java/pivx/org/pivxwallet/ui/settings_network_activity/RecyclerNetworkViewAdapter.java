package pivx.org.pivxwallet.ui.settings_network_activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 6/8/17.
 */

public class RecyclerNetworkViewAdapter extends RecyclerView.Adapter<NetworkViewHolder> {

    List<NetworkData> list = Collections.emptyList();
    Context context;

    public RecyclerNetworkViewAdapter(List<NetworkData> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public NetworkViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.network_row, parent, false);
        NetworkViewHolder holder = new NetworkViewHolder(v);
        return holder;

    }

    @Override
    public void onBindViewHolder(NetworkViewHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.address.setText(list.get(position).address);
        holder.network_ip.setText(list.get(position).network_ip);
        holder.protocol.setText(list.get(position).protocol);
        holder.blocks.setText(list.get(position).blocks);
        holder.speed.setText(list.get(position).speed);
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
    public void insert(int position, NetworkData data) {
        list.add(position, data);
        notifyItemInserted(position);
    }

    // Remove a RecyclerView item containing a specified Data object
    public void remove(NetworkData data) {
        int position = list.indexOf(data);
        list.remove(position);
        notifyItemRemoved(position);
    }
}
