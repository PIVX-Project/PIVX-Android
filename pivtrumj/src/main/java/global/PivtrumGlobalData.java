package global;

import java.util.ArrayList;
import java.util.List;

import pivtrum.PivtrumPeerData;

/**
 * Created by furszy on 7/2/17.
 */

public class PivtrumGlobalData {

    public static final String FURSZY_TESTNET_SERVER = "185.101.98.175";

    public static final List<PivtrumPeerData> listTrustedHosts(){
        List<PivtrumPeerData> list = new ArrayList<>();
        list.add(new PivtrumPeerData("192.168.0.10",55551,55552));
        list.add(new PivtrumPeerData(FURSZY_TESTNET_SERVER,55551,55552));
        return list;
    }

}
