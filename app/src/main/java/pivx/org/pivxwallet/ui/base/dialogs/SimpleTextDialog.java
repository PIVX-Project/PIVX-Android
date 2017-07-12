package pivx.org.pivxwallet.ui.base.dialogs;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pivx.org.pivxwallet.R;


/**
 * Created by mati on 26/01/17.
 */

public class SimpleTextDialog extends DialogFragment {

    private DialogListener cancelListener;
    private boolean actionCompleted;

    private String title;
    private String body;

    private int rootBackgroundRes;
    private int titleColor;
    private int bodyColor = -1;
    private int okBtnBackgroundColor;
    private int okBtnTextColor;
    private View.OnClickListener okBtnClickListener;

    private int imgAlertRes;

    private Align alignBody;

    private View root;


    public enum Align{

        LEFT,CENTER,RIGHT;

    }

    public static SimpleTextDialog newInstance() {
        SimpleTextDialog fragment = new SimpleTextDialog();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = inflater.inflate(R.layout.simple_dialog,null);

        TextView txt_title = (TextView) root.findViewById(R.id.txt_title);
        TextView txt_body = (TextView) root.findViewById(R.id.txt_body);
        ImageView imgAlert = (ImageView) root.findViewById(R.id.img_alert);
        TextView btn_ok = (TextView) root.findViewById(R.id.btn_ok);

        initRoot();
        initTitle(txt_title);
        initBody(txt_body);
        initImgAlert(imgAlert);
        initOkBtn(btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCompleted = true;
                if (okBtnClickListener!=null){
                    okBtnClickListener.onClick(v);
                }
                dismiss();
            }
        });

        return root;
    }

    private void initRoot() {
        if (rootBackgroundRes!=0){
            root.setBackgroundResource(rootBackgroundRes);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (cancelListener!=null)
            cancelListener.cancel(actionCompleted);
    }

    private void initTitle(TextView txt_title){
        if (title!=null){
            txt_title.setText(title);
            if (titleColor>0){
                txt_title.setTextColor(titleColor);
            }
        }else {
            txt_title.setVisibility(View.GONE);
        }
    }

    private void initBody(TextView txt_body){
        if (body!=null){
            txt_body.setText(body);
            if (bodyColor!=-1){
                txt_body.setTextColor(bodyColor);
            }
            if(alignBody!=null){
                switch (alignBody){
                    case LEFT:
                        txt_body.setGravity(Gravity.START);
                        txt_body.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                        break;
                }

            }
        }
    }

    private void initImgAlert(ImageView img_alert){
        if (imgAlertRes >0){
            img_alert.setImageResource(imgAlertRes);
        }
    }

    private void initOkBtn(TextView btn_ok){
        if (okBtnBackgroundColor!=0){
            btn_ok.setBackgroundColor(okBtnBackgroundColor);
        }
        if (okBtnTextColor!=0){
            btn_ok.setTextColor(okBtnTextColor);
        }
    }

    public void setListener(DialogListener listener) {
        this.cancelListener = listener;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTitleColor(int titleColor) {
        this.titleColor = titleColor;
    }

    public void setBodyColor(int bodyColor) {
        this.bodyColor = bodyColor;
    }

    public void setImgAlertRes(int imgAlertRes) {
        this.imgAlertRes = imgAlertRes;
    }

    public void setOkBtnBackgroundColor(int okBtnBackgroundColor) {
        this.okBtnBackgroundColor = okBtnBackgroundColor;
    }

    public SimpleTextDialog setOkBtnClickListener(View.OnClickListener okBtnClickListener) {
        this.okBtnClickListener = okBtnClickListener;
        return this;
    }

    public SimpleTextDialog setRootBackgroundRes(int rootBackgroundRes) {
        this.rootBackgroundRes = rootBackgroundRes;
        return this;
    }

    public void setAlignBody(Align alignBody) {
        this.alignBody = alignBody;
    }

    public void setOkBtnTextColor(int okBtnTextColor) {
        this.okBtnTextColor = okBtnTextColor;
    }
}
