package pivx.org.pivxwallet.ui.splash_activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;

import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.ui.start_activity.StartActivity;

/**
 * Created by Neoperol on 6/13/17.
 */

public class SplashActivity extends AppCompatActivity {
    VideoView videoView;
    @Override
    public void  onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        videoView = (VideoView) findViewById(R.id.videoView);

        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash);
        videoView.setVideoURI(video);

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {
                startNextActivity();
            }
        });

        videoView.start();

    }

    private void startNextActivity() {
        if (isFinishing())
            return;
        startActivity(new Intent(this, StartActivity.class));
        finish();
    }
}
