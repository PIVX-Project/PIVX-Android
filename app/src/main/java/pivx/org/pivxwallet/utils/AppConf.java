package pivx.org.pivxwallet.utils;

import android.content.SharedPreferences;

import pivtrum.PivtrumPeerData;

/**
 * Created by furszy on 6/8/17.
 */

public class AppConf extends Configurations {

    public static final String PREFERENCE_NAME = "app_conf";
    private static final String IS_APP_INIT = "is_app_init";
    private static final String PINCODE = "pincode";
    private static final String TRUSTED_NODE_HOST = "trusted_node_host";
    private static final String TRUSTED_NODE_TCP = "trusted_node_tcp";
    private static final String TRUSTED_NODE_SSL = "trusted_node_ssl";

    public AppConf(SharedPreferences prefs) {
        super(prefs);
    }

    public void setAppInit(boolean v){
        save(IS_APP_INIT,v);
    }

    public boolean isAppInit(){
        return getBoolean(IS_APP_INIT,false);
    }


    public void savePincode(String pincode) {
        save(PINCODE,pincode);
    }

    public String getPincode(){
        return getString(PINCODE,null);
    }

    public void saveTrustedNode(PivtrumPeerData pivtrumPeerData){
        save(TRUSTED_NODE_HOST,pivtrumPeerData.getHost());
        save(TRUSTED_NODE_TCP,pivtrumPeerData.getTcpPort());
        save(TRUSTED_NODE_SSL,pivtrumPeerData.getSslPort());
    }
    public PivtrumPeerData getTrustedNode(){
        String host = getString(TRUSTED_NODE_HOST,null);
        if (host!=null){
            int tcp = getInt(TRUSTED_NODE_TCP,-1);
            int ssl = getInt(TRUSTED_NODE_TCP,-1);
            return new PivtrumPeerData(host,tcp,ssl);
        }else
            return null;
    }

}
