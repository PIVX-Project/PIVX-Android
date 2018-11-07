package pivx.org.pivxwallet.module.store;

import com.zerocoinj.core.CoinDenomination;

import java.math.BigInteger;

public class StoredAccumulator {

    private int height;
    private CoinDenomination denom;
    private BigInteger value;

    public StoredAccumulator(int height, CoinDenomination denom, BigInteger value) {
        this.height = height;
        this.denom = denom;
        this.value = value;
    }

    public int getHeight() {
        return height;
    }

    public CoinDenomination getDenom() {
        return denom;
    }

    public BigInteger getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "StoredAccumulator{" +
                "height=" + height +
                ", denom=" + denom +
                ", value=" + value.toString(16) +
                '}';
    }
}
