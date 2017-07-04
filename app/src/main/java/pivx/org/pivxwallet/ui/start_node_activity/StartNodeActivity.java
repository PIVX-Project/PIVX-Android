package pivx.org.pivxwallet.ui.start_node_activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
    private List<String> hosts = new ArrayList<>();

    private List<PivtrumPeerData> trustedNodes = PivtrumGlobalData.listTrustedHosts();

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        if (pivxApplication.getAppConf().getTrustedNode()==null){
            goNext();
            finish();
        }

        getLayoutInflater().inflate(R.layout.fragment_start_node, container);
        setTitle("Select Node");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Open Dialog
        openDialog = (Button) findViewById(R.id.openDialog);
        openDialog.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LayoutInflater content = LayoutInflater.from(StartNodeActivity.this);
                View dialogView = content.inflate(R.layout.dialog_node, null);
                DialogBuilder nodeDialog = new DialogBuilder(StartNodeActivity.this);
                final EditText editHost = (EditText) dialogView.findViewById(R.id.hostText);
                final EditText editTcp = (EditText) dialogView.findViewById(R.id.tcpText);
                final EditText editSsl = (EditText) dialogView.findViewById(R.id.sslText);
                nodeDialog.setTitle("Add your Node");
                nodeDialog.setView(dialogView);
                nodeDialog.setPositiveButton("Add Node", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String host = editHost.getText().toString();
                        String tcpPort = editTcp.getText().toString();
                        String sslPort = editSsl.getText().toString();
                        Toast.makeText(StartNodeActivity.this,"Add new node not implemented yet",Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
                nodeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                nodeDialog.show();
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
                pivxApplication.setTrustedServer(selectedNode);
                pivxApplication.getAppConf().setAppInit(true);
                goNext();
                finish();
            }
        });

        dropdown = (Spinner)findViewById(R.id.spinner);

        for (PivtrumPeerData trustedNode : trustedNodes) {
            hosts.add(trustedNode.getHost());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,hosts){
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }

            @NonNull
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                view.setTextColor(Color.WHITE);
                return view;
            }
        };
        dropdown.setAdapter(adapter);
    }

    private void goNext() {
        Intent intent = new Intent(this, WalletActivity.class);
        startActivity(intent);
    }

}
