package global;

import org.pivxj.core.Sha256Hash;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by furszy on 6/22/17.
 */

public class AddressLabel implements Serializable{

    private int id;
    private String name;
    private List<String> addresses = new ArrayList<>();
    /** Set of tx sent to this contact */
    private Set<String> txHashes;

    public AddressLabel(int id, String name) {
        this.name = name;
    }

    public AddressLabel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    public void addAddress(String address){
        addresses.add(address);
    }

    public void addTx(Sha256Hash txHash){
        if (txHashes==null){
            txHashes = new HashSet<>();
        }
        txHashes.add(txHash.toString());
    }

    public Set<String> getTxHashes() {
        return txHashes;
    }

    @Override
    public String toString() {
        return addresses.get(0);
    }

    public void addAllTx(Set<String> txHashes) {
        if (txHashes==null){
            txHashes = new HashSet<>();
        }
        txHashes.addAll(txHashes);
    }

    public String toLabel() {
        return (name!=null)?name:addresses.get(0);
    }

    public int getId() {
        return id;
    }
}
