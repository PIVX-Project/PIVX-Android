package pivx.org.pivxwallet.ui.settings.settings_activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import pivx.org.pivxwallet.R;

import static android.view.View.GONE;

public class SimpleEditDialogFragment extends Dialog implements View.OnClickListener {

    private SimpleTwoBtnsDialogListener listener;
    private EditText editText;

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

    private int editInputType = -1;

    private int imgAlertRes;

    private boolean optionSelected;

    private View container_dialog;


    public SimpleEditDialogFragment(Context context) {
        super(context);
    }

    public static SimpleEditDialogFragment newInstance(Context context) {
        SimpleEditDialogFragment fragment = new SimpleEditDialogFragment(context);
        return fragment;
    }

    @Override
    public void onProvideKeyboardShortcuts(List<KeyboardShortcutGroup> data, @Nullable Menu menu, int deviceId) {

    }

    public interface SimpleTwoBtnsDialogListener{

        void onRightBtnClicked(SimpleEditDialogFragment dialog);

        void onLeftBtnClicked(SimpleEditDialogFragment dialog);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.single_edit_dialog);

        container_dialog = findViewById(R.id.container_dialog);
        editText = findViewById(R.id.edit_text);
        View title_container = findViewById(R.id.title_container);
        TextView txt_title = findViewById(R.id.txt_title);
        TextView txt_body = findViewById(R.id.txt_body);
        ImageView imgAlert = findViewById(R.id.img_alert);
        View btn_container = findViewById(R.id.btn_container);
        TextView btn_left = findViewById(R.id.btn_left);
        TextView btn_right = findViewById(R.id.btn_right);

        initRoot(container_dialog);
        initTitle(title_container,txt_title);
        initBody(txt_body);
        initImgAlert(imgAlert);
        initBtns(btn_container,btn_left,btn_right);
        if (editInputType != -1)
            editText.setInputType(editInputType);
        super.onCreate(savedInstanceState);
    }

    private void initRoot(View container_dialog) {
        if (rootBackgroundRes!=0){
            container_dialog.setBackgroundResource(rootBackgroundRes);
        }
    }


    private void initTitle(View title_container, TextView txt_title){
        if (title != null){
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
        if (containerBtnsBackgroundColor != 0){
            btn_container.setBackgroundColor(containerBtnsBackgroundColor);
        }
        if (leftBtnTextColor != 0){
            btn_left.setTextColor(leftBtnTextColor);
        }

        if (rightBtnTextColor != 0){
            btn_right.setTextColor(rightBtnTextColor);
        }

        if (leftBtnText != null){
            btn_left.setText(leftBtnText);
            if (leftBtnBackgroundColor != 0){
                btn_left.setBackgroundColor(leftBtnBackgroundColor);
            }
        }

        if (rightBtnText != null){
            btn_right.setText(rightBtnText);
            if (rightBtnBackgroundColor != 0){
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

    public void setEditInputType(int inputType){
        if(editText != null)
            editText.setInputType(inputType);
        else
            this.editInputType = inputType;
    }

    public SimpleEditDialogFragment setImgAlertRes(int imgAlertRes) {
        this.imgAlertRes = imgAlertRes;
        return this;
    }

    public SimpleEditDialogFragment setContainerBtnsBackgroundColor(int containerBtnsBackgroundColor) {
        this.containerBtnsBackgroundColor = containerBtnsBackgroundColor;
        return this;
    }

    public SimpleEditDialogFragment setLeftBtnTextColor(int leftBtnTextColor) {
        this.leftBtnTextColor = leftBtnTextColor;
        return this;
    }

    public SimpleEditDialogFragment setRightBtnTextColor(int rightBtnTextColor) {
        this.rightBtnTextColor = rightBtnTextColor;
        return this;
    }

    public void setBtnsTextColor(int color){
        setLeftBtnTextColor(color);
        setRightBtnTextColor(color);
    }

    public SimpleEditDialogFragment setLeftBtnText(String leftBtnText) {
        this.leftBtnText = leftBtnText;
        return this;
    }

    public SimpleEditDialogFragment setLeftBtnText(int resLeftBtnText) {
        this.leftBtnText = getContext().getString(resLeftBtnText);
        return this;
    }

    public SimpleEditDialogFragment setRightBtnText(String rightBtnText) {
        this.rightBtnText = rightBtnText;
        return this;
    }

    public SimpleEditDialogFragment setRightBtnText(int resRightBtnText) {
        this.rightBtnText = getContext().getString(resRightBtnText);
        return this;
    }

    public SimpleEditDialogFragment setRightBtnBackgroundColor(int rightBtnBackgroundColor) {
        this.rightBtnBackgroundColor = rightBtnBackgroundColor;
        return this;
    }

    public SimpleEditDialogFragment setLeftBtnBackgroundColor(int leftBtnBackgroundColor) {
        this.leftBtnBackgroundColor = leftBtnBackgroundColor;
        return this;
    }

    public void setRootBackgroundRes(int rootBackgroundRes) {
        this.rootBackgroundRes = rootBackgroundRes;
    }

    public boolean isOptionSelected() {
        return optionSelected;
    }

    public String getTextOnEditText(){
        return (editText != null) ? editText.getText().toString() : null;
    }

}