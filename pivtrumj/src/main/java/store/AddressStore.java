package store;

/**
 * Created by furszy on 6/14/17.
 */

public interface AddressStore {

    /**
     * Insert a base58 address an his status (hash of the address history)
     *
     * @param address
     * @param status
     */
    public void insert(String address, String status) throws CantInsertAddressException;

    /**
     * Get the address status
     */
    public String getAddressStatus(String address) throws AddressNotFoundException;

}
