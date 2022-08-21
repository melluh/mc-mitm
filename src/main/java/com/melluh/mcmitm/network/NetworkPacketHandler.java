package com.melluh.mcmitm.network;

import com.melluh.mcauth.utils.AuthUtils;
import com.melluh.mcmitm.MinecraftProxy;
import com.melluh.mcmitm.Session;
import com.melluh.mcmitm.auth.Account;
import com.melluh.mcmitm.auth.AuthenticationHandler;
import com.melluh.mcmitm.gui.MainGui;
import com.melluh.mcmitm.protocol.PacketType;
import com.melluh.mcmitm.protocol.ProtocolCodec.PacketDirection;
import com.melluh.mcmitm.protocol.ProtocolCodec.ProtocolStateCodec;
import com.melluh.mcmitm.protocol.ProtocolState;
import com.melluh.mcmitm.protocol.packet.Packet;
import com.melluh.mcmitm.protocol.packet.PacketData;
import com.melluh.mcmitm.util.CryptUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.tinylog.Logger;

import javax.crypto.SecretKey;
import java.security.PublicKey;

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

        if(packet.getType().getName().equals("ServerboundHelloPacket")) {
            String username = (String) packet.getData().getValue("name");
            session.setUsername(username);
            Logger.info("Logging in as {}", username);

            this.sendServerHelloPacket(username);
            return;
        }

        if(packet.getType().getName().equals("ClientboundHelloPacket")) {
            String username = session.getUsername();
            if(session.getUsername() == null) {
                Logger.warn("Server sent ClientboundHelloPacket before client sent ServerboundHelloPacket");
                session.disconnect();
                return;
            }

            Account account = session.getAccount();
            if(account == null) {
                Logger.warn("{} is not on the accounts list", username);
                return;
            }

            if(!account.refreshTokens()) {
                // exception already logged
                session.disconnect();
                return;
            }

            PacketData data = packet.getData();
            String serverId = (String) data.getValue("serverId");
            byte[] publicKeyBytes = (byte[]) data.getValue("publicKey");
            PublicKey publicKey = CryptUtil.readPublicKey(publicKeyBytes);
            byte[] nonce = (byte[]) data.getValue("nonce");

            SecretKey sharedSecret = CryptUtil.generateSharedSecret();
            String serverHash = AuthUtils.calculateServerHash(serverId, publicKey, sharedSecret);

            AuthenticationHandler.MOJANG_AUTHENTICATOR.sendJoin(account.getMojangToken(), account.getGameProfile(), serverHash).thenAccept(x -> {
                Logger.info("Logged in with Mojang");
                this.sendServerKeyPacket(publicKey, sharedSecret, nonce);
                session.enableEncryption(sharedSecret);
            }).exceptionally(ex -> {
                MainGui.getInstance().displayException(ex);
                Logger.error(ex, "Failed to authenticate");
                return null;
            });

            return;
        }

        if(direction == PacketDirection.CLIENTBOUND) {
            session.sendToClient(packet);
        } else {
            session.sendToServer(packet);
        }
    }

    // FIXME: this is proper shit
    // this just makes sure there are no mojang-issued public keys (chat crypto) sent along, as we don't support those yet
    private void sendServerHelloPacket(String username) {
        ProtocolStateCodec stateCodec = proxy.getCodec().getStateCodec(session.getState());
        PacketType type = stateCodec.getPacketType(PacketDirection.SERVERBOUND, "ServerboundHelloPacket");
        Packet packet = new Packet(type);

        PacketData data = packet.getData();
        data.setValue("name", username);
        data.setValue("hasKey", false);

        session.sendToServer(packet);
        proxy.getGui().addPacket(packet);
    }

    // FIXME: also proper shit
    private void sendServerKeyPacket(PublicKey publicKey, SecretKey sharedSecret, byte[] nonce) {
        ProtocolStateCodec stateCodec = proxy.getCodec().getStateCodec(session.getState());
        PacketType type = stateCodec.getPacketType(PacketDirection.SERVERBOUND, "ServerboundKeyPacket");
        Packet packet = new Packet(type);

        // TODO: This does not support servers with 'enforce-secure-chat' set to true
        PacketData data = packet.getData();
        data.setValue("key", CryptUtil.encrypt(sharedSecret.getEncoded(), publicKey));
        data.setValue("hasNonce", true);
        data.setValue("nonce", CryptUtil.encrypt(nonce, publicKey));

        session.sendToServer(packet);
        proxy.getGui().addPacket(packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Logger.info("Channel inactive: {}", ctx.channel().id());
    }

}
