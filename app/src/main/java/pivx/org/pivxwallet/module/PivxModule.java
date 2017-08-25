package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.Wallet;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import global.WalletConfiguration;
import pivx.org.pivxwallet.contacts.AddressLabel;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputWrapper;
import pivx.org.pivxwallet.ui.wallet_activity.TransactionWrapper;
import wallet.InsufficientInputsException;
import wallet.TxNotFoundException;
import wallet.exceptions.CantRestoreEncryptedWallet;

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

    boolean backupWallet(File backupFile, String password) throws IOException;

    /**
     *
     *
     * @param backupFile
     */
    void restoreWallet(File backupFile) throws IOException;

    void restoreWalletFromEncrypted(File file, String password) throws CantRestoreEncryptedWallet, IOException;

    void restoreWallet(List<String> mnemonic, long timestamp) throws IOException, MnemonicException;

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

    Coin getAvailableBalanceCoin();

    Coin getUnnavailableBalanceCoin();

    BigDecimal getAvailableBalanceLocale();

    /******    Address Label          ******/

    Collection<AddressLabel> getContacts();

    AddressLabel getAddressLabel(String address);

    void saveContact(AddressLabel addressLabel) throws ContactAlreadyExistException;

    void saveContactIfNotExist(AddressLabel addressLabel);

    void deleteAddressLabel(AddressLabel data);


    /******   End Address Label          ******/


    boolean chechAddress(String addressBase58);

    Transaction buildSendTx(String addressBase58, Coin amount, String memo) throws InsufficientMoneyException;

    WalletConfiguration getConf();

    List<TransactionWrapper> listTx();

    Coin getValueSentFromMe(Transaction transaction, boolean excludeChangeAddress);

    void commitTx(Transaction transaction);

    List<Peer> listConnectedPeers();

    int getChainHeight();

    PivxRate getRate(String selectedRateCoin);

    /**
     * Don't use this..
     * @return
     */
    @Deprecated
    Wallet getWallet();

    List<InputWrapper> listUnspentWrappers();

    Set<InputWrapper> convertFrom(List<TransactionInput> list) throws TxNotFoundException;

    Transaction getTx(Sha256Hash txId);

    List<String> getMnemonic();

    DeterministicKey getKeyPairForAddress(Address address);

    TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) throws TxNotFoundException;

    List<TransactionOutput> getRandomUnspentNotInListToFullCoins(List<TransactionInput> inputs, Coin amount) throws InsufficientInputsException;

    Transaction completeTx(Transaction transaction,Coin fee) throws InsufficientMoneyException;

    Transaction completeTxWithCustomFee(Transaction transaction,Coin fee) throws InsufficientMoneyException;

    Coin getUnspentValue(Sha256Hash parentTransactionHash, int index);

    boolean isAnyPeerConnected();

    long getConnectedPeerHeight();

    int getProtocolVersion();

    void checkMnemonic(List<String> mnemonic) throws MnemonicException;
}
