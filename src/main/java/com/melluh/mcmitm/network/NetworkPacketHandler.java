package com.melluh.mcmitm.network;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.Session;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.packet.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tinylog.Logger;

import java.util.List;

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
        List<PacketField> fields = msg.getType().getFieldList().getFields();
        for(int i = 0; i < fields.size(); i++) {
            PacketField field = fields.get(i);
            Object data = msg.getData().getValue(i);
            Logger.info("\t{} ({}): {}", field.getName(), field.getType().name(), data.toString());
        }

        // TODO: make this a lot better
        if(msg.getType().getName().equals("ClientIntentionPacket")) {
            int intention = (int) msg.getData().getValue(3);
            switch(intention) {
                case 1 -> session.setState(ProtocolState.STATUS);
                case 2 -> session.setState(ProtocolState.LOGIN);
                default -> throw new IllegalStateException("Invalid intention: " + intention);
            }
        }
    }

}
