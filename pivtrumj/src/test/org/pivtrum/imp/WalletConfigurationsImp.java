package org.pivtrum.imp;

import org.pivxj.core.Context;
import org.pivxj.core.NetworkParameters;
import org.pivxj.params.TestNet3Params;

import global.WalletConfiguration;

/**
 * Created by furszy on 6/16/17.
 */

public class WalletConfigurationsImp implements WalletConfiguration {

    public static NetworkParameters networkParameters = TestNet3Params.get();
    public static Context context = new Context(networkParameters);

    @Override
    public int getTrustedNodePort() {
        return 0;
    }

    @Override
    public String getTrustedNodeHost() {
        return null;
    }

    @Override
    public void saveTrustedNode(String host, int port) {

    }

    @Override
    public void saveScheduleBlockchainService(long time) {

    }

    @Override
    public long getScheduledBLockchainService() {
        return 0;
    }

    @Override
    public String getMnemonicFilename() {
        return null;
    }

    @Override
    public String getWalletProtobufFilename() {
        return "protobuf_filename.dat";
    }

    @Override
    public NetworkParameters getNetworkParams() {
        return networkParameters;
    }

    @Override
    public String getKeyBackupProtobuf() {
        return "key_backup_proto.dat";
    }

    @Override
    public long getWalletAutosaveDelayMs() {
        return  5000;
    }

    @Override
    public Context getWalletContext() {
        return context;
    }

    @Override
    public String getBlockchainFilename() {
        return null;
    }

    @Override
    public String getCheckpointFilename() {
        return null;
    }

    @Override
    public int getPeerTimeoutMs() {
        return 0;
    }

    @Override
    public long getPeerDiscoveryTimeoutMs() {
        return 0;
    }

    @Override
    public int getMinMemoryNeeded() {
        return 0;
    }

    @Override
    public long getBackupMaxChars() {
        return 0;
    }

    @Override
    public boolean isTest() {
        return false;
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }
}
