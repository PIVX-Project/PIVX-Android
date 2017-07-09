package pivx.org.pivxwallet.ui.restore_activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.codec.Charsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.module.wallet.WalletBackupHelper;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.utils.DialogBuilder;
import pivx.org.pivxwallet.utils.DialogsUtil;
import wallet.Crypto;
import wallet.WalletUtils;
import wallet.exceptions.CantRestoreEncryptedWallet;

/**
 * Created by Neoperol on 4/20/17.
 */

public class RestoreActivity extends BaseActivity {
    private static final int OPTIONS_RESTORE = 1;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 502;
    private View root;
    private EditText edit_password;
    private Spinner spinnerFiles;
    private TextView restoreMessage;
    private Button btn_restore;
    private FileAdapter fileAdapter;
    private List<File> files = new LinkedList<File>();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(0,OPTIONS_RESTORE,0,R.string.backup_restore);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == OPTIONS_RESTORE){
            restore();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_settings_restore, container);
        setTitle("Restore wallet");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        restoreMessage = (TextView) root.findViewById(R.id.restoreMessage);
        edit_password = (EditText) root.findViewById(R.id.edit_password);
        spinnerFiles = (Spinner) root.findViewById(R.id.spinner_files);
        fileAdapter = new FileAdapter(this) {
            @Override
            public View getDropDownView(int position, View row, ViewGroup parent) {
                final File file = getItem(position);
                final boolean isExternal = PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR.equals(file.getParentFile());
                final boolean isEncrypted = Crypto.OPENSSL_FILE_FILTER.accept(file);

                if (row == null)
                    row = inflater.inflate(R.layout.backup_file_row, null);

                final TextView filenameView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_filename);
                filenameView.setText(file.getName());

                final TextView securityView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_security);
                /*final String encryptedStr = context.getString(isEncrypted ? R.string.import_keys_dialog_file_security_encrypted
                        : R.string.import_keys_dialog_file_security_unencrypted);
                final String storageStr = context.getString(isExternal ? R.string.import_keys_dialog_file_security_external
                        : R.string.import_keys_dialog_file_security_internal);
                securityView.setText(encryptedStr + ", " + storageStr);*/

                final TextView createdView = (TextView) row.findViewById(R.id.wallet_import_keys_file_row_created);
                /*createdView
                        .setText(context.getString(isExternal ? R.string.import_keys_dialog_file_created_manual
                                : R.string.import_keys_dialog_file_created_automatic, DateUtils.getRelativeTimeSpanString(context,
                                file.lastModified(), true)));*/

                return row;
            }
        };
        final String path;
        final String backupPath = PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR.getAbsolutePath();
        final String storagePath = PivxContext.Files.EXTERNAL_STORAGE_DIR.getAbsolutePath();
        if (backupPath.startsWith(storagePath))
            path = backupPath.substring(storagePath.length());
        else
            path = backupPath;

        restoreMessage.setText(getString(R.string.import_keys_dialog_message, path));

        spinnerFiles.setAdapter(fileAdapter);

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void restore() {
        try {
            String password = edit_password.getText().toString().trim();
            edit_password.setText(null); // get rid of it asap
            File file = (File) spinnerFiles.getSelectedItem();
            if (WalletUtils.BACKUP_FILE_FILTER.accept(file)){
                pivxModule.restoreWallet(file);
                showRestoreSucced();
            }else if (KEYS_FILE_FILTER.accept(file)) {
                //module.restorePrivateKeysFromBase58(file);
            }else if (Crypto.OPENSSL_FILE_FILTER.accept(file)) {
                try {
                    pivxModule.restoreWalletFromEncrypted(file, password);
                    showRestoreSucced();
                } catch (CantRestoreEncryptedWallet x) {
                    final DialogBuilder warnDialog = DialogBuilder.warn(this, R.string.import_export_keys_dialog_failure_title);
                    warnDialog.setMessage(getString(R.string.import_keys_dialog_failure, x.getMessage()));
                    warnDialog.setPositiveButton(R.string.button_dismiss, null);
                    warnDialog.setNegativeButton(R.string.button_retry, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            restore();
                            dialog.dismiss();
                        }
                    });
                    warnDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error, please check logs", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void showRestoreSucced() {
        String message = this.getString(R.string.restore_wallet_dialog_success) +
                "\n\n" +
                this.getString(R.string.restore_wallet_dialog_success_replay);

        DialogsUtil.buildSimpleTextDialog(this,null,message)
                .show(getFragmentManager(),getResources().getString(R.string.restore_dialog_tag));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                pivxApplication.startPivxService();
            }
        }, TimeUnit.SECONDS.toMillis(5));
    }

    private void init(){

        // external storage
        if (PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR.exists() && PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR.isDirectory()) {
            File[] fileArray = PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR.listFiles();
            if (fileArray!=null) {
                for (final File file : fileArray)
                    if (Crypto.OPENSSL_FILE_FILTER.accept(file))
                        files.add(file);
            }
        }
        // internal storage
        for (final String filename : fileList())
            if (filename.startsWith(PivxContext.Files.WALLET_KEY_BACKUP_PROTOBUF + '.'))
                files.add(new File(getFilesDir(), filename));

        // sort
        Collections.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(final File lhs, final File rhs)
            {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });

        fileAdapter.setFiles(files);
    }

    private void checkPermissions() {
        // Assume thisActivity is the current activity
        if (Build.VERSION.SDK_INT > 22) {

            int permissionCheck = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_READ_EXTERNAL);

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //backup
                    restore();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to write your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public static final FileFilter KEYS_FILE_FILTER = new FileFilter() {

        @Override
        public boolean accept(final File file) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charsets.UTF_8));
                WalletUtils.readKeys(reader, PivxContext.NETWORK_PARAMETERS,PivxContext.BACKUP_MAX_CHARS);
                return true;
            } catch (final IOException x) {
                return false;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException x) {
                        // swallow
                    }
                }
            }
        }
    };
}
