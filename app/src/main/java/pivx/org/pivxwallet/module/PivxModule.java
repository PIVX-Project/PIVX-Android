package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;

import java.io.File;
import java.math.BigDecimal;
import java.util.Collection;

import pivx.org.pivxwallet.contacts.Contact;

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
    Address getAddress();

    boolean isAddressUsed(Address address);

    long getAvailableBalance();

    BigDecimal getAvailableBalanceLocale();

    Collection<Contact> getContacts();
}
