package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import chain.BlockchainManager;
import global.ContextWrapper;
import global.WalletConfiguration;
import pivtrum.NetworkConf;
import pivtrum.PivtrumPeergroup;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.contacts.ContactsStore;
import pivx.org.pivxwallet.module.wallet.WalletBackupHelper;
import pivx.org.pivxwallet.ui.wallet_activity.TransactionWrapper;
import store.AddressBalance;
import store.AddressNotFoundException;
import store.AddressStore;
import wallet.WalletManager;

/**
 * Created by mati on 18/04/17.
 */

public class PivxModuleImp implements PivxModule {

    private ContextWrapper context;
    private WalletConfiguration walletConfiguration;
    private WalletManager walletManager;
    private BlockchainManager blockchainManager;
    private PivtrumPeergroup peergroup;
    private AddressStore addressStore;
    private ContactsStore contactsStore;

    // cache balance
    private long availableBalance = 0;
    private BigDecimal pivInUsdHardcoded = new BigDecimal("1.5");

    public PivxModuleImp(ContextWrapper contextWrapper, WalletConfiguration walletConfiguration,AddressStore addressStore,ContactsStore contactsStore) {
        this.context = contextWrapper;
        this.walletConfiguration = walletConfiguration;
        this.addressStore = addressStore;
        this.contactsStore = contactsStore;
        walletManager = new WalletManager(contextWrapper,walletConfiguration);
        blockchainManager = new BlockchainManager(context,walletManager,walletConfiguration);
        for (AddressBalance addressBalance : addressStore.listBalance()) {
            availableBalance+=addressBalance.getConfirmedBalance();
        }
    }

    public void start() throws IOException{
        walletManager.init();
    }

    public void setPivtrumPeergroup(PivtrumPeergroup peergroup){
        peergroup.setAddressStore(addressStore);
        peergroup.setWalletManager(walletManager);
        this.peergroup = peergroup;
    }

    @Override
    public void createWallet() {

    }

    @Override
    public boolean backupWallet(File backupFile, String password) throws IOException {
        //todo: add the backup reminder here..
        return walletManager.backupWallet(backupFile,password);

    }

    @Override
    public void restoreWallet(File backupFile) throws IOException {
        // restore wallet and launch the restart of the blockchain...
        walletManager.restoreWalletFromProtobuf(backupFile);
    }

    @Override
    public void restoreWalletFromEncrypted(File file, String password) throws IOException {
        walletManager.restoreWalletFromEncrypted(file,password);
    }

    @Override
    public boolean isWalletCreated() {
        return false;
    }

    @Override
    public Address getAddress() {
        Address address = walletManager.getCurrentAddress();
        if (peergroup!=null && peergroup.isRunning()){
            peergroup.addWatchedAddress(address);
        }
        return address;
    }

    @Override
    public boolean isAddressUsed(Address address) {
        return walletManager.isMarkedAddress();
    }

    @Override
    public long getAvailableBalance() {
        return walletManager.getAvailableBalance().longValue();//availableBalance;
    }

    @Override
    public Coin getAvailableBalanceCoin() {
        return walletManager.getAvailableBalance();//availableBalance;
    }

    @Override
    public BigDecimal getAvailableBalanceLocale() {
        return pivInUsdHardcoded.multiply(new BigDecimal(availableBalance));
    }

    @Override
    public Collection<Contact> getContacts(){
        return contactsStore.list();
    }

    @Override
    public void saveContact(Contact contact) {
        contactsStore.insert(contact);
    }

    @Override
    public boolean chechAddress(String addressBase58) {
        boolean result = false;
        try {
            Address.fromBase58(walletConfiguration.getNetworkParams(), addressBase58);
            result = true;
        }catch (Exception e){
            // nothing..
        }
        return result;
    }

    @Override
    public Transaction buildSendTx(String addressBase58, Coin amount, String memo) throws InsufficientMoneyException {
        Address address = Address.fromBase58(walletConfiguration.getNetworkParams(), addressBase58);

        SendRequest sendRequest = SendRequest.to(address,amount);
        sendRequest.memo = memo;
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        //sendRequest.changeAddress -> add the change address with address that i know instead of give this job to the wallet.
        walletManager.completeSend(sendRequest);

        return sendRequest.tx;
    }

    @Override
    public WalletConfiguration getConf() {
        return walletConfiguration;
    }

    @Override
    public List<TransactionWrapper> listTx() {
        List<TransactionWrapper> list = new ArrayList<>();
        for (Transaction transaction : walletManager.listTransactions()) {
            boolean isMine = walletManager.isMine(transaction);
            Contact contact = null;
            if (isMine){
                try {
                    // if the tx is mine i know that the first output address is the sent and the second one is the change address
                    contact = contactsStore.getContact(transaction.getOutput(0).getAddressFromP2SH(getConf().getNetworkParams()).toBase58());
                }catch (Exception e){
                    e.printStackTrace();
                    //swallow this for now..
                }
            }
            list.add(new TransactionWrapper(
                    transaction,
                    contact,
                    isMine ? walletManager.getValueSentFromMe(transaction):walletManager.getValueSentToMe(transaction),
                    isMine ? TransactionWrapper.TransactionUse.SENT_SINGLE: TransactionWrapper.TransactionUse.RECEIVE
                    )
            );
        }
        return list;
    }

    @Override
    public Coin getValueSentFromMe(Transaction transaction) {
        return walletManager.getValueSentFromMe(transaction);
    }


    public BlockchainManager getBlockchainManager() {
        return blockchainManager;
    }

    public void addCoinsReceivedEventListener(WalletCoinsReceivedEventListener coinReceiverListener) {
        walletManager.addCoinsReceivedEventListener(coinReceiverListener);
    }

    public void removeCoinsReceivedEventListener(WalletCoinsReceivedEventListener coinReceiverListener) {
        walletManager.removeCoinsReceivedEventListener(coinReceiverListener);
    }
}
