package pivx.org.pivxwallet.ui.address_activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import pivx.org.pivxwallet.ui.address_add_activity.AddressAddActivity;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/11/17.
 */


public class AddressActivity extends BaseDrawerActivity {
    RecyclerView recyclerView;
    private RecyclerAddressViewAdapter adapter;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_address, container);
        setTitle("Address Book");

        // Recicler view
        List<AddressData> data = fill_with_data();
        recyclerView = (RecyclerView) findViewById(R.id.addressList);
        adapter = new RecyclerAddressViewAdapter(data, getApplication());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check current activity in the navigation drawer
        setNavigationMenuItemChecked(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.address, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_add:
                Intent intent = new Intent(this, AddressAddActivity.class);
                //      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void actionAdd(View view) {
        Intent intent = new Intent(AddressActivity.this, AddressAddActivity.class);
        startActivity(intent);
    }


    //Create a list of Data objects
    public List<AddressData> fill_with_data() {

        List<AddressData> data = new ArrayList<>();

        data.add(new AddressData("Antonio Lyons", "1BoatSLRHtKNngkdXEeobR76b53LETtpyT" ));
        data.add(new AddressData("Daniel Hardy", "1BoatSLRHtKNngkdXEeobR76b53LETtpyT" ));
        data.add(new AddressData("Nell Gutierrez", "1BoatSLRHtKNngkdXEeobR76b53LETtpyT" ));
        data.add(new AddressData("Edith Little", "1BoatSLRHtKNngkdXEeobR76b53LETtpyT" ));
        data.add(new AddressData("Antonio Lyons", "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"));
        data.add(new AddressData("Nell Gutierrez", "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"));


        return data;
    }
}
