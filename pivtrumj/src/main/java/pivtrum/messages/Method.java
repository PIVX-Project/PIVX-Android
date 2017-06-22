package pivtrum.messages;

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

    ADDRESS_SUBSCRIBE("blockchain.address.subscribe"),

    /**
     *
     * Return an ordered list of UTXOs sent to a pivx address.
     *
     * blockchain.address.listunspent(**address**)
     *
     * **address**
     *
     * The address as a Base58 string.
     *
     * **Response**
     *
     * A list of unspent outputs in blockchain order.  Each transaction
     * is a dictionary with keys *height* , *tx_pos*, *tx_height* and
     * *value* keys.  *height* is the integer height of the block the
     * transaction was confirmed in, *tx_hash* the transaction hash in
     * hexadecimal, *tx_pos* the zero-based index of the output in the
     * transaction's list of outputs, and *value* its integer value in
     * minimum coin units (satoshis in the case of Pivx).
     *
     */

    LIST_UNSPENT("blockchain.address.listunspent"),

    /**
     *
     * Return the *deserialized header* [2]_ of the block at the given height.
     *
     * blockchain.block.get_header(**height**)
     *
     * **height**
     *
     * The height of the block, an integer.
     *
     * **Response**
     *
     * .. [2] The *deserialized header* of a block is a dictionary like
     * so::
     *
     * {
     * "block_height": <integer>,
     * 'version': <integer>,
     * 'prev_block_hash': <hexadecimal string>,
     * 'merkle_root':  <hexadecimal string>,
     * 'timestamp': <integer>,
     * 'bits': <integer>,
     * 'nonce': <integer>
     * }
     *
     */

    GET_HEADER("blockchain.block.get_header"),

    /**
     * Subscribe to receive the block height when a new block is found.  This
     * subscription is deprecated in favour of *blockchain.headers.subscribe*
     * which provides more detailed information.
     *
     * blockchain.numblocks.subscribe()
     *
     * **Response**
     *
     * The height of the current block, an integer
     *
     * **Notification Parameters**
     *
     * As this is a subcription, the client will receive a notification
     * when a new block is found.  The parameters are:
     *
     * [**height**]
     */
    HEIGHT_SUBSCRIBE("blockchain.numblocks.subscribe"),
    /**
     *
     * Return the confirmed and unconfirmed balances of a bitcoin address.
     *
     * blockchain.address.get_balance(**address**)
     *
     * **address**
     *
     * The address as a Base58 string.
     *
     * **Response**
     *
     * A dictionary with keys *confirmed* and *unconfirmed*.  The value of
     * each is the appropriate balance in coin units as a string.
     *
     * **Response Example**::
     *
     * {
     * "confirmed": "1.03873966",
     * "unconfirmed": "0.236844"
     * }
     *
     */
    GET_BALANCE("blockchain.address.get_balance"),

    /**
     * Return the confirmed and unconfirmed history of a bitcoin address.
     *
     * blockchain.address.get_history(**address**)
     *
     * **address**
     *
     * The address as a Base58 string.
     *
     * **Response**
     *
     * A list of confirmed transactions in blockchain order, with the
     * output of *blockchain.address.get_mempool* appended to the list.
     * Each transaction is a dictionary with keys *height* and *tx_hash*.
     * *height* is the integer height of the block the transaction was
     * confirmed in, and *tx_hash* the transaction hash in hexadecimal.
     */

    GET_ADDRESS_HISTORY("blockchain.address.get_history"),

    /**
     *
     * Return a raw transaction.
     *
     * blockchain.transaction.get(**tx_hash**, **height**)
     *
     * **tx_hash**
     *
     * The transaction hash as a hexadecimal string.
     *
     * **height**
     *
     * The height at which it was confirmed, an integer.  This parameter
     * is optional and ignored; it is recommended that clients do not
     * send it as it will be removed in a future protocol version.
     *
     * **Response**
     *
     * The raw transaction as a hexadecimal string.
     *
     */

    GET_TX("blockchain.transaction.get")

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
