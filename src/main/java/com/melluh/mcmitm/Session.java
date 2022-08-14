package com.melluh.mcmitm;

import com.melluh.mcmitm.network.NetworkCompression;
import com.melluh.mcmitm.network.NetworkPacketCodec;
import com.melluh.mcmitm.network.NetworkPacketHandler;
import com.melluh.mcmitm.network.NetworkPacketSizer;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.packet.Packet;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.tinylog.Logger;

public class Session {

    private final MinecraftProxy proxy;
    private final Channel clientChannel;
    private Channel serverChannel;

    private ProtocolState state = ProtocolState.HANDSHAKING;
    private int compressionThreshold = -1;

    public Session(MinecraftProxy proxy, Channel channel) {
        this.proxy = proxy;
        this.clientChannel = channel;
    }

    public void connectServer() {
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
                                .addLast("handler", new NetworkPacketHandler(proxy, Session.this, PacketDirection.CLIENTBOUND));
                    }
                });

        bootstrap.connect(proxy.getTargetHost(), proxy.getTargetPort()).syncUninterruptibly();
        Logger.info("Connected to server");
    }

    public void disconnect() {
        this.disconnectServer();
        this.disconnectClient();
    }

    public void disconnectClient() {
        if(clientChannel != null) {
            try {
                clientChannel.close().sync();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void disconnectServer() {
        if(serverChannel != null) {
            try {
                serverChannel.close().sync();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            serverChannel = null;
        }
    }

    private void closeChannel(Channel channel) {
        if(channel == null)
            return;

        try {
            channel.close().sync();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void setCompressionThreshold(int compressionThreshold) {
        Logger.info("Compression threshold set to {}", compressionThreshold);
        this.compressionThreshold = compressionThreshold;

        // At the moment, compression is only enabled for transport between the server and proxy.
        // There is no compression between the proxy and client.
        if(compressionThreshold >= 0) {
            this.addCompression(serverChannel);
        } else {
            this.removeCompression(serverChannel);
        }
    }

    private static final String COMPRESSION_HANDLER_NAME = "compression";

    private void addCompression(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if(pipeline.get(COMPRESSION_HANDLER_NAME) == null)
            pipeline.addAfter("sizer", COMPRESSION_HANDLER_NAME, new NetworkCompression(this));
    }

    private void removeCompression(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        if(pipeline.get(COMPRESSION_HANDLER_NAME) != null)
            pipeline.remove(COMPRESSION_HANDLER_NAME);
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

    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    public void setState(ProtocolState state) {
        Logger.info("State transitioned: {}", state.name());
        this.state = state;
    }

}
