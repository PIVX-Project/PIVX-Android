package pivx.org.pivxwallet;

import java.text.NumberFormat;

import pivx.org.pivxwallet.utils.AppConf;

/**
 * Created by furszy on 7/8/17.
 */

public class CentralFormats {

    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance();

    public CentralFormats(AppConf appConf) {
        // number format
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }
}
