package pivx.org.pivxwallet.utils;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * Created by furszy on 8/18/17.
 */

public class AndroidUtils {

    static final Logger logger = LoggerFactory.getLogger(AndroidUtils.class);


    public static void shareText(final Context context, final CharSequence subject, final CharSequence text, final ArrayList<Uri> attachments) {
        final Intent intent;

        if (attachments.size() == 0) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
        } else if (attachments.size() == 1) {
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, attachments.get(0));
        } else {
            intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("text/plain");

            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachments);
        }

        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{PivxContext.REPORT_EMAIL});
        if (subject != null)
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
//		ArrayList<CharSequence> str = new ArrayList<CharSequence>();
//		str.add(text);
        if (Build.VERSION.SDK_INT > 21){
            intent.putExtra(Intent.EXTRA_TEXT, Lists.newArrayList(text));
        }else {
            intent.putExtra(Intent.EXTRA_TEXT, text);
        }


        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            String maiñChooser = context.getString(R.string.report_issue_dialog_mail_intent_chooser);
            if (!(context instanceof Activity)){
                PivxApplication.getInstance().getAppConf().saveShowReportScreenOnStart(true);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK|FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_SINGLE_TOP);
            }
            context.startActivity(Intent.createChooser(intent, maiñChooser));
            logger.info("invoked chooser for sending issue report");
        } catch (final Exception x) {
            Toast.makeText(context, R.string.report_issue_dialog_mail_intent_failed, Toast.LENGTH_LONG).show();
            logger.error("report issue failed", x);
        }
    }

    public static void copyToClipboard(Context context,String text){
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Address", text);
        clipboard.setPrimaryClip(clip);
    }


    public static boolean checkPermissions(Activity context, String permissionId, int permissionRequestId) {
        // Assume thisActivity is the current activity
        if (Build.VERSION.SDK_INT > 22) {

            int permissionCheck = ContextCompat.checkSelfPermission(context, permissionId);
            if (permissionCheck == PERMISSION_GRANTED){
                return true;
            }

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(context, permissionId) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permissionId)) {
                        //Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    // Show an expanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.
                    return false;

                } else {

                    // No explanation needed, we can request the permission.

                    ActivityCompat.requestPermissions(context,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            permissionRequestId);

                    return false;

                    // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                    // app-defined int constant. The callback method gets the
                    // result of the request.
                }
            }
            return false;
        }
        return false;
    }

}
