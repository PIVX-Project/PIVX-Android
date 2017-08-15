package pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutput;

import java.io.Serializable;

import pivx.org.pivxwallet.contacts.Contact;
import pivx.org.pivxwallet.module.PivxContext;

/**
 * Created by furszy on 8/4/17.
 */

public class InputWrapper implements Serializable{

    private transient TransactionOutput unspent;
    private Sha256Hash parentTxHash;
    private int index;
    private Contact contact;

    public InputWrapper(TransactionOutput unspent, Contact contact) {
        this.unspent = unspent;
        this.contact = contact;
        if (unspent!=null) {
            parentTxHash = unspent.getParentTransactionHash();
            index = unspent.getIndex();
        }
    }

    public TransactionOutput getUnspent() {
        return unspent;
    }

    public void setUnspent(TransactionOutput unspent) {
        this.unspent = unspent;
    }

    public Contact getContact() {
        return contact;
    }

    public Sha256Hash getParentTxHash() {
        return parentTxHash;
    }

    public int getIndex() {
        return index;
    }


    public String getLabel() {
        return contact!=null?contact.toLabel():unspent.getScriptPubKey().getToAddress(PivxContext.NETWORK_PARAMETERS,true).toBase58();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InputWrapper that = (InputWrapper) o;

        if (index != that.index) return false;
        return parentTxHash.equals(that.parentTxHash);

    }

    @Override
    public int hashCode() {
        int result = parentTxHash.hashCode();
        result = 31 * result + index;
        return result;
    }
}
