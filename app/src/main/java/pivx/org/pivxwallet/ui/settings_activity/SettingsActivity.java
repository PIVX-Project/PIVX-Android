package pivx.org.pivxwallet.ui.settings_activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.restore_activity.RestoreActivity;
import pivx.org.pivxwallet.ui.settings_backup_activity.SettingsBackupActivity;
import pivx.org.pivxwallet.ui.settings_network_activity.SettingsNetworkActivity;
import pivx.org.pivxwallet.ui.settings_pincode_activity.SettingsPincodeActivity;
import pivx.org.pivxwallet.ui.settings_restore_activity.SettingsRestoreActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;

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
        String text = "Made by<br> <font color=#55476c>Furszy</font> <br>(c) PIVX Community";
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

        // Open Restore Wallet
        buttonRestore = (Button) findViewById(R.id.btn_restore_wallet);
        buttonRestore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), RestoreActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Change Pincode
        buttonChange = (Button) findViewById(R.id.btn_change_pincode);
        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SettingsPincodeActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Network Monitor
        buttonChange = (Button) findViewById(R.id.btn_network);
        buttonChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SettingsNetworkActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        // Open Dialog
        buttonCurrency = (Button) findViewById(R.id.btn_local_currency);
        buttonCurrency.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                LayoutInflater content = LayoutInflater.from(SettingsActivity.this);
                View dialogView = content.inflate(R.layout.dialog_currency_picker, null);
                DialogBuilder currencyDialog = new DialogBuilder(SettingsActivity.this);
                currencyDialog.setView(dialogView);
                CharSequence items[] = new CharSequence[] {"USD", "GB", "Third"};
                currencyDialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface d, int n) {
                        // ...
                    }

                });
                currencyDialog.setPositiveButton("Select", null);
                currencyDialog.setNegativeButton("Cancel", null);
                currencyDialog.show();


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
