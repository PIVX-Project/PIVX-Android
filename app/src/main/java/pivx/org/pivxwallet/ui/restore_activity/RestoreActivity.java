package pivx.org.pivxwallet.ui.restore_activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
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
import java.util.concurrent.atomic.AtomicBoolean;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.DialogListener;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.ui.tutorial_activity.TutorialActivity;
import pivx.org.pivxwallet.ui.words_restore_activity.RestoreWordsActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;
import wallet.Crypto;
import wallet.WalletUtils;
import wallet.exceptions.CantRestoreEncryptedWallet;

/**
 * Created by Neoperol on 4/20/17.
 */

public class RestoreActivity extends BaseActivity {
    public static final String ACTION_RESTORE_AND_JUMP_TO_WIZARD = "jump_to_wizard";
    private static final int OPTIONS_RESTORE = 1;
    private static final int OPTIONS_ADVANCE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 502;
    private View root;
    private TextInputEditText edit_password;
    private Spinner spinnerFiles;
    private TextView restoreMessage;
    private Button btn_restore;
    private ProgressBar progress;
    private AtomicBoolean flag = new AtomicBoolean(false);
    private FileAdapter fileAdapter;
    private List<File> files = new LinkedList<File>();

    private boolean jumpToWizard = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(0,OPTIONS_RESTORE,0,R.string.restore_from_words);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == OPTIONS_RESTORE){
            Intent myIntent = new Intent(getApplicationContext(), RestoreWordsActivity.class);
            startActivity(myIntent);
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
        if (getIntent()!=null){
            if (getIntent().getAction()!=null) {
                if (getIntent().getAction().equals(ACTION_RESTORE_AND_JUMP_TO_WIZARD)){
                    jumpToWizard = true;
                }
            }
        }

        btn_restore = (Button) findViewById(R.id.btn_restore);
        btn_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
            }
        });
        progress = (ProgressBar) root.findViewById(R.id.progress);
        restoreMessage = (TextView) root.findViewById(R.id.restoreMessage);
        edit_password = (TextInputEditText) root.findViewById(R.id.edit_password);
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
        if (!flag.getAndSet(true)) {
            progress.setVisibility(View.VISIBLE);
            final String password = edit_password.getText().toString().trim();
            edit_password.setText(null); // get rid of it asap
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        org.pivxj.core.Context.propagate(PivxContext.CONTEXT);
                        File file = (File) spinnerFiles.getSelectedItem();
                        if (WalletUtils.BACKUP_FILE_FILTER.accept(file)) {
                            pivxModule.restoreWallet(file);
                            showRestoreSucced();
                        } else if (KEYS_FILE_FILTER.accept(file)) {
                            //module.restorePrivateKeysFromBase58(file);
                        } else if (Crypto.OPENSSL_FILE_FILTER.accept(file)) {
                            try {
                                pivxModule.restoreWalletFromEncrypted(file, password);
                                showRestoreSucced();
                            } catch (final CantRestoreEncryptedWallet x) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DialogsUtil.buildSimpleErrorTextDialog(
                                                RestoreActivity.this,
                                                getString(R.string.import_export_keys_dialog_failure_title),
                                                getString(R.string.import_keys_dialog_failure, x.getMessage())
                                        ).setOkBtnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                restore();
                                            }
                                        }).show(getFragmentManager(), getString(R.string.restore_dialog_error));
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(RestoreActivity.this, R.string.cannot_restore_wallet, Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RestoreActivity.this, R.string.cannot_restore_wallet, Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RestoreActivity.this, R.string.cannot_restore_wallet, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.GONE);
                            flag.set(false);
                        }
                    });
                }
            }).start();
        }
    }

    private void showRestoreSucced() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String message = getString(R.string.restore_wallet_dialog_success_replay);

                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleTextDialog(RestoreActivity.this,getString(R.string.restore_wallet_dialog_success),message);
                simpleTextDialog.setOkBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (jumpToWizard){
                            startActivity(new Intent(RestoreActivity.this, TutorialActivity.class));
                        }
                        finish();
                    }
                });
                simpleTextDialog.setListener(new DialogListener() {
                    @Override
                    public void cancel(boolean isActionCompleted) {
                        if (jumpToWizard){
                            startActivity(new Intent(RestoreActivity.this, TutorialActivity.class));
                        }
                        finish();
                    }
                });
                simpleTextDialog.show(getFragmentManager(),getResources().getString(R.string.restore_dialog_tag));

                if (!jumpToWizard) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pivxApplication.startPivxService();
                        }
                    }, TimeUnit.SECONDS.toMillis(5));
                }
            }
        });
    }

    private void init(){

        files.clear();

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
                if (file==null)return false;
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
