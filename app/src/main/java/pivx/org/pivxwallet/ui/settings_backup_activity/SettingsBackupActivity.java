package pivx.org.pivxwallet.ui.settings_backup_activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.wallet.WalletBackupHelper;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 5/18/17.
 */

public class SettingsBackupActivity extends BaseActivity {

    private static final int OPTIONS_CREATE = 1;

    private View root;
    private EditText edit_password;
    private EditText edit_repeat_password;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_settings_backup, container);
        setTitle("Backup Wallet");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        edit_password = (EditText) root.findViewById(R.id.edit_password);
        edit_repeat_password = (EditText) root.findViewById(R.id.edit_repeat_password);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(0,OPTIONS_CREATE,0,R.string.backup_create);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == OPTIONS_CREATE){
            backup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backup() {
        try {
            String firstPassword = edit_password.getText().toString();
            String repeatPassword = edit_repeat_password.getText().toString();
            if (!firstPassword.equals(repeatPassword)) {
                Toast.makeText(this, R.string.backup_passwords_doesnt_match, Toast.LENGTH_LONG).show();
                return;
            }
            File backupFile = new WalletBackupHelper().determineBackupFile();
            boolean result = pivxModule.backupWallet(backupFile, firstPassword);

            if (result){
                showSuccedBackupDialog(backupFile.getAbsolutePath());
            }else {
                Toast.makeText(this,"Backup fail",Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSuccedBackupDialog(final String backupAbsolutePath){
        Dialog dialog = new Dialog(this){
            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.backup_dialog);
                findViewById(R.id.txt_ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dismiss();
                        onBackPressed();
                    }
                });
                TextView textView = (TextView) findViewById(R.id.txt_backup_dir);
                textView.setText(backupAbsolutePath);
            }
        };
        dialog.show();
    }
}
