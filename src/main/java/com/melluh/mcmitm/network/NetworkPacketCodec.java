package com.melluh.mcmitm.network;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.Session;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.packet.Packet;
import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.ProtocolCodec.ProtocolStateCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.tinylog.Logger;

import java.util.List;

public class NetworkPacketCodec extends ByteToMessageCodec<Packet> {

    private final MinecraftProxy proxy;
    private final Session session;
    private final PacketDirection direction;

    public NetworkPacketCodec(MinecraftProxy proxy, Session session, PacketDirection direction) {
        this.proxy = proxy;
        this.session = session;
        this.direction = direction;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf buf) throws Exception {
        int initial = buf.writerIndex();
        try {
            int packetId = packet.getType().getId();
            NetworkUtils.writeVarInt(buf, packetId);
            packet.write(buf);
        } catch (Throwable throwable) {
            Logger.error(throwable, "Failed to write packet");
            buf.writerIndex(initial);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        try {
            int packetId = NetworkUtils.readVarInt(buf);

            ProtocolStateCodec codec = proxy.getCodec().getStateCodec(session.getState());
            PacketType packetType = codec.getPacket(direction, packetId);
            if(packetType == null)
                throw new IllegalStateException("Unknown packet type: 0x" + Integer.toHexString(packetId) + " (" + direction.getName() + ", " + session.getState().name() + ")");

            Packet packet = new Packet(packetType);
            packet.read(buf);

            if(buf.readableBytes() > 0)
                throw new IllegalStateException("Packet " + packetType.getName() + " not fully read (readable: " + buf.readableBytes() + ", state: " + session.getState().name() + ")");

            out.add(packet);
        } catch (Throwable throwable) {
            Logger.error(throwable, "Failed to read packet");
            buf.readerIndex(buf.readerIndex() + buf.readableBytes());
        }
    }

}
