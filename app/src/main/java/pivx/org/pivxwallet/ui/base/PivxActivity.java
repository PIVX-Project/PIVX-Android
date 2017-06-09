package pivx.org.pivxwallet.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import pivx.org.pivxwallet.PivxApplication;
import pivx.org.pivxwallet.module.PivxModule;

/**
 * Created by furszy on 6/8/17.
 */

public class PivxActivity extends AppCompatActivity {

    protected PivxApplication pivxApplication;
    protected PivxModule pivxModule;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pivxApplication = PivxApplication.getInstance();
        pivxModule = pivxApplication.getModule();
    }
}
