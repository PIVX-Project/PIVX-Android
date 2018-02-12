package pivx.org.pivxwallet.ui.transaction_send_activity.custom;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.pivxj.core.AddressFormatException;
import org.pivxj.uri.PivxURI;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.utils.DialogsUtil;
import pivx.org.pivxwallet.utils.scanner.ScanActivity;
import static android.Manifest.permission_group.CAMERA;
import static pivx.org.pivxwallet.utils.scanner.ScanActivity.INTENT_EXTRA_RESULT;


/**
 * Created by furszy on 10/17/17.
 */

public class ChangeAddressActivity extends BaseActivity {

    private static final int SCANNER_RESULT = 100;
    public static final String INTENT_EXTRA_CLEAR_CHANGE_ADDRESS = "intent_extra_clear_change";
    public static final String INTENT_EXTRA_CHANGE_ADDRESS = "intent_extra_change_address";
    public static final String INTENT_EXTRA_CHANGE_SEND_ORIGIN = "intent_extra_send_origin";

    // UI
    private View root;
    private CheckBox check_send_origin;
    private EditText edit_address;
    private ImageView img_qr;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        super.onCreateView(savedInstanceState, container);
        setTitle(R.string.option_change_address);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        root = getLayoutInflater().inflate(R.layout.change_address_main,container);
        check_send_origin = (CheckBox) root.findViewById(R.id.check_send_origin);
        edit_address = (EditText) root.findViewById(R.id.edit_address);
        img_qr = (ImageView) root.findViewById(R.id.img_qr);

        loadData(getIntent());

        check_send_origin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    disableAddress();
                }else {
                    enableAddress();
                }
            }
        });

        img_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkPermission(CAMERA)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        int permsRequestCode = 200;
                        String[] perms = {"android.permission.CAMERA"};
                        requestPermissions(perms, permsRequestCode);
                    }
                }
                startActivityForResult(new Intent(ChangeAddressActivity.this, ScanActivity.class),SCANNER_RESULT);
            }
        });

    }

    private void loadData(Intent intent) {
        if (intent.hasExtra(INTENT_EXTRA_CHANGE_ADDRESS)){
            edit_address.setText(intent.getStringExtra(INTENT_EXTRA_CHANGE_ADDRESS));
        }
        if (intent.hasExtra(INTENT_EXTRA_CHANGE_SEND_ORIGIN)){
            if (intent.getBooleanExtra(INTENT_EXTRA_CHANGE_SEND_ORIGIN,false)) {
                disableAddress();
            }else {
                enableAddress();
            }
        }
    }

    public void disableAddress(){
        edit_address.setEnabled(false);
        edit_address.setText("");
        edit_address.setHint(R.string.origin_address);
    }

    public void enableAddress(){
        edit_address.setHint(R.string.add_address);
        edit_address.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_menu_default,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId()==R.id.option_ok){
            try {
                Intent intent = new Intent();
                boolean sendOrigin = check_send_origin.isChecked();
                if (sendOrigin){
                    intent.putExtra(INTENT_EXTRA_CHANGE_SEND_ORIGIN,sendOrigin);
                }else {
                    String address = getAndCheckAddress();
                    intent.putExtra(INTENT_EXTRA_CLEAR_CHANGE_ADDRESS, address);
                }
                setResult(RESULT_OK,intent);
                finish();
                return true;
            } catch (AddressFormatException e) {
                e.printStackTrace();
                DialogsUtil.buildSimpleErrorTextDialog(this,getString(R.string.invalid_inputs),getString(R.string.invalid_input_address)).show(getFragmentManager(),"custom_change_address");
            }
        }else if (item.getItemId() == R.id.option_default){
            Intent intent = new Intent();
            intent.putExtra(INTENT_EXTRA_CLEAR_CHANGE_ADDRESS,true);
            setResult(RESULT_OK,intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCANNER_RESULT){
            if (resultCode==RESULT_OK) {
                try {
                    String address = "";
                    address = data.getStringExtra(INTENT_EXTRA_RESULT);
                    String usedAddress;
                    if (pivxModule.chechAddress(address)){
                        usedAddress = address;
                    }else {
                        PivxURI pivxUri = new PivxURI(address);
                        usedAddress = pivxUri.getAddress().toBase58();
                    }
                    edit_address.setText(usedAddress);
                }catch (Exception e){
                    Toast.makeText(ChangeAddressActivity.this,R.string.bad_address,Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getAndCheckAddress() {
        String address = edit_address.getText().toString();
        if (!pivxModule.chechAddress(address)){
            throw new AddressFormatException();
        }
        return address;
    }
}
