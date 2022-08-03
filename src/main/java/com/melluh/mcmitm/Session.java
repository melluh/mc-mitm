package com.melluh.mcmitm;

import com.melluh.mcmitm.network.NetworkPacketCodec;
import com.melluh.mcmitm.network.NetworkPacketHandler;
import com.melluh.mcmitm.network.NetworkPacketSizer;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.packet.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.tinylog.Logger;

public class Session {

    private final MinecraftProxy proxy;
    private final Channel clientChannel;
    private Channel serverChannel;

    private ProtocolState state = ProtocolState.HANDSHAKING;

    public Session(MinecraftProxy proxy, Channel channel) {
        this.proxy = proxy;
        this.clientChannel = channel;
    }

    public void startServerConnection() {
        Bootstrap bootstrap = new Bootstrap()
                .channel(NioSocketChannel.class)
                .group(new NioEventLoopGroup())
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) throws Exception {
                        serverChannel = ch;
                        ch.pipeline()
                                .addLast("sizer", new NetworkPacketSizer())
                                .addLast("codec", new NetworkPacketCodec(proxy, Session.this, PacketDirection.CLIENTBOUND))
                                .addLast("handler", new NetworkPacketHandler(Session.this, PacketDirection.CLIENTBOUND));
                    }
                });

        bootstrap.connect(proxy.getTargetHost(), proxy.getTargetPort()).syncUninterruptibly();
        Logger.info("Connected to server");
    }

    public void sendToClient(Packet packet) {
        clientChannel.writeAndFlush(packet);
    }

    public void sendToServer(Packet packet) {
        if(serverChannel != null) {
            serverChannel.writeAndFlush(packet);
        }
    }

    public Channel getClientChannel() {
        return clientChannel;
    }

    public Channel getServerChannel() {
        return serverChannel;
    }

    public ProtocolState getState() {
        return state;
    }

    public void setState(ProtocolState state) {
        Logger.info("State transitioned: {}", state.name());
        this.state = state;
    }

}
