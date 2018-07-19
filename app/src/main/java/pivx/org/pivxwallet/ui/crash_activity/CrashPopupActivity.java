package pivx.org.pivxwallet.ui.crash_activity;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Charsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import global.PivxModuleImp;
import global.utils.Io;
import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.utils.CrashReporter;

import static pivx.org.pivxwallet.utils.AndroidUtils.shareText;

/**
 * Created by furszy on 8/18/17.
 */

public class CrashPopupActivity extends AppCompatActivity implements View.OnClickListener {

    private Logger log = LoggerFactory.getLogger(CrashPopupActivity.class);

    private String authorities = "pivx.org.pivxwallet.myfileprovider";

    private EditText viewDescription;
    private CheckBox viewCollectDeviceInfo;
    private CheckBox viewCollectInstalledPackages;
    private CheckBox viewCollectApplicationLog;
    private CheckBox viewCollectWalletDump;
    private CheckBox viewCollectDb;

    private TextView txt_send;
    private TextView txt_cancel;

    private PivxApplication pivxApplication;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        pivxApplication = PivxApplication.getInstance();
        setTheme(R.style.AppTheme_Dialog);
        setContentView(R.layout.report_issue_activity_dialog);
        this.setFinishOnTouchOutside(false);

        ((TextView) findViewById(R.id.report_issue_dialog_message)).setText(R.string.report_crash_title);
        txt_send = (TextView) findViewById(R.id.txt_send);
        txt_cancel = (TextView) findViewById(R.id.txt_cancel);
        viewDescription = (EditText) findViewById(R.id.report_issue_dialog_description);
        viewCollectDeviceInfo = (CheckBox) findViewById(R.id.report_issue_dialog_collect_device_info);
        viewCollectInstalledPackages = (CheckBox) findViewById(R.id.report_issue_dialog_collect_installed_packages);
        viewCollectApplicationLog = (CheckBox) findViewById(R.id.report_issue_dialog_collect_application_log);
        viewCollectWalletDump = (CheckBox) findViewById(R.id.report_issue_dialog_collect_wallet_dump);
        viewCollectDb = (CheckBox) findViewById(R.id.report_issue_dialog_collect_db_data);

        txt_send.setOnClickListener(this);
        txt_cancel.setOnClickListener(this);

        pivxApplication.getAppConf().saveShowReportScreenOnStart(false);

        super.onCreate(savedInstanceState);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.txt_send) {
            final StringBuilder text = new StringBuilder();
            final ArrayList<Uri> attachments = new ArrayList<Uri>();
            final File cacheDir = getCacheDir();

            text.append(viewDescription.getText()).append('\n');

            try {
                text.append("\n\n\n=== application info ===\n\n");

                final CharSequence applicationInfo = collectApplicationInfo();

                text.append(applicationInfo);
            } catch (final IOException x) {
                text.append(x.toString()).append('\n');
            }

            try {
                final CharSequence stackTrace = collectStackTrace();

                if (stackTrace != null) {
                    text.append("\n\n\n=== stack trace ===\n\n");
                    text.append(stackTrace);
                }
            } catch (final IOException x) {
                text.append("\n\n\n=== stack trace ===\n\n");
                text.append(x.toString()).append('\n');
            }

            if (viewCollectDeviceInfo.isChecked()) {
                try {
                    text.append("\n\n\n=== device info ===\n\n");

                    final CharSequence deviceInfo = collectDeviceInfo();

                    text.append(deviceInfo);
                } catch (final IOException x) {
                    text.append(x.toString()).append('\n');
                }
            }

            if (viewCollectInstalledPackages.isChecked()) {
                try {
                    text.append("\n\n\n=== installed packages ===\n\n");
                    CrashReporter.appendInstalledPackages(text, this);
                } catch (final IOException x) {
                    text.append(x.toString()).append('\n');
                }
            }

            if (viewCollectApplicationLog.isChecked()) {
                try {
                    final File logDir = getDir("log", Context.MODE_PRIVATE);

                    for (final File logFile : logDir.listFiles()) {
                        final String logFileName = logFile.getName();
                        final File file;
                        if (logFileName.endsWith(".log.gz"))
                            file = File.createTempFile(logFileName.substring(0, logFileName.length() - 6), ".log.gz", cacheDir);
                        else if (logFileName.endsWith(".log"))
                            file = File.createTempFile(logFileName.substring(0, logFileName.length() - 3), ".log", cacheDir);
                        else
                            continue;

                        final InputStream is = new FileInputStream(logFile);
                        final OutputStream os = new FileOutputStream(file);

                        Io.copy(is, os);

                        os.close();
                        is.close();

                        attachments.add(FileProvider.getUriForFile(this, authorities, file));
                    }
                } catch (final IOException x) {
                    log.info("problem writing attachment", x);
                }
            }

            if (viewCollectWalletDump.isChecked()) {
                try {
                    final CharSequence walletDump = collectWalletDump();

                    if (walletDump != null) {
                        final File file = File.createTempFile("wallet-dump", ".txt", cacheDir);

                        final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
                        writer.write(walletDump.toString());
                        writer.close();

                        attachments.add(FileProvider.getUriForFile(this, authorities, file));
                    }
                } catch (final IOException x) {
                    log.info("problem writing attachment", x);
                }
            }


            if (viewCollectDb.isChecked()) {
                //todo: add contacts db and rates db here.
            /*try{
				List data = databaseCollector.collectData();
				if (data!=null && !data.isEmpty()){
					final File file = File.createTempFile("db-dump", ".txt", cacheDir);
					final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
					for (Object o : data) {
						writer.write(o.toString()+"\n");
					}
					writer.close();

					attachments.add(FileProvider.getUriForFile(getContext(),authorities, file));
				}

			}catch (Exception e){
				log.error("Exception",e);
			}*/

            }


            if (CrashReporter.hasSavedBackgroundTraces()) {
                text.append("\n\n\n=== saved exceptions ===\n\n");

                try {
                    CrashReporter.appendSavedBackgroundTraces(text);
                } catch (final IOException x) {
                    text.append(x.toString()).append('\n');
                }
            }

            text.append("\n\nPUT ADDITIONAL COMMENTS TO THE TOP. DOWN HERE NOBODY WILL NOTICE.");

            shareText(this, subject(), text, attachments);
            finish();
        }else if (id == R.id.txt_cancel){
            finish();
        }
    }

    protected CharSequence subject(){
        return "Crash report"+" "+ pivxApplication.getVersionName();
    }

    @Nullable
    protected CharSequence collectApplicationInfo() throws IOException{
        final StringBuilder applicationInfo = new StringBuilder();
        CrashReporter.appendApplicationInfo(applicationInfo, pivxApplication);
        return applicationInfo;
    }

    @Nullable
    protected CharSequence collectStackTrace() throws IOException{
        return null;
    }

    @Nullable
    protected CharSequence collectDeviceInfo() throws IOException{
        final StringBuilder deviceInfo = new StringBuilder();
        CrashReporter.appendDeviceInfo(deviceInfo, this);
        return deviceInfo;
    }

    @Nullable
    protected CharSequence collectWalletDump() throws IOException{
        return ((PivxModuleImp)pivxApplication.getModule()).getWallet().toString(false,true,true,null);
    }
}
