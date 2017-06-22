package pivtrum.utility;

import java.io.Serializable;

public class TxHashHeightWrapper implements Serializable {
    private String txHash;
    private long height;
    public TxHashHeightWrapper(String txHash, long height) {
        this.txHash = txHash;
        this.height = height;
    }
    public String getTxHash() {
        return txHash;
    }
    public long getHeight() {
        return height;
    }
}