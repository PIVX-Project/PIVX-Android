package pivx.org.pivxwallet.ui.base;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import pivx.org.pivxwallet.R;

/**
 * Created by mati on 18/04/17.
 */

public abstract class BaseActivity extends PivxActivity {

    protected Toolbar toolbar;
    protected FrameLayout childContainer;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (isFullScreen()) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setContentView(R.layout.activity_base_without_toolbar);
        } else{
            setContentView(R.layout.activity_base);
        }
        init();
        // onCreateChildMethod
        onCreateView(savedInstanceState,childContainer);

    }

    private final void init(){
        childContainer = (FrameLayout) findViewById(R.id.content);
        if (hasToolbar() && !isFullScreen()) {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onNavigationBackPressed();
                    onBackPressed();
                }
            });
        }
    }

    public boolean hasToolbar() {
        return true;
    }

    public boolean isFullScreen() {
        return false;
    }

    /**
     * Empty method to override.
     *
     * Launched when the user clicks on the toolbar navigation icon
     */
    protected void onNavigationBackPressed() {

    }

    /**
     * Empty method to override.
     *
     * @param savedInstanceState
     */
    protected void onCreateView(Bundle savedInstanceState, ViewGroup container){

    }

    protected boolean checkPermission(String permission) {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }



}
