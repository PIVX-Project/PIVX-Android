package wallet;

import com.google.protobuf.ByteString;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.LinuxSecureRandom;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.List;
import java.util.concurrent.TimeUnit;

import global.ContextWrapper;
import global.WalletConfiguration;

/**
 * Created by furszy on 6/4/17.
 */

public class WalletManager {

    private static final Logger logger = LoggerFactory.getLogger(WalletManager.class);

    private Wallet wallet;
    private File walletFile;

    private WalletConfiguration conf;
    private ContextWrapper contextWrapper;

    public WalletManager(ContextWrapper contextWrapper,WalletConfiguration conf) {
        this.conf = conf;
        this.contextWrapper = contextWrapper;
    }

    // methods

    public Address newFreshReceiveAddress() {
        return wallet.freshReceiveAddress();
    }

    /**
     * Get the last address active which not appear on a tx.
     * @return
     */
    public Address getCurrentAddress() {
        return wallet.currentReceiveAddress();
    }

    public List<Address> getIssuedReceiveAddresses(){
        return wallet.getIssuedReceiveAddresses();
    }

    /**
     * Method to know if an address is already used for receive coins.
     * @return
     */
    public boolean isMarkedAddress() {
        return false;
    }

    // init

    public void init(){
        // init mnemonic code first..
        // initMnemonicCode();
        restoreOrCreateWallet();
    }

    private void restoreOrCreateWallet() {
        walletFile = contextWrapper.getFileStreamPath(conf.getWalletProtobufFilename());
        loadWalletFromProtobuf(walletFile);
    }

    private void loadWalletFromProtobuf(File walletFile) {
        if (walletFile.exists()){
            FileInputStream walletStream = null;
            try {
                walletStream = new FileInputStream(walletFile);
                wallet = new WalletProtobufSerializer().readWallet(walletStream);

                if (!wallet.getParams().equals(conf.getNetworkParams()))
                    throw new UnreadableWalletException("bad wallet network parameters: " + wallet.getParams().getId());

            } catch (UnreadableWalletException e) {
                logger.error("problem loading wallet", e);
                wallet = restoreWalletFromBackup();
            } catch (FileNotFoundException e) {
                logger.error("problem loading wallet", e);
                //context.toast(e.getClass().getName());
                wallet = restoreWalletFromBackup();
            } finally {
                if (walletStream != null)
                    try {
                        walletStream.close();
                    } catch (IOException e) {
                        //nothing
                    }
            }
            if (!wallet.isConsistent()) {
                //contextWrapper.toast("inconsistent wallet: " + walletFile);
                logger.error("inconsistent wallet "+walletFile);
                wallet = restoreWalletFromBackup();
            }
            if (!wallet.getParams().equals(conf.getNetworkParams()))
                throw new Error("bad wallet network parameters: " + wallet.getParams().getId());

            afterLoadWallet();

        }else {
            if (Utils.isAndroidRuntime()){
                new LinuxSecureRandom();
            }
            SecureRandom secureRandom = new SecureRandom();
            byte[] seed = secureRandom.generateSeed(32);
            DeterministicSeed deterministicSeed = new DeterministicSeed(seed, ByteString.copyFrom(seed).toStringUtf8(),System.currentTimeMillis());
            KeyChainGroup keyChainGroup = new KeyChainGroup(conf.getNetworkParams(),deterministicSeed);
            wallet = new Wallet(conf.getNetworkParams(),keyChainGroup);

            saveWallet();
            backupWallet();

//            config.armBackupReminder();
            logger.info("new wallet created");
        }

        wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
            @Override
            public void onCoinsReceived(Wallet wallet, Transaction transaction, Coin coin, Coin coin1) {
                org.bitcoinj.core.Context.propagate(conf.getWalletContext());
                saveWallet();

            }
        });
    }

    private void afterLoadWallet() {
        wallet.autosaveToFile(walletFile, conf.getWalletAutosaveDelayMs(), TimeUnit.MILLISECONDS, new WalletAutosaveEventListener(conf));
        try {
            // clean up spam
            wallet.cleanup();
        }catch (Exception e){
            e.printStackTrace();
        }

        // make sure there is at least one recent backup
        if (!contextWrapper.getFileStreamPath(conf.getKeyBackupProtobuf()).exists())
            backupWallet();

        logger.info("Wallet loaded.");
    }

    /**
     * Restore wallet from backup
     * @return
     */
    private Wallet restoreWalletFromBackup(){

        InputStream is = null;
        try {
            is = contextWrapper.openFileInput(conf.getKeyBackupProtobuf());
            final Wallet wallet = new WalletProtobufSerializer().readWallet(is,true,null);
            if (!wallet.isConsistent())
                throw new Error("Inconsistent backup");
            // todo: acá tengo que resetear la wallet
            //resetBlockchain();
            //context.toast("Your wallet was reset!\\\\nIt will take some time to recover.");
            logger.info("wallet restored from backup: '" + conf.getKeyBackupProtobuf() + "'");
            return wallet;
        }catch (final IOException e){
            throw new Error("cannot read backup",e);
        }catch (UnreadableWalletException e){
            throw new Error("cannot read backup",e);
        }finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    /**
     * Este metodo puede tener varias implementaciones de guardado distintas.
     */
    public void saveWallet() {
        try {
            protobufSerializeWallet(wallet);
        } catch (final IOException x) {
            throw new RuntimeException(x);
        }
    }

    /**
     * Save wallet file
     *
     * @param wallet
     * @throws IOException
     */
    private void protobufSerializeWallet(final Wallet wallet) throws IOException {
        wallet.saveToFile(walletFile);
        // make wallets world accessible in test mode
        //if (conf.isTest())
        //    Io.chmod(walletFile, 0777);

        logger.info("wallet saved to: '{}', took {}", walletFile);
    }


    /**
     * Backup wallet
     */
    private void backupWallet() {

        final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(wallet).toBuilder();

        // strip redundant
        builder.clearTransaction();
        builder.clearLastSeenBlockHash();
        builder.setLastSeenBlockHeight(-1);
        builder.clearLastSeenBlockTimeSecs();
        final Protos.Wallet walletProto = builder.build();

        OutputStream os = null;

        try{
            os = contextWrapper.openFileOutputPrivateMode(conf.getKeyBackupProtobuf());
            walletProto.writeTo(os);
        } catch (FileNotFoundException e) {
            logger.error("problem writing wallet backup", e);
        } catch (IOException e) {
            logger.error("problem writing wallet backup", e);
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                // nothing
            }
        }
    }

    public List<Address> getWatchedAddresses() {
        return wallet.getWatchedAddresses();
    }

    private static final class WalletAutosaveEventListener implements WalletFiles.Listener {

        WalletConfiguration conf;

        public WalletAutosaveEventListener(WalletConfiguration walletConfiguration) {
            conf = walletConfiguration;
        }

        @Override
        public void onBeforeAutoSave(final File file) {
        }

        @Override
        public void onAfterAutoSave(final File file) {
            // make wallets world accessible in test mode
            //if (conf.isTest())
            //    Io.chmod(file, 0777);
        }
    }

}
