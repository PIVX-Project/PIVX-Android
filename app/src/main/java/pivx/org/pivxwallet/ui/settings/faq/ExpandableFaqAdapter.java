package pivx.org.pivxwallet.ui.settings.faq;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import java.util.List;

import pivx.org.pivxwallet.R;

public class ExpandableFaqAdapter extends ExpandableRecyclerAdapter<StringParent, String, ParentFaqHolder, ChildFaqHolder >{

    private final LayoutInflater mInflater;

    /**
     * Primary constructor. Sets up {@link #mParentList} and {@link #mFlatItemList}.
     * <p>
     * Any changes to {@link #mParentList} should be made on the original instance, and notified via
     * {@link #notifyParentInserted(int)}
     * {@link #notifyParentRemoved(int)}
     * {@link #notifyParentChanged(int)}
     * {@link #notifyParentRangeInserted(int, int)}
     * {@link #notifyChildInserted(int, int)}
     * {@link #notifyChildRemoved(int, int)}
     * {@link #notifyChildChanged(int, int)}
     * methods and not the notify methods of RecyclerView.Adapter.
     *
     * @param parentList List of all parents to be displayed in the RecyclerView that this
     *                   adapter is linked to
     */
    public ExpandableFaqAdapter(Context context, @NonNull List<StringParent> parentList) {
        super(parentList);
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ParentFaqHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.settings_faq_row_header, parentViewGroup, false);
        return new ParentFaqHolder(view);
    }

    @NonNull
    @Override
    public ChildFaqHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View view = mInflater.inflate(R.layout.settings_faq_row_body, childViewGroup, false);
        return new ChildFaqHolder(view);
    }

    @Override
    public void onBindParentViewHolder(@NonNull ParentFaqHolder parentViewHolder, int parentPosition, @NonNull StringParent parent) {
        parentViewHolder.textView.setText(parent.text);
    }

    @Override
    public void onBindChildViewHolder(@NonNull ChildFaqHolder childViewHolder, int parentPosition, int childPosition, @NonNull String child) {
        childViewHolder.textView.setText(child);
    }
}
