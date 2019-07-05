package pivx.org.pivxwallet.ui.settings.settings_activity;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import global.PivxModuleImp;
import host.furszy.zerocoinj.wallet.MultiWallet;
import pivx.org.pivxwallet.BuildConfig;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.ui.base.BaseDrawerActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.export_account.ExportKeyActivity;
import pivx.org.pivxwallet.ui.import_watch_only.SettingsWatchOnly;
import pivx.org.pivxwallet.ui.restore_activity.RestoreActivity;
import pivx.org.pivxwallet.ui.settings.faq.FaqActivity;
import pivx.org.pivxwallet.ui.settings.settings_backup_activity.SettingsBackupActivity;
import pivx.org.pivxwallet.ui.settings.settings_network_activity.SettingsNetworkActivity;
import pivx.org.pivxwallet.ui.settings.settings_pincode_activity.SettingsPincodeActivity;
import pivx.org.pivxwallet.ui.settings.settings_rates.SettingsRatesActivity;
import pivx.org.pivxwallet.ui.start_node_activity.StartNodeActivity;
import pivx.org.pivxwallet.ui.tutorial_activity.TutorialActivity;
import pivx.org.pivxwallet.utils.AndroidUtils;
import pivx.org.pivxwallet.utils.CrashReporter;
import pivx.org.pivxwallet.utils.DialogsUtil;
import pivx.org.pivxwallet.utils.IntentsUtils;
import pivx.org.pivxwallet.utils.NavigationUtils;
import pivx.org.pivxwallet.utils.ReportIssueDialogBuilder;

import static pivx.org.pivxwallet.ui.tutorial_activity.TutorialActivity.INTENT_EXTRA_INFO_TUTORIAL;

/**
 * Created by Neoperol on 5/11/17.
 */

public class SettingsActivity extends BaseDrawerActivity implements View.OnClickListener {
    private Button buttonBackup;
    private Button buttonRestore;
    private Button btn_export_pub_key;
    private Button btn_import_xpub;
    private Button buttonChange;
    private Button btn_change_node;
    private Button btn_reset_blockchain;
    private Button btn_reset_blockchain_to;
    private Button btn_report;
    private Button btn_support;
    private Button buttonTutorial, btn_faq;
    private TextView textAbout, text_rates;
    private TextView txt_network_info;

