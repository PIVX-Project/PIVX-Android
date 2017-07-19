package pivx.org.pivxwallet.ui.transaction_detail_activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.security_words_activity.SecurityWordsActivity;

/**
 * Created by Neoperol on 6/9/17.
 */

public class TransactionDetailActivity extends BaseActivity {

    private TextView text_transactionType;
    private TextView text_amount_Pivx;
    private TextView text_amount_currency;
    private TextView text_date;
    private TextView text_address;
    private TextView text_fee;
    private TextView text_transaction_hash;
    @Override
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container) {
        getLayoutInflater().inflate(R.layout.fragment_transaction_detail, container);
        setTitle("Transaction Detail");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        text_transactionType = (TextView) findViewById(R.id.text_transactionType);
        text_amount_Pivx = (TextView) findViewById(R.id.text_transactionType);
        text_amount_currency = (TextView) findViewById(R.id.text_transactionType);
        text_date = (TextView) findViewById(R.id.text_transactionType);
        text_address = (TextView) findViewById(R.id.text_transactionType);
        text_fee = (TextView) findViewById(R.id.text_fee);
        text_transaction_hash = (TextView) findViewById(R.id.text_transaction_hash);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItem = menu.add(0,0,0,R.string.explorer);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com"));
                startActivity(browserIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
