package pivx.org.pivxwallet.ui.start_node_activity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 6/27/17.
 */

public class StartNodeActivity extends BaseActivity {
    Button openDialog;
    Button btnSelectNode;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_start_node, container);
        setTitle("Select Node");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Open Dialog
        openDialog = (Button) findViewById(R.id.openDialog);

        // Node selected
        btnSelectNode = (Button) findViewById(R.id.btnSelectNode);

        Spinner dropdown = (Spinner)findViewById(R.id.spinner);
        String[] items = new String[]{"Nodes Number 1 available", "Nodes Number 2 available", "Nodes Number 3 available"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);
    }
}
