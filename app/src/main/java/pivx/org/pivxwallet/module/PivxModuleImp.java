package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

import chain.BlockchainManager;
import global.ContextWrapper;
import global.WalletConfiguration;
import pivtrum.NetworkConf;
import pivtrum.PivtrumPeergroup;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.contacts.ContactsStore;
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
    public void restoreWallet(File backupFile, String password) {

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
        return availableBalance;
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
        Transaction tx = new Transaction(walletConfiguration.getNetworkParams());

        // first check if the wallet has available balance.
        if (amount.isLessThan(Coin.valueOf(availableBalance))) throw new InsufficientMoneyException(amount,"Available amount: "+Coin.valueOf(availableBalance));
        // now get the unspent tx

        /*Script.createInputScript()
        TransactionInput transactionInput = new TransactionInput(
                walletConfiguration.getNetworkParams(),
                tx,

                );*/


        return tx;
    }

    @Override
    public WalletConfiguration getConf() {
        return walletConfiguration;
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
