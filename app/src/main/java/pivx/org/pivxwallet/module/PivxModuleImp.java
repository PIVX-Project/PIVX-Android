package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import chain.BlockchainManager;
import global.ContextWrapper;
import global.WalletConfiguration;
import pivtrum.PivtrumPeergroup;
import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.contacts.ContactsStore;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.rate.db.RateDb;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputWrapper;
import pivx.org.pivxwallet.ui.wallet_activity.TransactionWrapper;
import store.AddressBalance;
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
    private RateDb rateDb;

    // cache balance
    private long availableBalance = 0;
    private BigDecimal pivInUsdHardcoded = new BigDecimal("1.5");

    public PivxModuleImp(ContextWrapper contextWrapper, WalletConfiguration walletConfiguration,AddressStore addressStore,ContactsStore contactsStore,RateDb rateDb) {
        this.context = contextWrapper;
        this.walletConfiguration = walletConfiguration;
        this.addressStore = addressStore;
        this.contactsStore = contactsStore;
        this.rateDb = rateDb;
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
    public Coin getUnnavailableBalanceCoin() {
        return walletManager.getUnspensableBalance();
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
    public void saveContact(Contact contact) throws ContactAlreadyExistException {
        if (contactsStore.getContact(contact.getAddresses().get(0))!=null) throw new ContactAlreadyExistException();
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
            Map<Integer,Contact> outputsLabeled = new HashMap<>();
            Map<Integer,Contact> inputsLabeled = new HashMap<>();
            Address address = null;
            if (isMine){
                try {
                    for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                        address = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams());
                        // if the tx is mine i know that the first output address is the sent and the second one is the change address
                        outputsLabeled.put(transactionOutput.getIndex(),contactsStore.getContact(address.toBase58()));
                    }

                    /*for (TransactionInput transactionInput : transaction.getInputs()) {
                        try {
                            address = transactionInput.getScriptSig().getToAddress(getConf().getNetworkParams());
                            // if the tx is mine i know that the first output address is the sent and the second one is the change address
                            inputsLabeled.put((int) transactionInput.getOutpoint().getIndex(), contactsStore.getContact(address.toBase58()));
                        }catch (ScriptException e){
                            e.printStackTrace();
                        }
                    }*/

                }catch (Exception e){
                    e.printStackTrace();
                    //swallow this for now..
                }
            }else {
                for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                    Address addressToCheck = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams());
                    if(walletManager.isAddressMine(addressToCheck)){
                        address = addressToCheck;
                        break;
                    }
                }

                for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                    address = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams());
                    // if the tx is mine i know that the first output address is the sent and the second one is the change address
                    outputsLabeled.put(transactionOutput.getIndex(),contactsStore.getContact(address.toBase58()));
                }
            }
            list.add(new TransactionWrapper(
                    transaction,
                    inputsLabeled,
                    outputsLabeled,
                    isMine ? getValueSentFromMe(transaction,true):walletManager.getValueSentToMe(transaction),
                    isMine ? TransactionWrapper.TransactionUse.SENT_SINGLE: TransactionWrapper.TransactionUse.RECEIVE
                    )
            );
        }
        return list;
    }

    @Override
    public Coin getValueSentFromMe(Transaction transaction, boolean excludeChangeAddress) {
        if (excludeChangeAddress){
            return transaction.getOutput(0).getValue();
        }else
            return walletManager.getValueSentFromMe(transaction);
    }

    @Override
    public void commitTx(Transaction transaction) {
        walletManager.commitTx(transaction);
    }

    @Override
    public List<Peer> listConnectedPeers() {
        return blockchainManager.listConnectedPeers();
    }

    @Override
    public int getChainHeight() {
        return blockchainManager.getChainHeadHeight();
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

    public void addOnTransactionConfidenceChange(TransactionConfidenceEventListener transactionConfidenceEventListener) {
        walletManager.addOnTransactionsConfidenceChange(transactionConfidenceEventListener);
    }

    public void removeTransactionsConfidenceChange(TransactionConfidenceEventListener transactionConfidenceEventListener) {
        walletManager.removeTransactionConfidenceChange(transactionConfidenceEventListener);
    }

    public PivxRate getRate(String coin) {
        return rateDb.getRate(coin);
    }

    @Override
    public Wallet getWallet() {
        return walletManager.getWallet();
    }

    @Override
    public List<InputWrapper> listUnspentWrappers() {
        List<InputWrapper> inputWrappers = new ArrayList<>();
        for (TransactionOutput transactionOutput : walletManager.listUnspent()) {
            Address address = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams());
            // if the tx is mine i know that the first output address is the sent and the second one is the change address
            Contact contact = contactsStore.getContact(address.toBase58());
            inputWrappers.add(
                    new InputWrapper(
                            transactionOutput,
                            contact
                    )

            );
        }
        return inputWrappers;
    }

    @Override
    public Transaction getTx(Sha256Hash txId) {
        return walletManager.getTransaction(txId);
    }

    @Override
    public List<String> getMnemonic() {
        return walletManager.getMnemonic();
    }

    @Override
    public DeterministicKey getKeyPairForAddress(Address address) {
        return walletManager.getKeyPairForAddress(address);
    }

    @Override
    public TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) {
        return walletManager.getUnspent(parentTxHash,index);
    }

    public void saveRate(PivxRate pivxRate){
        rateDb.insertOrUpdateIfExist(pivxRate);
    }
}
