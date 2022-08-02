package com.melluh.mcmitm;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.network.NetworkPacketCodec;
import com.melluh.mcmitm.network.NetworkPacketHandler;
import com.melluh.mcmitm.network.NetworkPacketSizer;
import com.melluh.mcmitm.protocol.ProtocolCodec;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.util.Utils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MinecraftProxy {

    private final int listenPort;
    private final String targetHost;
    private final int targetPort;

    private ProtocolCodec codec;
    private final List<Session> sessions = new ArrayList<>();

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
                        protected void initChannel(SocketChannel ch) {
                            Session session = new Session(ch);
                            sessions.add(session);

                            ch.pipeline()
                                    .addLast("sizer", new NetworkPacketSizer())
                                    .addLast("decoder", new NetworkPacketCodec(MinecraftProxy.this, session, PacketDirection.SERVERBOUND))
                                    .addLast("handler", new NetworkPacketHandler(MinecraftProxy.this, session));

                            Logger.info("Session initialized: {}", ch.localAddress().getAddress().getHostAddress());
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

}
