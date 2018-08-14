package pivx.org.pivxwallet.ui.export_zpiv;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.zerocoinj.core.ZCoin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pivx.org.pivxwallet.R;

public class ExportZpiv extends AppCompatActivity {

    public static final String INTENT_EXTRA_EXPORT_ZCOINS = "export";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_zpiv_main);


        Toolbar toolbar = ((Toolbar)findViewById(R.id.toolbar));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        String zcoins =getIntent().getStringExtra(INTENT_EXTRA_EXPORT_ZCOINS);
        TextView txt_export = (TextView) findViewById(R.id.txt_export);

        txt_export.setText(zcoins);

        Log.i("REMOVE ME!", zcoins);
    }
}
