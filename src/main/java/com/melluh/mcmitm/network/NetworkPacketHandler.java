package com.melluh.mcmitm.network;

import com.melluh.mcmitm.Session;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tinylog.Logger;

public class NetworkPacketHandler extends SimpleChannelInboundHandler<Packet> {

    private final Session session;
    private final PacketDirection direction;

    public NetworkPacketHandler(Session session, PacketDirection direction) {
        this.session = session;
        this.direction = direction;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        String name = packet.getType().getName();
        if(!name.startsWith("ClientboundMoveEntityPacket") && !name.equals("ClientboundRotateHeadPacket") && !name.startsWith("ServerboundMovePlayerPacket")) {
            Logger.info(name);
            for (PacketField field : packet.getType().getFields()) {
                Object data = packet.getData().getValue(field.getName());
                Logger.info("\t{} ({}): {}", field.getName(), field.getType().name(), (data != null ? data.toString() : "[skipped]"));
            }
        }

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

        if(direction == PacketDirection.CLIENTBOUND) {
            session.sendToClient(packet);
        } else {
            session.sendToServer(packet);
        }
    }

}
