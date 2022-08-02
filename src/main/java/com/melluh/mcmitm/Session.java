package com.melluh.mcmitm;

import com.melluh.mcmitm.protocol.ProtocolState;
import io.netty.channel.Channel;
import org.tinylog.Logger;

public class Session {

    private final Channel channel;
    private ProtocolState state = ProtocolState.HANDSHAKING;

    public Session(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    public ProtocolState getState() {
        return state;
    }

    public void setState(ProtocolState state) {
        Logger.info("State transitioned: {}", state.name());
        this.state = state;
    }

}
