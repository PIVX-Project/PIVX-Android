package pivx.org.pivxwallet.ui.pincode_activity;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import pivx.org.pivxwallet.MainActivity;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 4/20/17.
 */

public class PincodeActivity extends BaseActivity {
    EditText enter_main;
    ImageView i1, i2, i3, i4;

    String nam = new String();
    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        setContentView(R.layout.fragment_pincode);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Pincode");
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
                    Intent myIntent = new Intent(PincodeActivity.this,WalletActivity.class);
                    PincodeActivity.this.startActivity(myIntent);
                }
                Log.d("TAG", "onKey: screen key pressed");
                i1.setImageResource(R.drawable.pin_circle_active);
            }
        });
    }

}
