package pivx.org.pivxwallet.contacts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by furszy on 6/22/17.
 */

public class Contact implements Serializable{

    private int id;
    private String name;
    private List<String> addresses = new ArrayList<>();

    public Contact(int id,String name) {
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
}
