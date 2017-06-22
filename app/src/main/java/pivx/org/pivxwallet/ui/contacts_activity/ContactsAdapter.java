package pivx.org.pivxwallet.ui.contacts_activity;

import android.content.Context;
import android.view.View;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.ui.base.tools.adapter.RecyclerAdapter;

/**
 * Created by Neoperol on 5/18/17.
 */

public class ContactsAdapter extends RecyclerAdapter<Contact,ContactViewHolder> {

    public ContactsAdapter(Context context) {
        super(context);
    }

    @Override
    protected ContactViewHolder createHolder(View itemView, int type) {
        return new ContactViewHolder(itemView);
    }

    @Override
    protected int getCardViewResource(int type) {
        return R.layout.contact_row;
    }

    @Override
    protected void bindHolder(ContactViewHolder holder, Contact data, int position) {
        holder.address.setText(data.getAddresses().get(0));
        holder.name.setText(data.getName());
    }

}
