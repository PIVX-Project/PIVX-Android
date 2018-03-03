package global.store;

import global.AddressLabel;

/**
 * Created by furszy on 3/3/18.
 */

public interface ContactsStoreDao<T> extends AbstractDbDao<T> {

    AddressLabel getContact(String address);

    void delete(AddressLabel data);

}
