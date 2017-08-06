package pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs;

import org.bitcoinj.core.TransactionOutput;

import pivx.org.pivxwallet.contacts.Contact;

/**
 * Created by furszy on 8/4/17.
 */

public class InputWrapper {

    private TransactionOutput unspent;
    private Contact contact;

    public InputWrapper(TransactionOutput unspent, Contact contact) {
        this.unspent = unspent;
        this.contact = contact;
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
}
