package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;

import java.io.File;

import pivx.org.pivxwallet.ui.address_activity.AddressActivity;

/**
 * Created by mati on 18/04/17.
 */

public interface PivxModule {

    /**
     * ...
     */
    void createWallet();

    /**
     *
     *
     * @param backupFile
     * @param password
     */
    void restoreWallet(File backupFile, String password);

    /**
     * If the wallet already exist
     * @return
     */
    boolean isWalletCreated();

    /**
     * Return a new address.
     */
    Address getCurrentAddress();

    boolean isAddressUsed(Address address);
}
