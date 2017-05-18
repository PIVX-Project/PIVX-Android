package pivx.org.pivxwallet.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.R;
import pivx.org.pivxwallet.module.PivxModule;

/**
 * Created by mati on 18/04/17.
 */

public abstract class BaseActivity extends AppCompatActivity{
    public Toolbar toolbar;
    public FrameLayout frameLayout;
    protected PivxModule pivxModule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pivxModule = ((PivxApplication) getApplication()).getModule();

        // onCreateChildMethod
        onCreateView(savedInstanceState);
    }
    
    protected void onCreateView(Bundle savedInstanceState){
        super.setContentView(R.layout.activity_base);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        frameLayout = (FrameLayout) findViewById(R.id.content);
    }

}
