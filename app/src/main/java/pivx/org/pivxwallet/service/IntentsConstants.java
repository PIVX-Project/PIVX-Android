package pivx.org.pivxwallet.service;

/**
 * Created by furszy on 6/19/17.
 */

public class IntentsConstants {

    /** Action to notify the address change */
    public static final String ACTION_ADDRESS_BALANCE_CHANGE = "act_add_bal_chan";
    /** Action to notify the trusted peer connection have been disconnected  */
    public static final String ACTION_TRUSTED_PEER_CONNECTION_FAIL = "act_trs_peer_conn_fail";
    /** block store error */
    public static final String ACTION_STORED_BLOCKCHAIN_ERROR = "act_str_blc_err";

    // Notifications numbers
    public static final int NOT_COINS_RECEIVED = 1;
    public static final int NOT_BLOCKCHAIN_ALERT = 15;

    //service
    public static final String ACTION_SCHEDULE_SERVICE = "schedule_service";
    public static final String ACTION_CANCEL_COINS_RECEIVED = "cancel_coins";
    public static final String ACTION_RESET_BLOCKCHAIN = "reset_blockchain";
    public static final String ACTION_BROADCAST_TRANSACTION = "broad_tx";

    public static final String ACTION_NOTIFICATION = "service_not";
    public static final String INTENT_BROADCAST_DATA_TYPE = "service_not_type";
    public static final String INTENT_BROADCAST_DATA_BLOCKCHAIN_STATE = "blockchain_state";
    public static final String INTENT_BROADCAST_DATA_PEER_CONNECTED = "peer_connected";

    // Data
    public static final String DATA_TRANSACTION_HASH = "tx_hash";
    public static final String INTENT_EXTRA_BLOCKCHAIN_STATE = "blockchain_state_data";

    // Notifications types
    public static final String INTENT_BROADCAST_DATA_ON_COIN_RECEIVED = "on_coin_received";

}
