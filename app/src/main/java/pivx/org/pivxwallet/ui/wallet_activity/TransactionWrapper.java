package pivx.org.pivxwallet.ui.wallet_activity;

import com.google.protobuf.InvalidProtocolBufferException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.BitcoinSerializer;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Protos;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import pivx.org.pivxwallet.contacts.Contact;

/**
 * Created by furszy on 6/29/17.
 */
public class TransactionWrapper implements Serializable{

    public static enum TransactionUse{
        SENT_SINGLE,
        RECEIVE,
        STAKE
        ;

    }

    private transient Transaction transaction;
    private Sha256Hash txId;
    /** Map of Address labels ordered by output position */
    private Map<Integer,Contact> outputLabels;
    private Map<Integer,Contact> inputsLabels;
    private Coin amount;
    private TransactionUse transactionUse;


    public TransactionWrapper(Transaction transaction,Map<Integer,Contact> inputsLabels, Map<Integer,Contact> outputLabels, Coin amount, TransactionUse transactionUse) {
        this.transaction = transaction;
        this.txId = transaction.getHash();
        this.inputsLabels = inputsLabels;
        this.outputLabels = outputLabels;
        this.amount = amount;
        this.transactionUse = transactionUse;
    }


    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Sha256Hash getTxId() {
        return txId;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Coin getAmount() {
        return amount;
    }

    public TransactionUse getTransactionUse() {
        return transactionUse;
    }

    public Map<Integer, Contact> getInputsLabels() {
        return inputsLabels;
    }

    public Map<Integer, Contact> getOutputLabels() {
        return outputLabels;
    }

    public boolean isSent() {
        return transactionUse == TransactionUse.SENT_SINGLE;
    }

    public boolean isStake() {
        return transactionUse == TransactionUse.STAKE;
    }

    @Override
    public String toString() {
        return "TransactionWrapper{" +
                "transaction=" + transaction +
                ", txId=" + txId +
                ", outputLabels=" + outputLabels +
                ", inputsLabels=" + inputsLabels +
                ", amount=" + amount +
                ", transactionUse=" + transactionUse +
                '}';
    }
}
