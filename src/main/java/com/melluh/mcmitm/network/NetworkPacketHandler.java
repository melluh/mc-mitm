package com.melluh.mcmitm.network;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.Session;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tinylog.Logger;

public class NetworkPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final MinecraftProxy proxy;
    private final Session session;
    private final PacketDirection direction;

    public NetworkPacketHandler(MinecraftProxy proxy, Session session, PacketDirection direction) {
        this.proxy = proxy;
        this.session = session;
        this.direction = direction;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) {
        proxy.getGui().addPacket(packet);

        // TODO: make this a lot better
        if(packet.getType().getName().equals("ClientIntentionPacket")) {
            int intention = (int) packet.getData().getValue("intention");
            switch(intention) {
                case 1 -> session.setState(ProtocolState.STATUS);
                case 2 -> session.setState(ProtocolState.LOGIN);
                default -> throw new IllegalStateException("Invalid intention: " + intention);
            }
        }

        if(packet.getType().getName().equals("ClientboundGameProfilePacket")) {
            session.setState(ProtocolState.PLAY);
        }

        if(packet.getType().getName().equals("ClientboundLoginCompressionPacket")) {
            session.setCompressionThreshold((int) packet.getData().getValue("compressionThreshold"));
            return; // don't forward to client
        }

        if(packet.getType().getName().equals("ClientboundHelloPacket")) {
            Logger.warn("Server is in online mode, disconnecting");
            session.disconnect();
            return;
        }

        if(direction == PacketDirection.CLIENTBOUND) {
            session.sendToClient(packet);
        } else {
            session.sendToServer(packet);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.info("Channel inactive: {}", ctx.channel().id());
    }

}
