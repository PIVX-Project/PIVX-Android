package pivx.org.pivxwallet.ui.settings_activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.settings_backup_activity.SettingsBackupActivity;
import pivx.org.pivxwallet.ui.settings_pincode_activity.SettingsPincodeActivity;
import pivx.org.pivxwallet.ui.settings_restore_activity.SettingsRestoreActivity;

/**
 * Created by Neoperol on 5/11/17.
 */

public class SettingsActivity extends BaseDrawerActivity {
    Button buttonBackup;
    Button buttonRestore;
    Button buttonChange;
    Button buttonCurrency;
    TextView textAbout;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_settings, container);
        setTitle("Settings");

        textAbout = (TextView)findViewById(R.id.text_about);
        textAbout.setText("Hello");
        String text = "MadeBy<br> <font color=#55476c>IoP Ventures LLC</font>  <br>Foundet <br> <font color=#55476c> By the Decentralized Society Foundation</font> <br>(c) PIVX Community";
        textAbout.setText(Html.fromHtml(text));
        // Open Backup Wallet
        buttonBackup = (Button) findViewById(R.id.btn_backup_wallet);
        buttonBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SettingsBackupActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Backup Wallet
        buttonRestore = (Button) findViewById(R.id.btn_restore_wallet);
        buttonRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SettingsRestoreActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Backup Wallet
        buttonChange = (Button) findViewById(R.id.btn_change_pincode);
        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SettingsPincodeActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Dialog
        buttonCurrency = (Button) findViewById(R.id.btn_local_currency);
        buttonCurrency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Intent myIntent = new Intent(view.getContext(), agones.class);
                //startActivityForResult(myIntent, 0);


                AlertDialog alertDialog = new AlertDialog.Builder(SettingsActivity.this).create(); //Read Update
                alertDialog.setTitle("hi");
                alertDialog.setMessage("this is my app");

                alertDialog.setButton("Continue..", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // here you can add functions
                    }
                });

                alertDialog.show();  //<-- See This!
            }

        });
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(2);
    }
}
