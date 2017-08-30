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
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.CoinSelector;
import org.bitcoinj.wallet.DefaultCoinSelector;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chain.BlockchainManager;
import chain.BlockchainState;
import global.ContextWrapper;
import global.WalletConfiguration;
import pivtrum.PivtrumPeergroup;
import pivx.org.pivxwallet.contacts.AddressLabel;
import pivx.org.pivxwallet.contacts.ContactsStore;
import pivx.org.pivxwallet.rate.db.PivxRate;
import pivx.org.pivxwallet.rate.db.RateDb;
import pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs.InputWrapper;
import pivx.org.pivxwallet.ui.wallet_activity.TransactionWrapper;
import store.AddressBalance;
import store.AddressStore;
import wallet.InsufficientInputsException;
import wallet.TxNotFoundException;
import wallet.WalletManager;

/**
 * Created by mati on 18/04/17.
 */

public class PivxModuleImp implements PivxModule {

    private static final Logger logger = LoggerFactory.getLogger(PivxModuleImp.class);

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
    public void restoreWallet(List<String> mnemonic, long timestamp) throws IOException, MnemonicException {
        walletManager.restoreWalletFrom(mnemonic,timestamp);
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
    public Collection<AddressLabel> getContacts(){
        return contactsStore.list();
    }

    @Override
    public AddressLabel getAddressLabel(String address) {
        return contactsStore.getContact(address);
    }

    @Override
    public void saveContact(AddressLabel addressLabel) throws ContactAlreadyExistException {
        if (contactsStore.getContact(addressLabel.getAddresses().get(0))!=null) throw new ContactAlreadyExistException();
        contactsStore.insert(addressLabel);
    }
    @Override
    public void saveContactIfNotExist(AddressLabel addressLabel){
        if (contactsStore.getContact(addressLabel.getAddresses().get(0))!=null) return;
        contactsStore.insert(addressLabel);
    }

    @Override
    public void deleteAddressLabel(AddressLabel data) {
        contactsStore.delete(data);
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
        return buildSendTx(addressBase58,amount,null,memo);
    }
    @Override
    public Transaction buildSendTx(String addressBase58, Coin amount,Coin feePerKb, String memo) throws InsufficientMoneyException{
        Address address = Address.fromBase58(walletConfiguration.getNetworkParams(), addressBase58);

        SendRequest sendRequest = SendRequest.to(address,amount);
        sendRequest.memo = memo;
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        if (feePerKb!=null)
            sendRequest.feePerKb = feePerKb;
        //sendRequest.changeAddress -> add the change address with address that i know instead of give this job to the wallet.
        walletManager.completeSend(sendRequest);

        return sendRequest.tx;
    }

    @Override
    public Transaction completeTx(Transaction transaction,Coin feePerKb) throws InsufficientMoneyException {
        SendRequest sendRequest = SendRequest.forTx(transaction);
        if (transaction.getInputs()!=null && !transaction.getInputs().isEmpty()){
            List<TransactionOutput> unspent = new ArrayList<>();
            for (TransactionInput input : transaction.getInputs()) {
                unspent.add(input.getConnectedOutput());
            }
            sendRequest.coinSelector = new pivx.org.pivxwallet.module.wallet.DefaultCoinSelector(unspent);
        }
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        if (feePerKb!=null)
            sendRequest.feePerKb = feePerKb;
        //sendRequest.changeAddress -> add the change address with address that i know instead of give this job to the wallet.
        walletManager.completeSend(sendRequest);

        return sendRequest.tx;
    }

    @Override
    public Transaction completeTxWithCustomFee(Transaction transaction,Coin fee) throws InsufficientMoneyException{
        SendRequest sendRequest = SendRequest.forTx(transaction);
        sendRequest.signInputs = true;
        sendRequest.shuffleOutputs = false; // don't shuffle outputs to know the contact
        sendRequest.feePerKb = fee;
        sendRequest.changeAddress = walletManager.newFreshReceiveAddress();
        walletManager.completeSend(sendRequest);
        // if the fee is different to the custom fee and the tx size is lower than 1kb (1000 bytes in pivx core)
        /*if(!sendRequest.tx.getFee().equals(fee) && sendRequest.tx.unsafeBitcoinSerialize().length<1000){
            // re acomodate outputs to include the selected fee
            List<TransactionOutput> oldOutputs = sendRequest.tx.getOutputs();
            sendRequest.tx.clearOutputs();
            for (TransactionOutput oldOutput : oldOutputs) {
                if (oldOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams()).equals(sendRequest.changeAddress)){
                    //nothing
                }else {
                    sendRequest.tx.addOutput(oldOutput);
                }
            }


        }*/

        return sendRequest.tx;
    }

    @Override
    public Coin getUnspentValue(Sha256Hash parentTransactionHash, int index) {
        return walletManager.getUnspentValue(parentTransactionHash,index);
    }

    @Override
    public boolean isAnyPeerConnected() {
        return (blockchainManager != null && blockchainManager.getConnectedPeers() != null) && !blockchainManager.getConnectedPeers().isEmpty();
    }

    @Override
    public long getConnectedPeerHeight() {
        if (blockchainManager!=null && blockchainManager.getConnectedPeers() !=null && !blockchainManager.getConnectedPeers().isEmpty()){
            return blockchainManager.getConnectedPeers().get(0).getBestHeight();
        }else
            return -1;
    }

    @Override
    public int getProtocolVersion() {
        return blockchainManager.getProtocolVersion();
    }

    @Override
    public void checkMnemonic(List<String> mnemonic) throws MnemonicException {
        walletManager.checkMnemonic(mnemonic);
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
            boolean isStaking = false;
            Map<Integer,AddressLabel> outputsLabeled = new HashMap<>();
            Map<Integer,AddressLabel> inputsLabeled = new HashMap<>();
            Address address = null;
            if (isMine){
                try {
                    for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                        Script script = transactionOutput.getScriptPubKey();
                        if (script.isSentToAddress() || script.isPayToScriptHash()) {
                            try {
                                address = script.getToAddress(getConf().getNetworkParams(),true);
                                // if the tx is mine i know that the first output address is the sent and the second one is the change address
                                outputsLabeled.put(transactionOutput.getIndex(), contactsStore.getContact(address.toBase58()));
                            }catch (ScriptException e){
                                logger.warn("unknown tx output, "+script.toString()+", is tx coinbase: "+transaction.isCoinBase());
                                e.printStackTrace();
                            }
                        }else if (script.isSentToRawPubKey()){
                            // is the staking reward
                            address = script.getToAddress(getConf().getNetworkParams(),true);
                            // if the tx is mine i know that the first output address is the sent and the second one is the change address
                            outputsLabeled.put(transactionOutput.getIndex(), contactsStore.getContact(address.toBase58()));
                            isStaking = true;
                        }else {
                            logger.warn("unknown tx output, "+script.toString()+", is tx coinbase: "+transaction.isCoinBase());
                        }
                    }

                    /*for (TransactionInput transactionInput : transaction.getInputs()) {
                        try {
                            address = transactionInput.getScriptSig().getToAddress(getConf().getNetworkParams());
                            // if the tx is mine i know that the first output address is the sent and the second one is the change address
                            inputsLabeled.put((int) transactionInput.getOutpoint().getIndex(), contactsStore.getAddressLabel(address.toBase58()));
                        }catch (ScriptException e){
                            e.printStackTrace();
                        }
                    }*/

                }catch (Exception e){
                    e.printStackTrace();
                    //swallow this for now..
                }
            }else {
                /*for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                    Address addressToCheck = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams());
                    if(walletManager.isAddressMine(addressToCheck)){
                        address = addressToCheck;
                        break;
                    }
                }*/

                for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                    address = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams(),true);
                    // if the tx is mine i know that the first output address is the sent and the second one is the change address
                    outputsLabeled.put(transactionOutput.getIndex(),contactsStore.getContact(address.toBase58()));
                }
            }
            TransactionWrapper wrapper;
            if (!isStaking){
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        isMine ? getValueSentFromMe(transaction,true):walletManager.getValueSentToMe(transaction),
                        isMine ? TransactionWrapper.TransactionUse.SENT_SINGLE: TransactionWrapper.TransactionUse.RECEIVE
                        );
            }else {
                wrapper = new TransactionWrapper(
                        transaction,
                        inputsLabeled,
                        outputsLabeled,
                        walletManager.getValueSentToMe(transaction),
                        TransactionWrapper.TransactionUse.STAKE
                );
            }
            list.add(wrapper);
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
            Address address = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams(),true);
            AddressLabel addressLabel = contactsStore.getContact(address.toBase58());
            inputWrappers.add(
                    new InputWrapper(
                            transactionOutput,
                            addressLabel
                    )

            );
        }
        return inputWrappers;
    }

    @Override
    public Set<InputWrapper> convertFrom(List<TransactionInput> list) throws TxNotFoundException {
        Set<InputWrapper> ret = new HashSet<>();
        for (TransactionInput input : list) {
            TransactionOutput transactionOutput = input.getConnectedOutput();
            if (transactionOutput==null){
                transactionOutput = getUnspent(input.getOutpoint().getHash(), (int) input.getOutpoint().getIndex());
            }
            Address address = transactionOutput.getScriptPubKey().getToAddress(getConf().getNetworkParams(),true);
            AddressLabel addressLabel = contactsStore.getContact(address.toBase58());
            ret.add(
                    new InputWrapper(
                            transactionOutput,
                            addressLabel
                            )
            );
        }
        return ret;
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
    public String getWatchingPubKey() {
        return walletManager.getExtPubKey();
    }

    @Override
    public DeterministicKey getWatchingKey() {
        return walletManager.getWatchingPubKey();
    }

    @Override
    public DeterministicKey getKeyPairForAddress(Address address) {
        return walletManager.getKeyPairForAddress(address);
    }

    @Override
    public TransactionOutput getUnspent(Sha256Hash parentTxHash, int index) throws TxNotFoundException {
        return walletManager.getUnspent(parentTxHash,index);
    }

    @Override
    public List<TransactionOutput> getRandomUnspentNotInListToFullCoins(List<TransactionInput> inputs, Coin amount) throws InsufficientInputsException {
        return walletManager.getRandomListUnspentNotInListToFullCoins(inputs,amount);
    }

   @Override
   public boolean isSyncWithNode() throws NoPeerConnectedException {
       boolean isSync = false;
       if (isAnyPeerConnected()) {
           long peerHeight = getConnectedPeerHeight();
           if (peerHeight!=-1){
               if (getChainHeight()+10>peerHeight) {
                   isSync = true;
               }
           }
       }else {
          throw new NoPeerConnectedException();
       }
       return isSync;
   }

    @Override
    public void watchOnlyMode(String xpub) throws IOException {
        walletManager.watchOnlyMode(xpub);
    }


    public void saveRate(PivxRate pivxRate){
        rateDb.insertOrUpdateIfExist(pivxRate);
    }
}
