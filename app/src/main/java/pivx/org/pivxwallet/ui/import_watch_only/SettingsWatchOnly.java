package pivx.org.pivxwallet.ui.import_watch_only;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.pivxj.wallet.DeterministicKeyChain;

import java.io.IOException;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.base.dialogs.DialogListener;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.utils.DialogsUtil;

import static pivx.org.pivxwallet.utils.CrashReporter.appendSavedBackgroundTraces;

/**
 * Created by furszy on 8/30/17.
 */

public class SettingsWatchOnly extends BaseActivity {

    private View root;
    private Button btn_import;
    private EditText edit_xpub;
    private CheckBox check_bip32;
    private ProgressBar progress;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        setTitle(R.string.screen_title_watch_only);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        root = getLayoutInflater().inflate(R.layout.import_watch_only_main,container);
        btn_import = (Button) root.findViewById(R.id.btn_import);
        edit_xpub = (EditText) root.findViewById(R.id.edit_xpub);
        check_bip32 = (CheckBox) root.findViewById(R.id.check_bip32);
        progress = (ProgressBar) root.findViewById(R.id.progress);
        btn_import.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    importXpub();
                }catch (Exception e){
                    e.printStackTrace();
                    showError();
                    appendSavedBackgroundTraces(e);
                }
            }
        });
    }

    private void importXpub() {
        String xpub = edit_xpub.getText().toString();
        boolean isBip32 = check_bip32.isChecked();
        if (xpub.length()>0){
            try {
                pivxModule.watchOnlyMode(
                        xpub,
                        isBip32 ? DeterministicKeyChain.KeyChainType.BIP32: DeterministicKeyChain.KeyChainType.BIP44_PIVX_ONLY
                );
                SimpleTextDialog simpleTextDialog = DialogsUtil.buildSimpleTextDialog(
                        this,
                        getString(R.string.watch_only_mode_activated),
                        getString(R.string.watch_only_mode_activated_text)
                );
                simpleTextDialog.setListener(new DialogListener() {
                    @Override
                    public void cancel(boolean isActionCompleted) {
                        finish();
                    }
                });
                simpleTextDialog.show(getFragmentManager(),"watch_only_dialog");
            } catch (IOException e) {
                e.printStackTrace();
                showError();
            }
        }else {
            Toast.makeText(this,R.string.invalid_inputs,Toast.LENGTH_SHORT).show();
        }
    }

    private void showError(){
        DialogsUtil.buildSimpleErrorTextDialog(
                this,
                getString(R.string.error_importing_xpub_title),
                getString(R.string.error_importing_xpub
                )
        ).show(getFragmentManager(),"error_importing_xpub");
    }
}
