package pivx.org.pivxwallet.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
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
        setContentView(R.layout.activity_base);
        init();
        // onCreateChildMethod
        onCreateView(savedInstanceState,childContainer);
    }

    private void init(){
        childContainer = (FrameLayout) findViewById(R.id.content);
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

}