    private final int REQUEST_WRITE_PERMISSION = 300;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_settings, container);
        setTitle(R.string.title_settings);

        TextView app_version = (TextView) findViewById(R.id.app_version);
        app_version.setText(BuildConfig.VERSION_NAME);

        txt_network_info = (TextView) findViewById(R.id.txt_network_info);

        textAbout = (TextView)findViewById(R.id.text_about);
        String text = "Made by<br> <font color=#5c4c7c>Furszy</font> <br>PIVX Community © 2018";
        textAbout.setText(Html.fromHtml(text));
        // Open Backup Wallet
        buttonBackup = (Button) findViewById(R.id.btn_backup_wallet);
        buttonBackup.setOnClickListener(this);

        // Open Restore Wallet
        buttonRestore = (Button) findViewById(R.id.btn_restore_wallet);
        buttonRestore.setOnClickListener(this);

        btn_export_pub_key = (Button) findViewById(R.id.btn_export_pub_key);
        btn_export_pub_key.setOnClickListener(this);

        btn_import_xpub = (Button) findViewById(R.id.btn_import_xpub);
        btn_import_xpub.setOnClickListener(this);

        // Open Change Pincode
        buttonChange = (Button) findViewById(R.id.btn_change_pincode);
        buttonChange.setOnClickListener(this);

        btn_change_node = (Button) findViewById(R.id.btn_change_node);
        btn_change_node.setOnClickListener(this);

        btn_reset_blockchain = (Button) findViewById(R.id.btn_reset_blockchain);
        btn_reset_blockchain.setOnClickListener(this);

        btn_reset_blockchain_to = (Button) findViewById(R.id.btn_reset_blockchain_to);
        btn_reset_blockchain_to.setOnClickListener(this);

        // rates
        findViewById(R.id.btn_rates).setOnClickListener(this);
        text_rates = (TextView) findViewById(R.id.text_rates);
        text_rates.setText(pivxApplication.getAppConf().getSelectedRateCoin());

        // Open Network Monitor
        buttonChange = (Button) findViewById(R.id.btn_network);
        buttonChange.setOnClickListener(this);

        btn_report = (Button) findViewById(R.id.btn_report);
        btn_report.setOnClickListener(this);

        findViewById(R.id.btn_export_log).setOnClickListener(this);

        btn_support = (Button) findViewById(R.id.btn_support);
        btn_support.setOnClickListener(this);

        // Open Tutorial
        buttonTutorial = (Button) findViewById(R.id.btn_tutorial);
        buttonTutorial.setOnClickListener(this);

        btn_faq = (Button) findViewById(R.id.btn_faq);
        btn_faq.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // to check current activity in the navigation drawer
        setNavigationMenuItemChecked(3);
        updateNetworkStatus();
        text_rates.setText(pivxApplication.getAppConf().getSelectedRateCoin());
    }

    private void updateNetworkStatus() {
        // Check if the activity is on foreground
        if (!isOnForeground)return;
        txt_network_info.setText(
                Html.fromHtml(
                        "Network<br><font color=#5c4c7c>"+pivxModule.getConf().getNetworkParams().getId()+
                                "</font><br>" +
                                "Block Height<br><font color=#5c4c7c>"+pivxModule.getChainHeight()+"</font><br>" +
                                "Protocol Version<br><font color=#5c4c7c>"+
                                pivxModule.getProtocolVersion()+"</font>"

                )
        );
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_backup_wallet){
            Intent myIntent = new Intent(v.getContext(), SettingsBackupActivity.class);
            startActivity(myIntent);
        }else if (id == R.id.btn_tutorial){
            Intent myIntent = new Intent(v.getContext(), TutorialActivity.class);
            myIntent.putExtra(INTENT_EXTRA_INFO_TUTORIAL,true);
            startActivity(myIntent);
        }else if (id == R.id.btn_restore_wallet){
            Intent myIntent = new Intent(v.getContext(), RestoreActivity.class);
            startActivity(myIntent);
        }else if (id == R.id.btn_change_pincode){
            Intent myIntent = new Intent(v.getContext(), SettingsPincodeActivity.class);
            startActivity(myIntent);
        }else if (id == R.id.btn_network){
            startActivity(new Intent(v.getContext(),SettingsNetworkActivity.class));
        }else if(id == R.id.btn_change_node) {
            startActivity(new Intent(v.getContext(), StartNodeActivity.class));
        }else if(id == R.id.btn_reset_blockchain) {
            launchResetBlockchainDialog();
        }else if(id == R.id.btn_reset_blockchain_to){
            launchRollbackBlockchainTo();
        }else if (id == R.id.btn_report) {
            launchReportDialog(false);
        }else if (id == R.id.btn_export_log){
            launchReportDialog(true);
        }else if(id == R.id.btn_support){
            IntentsUtils.startSend(
                    this,
                    getString(R.string.support_subject),
                    getString(R.string.report_issue_dialog_message_issue),
                    new ArrayList<Uri>()
            );
        }else if (id == R.id.btn_export_pub_key){
            startActivity(new Intent(v.getContext(), ExportKeyActivity.class));
        }else if (id == R.id.btn_import_xpub){
            startActivity(new Intent(v.getContext(), SettingsWatchOnly.class));
        }else if (id == R.id.btn_rates){
            startActivity(new Intent(v.getContext(), SettingsRatesActivity.class));
        }else if (id == R.id.btn_faq){
            startActivity(new Intent(v.getContext(), FaqActivity.class));
        }
    }

    private void launchRollbackBlockchainTo() {
        SimpleEditDialogFragment dialog = new SimpleEditDialogFragment(this);
        dialog.setTitle("Rollback Chain");
        dialog.setTitleColor(Color.BLACK);
        dialog.setBody("You are going to rollback the chain N blocks");
        dialog.setBodyColor(Color.BLACK);
        dialog.setEditInputType(InputType.TYPE_CLASS_NUMBER);
        dialog.setListener(new SimpleEditDialogFragment.SimpleTwoBtnsDialogListener() {
            @Override
            public void onRightBtnClicked(SimpleEditDialogFragment dialog) {
                String heightStr = dialog.getTextOnEditText();
                if (heightStr.isEmpty()){
                    Toast.makeText(SettingsActivity.this, R.string.invalid_inputs,Toast.LENGTH_LONG).show();
                    return;
                }
                int height = Integer.valueOf(heightStr);
                pivxApplication.stopBlockchainAndRollBackitTo(height);
                dialog.dismiss();
                Toast.makeText(SettingsActivity.this,R.string.reseting_blockchain,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLeftBtnClicked(SimpleEditDialogFragment dialog) {
                dialog.dismiss();
            }
        });
        dialog.setContainerBtnsBackgroundColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialog.setRightBtnBackgroundColor(this.getResources().getColor(R.color.colorPurple, null));
        }else {
            dialog.setRightBtnBackgroundColor(ContextCompat.getColor(this,R.color.colorPurple));
        }
        dialog.setLeftBtnTextColor(Color.BLACK);
        dialog.setRightBtnTextColor(Color.BLACK);
        dialog.setRootBackgroundRes(R.drawable.dialog_bg);
        dialog.show();
    }

    private void launchResetBlockchainDialog() {
        SimpleTwoButtonsDialog dialog = DialogsUtil.buildSimpleTwoBtnsDialog(
                this,
                getString(R.string.dialog_reset_blockchain_title),
                getString(R.string.dialog_reset_blockchain_body),
                new SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener() {
                    @Override
                    public void onRightBtnClicked(SimpleTwoButtonsDialog dialog) {
                        pivxApplication.stopBlockchain();
                        Toast.makeText(SettingsActivity.this,R.string.reseting_blockchain,Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onLeftBtnClicked(SimpleTwoButtonsDialog dialog) {
                        dialog.dismiss();
                    }
                }
        );
        dialog.setLeftBtnText(R.string.button_cancel)
                .setRightBtnText(R.string.button_ok);
        dialog.show();
    }

    private void launchReportDialog(boolean isInternalReport) {

        if (isInternalReport){
            if (!AndroidUtils.checkPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_WRITE_PERMISSION)){
                Toast.makeText(this, R.string.write_external_denied, Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ReportIssueDialogBuilder dialog = new ReportIssueDialogBuilder(
                this,
                "pivx.org.pivxwallet.myfileprovider",
                R.string.report_issuea_dialog_title,
                (isInternalReport) ? R.string.internal_log : R.string.report_issue_dialog_message_issue,
                isInternalReport) {
            @Nullable
            @Override
            protected CharSequence subject() {
                return PivxContext.REPORT_SUBJECT_ISSUE+" "+pivxApplication.getVersionName();
            }

            @Nullable
            @Override
            protected CharSequence collectApplicationInfo() throws IOException {
                final StringBuilder applicationInfo = new StringBuilder();
                CrashReporter.appendApplicationInfo(applicationInfo, pivxApplication);
                return applicationInfo;
            }

            @Nullable
            @Override
            protected CharSequence collectStackTrace() throws IOException {
                return null;
            }

            @Nullable
            @Override
            protected CharSequence collectDeviceInfo() throws IOException {
                final StringBuilder deviceInfo = new StringBuilder();
                CrashReporter.appendDeviceInfo(deviceInfo, SettingsActivity.this);
                return deviceInfo;
            }

            @Nullable
            @Override
            protected CharSequence collectWalletDump() throws IOException {
                MultiWallet multiWallet = ((PivxModuleImp)pivxApplication.getModule()).getWallet();
                return multiWallet.getPivWallet().toString(false, true, true, null) +
                        " |||| " +
                        multiWallet.getZpivWallet().toString(false, true, true, null);
            }
        };

        dialog.show();
    }
    @Override
    protected void onBlockchainStateChange() {
        updateNetworkStatus();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NavigationUtils.goBackToHome(this);
    }
}
