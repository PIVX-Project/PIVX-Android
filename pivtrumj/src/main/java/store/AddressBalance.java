package store;

import java.io.Serializable;

/**
 * Created by furszy on 6/18/17.
 */

public class AddressBalance implements Serializable{

    private String status;
    private long confirmedBalance;
    private long unconfirmedBalance;
    /** Amount of peers whom confirme this status and balance */
    private int amountOfStatusConfirmations = 0;
    private int amountOfBalanceConfirmations = 0;

    public AddressBalance(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getConfirmedBalance() {
        return confirmedBalance;
    }

    public void setConfirmedBalance(long confirmedBalance) {
        this.confirmedBalance = confirmedBalance;
    }

    public long getUnconfirmedBalance() {
        return unconfirmedBalance;
    }

    public void setUnconfirmedBalance(long unconfirmedBalance) {
        this.unconfirmedBalance = unconfirmedBalance;
    }

    public int getAmountOfStatusConfirmations() {
        return amountOfStatusConfirmations;
    }

    public int getAmountOfBalanceConfirmations() {
        return amountOfBalanceConfirmations;
    }

    public void addStatusConfirmation(){
        amountOfStatusConfirmations++;
    }
    public void addBalanceConfirmation(){
        amountOfBalanceConfirmations++;
    }
}
