package pivx.org.pivxwallet.ui.transaction_send_activity;

import android.text.Editable;
import android.text.TextWatcher;

public class MyTextWatcher implements TextWatcher {
    private MyFilterableAdapter lAdapter;

    public MyTextWatcher(MyFilterableAdapter lAdapter) {
        this.lAdapter = lAdapter;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        lAdapter.getFilter().filter(s.toString().toLowerCase());
    }
}