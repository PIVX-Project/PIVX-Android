package pivx.org.pivxwallet.ui.transaction_send_activity.custom.inputs;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.TransactionInput;

/**
 * Created by furszy on 8/4/17.
 */

public class InputWrapper {

    private TransactionInput input;
    private String addressLabel;

    public InputWrapper(TransactionInput input, String addressLabel) {
        this.input = input;
        this.addressLabel = addressLabel;
    }

    public TransactionInput getInput() {
        return input;
    }

    public void setInput(TransactionInput input) {
        this.input = input;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public void setAddressLabel(String addressLabel) {
        this.addressLabel = addressLabel;
    }
}
