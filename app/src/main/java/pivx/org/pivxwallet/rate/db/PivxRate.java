package pivx.org.pivxwallet.rate.db;

import java.math.BigDecimal;

/**
 * Created by furszy on 7/5/17.
 */

public class PivxRate {

    private long id;
    /** Coin letters (USD,EUR,etc..) */
    private String coin;
    /** Value of 1 piv in this rate */
    private BigDecimal value;
    /** Last update time */
    private long timestamp;
    /** Link to get currency price */
    private String link;

    public PivxRate(String coin, BigDecimal value, long timestamp, String link) {
        this.coin = coin;
        this.value = value;
        this.timestamp = timestamp;
        this.link = link;
    }

    public String getCoin() {
        return coin;
    }

    public BigDecimal getValue() {
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getLink() {
        return link;
    }
}
