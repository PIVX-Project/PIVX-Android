package pivx.org.pivxwallet;

import android.app.Application;

import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.module.PivxModuleImp;

/**
 * Created by mati on 18/04/17.
 */

public class PivxApplication extends Application {


    private PivxModule pivxModule;


    @Override
    public void onCreate() {
        super.onCreate();

        pivxModule = new PivxModuleImp();

    }


    public PivxModule getModule(){
        return pivxModule;
    }
}
