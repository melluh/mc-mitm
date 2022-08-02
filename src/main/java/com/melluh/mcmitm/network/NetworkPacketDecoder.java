package com.melluh.mcmitm.network;

import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.protocol.field.PacketField;
import com.melluh.mcmitm.protocol.packet.Packet;
import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.ProtocolCodec.ProtocolStateCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import org.tinylog.Logger;

import java.util.List;

public class NetworkPacketDecoder extends ByteToMessageCodec<Packet> {

    private final MinecraftProxy proxy;

    public NetworkPacketDecoder(MinecraftProxy proxy) {
        this.proxy = proxy;
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

            ProtocolStateCodec codec = proxy.getCodec().getStateCodec(proxy.getState());
            PacketType packetType = codec.getServerboundPacket(packetId); // TODO: this needs to work in both directions
            if(packetType == null)
                throw new IllegalStateException("Unknown packet type: 0x" + Integer.toHexString(packetId));

            Packet packet = new Packet(packetType);
            packet.read(buf);

            if(buf.readableBytes() > 0)
                throw new IllegalStateException("Packet " + packetType.getName() + " not fully read (readable: " + buf.readableBytes() + ")");

            Logger.info(packet.getType().getName());

            List<PacketField> packetFields = packet.getType().getFieldList().getFields();
            for(int i = 0; i < packetFields.size(); i++) {
                PacketField field = packetFields.get(i);
                Logger.info("\t{} ({}): {}", field.getName(), field.getType().name(), packet.getData().getValue(i));
            }

            out.add(packet);
        } catch (Throwable throwable) {
            Logger.error(throwable, "Failed to read packet");
            buf.readerIndex(buf.readerIndex() + buf.readableBytes());
        }
    }

}
