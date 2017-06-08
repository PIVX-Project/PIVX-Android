package pivx.org.pivxwallet.module;

import org.bitcoinj.core.Context;
import org.bitcoinj.core.NetworkParameters;

import static pivx.org.pivxwallet.module.PivxContext.CONTEXT;
import static pivx.org.pivxwallet.module.PivxContext.Files.WALLET_FILENAME_PROTOBUF;
import static pivx.org.pivxwallet.module.PivxContext.Files.WALLET_KEY_BACKUP_PROTOBUF;

/**
 * Created by furszy on 6/4/17.
 */

public class WalletConfImp implements WalletConfiguration {


    @Override
    public String getMnemonicFilename() {
        return null;//PivxContext.Files.;
    }

    @Override
    public String getWalletProtobufFilename() {
        return WALLET_FILENAME_PROTOBUF;
    }

    @Override
    public NetworkParameters getNetworkParams() {
        return PivxContext.NETWORK_PARAMETERS;
    }

    @Override
    public String getKeyBackupProtobuf() {
        return WALLET_KEY_BACKUP_PROTOBUF;
    }

    @Override
    public long getWalletAutosaveDelayMs() {
        return PivxContext.Files.WALLET_AUTOSAVE_DELAY_MS;
    }

    @Override
    public Context getWalletContext() {
        return CONTEXT;
    }
}
