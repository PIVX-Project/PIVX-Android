/*
 * Copyright 2013-2015 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pivx.org.pivxwallet.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import global.utils.Io;
import host.furszy.zerocoinj.store.AccStore;
import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.module.store.AccStoreDb;
import pivx.org.pivxwallet.module.store.StoredAccumulator;

import static pivx.org.pivxwallet.utils.AndroidUtils.shareText;

import static pivx.org.pivxwallet.utils.AndroidUtils.shareText;


public abstract class ReportIssueDialogBuilder extends DialogBuilder implements OnClickListener {

    private static final Logger log = LoggerFactory.getLogger(ReportIssueDialogBuilder.class);

    private final Context context;

    private String authorities;

    private EditText viewDescription;
    private CheckBox viewCollectDeviceInfo;
    private CheckBox viewCollectInstalledPackages;
    private CheckBox viewCollectApplicationLog;
    private CheckBox viewCollectWalletDump;
    private CheckBox viewCollectDb;

    // IF this is true then the dialog will store this in a zip in the download folder
    private boolean exportInternally = false;

    public ReportIssueDialogBuilder(final Context context, String authorities, final int titleResId, final int messageResId, boolean exportInternally) {
        this(context,authorities,titleResId,messageResId);
        this.exportInternally = exportInternally;

        if (this.exportInternally){
            viewDescription.setVisibility(View.GONE);
        }
    }

    public ReportIssueDialogBuilder(final Context context, String authorities, final int titleResId, final int messageResId) {
        super(context);

        this.authorities = authorities;
        this.context = context;

        final LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.report_issue_dialog, null);

        ((TextView) view.findViewById(R.id.report_issue_dialog_message)).setText(messageResId);

        viewDescription = (EditText) view.findViewById(R.id.report_issue_dialog_description);

        viewCollectDeviceInfo = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_device_info);
        viewCollectInstalledPackages = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_installed_packages);
        viewCollectApplicationLog = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_application_log);
        viewCollectWalletDump = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_wallet_dump);
        viewCollectDb = (CheckBox) view.findViewById(R.id.report_issue_dialog_collect_db_data);

        setTitle(titleResId);
        setView(view);
        setPositiveButton(R.string.report_issue_dialog_report, this);
        setNegativeButton(R.string.button_cancel, null);
        setTitleColor(Color.BLACK);

    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        final StringBuilder text = new StringBuilder();
        final ArrayList<Uri> attachments = new ArrayList<Uri>();
        final File cacheDir = context.getCacheDir();

        List<File> files = new ArrayList<>();

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
                CrashReporter.appendInstalledPackages(text, context);
            } catch (final IOException x) {
                text.append(x.toString()).append('\n');
            }
        }

        if (viewCollectApplicationLog.isChecked()) {
            try {
                final File logDir = context.getDir("log", Context.MODE_PRIVATE);

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

                    attachments.add(FileProvider.getUriForFile(getContext(), authorities, file));
                    // Add file for internal store
                    files.add(file);
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

                    attachments.add(FileProvider.getUriForFile(getContext(), authorities, file));

                    // Add file
                    files.add(file);
                }
            } catch (final IOException x) {
                log.info("problem writing attachment", x);
            }
        }


        if (viewCollectDb.isChecked()) {
            try {
                //todo: add contacts db and rates db here.
                List<StoredAccumulator> data = ((AccStoreDb) PivxContext.CONTEXT.accStore).list();
                if (data != null && !data.isEmpty()) {
                    final File file = File.createTempFile("db-dump", ".txt", cacheDir);
                    final Writer writer = new OutputStreamWriter(new FileOutputStream(file), Charsets.UTF_8);
                    for (Object o : data) {
                        writer.write(o.toString() + "\n");
                    }
                    writer.close();

                    attachments.add(FileProvider.getUriForFile(getContext(), authorities, file));

                    files.add(file);
                }

            }catch (Exception e){
                log.error("Exception",e);
            }
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

        if (exportInternally){
            // TODO: Store this files in a zip in the download folder.
            // TODO: Check if we have read/write permission..

            File exportedFolder = new File(PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR, "pivx_log_export");
            int i = 1;
            while (exportedFolder.exists()){
                exportedFolder = new File(PivxContext.Files.EXTERNAL_WALLET_BACKUP_DIR, "pivx_log_export ("+i+")");
            }
            if(exportedFolder.mkdir()){
                for (File file : files) {
                    try {
                        File storeFile = new File(exportedFolder, file.getName());

                        final InputStream is = new FileInputStream(file);
                        final OutputStream os = new FileOutputStream(storeFile);

                        Io.copy(is, os);

                        os.close();
                        is.close();
                    }catch (Exception e){
                        Toast.makeText(context, R.string.error_copying_files_to_log_folder, Toast.LENGTH_SHORT).show();
                    }
                }
                Toast.makeText(context, R.string.log_folder_created, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context, R.string.error_creating_log_folder, Toast.LENGTH_SHORT).show();
            }
        }else {
            shareText(context,subject(), text, attachments);
        }
    }


    @Nullable
    protected abstract CharSequence subject();

    @Nullable
    protected abstract CharSequence collectApplicationInfo() throws IOException;

    @Nullable
    protected abstract CharSequence collectStackTrace() throws IOException;

    @Nullable
    protected abstract CharSequence collectDeviceInfo() throws IOException;

    @Nullable
    protected abstract CharSequence collectWalletDump() throws IOException;
}
