package pivx.org.pivxwallet.ui.base.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import pivx.org.pivxwallet.R;

import static android.view.View.GONE;

/**
 * Created by mati on 26/01/17.
 */

public class SimpleTwoButtonsDialog extends Dialog implements View.OnClickListener {

    private SimpleTwoBtnsDialogListener listener;

    private String title;
    private String body;

    private String leftBtnText;
    private String rightBtnText;

    private int titleColor;
    private int bodyColor;
    private int containerBtnsBackgroundColor;
    private int leftBtnTextColor;
    private int rightBtnTextColor;
    private int rightBtnBackgroundColor;
    private int leftBtnBackgroundColor;
    private int rootBackgroundRes;

    private int imgAlertRes;

    private boolean optionSelected;

    private View container_dialog;



    public SimpleTwoButtonsDialog(Context context) {
        super(context);
    }

    public static SimpleTwoButtonsDialog newInstance(Context context) {
        SimpleTwoButtonsDialog fragment = new SimpleTwoButtonsDialog(context);
        return fragment;
    }

    public interface SimpleTwoBtnsDialogListener{

        void onRightBtnClicked(SimpleTwoButtonsDialog dialog);

        void onLeftBtnClicked(SimpleTwoButtonsDialog dialog);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.simple_two_btns_dialog);

        container_dialog = findViewById(R.id.container_dialog);
        View title_container = findViewById(R.id.title_container);
        TextView txt_title = (TextView) findViewById(R.id.txt_title);
        TextView txt_body = (TextView) findViewById(R.id.txt_body);
        ImageView imgAlert = (ImageView) findViewById(R.id.img_alert);
        View btn_container = findViewById(R.id.btn_container);
        TextView btn_left = (TextView) findViewById(R.id.btn_left);
        TextView btn_right = (TextView) findViewById(R.id.btn_right);

        initRoot(container_dialog);
        initTitle(title_container,txt_title);
        initBody(txt_body);
        initImgAlert(imgAlert);
        initBtns(btn_container,btn_left,btn_right);

//        setOnDismissListener(this);

        super.onCreate(savedInstanceState);
    }

    private void initRoot(View container_dialog) {
        if (rootBackgroundRes!=0){
            container_dialog.setBackgroundResource(rootBackgroundRes);
        }
    }


    private void initTitle(View title_container, TextView txt_title){
        if (title!=null){
            title_container.setVisibility(View.VISIBLE);
            txt_title.setText(title);
            if (titleColor!=0){
                txt_title.setTextColor(titleColor);
            }
        }else {
            title_container.setVisibility(GONE);
        }
    }

    private void initBody(TextView txt_body){
        if (body!=null){
            txt_body.setText(body);
            if (bodyColor!=0){
                txt_body.setTextColor(bodyColor);
            }
        }
    }

    private void initImgAlert(ImageView img_alert){
        if (imgAlertRes!=0){
            img_alert.setImageResource(imgAlertRes);
        }
    }

    private void initBtns(View btn_container, TextView btn_left, TextView btn_right){
        if (containerBtnsBackgroundColor!=0){
            btn_container.setBackgroundColor(containerBtnsBackgroundColor);
        }
        if (leftBtnTextColor!=0){
            btn_left.setTextColor(leftBtnTextColor);
        }

        if (rightBtnTextColor!=0){
            btn_right.setTextColor(rightBtnTextColor);
        }

        if (leftBtnText!=null){
            btn_left.setText(leftBtnText);
            if (leftBtnBackgroundColor!=0){
                btn_left.setBackgroundColor(leftBtnBackgroundColor);
            }
        }

        if (rightBtnText!=null){
            btn_right.setText(rightBtnText);
            if (rightBtnBackgroundColor!=0){
                btn_right.setBackgroundColor(rightBtnBackgroundColor);
            }
        }

        btn_left.setOnClickListener(this);
        btn_right.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (listener!=null) {

            if (id == R.id.btn_left) {
                optionSelected = true;
                listener.onLeftBtnClicked(this);
            } else if (id == R.id.btn_right) {
                optionSelected = true;
                listener.onRightBtnClicked(this);
            }

        }
    }

    public void setListener(SimpleTwoBtnsDialogListener listener) {
        this.listener = listener;
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

    public SimpleTwoButtonsDialog setImgAlertRes(int imgAlertRes) {
        this.imgAlertRes = imgAlertRes;
        return this;
    }

    public void setContainerBtnsBackgroundColor(int containerBtnsBackgroundColor) {
        this.containerBtnsBackgroundColor = containerBtnsBackgroundColor;
    }

    public SimpleTwoButtonsDialog setLeftBtnTextColor(int leftBtnTextColor) {
        this.leftBtnTextColor = leftBtnTextColor;
        return this;
    }

    public SimpleTwoButtonsDialog setRightBtnTextColor(int rightBtnTextColor) {
        this.rightBtnTextColor = rightBtnTextColor;
        return this;
    }

    public void setBtnsTextColor(int color){
        setLeftBtnTextColor(color);
        setRightBtnTextColor(color);
    }

    public SimpleTwoButtonsDialog setLeftBtnText(String leftBtnText) {
        this.leftBtnText = leftBtnText;
        return this;
    }

    public SimpleTwoButtonsDialog setLeftBtnText(int resLeftBtnText) {
        this.leftBtnText = getContext().getString(resLeftBtnText);
        return this;
    }

    public SimpleTwoButtonsDialog setRightBtnText(String rightBtnText) {
        this.rightBtnText = rightBtnText;
        return this;
    }

    public SimpleTwoButtonsDialog setRightBtnText(int resRightBtnText) {
        this.rightBtnText = getContext().getString(resRightBtnText);
        return this;
    }

    public SimpleTwoButtonsDialog setRightBtnBackgroundColor(int rightBtnBackgroundColor) {
        this.rightBtnBackgroundColor = rightBtnBackgroundColor;
        return this;
    }

    public SimpleTwoButtonsDialog setLeftBtnBackgroundColor(int leftBtnBackgroundColor) {
        this.leftBtnBackgroundColor = leftBtnBackgroundColor;
        return this;
    }

    public void setRootBackgroundRes(int rootBackgroundRes) {
        this.rootBackgroundRes = rootBackgroundRes;
    }

    public boolean isOptionSelected() {
        return optionSelected;
    }
}
