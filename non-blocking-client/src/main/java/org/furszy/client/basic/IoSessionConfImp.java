package org.furszy.client.basic;

import org.furszy.client.interfaces.IoSessionConf;
import org.furszy.client.interfaces.ProtocolDecoder;
import org.furszy.client.interfaces.ProtocolEncoder;

import java.util.concurrent.TimeUnit;

/**
 * Created by mati on 12/05/17.
 */

public class IoSessionConfImp implements IoSessionConf{

    ProtocolDecoder protocolDecoder;

    ProtocolEncoder protocolEncoder;

    public IoSessionConfImp() {
    }

    @Override
    public int getReadBufferSize() {
        return 65353;
    }

    @Override
    public int getMaxReadBufferSize() {
        return 65353;
    }

    @Override
    public int getWriteBufferSize() {
        return 65353;
    }

    @Override
    public int getSocketTimeout() {
        return (int) TimeUnit.SECONDS.toMillis(60);
    }

    @Override
    public int getIdleTime(IdleStatus status) {
        return (int) TimeUnit.SECONDS.toMillis(60);
    }

    @Override
    public long getIdleTimeInMillis(IdleStatus idleStatus) {
        return (int) TimeUnit.SECONDS.toMillis(60);
    }

    @Override
    public long getWriteTimeoutInMillis() {
        return (int) TimeUnit.SECONDS.toMillis(60);
    }

    @Override
    public int getSocketReadBufferSize() {
        return 65353;
    }

    @Override
    public Class<? extends IoSessionImp> getIoSessionClass() {
        return null;// PfSession.class;
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return protocolDecoder;
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return protocolEncoder;
    }

    public void setProtocolDecoder(ProtocolDecoder protocolDecoder) {
        this.protocolDecoder = protocolDecoder;
    }

    public void setProtocolEncoder(ProtocolEncoder protocolEncoder) {
        this.protocolEncoder = protocolEncoder;
    }
}
