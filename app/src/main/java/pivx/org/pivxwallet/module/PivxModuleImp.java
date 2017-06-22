package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;

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

    private WalletConfiguration walletConfiguration;
    private WalletManager walletManager;
    private PivtrumPeergroup peergroup;
    private AddressStore addressStore;
    private ContactsStore contactsStore;

    // cache balance
    private long availableBalance = 0;
    private BigDecimal pivInUsdHardcoded = new BigDecimal("1.5");

    public PivxModuleImp(ContextWrapper contextWrapper, WalletConfiguration walletConfiguration,AddressStore addressStore,ContactsStore contactsStore) throws IOException {
        this.walletConfiguration = walletConfiguration;
        this.addressStore = addressStore;
        this.contactsStore = contactsStore;
        walletManager = new WalletManager(contextWrapper,walletConfiguration);
        walletManager.init();
        for (AddressBalance addressBalance : addressStore.listBalance()) {
            availableBalance+=addressBalance.getConfirmedBalance();
        }
    }

    public void setPeergroup(PivtrumPeergroup peergroup){
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


}
