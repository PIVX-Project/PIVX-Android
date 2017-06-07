package pivx.org.pivxwallet;

import android.app.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import pivx.org.pivxwallet.module.ContextWrapper;
import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.module.PivxModuleImp;
import pivx.org.pivxwallet.module.WalletConfImp;
import pivx.org.pivxwallet.module.WalletConfiguration;

/**
 * Created by mati on 18/04/17.
 */

public class PivxApplication extends Application implements ContextWrapper {


    private PivxModule pivxModule;

    @Override
    public void onCreate() {
        super.onCreate();

        WalletConfiguration walletConfiguration = new WalletConfImp();

        pivxModule = new PivxModuleImp(this,walletConfiguration);
        pivxModule.createWallet();
    }

    public PivxModule getModule(){
        return pivxModule;
    }

    @Override
    public FileOutputStream openFileOutputPrivateMode(String name) throws FileNotFoundException {
        return openFileOutput(name,MODE_PRIVATE);
    }

    @Override
    public File getDirPrivateMode(String name) {
        return getDir(name,MODE_PRIVATE);
    }

    @Override
    public InputStream openAssestsStream(String name) throws IOException {
        return null;
    }


}
