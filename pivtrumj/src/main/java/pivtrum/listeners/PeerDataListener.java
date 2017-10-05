package pivtrum.listeners;

import java.util.List;

import pivtrum.PivtrumPeer;
import pivtrum.messages.responses.StatusHistory;
import pivtrum.messages.responses.Unspent;
import pivtrum.utility.TxHashHeightWrapper;

/**
 * Created by furszy on 6/17/17.
 */

public interface PeerDataListener {

    void onSubscribedAddressChange(PivtrumPeer pivtrumPeer, String address, String status);

    void onListUnpent(PivtrumPeer pivtrumPeer,String address, List<Unspent> unspent);

    void onBalanceReceive(PivtrumPeer pivtrumPeer, String address, long confirmed, long unconfirmed);

    void onGetHistory(PivtrumPeer pivtrumPeer, StatusHistory statusHistory);
}
