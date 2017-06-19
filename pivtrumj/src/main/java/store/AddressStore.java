package store;

import java.util.List;

/**
 * Created by furszy on 6/14/17.
 */

public interface AddressStore {

    /**
     * Insert a base58 address an his status (hash of the address history)
     *
     * @param address
     * @param addressBalance
     */
    void insert(String address, AddressBalance addressBalance) throws CantInsertAddressException;

    /**
     * Get the address status
     */
    AddressBalance getAddressStatus(String address) throws AddressNotFoundException;

    /**
     *
     * @return
     */
    List<AddressBalance> listBalance();
}
