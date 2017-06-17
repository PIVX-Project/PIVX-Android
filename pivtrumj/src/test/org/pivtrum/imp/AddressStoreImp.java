package org.pivtrum.imp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import store.AddressNotFoundException;
import store.AddressStore;
import store.CantInsertAddressException;

/**
 * Created by furszy on 6/16/17.
 */

public class AddressStoreImp implements AddressStore {

    ConcurrentMap<String,String> addresses = new ConcurrentHashMap();

    @Override
    public void insert(String address, String status) throws CantInsertAddressException {
        addresses.put(address,status);
    }

    @Override
    public String getAddressStatus(String address) throws AddressNotFoundException {
        return addresses.get(address);
    }
}
