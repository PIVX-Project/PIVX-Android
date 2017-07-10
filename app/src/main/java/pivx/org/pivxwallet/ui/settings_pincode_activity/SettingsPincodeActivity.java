package pivx.org.pivxwallet.ui.settings_pincode_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.settings_activity.SettingsActivity;


/**
 * Created by Neoperol on 5/18/17.
 */

public class SettingsPincodeActivity extends BaseActivity {
    EditText enter_main;
    ImageView i1, i2, i3, i4;
    TextView key_0;
    TextView key_1;
    TextView key_2;
    TextView key_3;
    TextView key_4;
    TextView key_5;
    TextView key_6;
    TextView key_7;
    TextView key_8;
    TextView key_9;
    TextView key_clear;
    ImageView key_back;

    int[] pin = new int[4];
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_settings_pincode, container);
        setTitle("Update PIN");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        i1 = (ImageView) findViewById(R.id.imageview_circle1);
        i2 = (ImageView) findViewById(R.id.imageview_circle2);
        i3 = (ImageView) findViewById(R.id.imageview_circle3);
        i4 = (ImageView) findViewById(R.id.imageview_circle4);
        key_0 = (TextView) findViewById(R.id.anti_theft_t9_key_0);
        key_1 = (TextView) findViewById(R.id.anti_theft_t9_key_1);
        key_2 = (TextView) findViewById(R.id.anti_theft_t9_key_2);
        key_3 = (TextView) findViewById(R.id.anti_theft_t9_key_3);
        key_4 = (TextView) findViewById(R.id.anti_theft_t9_key_4);
        key_5 = (TextView) findViewById(R.id.anti_theft_t9_key_5);
        key_6 = (TextView) findViewById(R.id.anti_theft_t9_key_6);
        key_7 = (TextView) findViewById(R.id.anti_theft_t9_key_7);
        key_8 = (TextView) findViewById(R.id.anti_theft_t9_key_8);
        key_9 = (TextView) findViewById(R.id.anti_theft_t9_key_9);
        key_clear = (TextView) findViewById(R.id.anti_theft_t9_key_clear);
        key_back = (ImageView) findViewById(R.id.anti_theft_t9_key_backspace);

        enter_main = (EditText) findViewById(R.id.editText_enter_mpin);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(enter_main, InputMethodManager.SHOW_IMPLICIT);
        enter_main.requestFocus();
        enter_main.setInputType(InputType.TYPE_CLASS_NUMBER);
        enter_main.setFocusableInTouchMode(true);

        enter_main.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() == 0) {
                    pin[0] = -1;
                    i1.setImageResource(R.drawable.pin_circle_white);
                } else if (s.length() == 1) {
                    pin[0]= s.charAt(0);
                    pin[1] = -1;
                    i1.setImageResource(R.drawable.pin_circle_active);
                    i2.setImageResource(R.drawable.pin_circle_white);
                } else if (s.length() == 2) {
                    pin[1]= s.charAt(1);
                    pin[2] = -1;
                    i2.setImageResource(R.drawable.pin_circle_active);
                    i3.setImageResource(R.drawable.pin_circle_white);
                } else if (s.length() == 3) {
                    pin[2]= s.charAt(2);
                    pin[3] = -1;
                    i3.setImageResource(R.drawable.pin_circle_active);
                    i4.setImageResource(R.drawable.pin_circle_white);
                } else if (s.length() == 4) {
                    pin[3]= s.charAt(3);
                    i4.setImageResource(R.drawable.pin_circle_active);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(enter_main.getText().toString().length()==4 ) {
                    // Not null and OK, launch the activity
                    // just save pincode
                    String pincode = String.valueOf(pin[0])+String.valueOf(pin[1])+String.valueOf(pin[2])+String.valueOf(pin[3]);
                    pivxApplication.getAppConf().savePincode(pincode);
                    finish();
                }
                i1.setImageResource(R.drawable.pin_circle_active);
            }


        });

    }


    private void onT9KeyClicked(int key) {
        switch (key) {
            case R.id.anti_theft_t9_key_0:
                enter_main.append("0");
                break;
            case R.id.anti_theft_t9_key_1:
                enter_main.append("1");
                break;
            case R.id.anti_theft_t9_key_2:
                enter_main.append("2");
                break;
            case R.id.anti_theft_t9_key_3:
                enter_main.append("3");
                break;
            case R.id.anti_theft_t9_key_4:
                enter_main.append("4");
                break;
            case R.id.anti_theft_t9_key_5:
                enter_main.append("5");
                break;
            case R.id.anti_theft_t9_key_6:
                enter_main.append("6");
                break;
            case R.id.anti_theft_t9_key_7:
                enter_main.append("7");
                break;
            case R.id.anti_theft_t9_key_8:
                enter_main.append("8");
                break;
            case R.id.anti_theft_t9_key_9:
                enter_main.append("9");
                break;
            case R.id.anti_theft_t9_key_backspace: {
                // delete one character
                String passwordStr = enter_main.getText().toString();
                if (passwordStr.length() > 0) {
                    String newPasswordStr = new StringBuilder(passwordStr)
                            .deleteCharAt(passwordStr.length() - 1).toString();
                    enter_main.setText(newPasswordStr);
                }
            }
            break;
            case R.id.anti_theft_t9_key_clear:
                // clear password field
                enter_main.setText(null);
                break;
        }
    }
}
