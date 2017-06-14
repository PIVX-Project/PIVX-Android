package pivx.org.pivxwallet.module.pivtrum;

/**
 * Created by furszy on 6/13/17.
 */

public class PivtrumPeerData {

    private String host;
    private int tcpPort;
    private int sslPort;
    private long prunningLimit;

    public PivtrumPeerData(String host, int tcpPort, int sslPort) {
        this.host = host;
        this.tcpPort = tcpPort;
        this.sslPort = sslPort;
    }

    public String getHost() {
        return host;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public int getSslPort() {
        return sslPort;
    }
}
