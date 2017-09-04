package pivtrum.messages.responses;

/**
 * Created by furszy on 6/18/17.
 *
 * * "block_height": <integer>,
 * 'version': <integer>,
 * 'prev_block_hash': <hexadecimal string>,
 * 'merkle_root':  <hexadecimal string>,
 * 'timestamp': <integer>,
 * 'bits': <integer>,
 * 'nonce': <integer>
 */

public class Header {

    private long blockHeight;
    private int version;
    private String prevBlockHash;
    private String merkleRoot;
    private int timestamp;
    private int bits;
    private int nonce;

    public Header(long blockHeight, int version, String prevBlockHash, String merkleRoot, int timestamp, int bits, int nonce) {
        this.blockHeight = blockHeight;
        this.version = version;
        this.prevBlockHash = prevBlockHash;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.bits = bits;
        this.nonce = nonce;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public int getVersion() {
        return version;
    }

    public String getPrevBlockHash() {
        return prevBlockHash;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public int getBits() {
        return bits;
    }

    public int getNonce() {
        return nonce;
    }


    @Override
    public String toString() {
        return "Header{" +
                "blockHeight=" + blockHeight +
                ", version=" + version +
                ", prevBlockHash='" + prevBlockHash + '\'' +
                ", merkleRoot='" + merkleRoot + '\'' +
                ", timestamp=" + timestamp +
                ", bits=" + bits +
                ", nonce=" + nonce +
                '}';
    }
}
