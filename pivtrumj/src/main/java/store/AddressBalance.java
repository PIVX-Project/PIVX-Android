package store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import pivtrum.utility.TxHashHeightWrapper;

/**
 * Created by furszy on 6/18/17.
 */

public class AddressBalance implements Serializable{

    private String status;
    private long confirmedBalance = 0;
    private long unconfirmedBalance = 0;
    /** List of tx in which this address was used */
    private List<TxHashHeightWrapper> txList;
    /** Amount of peers whom confirme this status and balance */
    private int amountOfStatusConfirmations = 0;
    private int amountOfBalanceConfirmations = 0;

    public AddressBalance() {
    }

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

    public void addTx(TxHashHeightWrapper tx){
        if (txList==null) txList = new ArrayList<>();
        txList.add(tx);
    }

    public void addAllTx(Collection<TxHashHeightWrapper> txs){
        if (txList==null) txList = new ArrayList<>();
        txList.addAll(txs);
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

    public List<TxHashHeightWrapper> getTxList() {
        return txList;
    }

    @Override
    public String toString() {
        return "AddressBalance{" +
                "status='" + status + '\'' +
                ", confirmedBalance=" + confirmedBalance +
                ", unconfirmedBalance=" + unconfirmedBalance +
                ", txList=" + txList +
                ", amountOfStatusConfirmations=" + amountOfStatusConfirmations +
                ", amountOfBalanceConfirmations=" + amountOfBalanceConfirmations +
                '}';
    }
}
