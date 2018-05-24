package pivx.org.pivxwallet.wallofcoins.buying_wizard.utils;

import android.content.SharedPreferences;

/**
 * Created by  on 13-Mar-18.
 */

public class BuyingWizardAddressPref {
    private final SharedPreferences prefs;
    private static final String BUY_PIV_ADDRESS = "addres";

    public BuyingWizardAddressPref(final SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public void setBuyPivAddress(String address) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(BUY_PIV_ADDRESS, address);
        editor.commit();
    }

    public String getBuyPivAddress() {
        return prefs.getString(BUY_PIV_ADDRESS, "");
    }

    public void clearBuyPivAddress() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
    }
}
