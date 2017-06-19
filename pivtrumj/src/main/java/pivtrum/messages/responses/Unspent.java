package pivtrum.messages.responses;

/**
 * Created by furszy on 6/18/17.
 */

public class Unspent {

    private int txPos;
    private String txHash;
    private long value;
    private long blockHeight;

    public Unspent(int txPos, String txHash, long value, long blockHeight) {
        this.txPos = txPos;
        this.txHash = txHash;
        this.value = value;
        this.blockHeight = blockHeight;
    }

    public int getTxPos() {
        return txPos;
    }

    public String getTxHash() {
        return txHash;
    }

    public long getValue() {
        return value;
    }

    public long getBlockHeight() {
        return blockHeight;
    }
}
