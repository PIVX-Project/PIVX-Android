package pivx.org.pivxwallet.ui.loading;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.start_activity.StartActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

public class LoadingActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // remove title
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.loading_activity);

        videoView = (VideoView) findViewById(R.id.videoView);
        Uri video;
        video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.loading_video);

        if (videoView != null) {
            videoView.setVideoURI(video);
            videoView.setZOrderOnTop(true);
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            android.widget.FrameLayout.LayoutParams params = (android.widget.FrameLayout.LayoutParams) videoView.getLayoutParams();
            params.width =  metrics.widthPixels;
            params.height = metrics.heightPixels;
            params.leftMargin = 0;
            videoView.setLayoutParams(params);
            videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
            });
            videoView.setOnErrorListener((mediaPlayer, i, i1) -> {
                videoView.setVisibility(View.GONE);
                return true;
            });
            videoView.start();
        }


        start(TimeUnit.SECONDS.toMillis(4));
    }

    private void start(long millis){
        new Handler().postDelayed(
                () -> {
                    if(PivxApplication.getInstance().isCoreStarted()) {
                        runOnUiThread(() -> {
                            if (PivxApplication.getInstance().getAppConf().isAppInit()) {
                                Intent intent = new Intent(this, WalletActivity.class);
                                startActivity(intent);
                            } else {
                                // Jump to your Next Activity or MainActivity
                                Intent intent = new Intent(this, StartActivity.class);
                                startActivity(intent);
                            }
                            finish();
                        });
                    }else {
                        start(TimeUnit.SECONDS.toMillis(15));
                    }
                }, millis
        );
    }
}
