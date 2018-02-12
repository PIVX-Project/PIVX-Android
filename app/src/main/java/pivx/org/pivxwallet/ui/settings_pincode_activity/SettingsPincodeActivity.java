package pivx.org.pivxwallet.ui.settings_pincode_activity;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;


/**
 * Created by Neoperol on 5/18/17.
 */

public class SettingsPincodeActivity extends BaseActivity implements KeyboardFragment.onKeyListener {

    private static final int OLDER_PIN = 99;
    private static final int NEW_PIN = 100;

    private ImageView i1, i2, i3, i4;
    private KeyboardFragment keyboardFragment;
    private TextView pincodeMessage;

    int[] pin = new int[4];
    int lastPos = 0;

    private int reason = OLDER_PIN;

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
        pincodeMessage = (TextView) findViewById(R.id.pincodeMessage);
        keyboardFragment = (KeyboardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_keyboard);
        keyboardFragment.setOnKeyListener(this);
        if (reason==OLDER_PIN){
            pincodeMessage.setText(R.string.insert_old_pin);
        }
    }

    @Override
    public void onKeyClicked(KeyboardFragment.KEYS key) {
        if (key.getValue()<10){
            pin[lastPos] = key.getValue();
            activeCheck(lastPos);
            lastPos++;
            if (lastPos==4){
                String pincode = String.valueOf(pin[0])+String.valueOf(pin[1])+String.valueOf(pin[2])+String.valueOf(pin[3]);
                if (reason==OLDER_PIN){
                    if (pincode.equals(pivxApplication.getAppConf().getPincode())){
                        clear();
                        reason = NEW_PIN;
                        pincodeMessage.setText(R.string.insert_new_pin);
                    }else {
                        clear();
                        Toast.makeText(this,R.string.invalid_pincode,Toast.LENGTH_SHORT).show();
                    }
                }else {
                    pivxApplication.getAppConf().savePincode(pincode);
                    Toast.makeText(this,R.string.pincode_saved,Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        }else if (key == KeyboardFragment.KEYS.DELETE){
            if (lastPos!=0) {
                lastPos--;
                unactiveCheck(lastPos);
            }
        }else if (key == KeyboardFragment.KEYS.CLEAR){
            clear();
        }
    }

    private void clear(){
        unactiveCheck(0);
        unactiveCheck(1);
        unactiveCheck(2);
        unactiveCheck(3);
        lastPos = 0;
    }

    private void activeCheck(int pos){
        switch (pos){
            case 0:
                i1.setImageResource(R.drawable.pin_circle_active);
                break;
            case 1:
                i2.setImageResource(R.drawable.pin_circle_active);
                break;
            case 2:
                i3.setImageResource(R.drawable.pin_circle_active);
                break;
            case 3:
                i4.setImageResource(R.drawable.pin_circle_active);
                break;
        }
    }

    private void unactiveCheck(int pos){
        switch (pos){
            case 0:
                i1.setImageResource(R.drawable.pin_circle_white);
                break;
            case 1:
                i2.setImageResource(R.drawable.pin_circle_white);
                break;
            case 2:
                i3.setImageResource(R.drawable.pin_circle_white);
                break;
            case 3:
                i4.setImageResource(R.drawable.pin_circle_white);
                break;
        }
    }

}
