package pivx.org.pivxwallet.ui.start_activity;

import android.content.Intent;
import android.graphics.RadialGradient;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.base.BaseActivity;
import pivx.org.pivxwallet.ui.restore_activity.RestoreActivity;

/**
 * Created by mati on 18/04/17.
 */

public class StartActivity extends BaseActivity {


    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        super.onCreateView(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.fragment_start);

        // example..
        pivxModule.createWallet();
        Button next = (Button) findViewById(R.id.btnCreate);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), RestoreActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

    }
}
