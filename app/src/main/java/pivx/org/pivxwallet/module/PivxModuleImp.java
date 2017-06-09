package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;

import java.io.File;

import pivx.org.pivxwallet.module.wallet.WalletManager;

/**
 * Created by mati on 18/04/17.
 */

public class PivxModuleImp implements PivxModule {

    WalletManager walletManager;


    public PivxModuleImp(ContextWrapper contextWrapper,WalletConfiguration walletConfiguration) {
        walletManager = new WalletManager(contextWrapper,walletConfiguration);
        walletManager.init();
    }

    @Override
    public void createWallet() {

    }

    @Override
    public void restoreWallet(File backupFile, String password) {

    }

    @Override
    public boolean isWalletCreated() {
        return false;
    }

    @Override
    public Address freshNewAddress() {
        return walletManager.newFreshReceiveAddress();
    }

    @Override
    public boolean isAddressUsed(Address address) {
        return walletManager.isMarkedAddress();
    }
}
