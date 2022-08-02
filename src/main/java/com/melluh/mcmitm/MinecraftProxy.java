package com.melluh.mcmitm;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkPacketDecoder;
import com.melluh.mcmitm.network.NetworkPacketSizer;
import com.melluh.mcmitm.protocol.ProtocolCodec;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.util.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.tinylog.Logger;

public class MinecraftProxy {

    private final int listenPort;
    private final String targetHost;
    private final int targetPort;

    private ProtocolCodec codec;
    private ProtocolState state = ProtocolState.HANDSHAKING;

    public MinecraftProxy(int listenPort, String targetHost, int targetPort) {
        this.listenPort = listenPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public void run() throws Exception {
        JsonObject protocolJson = Utils.loadJsonFromFile("protocol/versions/759.json");
        this.codec = ProtocolCodec.loadFromJson(protocolJson);

        /*for(ProtocolState state : ProtocolState.values()) {
            Logger.info("-- {}", state.name());
            ProtocolStateCodec stateCodec = codec.getStateCodec(state);
            for(PacketType packet : stateCodec.getServerboundPackets()) {
                Logger.info("C->S: 0x{} {}", Integer.toHexString(packet.getId()), packet.getName());
                packet.getFieldList().getFields().forEach(field -> {
                    Logger.info("\t{} ({})", field.getName(), field.getType().name());
                });
            }
            for(PacketType packet : stateCodec.getClientboundPackets()) {
                Logger.info("S->C: 0x{} {}", Integer.toHexString(packet.getId()), packet.getName());
                packet.getFieldList().getFields().forEach(field -> {
                    Logger.info("\t{} ({})", field.getName(), field.getType().name());
                });
            }
        }*/

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            Logger.info("Channel initialized: {}", ch.localAddress().getAddress().getHostAddress());
                            ch.pipeline()
                                    .addLast("sizer", new NetworkPacketSizer())
                                    .addLast("decoder", new NetworkPacketDecoder(MinecraftProxy.this));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture future = bootstrap.bind(listenPort).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public ProtocolCodec getCodec() {
        return codec;
    }

    public void setState(ProtocolState state) {
        this.state = state;
    }

    public ProtocolState getState() {
        return state;
    }

}
