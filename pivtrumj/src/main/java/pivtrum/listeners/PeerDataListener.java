package pivtrum.listeners;

import java.util.List;

import pivtrum.PivtrumPeer;
import pivtrum.messages.responses.Unspent;

/**
 * Created by furszy on 6/17/17.
 */

public interface PeerDataListener {

    void onSubscribedAddress(PivtrumPeer pivtrumPeer, String address, String status);

    void onListUnpent(PivtrumPeer pivtrumPeer,String address, List<Unspent> unspent);

    void onGetBalance(PivtrumPeer pivtrumPeer,String address,long confirmed,long unconfirmed);
    // todo: esto está mal, devuelve una lista de txHash:height y no solo una.
    // {"result":[{"tx_hash":"d2b6046de1febf450f416eef820ecdfee30112d7522bc9470fb0ae44fc704e02","height":131213},{"tx_hash":"a79c6eefb61e544303e7e4c6d12150018d253ed92a7538ceddd38add228942cd","height":132939}],"id":3,"jsonrpc":"2.0"},
    // todo: deberia pasar la lista en vez de esto..
    void onGetHistory(PivtrumPeer pivtrumPeer, String address, String status);
}
