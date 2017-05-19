package pivx.org.pivxwallet.ui.address_activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.MainActivity;
import pivx.org.pivxwallet.R;

/**
 * Created by Neoperol on 5/11/17.
 */

public class AddressActivity extends MainActivity {
    RecyclerView recyclerView;
    private RecyclerAddressViewAdapter adapter;
    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getLayoutInflater().inflate(R.layout.fragment_address, frameLayout);
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
        // to check current activity in the navigation drawer
        navigationView.getMenu().getItem(1).setChecked(true);
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
