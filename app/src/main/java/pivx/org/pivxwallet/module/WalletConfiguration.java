package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;

/**
 * Created by furszy on 6/4/17.
 */

public interface WalletConfiguration {

    String getMnemonicFilename();

    String getWalletProtobufFilename();

    NetworkParameters getNetworkParams();

    String getKeyBackupProtobuf();

    long getWalletAutosaveDelayMs();

    Context getWalletContext();
}
