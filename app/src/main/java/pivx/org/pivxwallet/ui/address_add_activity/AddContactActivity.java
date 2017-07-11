package pivx.org.pivxwallet.ui.address_add_activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import org.bitcoinj.core.Address;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.module.ContactAlreadyExistException;
import pivx.org.pivxwallet.ui.base.BaseActivity;

/**
 * Created by Neoperol on 6/8/17.
 */

public class AddContactActivity extends BaseActivity {

    public static final String ADDRESS_TO_ADD = "address";

    private View root;
    private EditText edit_name;
    private EditText edit_address;

    private String address;
    private String name;

    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        root = getLayoutInflater().inflate(R.layout.fragment_new_address, container);
        setTitle("New Contact");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        edit_name = (EditText) root.findViewById(R.id.edit_name);
        edit_address = (EditText) root.findViewById(R.id.edit_address);
        edit_address.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp = s.toString();
                if(pivxModule.chechAddress(temp)){
                    address = temp;
                    edit_address.setTextColor(Color.parseColor("#55476c"));;
                }else {
                    edit_address.setTextColor(Color.parseColor("#4d4d4d"));;
                }
            }
        });
        Intent intent = getIntent();
        if (intent!=null){
            if (intent.hasExtra(ADDRESS_TO_ADD)){
                edit_address.setText(intent.getStringExtra(ADDRESS_TO_ADD));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                name = edit_name.getText().toString();
                if (name.length()>0 && address.length()>0) {
                    try {
                        Contact contact = new Contact(name);
                        contact.addAddress(address);
                        pivxModule.saveContact(contact);
                        Toast.makeText(this, "Contact saved", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    } catch (ContactAlreadyExistException e) {
                        Toast.makeText(this,R.string.contact_already_exist,Toast.LENGTH_LONG).show();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onNavigationBackPressed() {
        // save contact

    }
}
