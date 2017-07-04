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
import pivx.org.pivxwallet.BuildConfig;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.restore_activity.RestoreActivity;
import pivx.org.pivxwallet.ui.settings_backup_activity.SettingsBackupActivity;
import pivx.org.pivxwallet.ui.settings_network_activity.SettingsNetworkActivity;
import pivx.org.pivxwallet.ui.settings_pincode_activity.SettingsPincodeActivity;
import pivx.org.pivxwallet.utils.DialogBuilder;

/**
 * Created by Neoperol on 5/11/17.
 */

public class SettingsActivity extends BaseDrawerActivity implements View.OnClickListener {

    private Button buttonBackup;
    private Button buttonRestore;
    private Button buttonChange;
    private Button buttonCurrency;
    private TextView textAbout;
    private TextView txt_network_info;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_settings, container);
        setTitle("Settings");

        TextView app_version = (TextView) findViewById(R.id.app_version);
        app_version.setText(BuildConfig.VERSION_NAME);

        txt_network_info = (TextView) findViewById(R.id.txt_network_info);

        textAbout = (TextView)findViewById(R.id.text_about);
        String text = "Made by<br> <font color=#55476c>Furszy</font> <br>(c) PIVX Community";
        textAbout.setText(Html.fromHtml(text));
        // Open Backup Wallet
        buttonBackup = (Button) findViewById(R.id.btn_backup_wallet);
        buttonBackup.setOnClickListener(this);

        // Open Restore Wallet
        buttonRestore = (Button) findViewById(R.id.btn_restore_wallet);
        buttonRestore.setOnClickListener(this);

        // Open Change Pincode
        buttonChange = (Button) findViewById(R.id.btn_change_pincode);
        buttonChange.setOnClickListener(this);

        // Open Network Monitor
        buttonChange = (Button) findViewById(R.id.btn_network);
        buttonChange.setOnClickListener(this);

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

        updateNetworkStatus();
    }

    private void updateNetworkStatus() {
        txt_network_info.setText(
                Html.fromHtml(
                        "Network<br><font color=#55476c>"+pivxModule.getConf().getNetworkParams().getId()+
                                "</font><br>" +
                                "Height<br><font color=#55476c>"+pivxModule.getChainHeight()+"</font>"
                )
        );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_backup_wallet){
            Intent myIntent = new Intent(v.getContext(), SettingsBackupActivity.class);
            startActivity(myIntent);
        }else if (id == R.id.btn_restore_wallet){
            Intent myIntent = new Intent(v.getContext(), RestoreActivity.class);
            startActivity(myIntent);
        }else if (id == R.id.btn_change_pincode){
            Intent myIntent = new Intent(v.getContext(), SettingsPincodeActivity.class);
            startActivityForResult(myIntent, 0);
        }else if (id == R.id.btn_network){
            startActivity(new Intent(v.getContext(),SettingsNetworkActivity.class));
        }
    }
}
