package pivx.org.pivxwallet.module;

import org.bitcoinj.wallet.Wallet;

import java.io.File;

/**
 * Created by mati on 18/04/17.
 */

public class PivxModuleImp implements PivxModule {

    WalletManager walletManager;


    public PivxModuleImp(ContextWrapper contextWrapper,WalletConfiguration walletConfiguration) {
        walletManager = new WalletManager(contextWrapper,walletConfiguration);
    }

    @Override
    public void createWallet() {
        walletManager.init();
    }

    @Override
    public void restoreWallet(File backupFile, String password) {

    }

    @Override
    public boolean isWalletCreated() {
        return false;
    }
}
