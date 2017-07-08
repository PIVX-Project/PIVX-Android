package pivx.org.pivxwallet.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pivtrum.PivtrumPeerData;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.start_node_activity.StartNodeActivity;
import pivx.org.pivxwallet.ui.transaction_send_activity.SendActivity;

/**
 * Created by furszy on 7/5/17.
 */

public class DialogsUtil {

    public static AlertDialog buildErrorDialog(Context context,String text) {
        DialogBuilder dialogBuilder = new DialogBuilder(context);
        dialogBuilder.setMessage(text);
        dialogBuilder.setView(R.layout.dialog_error);
        return dialogBuilder.create();
    }

    public interface TrustedNodeDialogListener{
        void onNodeSelected(PivtrumPeerData pivtrumPeerData);
    }

    public static DialogBuilder buildtrustedNodeDialog(Context context, final TrustedNodeDialogListener trustedNodeDialogListener){
        LayoutInflater content = LayoutInflater.from(context);
        View dialogView = content.inflate(R.layout.dialog_node, null);
        DialogBuilder nodeDialog = new DialogBuilder(context);
        final EditText editHost = (EditText) dialogView.findViewById(R.id.hostText);
        final EditText editTcp = (EditText) dialogView.findViewById(R.id.tcpText);
        final EditText editSsl = (EditText) dialogView.findViewById(R.id.sslText);
        nodeDialog.setTitle("Add your Node");
        nodeDialog.setView(dialogView);
        nodeDialog.setPositiveButton("Add Node", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String host = editHost.getText().toString();
                String tcpPort = editTcp.getText().toString();
                String sslPort = editSsl.getText().toString();
                if (tcpPort.length()<1){
                    // todo: show error dialog..
                }
                if (sslPort.length()<1){
                    // todo: show error dialog..
                }
                trustedNodeDialogListener.onNodeSelected(
                        new PivtrumPeerData(
                                host,
                                Integer.valueOf(tcpPort),
                                Integer.valueOf(sslPort))
                );
                dialog.dismiss();
            }
        });
        nodeDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        return nodeDialog;
    }

}
