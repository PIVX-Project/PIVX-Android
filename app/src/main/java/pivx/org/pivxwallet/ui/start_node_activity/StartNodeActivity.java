package pivx.org.pivxwallet.ui.start_node_activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import global.PivtrumGlobalData;
import pivtrum.PivtrumPeerData;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;
import pivx.org.pivxwallet.utils.Dialogs;

/**
 * Created by Neoperol on 6/27/17.
 */

public class StartNodeActivity extends BaseActivity {

    private Button openDialog;
    private Button btnSelectNode;
    private EditText tcpText;
    private EditText sslText;
    private EditText hostText;
    private Spinner dropdown;
    private ArrayAdapter<String> adapter;
    private List<String> hosts = new ArrayList<>();

    private List<PivtrumPeerData> trustedNodes = PivtrumGlobalData.listTrustedHosts();

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        getLayoutInflater().inflate(R.layout.fragment_start_node, container);
        setTitle("Select Node");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Open Dialog
        openDialog = (Button) findViewById(R.id.openDialog);
        openDialog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                DialogBuilder dialogBuilder = Dialogs.buildtrustedNodeDialog(view.getContext(), new Dialogs.TrustedNodeDialogListener() {
                    @Override
                    public void onNodeSelected(PivtrumPeerData pivtrumPeerData) {
                        trustedNodes.add(pivtrumPeerData);
                        hosts.add(pivtrumPeerData.getHost());
                        adapter.clear();
                        adapter.addAll(hosts);
                        dropdown.setSelection(hosts.size()-1);
                    }
                });
                dialogBuilder.show();
            }

        });

        tcpText = (EditText) findViewById(R.id.tcpText);
        sslText = (EditText) findViewById(R.id.sslText);
        hostText = (EditText) findViewById(R.id.hostText);

        // Node selected
        btnSelectNode = (Button) findViewById(R.id.btnSelectNode);
        btnSelectNode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = dropdown.getSelectedItemPosition();
                PivtrumPeerData selectedNode = trustedNodes.get(selected);
                if (pivxApplication.getAppConf().getTrustedNode()!=null){
                    pivxApplication.stopBlockchain();
                }
                pivxApplication.setTrustedServer(selectedNode);
                pivxApplication.getAppConf().setAppInit(true);
                // now that everything is good, start the service
                pivxApplication.startPivxService();
                goNext();
                finish();
            }
        });

        dropdown = (Spinner)findViewById(R.id.spinner);

        for (PivtrumPeerData trustedNode : trustedNodes) {
            hosts.add(trustedNode.getHost());
        }
        adapter = new ArrayAdapter<String>(this, R.layout.simple_spinner_dropdown_item,hosts){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                view.setPadding(16, 16, 16, 16);
                return view;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.BLACK);
                view.setPadding(8, 8, 8, 8);
                return view;
            }
        };
        dropdown.setAdapter(adapter);
    }

    private void goNext() {
        Intent intent = new Intent(this, WalletActivity.class);
        startActivity(intent);
    }

    public static int convertDpToPx(Resources resources, int dp){
        return Math.round(dp*(resources.getDisplayMetrics().xdpi/ DisplayMetrics.DENSITY_DEFAULT));
    }

}
