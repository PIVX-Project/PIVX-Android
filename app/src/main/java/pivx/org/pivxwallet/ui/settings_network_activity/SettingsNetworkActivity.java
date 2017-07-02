package pivx.org.pivxwallet.ui.settings_network_activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 6/8/17.
 */

public class SettingsNetworkActivity extends BaseActivity {

    View root;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_network, container);
        setTitle("Network Monitor");
    }

    //Create a list of Data objects
    public List<NetworkData> fill_with_data() {

        List<NetworkData> data = new ArrayList<>();

        data.add(new NetworkData("237.120.211.120.bcludusercontent.com", "/Pivx:4.0.2.53/", "protocol:70014", "38123 Blocks", "140ms" ));
        data.add(new NetworkData("237.120.211.120.bcludusercontent.com", "/Pivx:4.0.2.53/", "protocol:70014", "38123 Blocks", "140ms" ));
        data.add(new NetworkData("237.120.211.120.bcludusercontent.com", "/Pivx:4.0.2.53/", "protocol:70014", "38123 Blocks", "140ms" ));
        data.add(new NetworkData("237.120.211.120.bcludusercontent.com", "/Pivx:4.0.2.53/", "protocol:70014", "38123 Blocks", "140ms" ));
        data.add(new NetworkData("237.120.211.120.bcludusercontent.com", "/Pivx:4.0.2.53/", "protocol:70014", "38123 Blocks", "140ms"));
        data.add(new NetworkData("237.120.211.120.bcludusercontent.com", "/Pivx:4.0.2.53/", "protocol:70014", "38123 Blocks", "140ms"));


        return data;
    }
}
