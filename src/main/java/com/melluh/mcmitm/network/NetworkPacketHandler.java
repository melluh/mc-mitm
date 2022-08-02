package com.melluh.mcmitm.network;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.Session;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tinylog.Logger;

public class NetworkPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final MinecraftProxy proxy;
    private final Session session;

    public NetworkPacketHandler(MinecraftProxy proxy, Session session) {
        this.proxy = proxy;
        this.session = session;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
        Logger.info(msg.getType().getName());
        for (PacketField field : msg.getType().getFields()) {
            Object data = msg.getData().getValue(field.getName());
            Logger.info("\t{} ({}): {}", field.getName(), field.getType().name(), (data != null ? data.toString() : "[skipped]"));
        }

        // TODO: make this a lot better
        if(msg.getType().getName().equals("ClientIntentionPacket")) {
            int intention = (int) msg.getData().getValue("intention");
            switch(intention) {
                case 1 -> session.setState(ProtocolState.STATUS);
                case 2 -> session.setState(ProtocolState.LOGIN);
                default -> throw new IllegalStateException("Invalid intention: " + intention);
            }
        }
    }

}
