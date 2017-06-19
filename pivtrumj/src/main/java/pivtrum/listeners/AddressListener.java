package pivtrum.listeners;

/**
 * Created by furszy on 6/18/17.
 */

public interface AddressListener {

    void onCoinReceived(String address, long confirmed, long unconfirmed);

}
