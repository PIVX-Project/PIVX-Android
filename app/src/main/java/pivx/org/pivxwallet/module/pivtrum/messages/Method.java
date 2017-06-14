package pivx.org.pivxwallet.module.pivtrum.messages;

/**
 * Created by furszy on 6/12/17.
 */

public enum Method {

    VERSION("server.version"),

    /**
     * Return a list of peer servers.  Despite the name this is not a
     * subscription and the server must send no notifications.
     *
     * server.peers.subscribe()
     *
     *    **Response**
     *
     * An array of peer servers.  Each entry is a triple like
     *
     * ["107.150.45.210", "e.anonyhost.org", ["v1.0", "p10000", "t", "s995"]]
     *
     * The first element is the IP address, the second is the host name
     * (which might also be an IP address), and the third is a list of
     * server features.  Each feature and starts with a letter.  'v'
     * indicates the server maximum protocol version, 'p' its pruning limit
     * and is omitted if it does not prune, 't' is the TCP port number, and
     * 's' is the SSL port number.  If a port is not given for 's' or 't'
     * the default port for the coin network is implied.  If 's' or 't' is
     * missing then the server does not support that transport.
     *
     */
    GET_PEERS("server.peers.subscribe"),


    /**
     *
     * Subscribe to a bitcoin address.
     *
     * blockchain.address.subscribe(**address**)
     *
     * **address**
     *
     * The address as a Base58 string.
     *
     * **Response**
     *
     * The *status* [1]_ of the address.
     *
     * **Notifications**
     *
     * As this is a subcription, the client will receive a notification
     * when the status of the address changes.  The parameters are:
     *
     * [**address**, **status**]
     *
     * .. [1] To calculate the *status* of an address, order confirmed
     * transactions touching the address by height (and position in
     * the block if there are more than one in a block).  Form a
     * string that is the concatenation of strings 'tx_hash:height:'
     * for each transaction in order.  *tx_hash* is the transaction
     * hash in hexadecimal, *height* the height of the block it is in.
     * Next, with mempool transactions in any order, append a string
     * that is the same, but where *height* is `-1` if the transaction
     * has at least one unconfirmed input, and `0` if all inputs are
     * confirmed.  The *status* is the **sha256** hash of this string
     * expressed as a hexadecimal string.
     *
     */

    ADDRESS_SUBSCRIBE("blockchain.address.subscribe")
    ;

    String method;

    Method(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }

    /**
     * Lazy method by name
     * @param name
     * @return
     */
    public static Method getMethodByName(String name){
        for (Method method : Method.values()) {
            if (method.getMethod().equals(name)){
                return method;
            }
        }
        throw new IllegalArgumentException("No method for name: "+name);
    }
}
