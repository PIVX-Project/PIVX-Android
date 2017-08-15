package pivx.org.pivxwallet.ui.contacts_activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.ui.address_add_activity.AddContactActivity;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.tools.adapter.ListItemListeners;

/**
 * Created by Neoperol on 5/11/17.
 */


public class ContactsActivity extends BaseDrawerActivity implements ListItemListeners<Contact> {

    RecyclerView recyclerView;
    private ContactsAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private Collection<Contact> contacts;
    private LinearLayout emptyView;
    private ExecutorService executor;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_address, container);
        setTitle("Contacts Book");
        recyclerView = (RecyclerView) findViewById(R.id.addressList);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ContactsAdapter(this);
        adapter.setListEventListener(this);
        recyclerView.setAdapter(adapter);
        emptyView = (LinearLayout) findViewById(R.id.empty_view);
        emptyView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check current activity in the navigation drawer
        setNavigationMenuItemChecked(1);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // re load
        load();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (executor!=null){
            executor.shutdownNow();
            executor = null;
        }
    }

    private void load() {
        // add loading..
        if (executor==null){
            executor = Executors.newSingleThreadExecutor();
        }
        executor.submit(new Runnable() {
            @Override
            public void run() {
                contacts = pivxModule.getContacts();
                if (contacts!=null && !contacts.isEmpty())
                    adapter.changeDataSet(new ArrayList(contacts));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (contacts==null || contacts.isEmpty()) {
                            emptyView.setVisibility(View.VISIBLE);
                        }else {
                            emptyView.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, AddContactActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onItemClickListener(Contact data, int position) {

    }

    @Override
    public void onLongItemClickListener(Contact data, int position) {

    }
}
