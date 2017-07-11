package pivx.org.pivxwallet.ui.pincode_activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import java.util.List;
import java.util.Random;

import global.PivtrumGlobalData;
import pivtrum.PivtrumPeerData;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.settings_pincode_activity.KeyboardFragment;
import pivx.org.pivxwallet.ui.start_node_activity.StartNodeActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 4/20/17.
 */

public class PincodeActivity extends BaseActivity implements KeyboardFragment.onKeyListener {

    ImageView i1, i2, i3, i4;
    int[] pin = new int[4];
    int lastPos = 0;

    KeyboardFragment keyboardFragment;

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
        keyboardFragment = (KeyboardFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_keyboard);
        keyboardFragment.setOnKeyListener(this);
        keyboardFragment.setTextButtonsColor(Color.WHITE);
    }

    private void goNext() {

        if (pivxApplication.getAppConf().getTrustedNode()==null){
            // select random trusted node
            List<PivtrumPeerData> nodes = PivtrumGlobalData.listTrustedHosts();
            Random random = new Random();
            pivxApplication.setTrustedServer(nodes.get(random.nextInt(nodes.size())));
            pivxApplication.stopBlockchain();
        }

        Intent myIntent = new Intent(PincodeActivity.this,WalletActivity.class);
        pivxApplication.getAppConf().setAppInit(true);
        startActivity(myIntent);
    }


    @Override
    public void onKeyClicked(KeyboardFragment.KEYS key) {
        if (key.getValue()<10){
            pin[lastPos] = key.getValue();
            activeCheck(lastPos);
            lastPos++;
            if (lastPos==4){
                String pincode = String.valueOf(pin[0])+String.valueOf(pin[1])+String.valueOf(pin[2])+String.valueOf(pin[3]);
                pivxApplication.getAppConf().savePincode(pincode);
                Toast.makeText(this,R.string.pincode_saved,Toast.LENGTH_SHORT).show();
                goNext();
            }
        }else if (key == KeyboardFragment.KEYS.DELETE){
            lastPos--;
            unactiveCheck(lastPos);
        }else if (key == KeyboardFragment.KEYS.CLEAR){
            unactiveCheck(0);
            unactiveCheck(1);
            unactiveCheck(2);
            unactiveCheck(3);
            lastPos = 0;
        }
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
