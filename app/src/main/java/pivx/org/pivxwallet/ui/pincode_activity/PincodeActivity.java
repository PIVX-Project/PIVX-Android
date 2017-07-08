package pivx.org.pivxwallet.ui.pincode_activity;

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
import pivx.org.pivxwallet.ui.start_node_activity.StartNodeActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 4/20/17.
 */

public class PincodeActivity extends BaseActivity {
    EditText enter_main;
    ImageView i1, i2, i3, i4;

    int[] pin = new int[4];

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {

        if (pivxApplication.getAppConf().getPincode()!=null){
            goNext();
            finish();
        }
        getLayoutInflater().inflate(R.layout.fragment_pincode, container);
        setTitle("Create Pin");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        i1 = (ImageView) findViewById(R.id.imageview_circle1);
        i2 = (ImageView) findViewById(R.id.imageview_circle2);
        i3 = (ImageView) findViewById(R.id.imageview_circle3);
        i4 = (ImageView) findViewById(R.id.imageview_circle4);

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
                    i1.setImageDrawable(getResources().getDrawable(R.drawable.pin_circle,null));
                } else if (s.length() == 1) {
                    pin[0]= s.charAt(0);
                    pin[1] = -1;
                    i1.setImageResource(R.drawable.pin_circle_active);
                    i2.setImageResource(R.drawable.pin_circle);
                } else if (s.length() == 2) {
                    pin[1]= s.charAt(1);
                    pin[2] = -1;
                    i2.setImageResource(R.drawable.pin_circle_active);
                    i3.setImageResource(R.drawable.pin_circle);
                } else if (s.length() == 3) {
                    pin[2]= s.charAt(2);
                    pin[3] = -1;
                    i3.setImageResource(R.drawable.pin_circle_active);
                    i4.setImageResource(R.drawable.pin_circle);
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
                    goNext();
                    finish();
                }
                i1.setImageResource(R.drawable.pin_circle_active);
            }
        });
    }

    private void goNext() {
        Intent myIntent = new Intent(PincodeActivity.this,StartNodeActivity.class);
        PincodeActivity.this.startActivity(myIntent);
    }

}
