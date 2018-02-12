package pivx.org.pivxwallet.ui.transaction_send_activity.custom.outputs;

import org.pivxj.core.Coin;

import java.io.Serializable;

/**
 * Created by furszy on 8/4/17.
 */

public class OutputWrapper implements Serializable{

    private int id;
    private String address;
    private Coin amount;
    private String addressLabel;

    public OutputWrapper(int id,String address, Coin amount, String addressLabel) {
        this.id = id;
        this.address = address;
        this.amount = amount;
        this.addressLabel = addressLabel;
    }


    public String getAddress() {
        return address;
    }

    public Coin getAmount() {
        return amount;
    }

    public String getAddressLabel() {
        return addressLabel;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAmount(Coin amount) {
        this.amount = amount;
    }

    public void setAddressLabel(String addressLabel) {
        this.addressLabel = addressLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OutputWrapper that = (OutputWrapper) o;

        return address != null ? address.equals(that.address) : that.address == null;

    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
