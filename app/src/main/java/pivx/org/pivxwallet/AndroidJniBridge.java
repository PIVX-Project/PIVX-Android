package pivx.org.pivxwallet;

import com.zerocoinj.utils.JniBridgeWrapper;

public class AndroidJniBridge implements JniBridgeWrapper{

    static {
        System.loadLibrary("native-lib");
    }

    public AndroidJniBridge() {
    }

    @Override
    public byte[] compute1024seed(byte[] bytes) {
        return compute1024seedNative(bytes);
    }

    @Override
    public String computeVSeedAndVExpanded() {
        return computeVSeedAndVExpandedNative();
    }

    public native byte[] compute1024seedNative(byte[] bytes);


    public native String computeVSeedAndVExpandedNative();
}
