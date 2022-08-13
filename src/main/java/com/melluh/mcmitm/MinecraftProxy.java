package com.melluh.mcmitm;

import com.grack.nanojson.JsonObject;
import com.melluh.mcmitm.gui.MainGui;
import com.melluh.mcmitm.network.NetworkPacketCodec;
import com.melluh.mcmitm.network.NetworkPacketHandler;
import com.melluh.mcmitm.network.NetworkPacketSizer;
import com.melluh.mcmitm.protocol.ProtocolCodec;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MinecraftProxy {

    private final MainGui gui;
    private final int listenPort;
    private final String targetHost;
    private final int targetPort;

    private ProtocolCodec codec;
    private final List<Session> sessions = new ArrayList<>();

    private ProxyState state = ProxyState.IDLE;
    private Consumer<ProxyState> stateUpdateConsumer;

    private EventLoopGroup group;

    public MinecraftProxy(MainGui gui, int listenPort, String targetHost, int targetPort) {
        this.gui = gui;
        this.listenPort = listenPort;
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    public void onStateChange(Consumer<ProxyState> stateUpdateConsumer) {
        this.stateUpdateConsumer = stateUpdateConsumer;
    }

    private void setState(ProxyState state) {
        Logger.info("Proxy state changed: {}", state);
        this.state = state;
        if(stateUpdateConsumer != null)
            stateUpdateConsumer.accept(state);
    }

    public void run() throws Exception {
        Logger.info("Starting proxy...");
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

        this.group = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        Session session = new Session(MinecraftProxy.this, ch);
                        sessions.add(session);

                        ch.pipeline()
                                .addLast("sizer", new NetworkPacketSizer())
                                .addLast("codec", new NetworkPacketCodec(MinecraftProxy.this, session, PacketDirection.SERVERBOUND))
                                .addLast("handler", new NetworkPacketHandler(MinecraftProxy.this, session, PacketDirection.SERVERBOUND));

                        Logger.info("Session initialized: {}", ch.localAddress().getAddress().getHostAddress());
                        session.connectServer();
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        ChannelFuture future = bootstrap.bind(listenPort).sync();
        future.addListener(future1 -> {
           if(future1.isSuccess()) {
               this.setState(ProxyState.RUNNING);
           } else {
               if(future1.cause() != null) {
                   Logger.error(future1.cause(), "Proxy failed to start");
               } else {
                   Logger.error("Proxy failed to start; no cause attached");
               }

               this.setState(ProxyState.IDLE);
           }
        });
    }

    public void stop() {
        if(group != null) {
            group.shutdownGracefully();
            group = null;
        }

        sessions.forEach(Session::disconnectServer);
        this.setState(ProxyState.IDLE);
    }

    public ProtocolCodec getCodec() {
        return codec;
    }

    public String getTargetHost() {
        return targetHost;
    }

    public int getTargetPort() {
        return targetPort;
    }

    public MainGui getGui() {
        return gui;
    }

    public enum ProxyState {
        IDLE, RUNNING
    }

}
