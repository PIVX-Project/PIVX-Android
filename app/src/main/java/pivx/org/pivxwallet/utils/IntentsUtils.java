package pivx.org.pivxwallet.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;

/**
 * Created by furszy on 7/10/17.
 */

public class IntentsUtils {

    private static final Logger logger = LoggerFactory.getLogger(IntentsUtils.class);

    public static void startSend(Context context, final CharSequence subject, final CharSequence text, final ArrayList<Uri> attachments) {
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
        intent.putExtra(Intent.EXTRA_TEXT, text);

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            String maiñChooser = context.getString(R.string.report_issue_dialog_mail_intent_chooser);
            context.startActivity(Intent.createChooser(intent, maiñChooser));
            logger.info("invoked chooser for sending issue report");
        } catch (final Exception x) {
            Toast.makeText(context, R.string.report_issue_dialog_mail_intent_failed, Toast.LENGTH_LONG).show();
            logger.error("report issue failed", x);
        }
    }

}
