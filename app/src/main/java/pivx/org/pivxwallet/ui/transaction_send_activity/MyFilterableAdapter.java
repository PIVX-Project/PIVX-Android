package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import global.AddressLabel;

public class MyFilterableAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private List<AddressLabel> items;
    private List<AddressLabel> filteredItems = new ArrayList<>();
    private ItemFilter mFilter = new ItemFilter();

    public MyFilterableAdapter(Context context, List<AddressLabel> items) {
        //super(context, R.layout.your_row, items);
        this.context = context;
        this.items = items;
        this.filteredItems = items;
    }

    @Override
    public int getCount() {
        return filteredItems.size();
    }

    @Override
    public Object getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.name_row, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.txt_name);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String location = filteredItems.get(position).getName();
        if (!location.isEmpty() || viewHolder != null) {
            viewHolder.tvTitle.setText(location);
        }
        return convertView;
    }

    public static class ViewHolder {
        TextView tvTitle;
    }

    private class ItemFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint!=null? constraint.toString().toLowerCase():"";
            FilterResults results = new FilterResults();

            int count = items.size();
            final List<AddressLabel> tempItems = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                if (items.get(i).getName().toLowerCase().contains(filterString)) {
                    tempItems.add(items.get(i));
                }
            }

            results.values = tempItems;
            results.count = tempItems.size();

            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (ArrayList<AddressLabel>) results.values;
            notifyDataSetChanged();
        }
    }

    public Filter getFilter() {
        return mFilter;
    }


}