package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

import global.WalletConfiguration;
import pivx.org.pivxwallet.contacts.Contact;

/**
 * Created by mati on 18/04/17.
 */

public interface PivxModule {

    /**
     * Initialize the module
     */
    void start() throws IOException;

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

    void saveContact(Contact contact);

    boolean chechAddress(String addressBase58);

    Transaction buildSendTx(String addressBase58, Coin amount, String memo) throws InsufficientMoneyException;

    WalletConfiguration getConf();

}
