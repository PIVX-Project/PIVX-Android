package pivx.org.pivxwallet.ui.settings_pincode_activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseFragment;

/**
 * Created by furszy on 7/11/17.
 */

public class KeyboardFragment extends BaseFragment implements View.OnClickListener {

    private ViewGroup root;
    private TextView key_0;
    private TextView key_1;
    private TextView key_2;
    private TextView key_3;
    private TextView key_4;
    private TextView key_5;
    private TextView key_6;
    private TextView key_7;
    private TextView key_8;
    private TextView key_9;
    private ImageView key_clear;
    private ImageView key_back;

    private onKeyListener onKeyListener;

    public enum KEYS{
        ZERO(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6),
        SEVEN(7),
        EIGHT(8),
        NINE(9),
        DELETE(10),
        ENTER(11),
        CLEAR(12);

        KEYS(int value) {
            this.value = value;
        }

        int value;

        public int getValue() {
            return value;
        }
    }

    public interface onKeyListener{

        void onKeyClicked(KEYS key);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.keyboard,container);
        key_0 = (TextView) root.findViewById(R.id.key_0);
        key_0.setOnClickListener(this);
        key_1 = (TextView) root.findViewById(R.id.key_1);
        key_1.setOnClickListener(this);
        key_2 = (TextView) root.findViewById(R.id.key_2);
        key_2.setOnClickListener(this);
        key_3 = (TextView) root.findViewById(R.id.key_3);
        key_3.setOnClickListener(this);
        key_4 = (TextView) root.findViewById(R.id.key_4);
        key_4.setOnClickListener(this);
        key_5 = (TextView) root.findViewById(R.id.key_5);
        key_5.setOnClickListener(this);
        key_6 = (TextView) root.findViewById(R.id.key_6);
        key_6.setOnClickListener(this);
        key_7 = (TextView) root.findViewById(R.id.key_7);
        key_7.setOnClickListener(this);
        key_8 = (TextView) root.findViewById(R.id.key_8);
        key_8.setOnClickListener(this);
        key_9 = (TextView) root.findViewById(R.id.key_9);
        key_9.setOnClickListener(this);
        key_clear = (ImageView) root.findViewById(R.id.key_clear);
        key_clear.setOnClickListener(this);
        key_back = (ImageView) root.findViewById(R.id.key_backspace);
        key_back.setOnClickListener(this);
        return root;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (onKeyListener==null) return;
        KEYS keyClicked = null;
        if (id == R.id.key_0){
            keyClicked = KEYS.ZERO;
        }else if (id == R.id.key_1){
            keyClicked = KEYS.ONE;
        }else if (id == R.id.key_2){
            keyClicked = KEYS.TWO;
        }else if (id == R.id.key_3){
            keyClicked = KEYS.THREE;
        }else if (id == R.id.key_4){
            keyClicked = KEYS.FOUR;
        }else if (id == R.id.key_5){
            keyClicked = KEYS.FIVE;
        }else if (id == R.id.key_6){
            keyClicked = KEYS.SIX;
        }else if (id == R.id.key_7){
            keyClicked = KEYS.SEVEN;
        }else if (id == R.id.key_8){
            keyClicked = KEYS.EIGHT;
        }else if (id == R.id.key_9){
            keyClicked = KEYS.NINE;
        }else if (id == R.id.key_clear){
            // clear password field
            keyClicked = KEYS.CLEAR;
        }else if (id == R.id.key_backspace){
            keyClicked = KEYS.DELETE;
        }
        onKeyListener.onKeyClicked(keyClicked);
    }

    public void setOnKeyListener(KeyboardFragment.onKeyListener onKeyListener) {
        this.onKeyListener = onKeyListener;
    }

    public void setTextButtonsColor(int color){
        key_0.setTextColor(color);
        key_1.setTextColor(color);
        key_2.setTextColor(color);
        key_3.setTextColor(color);
        key_4.setTextColor(color);
        key_5.setTextColor(color);
        key_6.setTextColor(color);
        key_7.setTextColor(color);
        key_8.setTextColor(color);
        key_9.setTextColor(color);
    }

}
