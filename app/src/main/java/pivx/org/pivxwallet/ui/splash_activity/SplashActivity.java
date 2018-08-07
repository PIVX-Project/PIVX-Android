package pivx.org.pivxwallet.ui.splash_activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.loading.LoadingActivity;
import pivx.org.pivxwallet.ui.start_activity.StartActivity;
import pivx.org.pivxwallet.ui.wallet_activity.WalletActivity;

/**
 * Created by Neoperol on 6/13/17.
 */

public class SplashActivity extends AppCompatActivity {
    VideoView videoView;
    private boolean ispaused = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        videoView = (VideoView) findViewById(R.id.video_view);
        Uri video;
        if(PivxApplication.getInstance().getAppConf().isSplashSoundEnabled())
            video = Uri.parse("android.resource://" + getPackageName() + "/"
                + R.raw.splash_video);
        else {
            //video = Uri.parse("android.resource://" + getPackageName() + "/"
            //        + R.raw.splash_video_muted);
            //Intent intent = new Intent(this, WalletActivity.class);
            //startActivity(intent);
            jump();
            return;
        }

        if (videoView != null) {
            videoView.setVideoURI(video);
            videoView.setZOrderOnTop(true);
            videoView.setOnCompletionListener(mp -> jump());

            videoView.setOnErrorListener((mediaPlayer, i, i1) -> {
                jump();
                return true;
            });

            videoView.start();

        }else{
            jump();
        }
    }


    private void jump() {
        startActivity(new Intent(this, LoadingActivity.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ispaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ispaused) {
            jump();
        }

    }
}
