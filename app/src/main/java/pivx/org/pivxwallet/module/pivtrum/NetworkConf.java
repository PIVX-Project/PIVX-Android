package pivx.org.pivxwallet.module.pivtrum;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by furszy on 6/12/17.
 *
 * Class in charge of have the default params and save data from the network like servers.
 */

public class NetworkConf {

    private static final String CLIENT_NAME = "pivx_mobile";
    private static final String MAX_PROTOCOL_VERSION = "2.9.5";
    private static final String MIN_PROTOCOL_VERSION = "1.0";
    /** Trusted server selected on the first screen of the app */
    private InetSocketAddress trustedServer;
    /** Known servers from the network */
    private List<InetSocketAddress> networkServers;

    public NetworkConf(InetSocketAddress trustedServer) {
        this.trustedServer = trustedServer;
        this.networkServers = new ArrayList<>();
    }

    public PivtrumPeerData getTrustedServer() {
        return new PivtrumPeerData(trustedServer.getHostName(),trustedServer.getPort(),0);
    }

    public void setTrustedServer(InetSocketAddress trustedServer) {
        this.trustedServer = trustedServer;
    }

    public void addAll(Collection<InetSocketAddress> networkServers){
        networkServers.addAll(networkServers);
    }

    public List<InetSocketAddress> getNetworkServers(){
        return networkServers;
    }

    public String getClientName() {
        return CLIENT_NAME;
    }

    public String getMaxProtocolVersion() {
        return MAX_PROTOCOL_VERSION;
    }

    public String getMinProtocolVersion() {
        return MIN_PROTOCOL_VERSION;
    }
}
