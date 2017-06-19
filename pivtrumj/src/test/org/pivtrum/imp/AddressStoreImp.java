package org.pivtrum.imp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import store.AddressBalance;
import store.AddressNotFoundException;
import store.AddressStore;
import store.CantInsertAddressException;

/**
 * Created by furszy on 6/16/17.
 */

public class AddressStoreImp implements AddressStore {

    ConcurrentMap<String,AddressBalance> addresses = new ConcurrentHashMap();

    @Override
    public void insert(String address, AddressBalance addressBalance) throws CantInsertAddressException {
        addresses.put(address,addressBalance);
    }

    @Override
    public AddressBalance getAddressStatus(String address) throws AddressNotFoundException {
        return addresses.get(address);
    }
}
