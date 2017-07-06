package pivx.org.pivxwallet.ui.settings_pincode_activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.settings_activity.SettingsActivity;


/**
 * Created by Neoperol on 5/18/17.
 */

public class SettingsPincodeActivity extends BaseActivity {
    EditText enter_main;
    ImageView i1, i2, i3, i4;

    String nam = new String();
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_settings_pincode, container);
        setTitle("Update Pincode");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        i1 = (ImageView) findViewById(R.id.imageview_circle1);
        i2 = (ImageView) findViewById(R.id.imageview_circle2);
        i3 = (ImageView) findViewById(R.id.imageview_circle3);
        i4 = (ImageView) findViewById(R.id.imageview_circle4);
        nam ="1234";

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

                } else if (s.length() == 1) {
                    i1.setImageResource(R.drawable.pin_circle_active);
                } else if (s.length() == 2) {
                    i2.setImageResource(R.drawable.pin_circle_active);
                } else if (s.length() == 3) {
                    i3.setImageResource(R.drawable.pin_circle_active);
                } else if (s.length() == 4) {
                    i4.setImageResource(R.drawable.pin_circle_active);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(enter_main.getText().toString().equals(nam) )
                {
                    // Not null and OK, launch the activity
                    Log.d("TAG", "onKey: screen key pressed");
                    Intent myIntent = new Intent(SettingsPincodeActivity.this,SettingsActivity.class);
                    SettingsPincodeActivity.this.startActivity(myIntent);
                }
                Log.d("TAG", "onKey: screen key pressed");
                i1.setImageResource(R.drawable.pin_circle_active);
            }


        });

    }
}
