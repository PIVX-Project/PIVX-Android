package pivx.org.pivxwallet;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;

import com.github.anrwatchdog.ANRWatchDog;
import com.snappydb.SnappydbException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import global.ContextWrapper;
import pivtrum.NetworkConf;
import pivx.org.pivxwallet.contacts.ContactsStore;
import pivx.org.pivxwallet.module.PivxModule;
import pivx.org.pivxwallet.module.PivxModuleImp;
import pivx.org.pivxwallet.module.WalletConfImp;
import global.WalletConfiguration;
import pivx.org.pivxwallet.module.store.SnappyStore;
import pivx.org.pivxwallet.service.PivxWalletService;
import pivx.org.pivxwallet.utils.AppConf;
import store.AddressStore;

import static pivx.org.pivxwallet.service.IntentsConstants.ACTION_RESET_BLOCKCHAIN;

/**
 * Created by mati on 18/04/17.
 */

public class PivxApplication extends Application implements ContextWrapper {

    private static Logger log;

    /** Singleton */
    private static PivxApplication instance;

    private PivxModule pivxModule;
    private AppConf appConf;
    private NetworkConf networkConf;

    private ActivityManager activityManager;
    private PackageInfo info;

    public static PivxApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        try {
            initLogging();
            log = LoggerFactory.getLogger(PivxApplication.class);
            PackageManager manager = getPackageManager();
            info = manager.getPackageInfo(this.getPackageName(), 0);
            activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            new ANRWatchDog().setIgnoreDebugger(true).start();
            // Default network conf for localhost test
            networkConf = new NetworkConf("10.0.2.2",50001);
            appConf = new AppConf(getSharedPreferences(AppConf.PREFERENCE_NAME, MODE_PRIVATE));
            WalletConfiguration walletConfiguration = new WalletConfImp(getSharedPreferences("pivx_wallet",MODE_PRIVATE));
            //todo: add this on the initial wizard..
            walletConfiguration.saveTrustedNode(HardcodedConstants.TESTNET_HOST,0);
            AddressStore addressStore = new SnappyStore(getDirPrivateMode("address_store").getAbsolutePath());
            ContactsStore contactsStore = new ContactsStore(this);
            pivxModule = new PivxModuleImp(this, walletConfiguration,addressStore,contactsStore);
            pivxModule.start();
            // start service
            startPivxService();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SnappydbException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startPivxService() {
        Intent intent = new Intent(this,PivxWalletService.class);
        startService(intent);
    }

    private void initLogging() {
        final File logDir = getDir("log", MODE_PRIVATE);
        final File logFile = new File(logDir, "app.log");
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        final PatternLayoutEncoder filePattern = new PatternLayoutEncoder();
        filePattern.setContext(context);
        filePattern.setPattern("%d{HH:mm:ss,UTC} [%thread] %logger{0} - %msg%n");
        filePattern.start();

        final RollingFileAppender<ILoggingEvent > fileAppender = new RollingFileAppender<ILoggingEvent>();
        fileAppender.setContext(context);
        fileAppender.setFile(logFile.getAbsolutePath());

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(context);
        rollingPolicy.setParent(fileAppender);
        rollingPolicy.setFileNamePattern(logDir.getAbsolutePath() + "/wallet.%d{yyyy-MM-dd,UTC}.log.gz");
        rollingPolicy.setMaxHistory(7);
        rollingPolicy.start();

        fileAppender.setEncoder(filePattern);
        fileAppender.setRollingPolicy(rollingPolicy);
        fileAppender.start();

        final PatternLayoutEncoder logcatTagPattern = new PatternLayoutEncoder();
        logcatTagPattern.setContext(context);
        logcatTagPattern.setPattern("%logger{0}");
        logcatTagPattern.start();

        final PatternLayoutEncoder logcatPattern = new PatternLayoutEncoder();
        logcatPattern.setContext(context);
        logcatPattern.setPattern("[%thread] %msg%n");
        logcatPattern.start();

        final LogcatAppender logcatAppender = new LogcatAppender();
        logcatAppender.setContext(context);
        logcatAppender.setTagEncoder(logcatTagPattern);
        logcatAppender.setEncoder(logcatPattern);
        logcatAppender.start();

        final ch.qos.logback.classic.Logger log = context.getLogger(Logger.ROOT_LOGGER_NAME);
        log.addAppender(fileAppender);
        log.addAppender(logcatAppender);
        log.setLevel(Level.INFO);
    }

    public PivxModule getModule(){
        return pivxModule;
    }

    public AppConf getAppConf(){
        return appConf;
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
        return getAssets().open(name);
    }

    @Override
    public boolean isMemoryLow() {
        final int memoryClass = activityManager.getMemoryClass();
        return memoryClass<=pivxModule.getConf().getMinMemoryNeeded();
    }

    @Override
    public String getVersionName() {
        return info.versionName;
    }

    @Override
    public void stopBlockchain() {
        Intent intent = new Intent(this,PivxWalletService.class);
        intent.setAction(ACTION_RESET_BLOCKCHAIN);
        startService(intent);
    }

    public NetworkConf getNetworkConf() {
        return networkConf;
    }



    /*public ServiceConnection pivxServiceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder binder) {
            log.info("profile service connected");
            pivxWalletService = ((PivxWalletService.PivxBinder)binder).getService();
            //isConnected.set(true);
            //listener.onConnected();
        }
        //binder comes from server to communicate with method's of

        public void onServiceDisconnected(ComponentName className) {
            Log.d("ServiceConnection","disconnected");
            //isConnected.set(false);
            pivxWalletService = null;
            //listener.onDisconnected();
        }
    };*/


}
