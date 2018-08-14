package pivx.org.pivxwallet.ui.loading;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.start_activity.StartActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

public class LoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_activity);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                PivxApplication.getInstance().startCore();
                runOnUiThread(() -> {
                    if (PivxApplication.getInstance().getAppConf().isAppInit()){
                        Intent intent = new Intent(this, WalletActivity.class);
                        startActivity(intent);
                    }else {
                        // Jump to your Next Activity or MainActivity
                        Intent intent = new Intent(this, StartActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
            } catch (IOException e) {
                Toast.makeText(LoadingActivity.this, "Error loading wallet",Toast.LENGTH_SHORT).show();
            }
        });
        executor.shutdown();
    }
}
