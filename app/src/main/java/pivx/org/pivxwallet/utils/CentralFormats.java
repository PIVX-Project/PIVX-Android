package pivx.org.pivxwallet.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by furszy on 7/8/17.
 */

public class CentralFormats {

    private NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public CentralFormats(AppConf appConf) {
        // number format
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public String format(BigDecimal bigDecimal){
        return numberFormat.format(bigDecimal).substring(1);
    }
}
