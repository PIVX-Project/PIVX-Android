package pivx.org.pivxwallet.ui.settings_network_activity;

/**
 * Created by Neoperol on 6/8/17.
 */

public class NetworkData {

    public String address;
    public String network_ip;
    public String protocol;
    public String blocks;
    public String speed;

    public NetworkData(String address, String network_ip, String protocol, String blocks, String speed ) {
        this.address = address;
        this.network_ip = network_ip;
        this.protocol = protocol;
        this.blocks = blocks;
        this.speed = speed;


    }
}
