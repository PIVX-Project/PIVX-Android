package pivx.org.pivxwallet.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import pivtrum.PivtrumPeerData;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxContext;
import pivx.org.pivxwallet.ui.address_add_activity.AddContactActivity;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTextDialog;
import pivx.org.pivxwallet.ui.base.dialogs.SimpleTwoButtonsDialog;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by furszy on 7/5/17.
 */

public class DialogsUtil {


    public static SimpleTextDialog buildSimpleErrorTextDialog(Context context, String title, String body){
        final SimpleTextDialog dialog = SimpleTextDialog.newInstance();
        dialog.setTitle(title);
        dialog.setBody(body);
        dialog.setOkBtnBackgroundColor(Color.RED);
        dialog.setOkBtnTextColor(Color.WHITE);
        dialog.setRootBackgroundRes(R.drawable.dialog_bg);
        return dialog;
    }

    public static SimpleTextDialog buildSimpleTextDialog(Context context, String title, String body){
        final SimpleTextDialog dialog = SimpleTextDialog.newInstance();
        dialog.setTitle(title);
        dialog.setBody(body);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialog.setOkBtnBackgroundColor(context.getResources().getColor(R.color.lightGreen, null));
        }else {
            dialog.setOkBtnBackgroundColor(ContextCompat.getColor(context, R.color.lightGreen));
        }
        dialog.setOkBtnTextColor(Color.WHITE);
        dialog.setRootBackgroundRes(R.drawable.dialog_bg);
        return dialog;
    }

    public static SimpleTwoButtonsDialog buildSimpleTwoBtnsDialog(Context context, int titleRes, int bodyRes, SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener simpleTwoBtnsDialogListener){
        return buildSimpleTwoBtnsDialog(context,context.getString(titleRes),context.getString(bodyRes),simpleTwoBtnsDialogListener);
    }

    public static SimpleTwoButtonsDialog buildSimpleTwoBtnsDialog(Context context, String title, String body, SimpleTwoButtonsDialog.SimpleTwoBtnsDialogListener simpleTwoBtnsDialogListener){
        final SimpleTwoButtonsDialog dialog = SimpleTwoButtonsDialog.newInstance(context);
        dialog.setTitle(title);
        dialog.setTitleColor(Color.BLACK);
        dialog.setBody(body);
        dialog.setBodyColor(Color.BLACK);
        dialog.setListener(simpleTwoBtnsDialogListener);
        dialog.setContainerBtnsBackgroundColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            dialog.setRightBtnBackgroundColor(context.getResources().getColor(R.color.lightGreen, null));
        }else {
            dialog.setRightBtnBackgroundColor(ContextCompat.getColor(context,R.color.lightGreen));
        }
        dialog.setLeftBtnTextColor(Color.BLACK);
        dialog.setRightBtnTextColor(Color.WHITE);
        dialog.setRootBackgroundRes(R.drawable.dialog_bg);
        return dialog;
    }


    public interface TrustedNodeDialogListener{
        void onNodeSelected(PivtrumPeerData pivtrumPeerData);
    }

    public static DialogBuilder buildtrustedNodeDialog(final Context context, final TrustedNodeDialogListener trustedNodeDialogListener){
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
                String tcpPortStr = editTcp.getText().toString();
                String sslPortStr = editSsl.getText().toString();
                int tcpPort = PivxContext.NETWORK_PARAMETERS.getPort();
                int sslPort = 0;
                if (tcpPortStr.length()>0){
                    tcpPort = Integer.valueOf(tcpPortStr);
                }
                if (sslPortStr.length()>0){
                    sslPort = Integer.valueOf(sslPortStr);
                }
                if (host.equals("")){
                    Toast.makeText(context,R.string.invalid_host,Toast.LENGTH_SHORT).show();
                }else {
                    trustedNodeDialogListener.onNodeSelected(
                            new PivtrumPeerData(
                                    host,
                                    tcpPort,
                                    sslPort)
                    );
                }
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


    public static void showCreateAddressLabelDialog(final Context context, String address){
        final DialogBuilder dialog = DialogBuilder.warn(context, R.string.scan_result_address_title);
        dialog.setMessage(address+"\n\nCreate contact?");
        final String tempPubKey = address;
        DialogInterface.OnClickListener rightListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                Intent intent = new Intent(context, AddContactActivity.class);
                intent.putExtra(AddContactActivity.ADDRESS_TO_ADD,tempPubKey);
                context.startActivity(intent);
                dialog.dismiss();
            }
        };
        DialogInterface.OnClickListener lefttListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                // nothing yet
                dialog.dismiss();
            }
        };
        dialog.twoButtons(lefttListener,rightListener);
        dialog.create().show();
    }

}
