package global;

import java.math.BigDecimal;

/**
 * Created by furszy on 7/5/17.
 */

public class PivxRate {

    /** Coin letters (USD,EUR,etc..) */
    private final String code;
    /** Value of 1 piv in this rate */
    private final BigDecimal rate;
    /** Last update time */
    private final long timestamp;

    public PivxRate(String code, BigDecimal rate, long timestamp) {
        this.code = code;
        this.rate = rate;
        this.timestamp = timestamp;

    }

    public String getCode() {
        return code;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Old method..
     */
    public String getLink(){
        return null;
    }

}
