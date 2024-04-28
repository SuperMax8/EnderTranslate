package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.Packet;
import fr.supermax_8.endertranslate.core.communication.WsServer;
import fr.supermax_8.endertranslate.core.communication.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Optional;

public class ServerAuthPacket implements Packet {

    private String secret;

    @Override
    public void receivePacket(Session jettySession, WebSocket socket) {
        if (EnderTranslateConfig.getInstance().getSecret().equals(secret)) {
            socket.getSessions().put(jettySession, Optional.of(new WsServer(jettySession)));
        } else jettySession.close();
    }


}