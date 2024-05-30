package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import fr.supermax_8.endertranslate.core.communication.WsServer;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Optional;

public class ServerAuthPacket implements WsPacket {

    private final String secret;

    public ServerAuthPacket(String secret) {
        this.secret = secret;
    }

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {
        if (EnderTranslateConfig.getInstance().getSecret().equals(secret)) {
            // Auth server
            WsServer server = new WsServer(jettySession);
            socket.getSessions().put(jettySession, Optional.of(server));

            // Send the main server config to him
            server.sendPacket(new MainServerInfoPacket(
                    EnderTranslateConfig.getInstance().getLanguages(),
                    TranslationManager.getInstance().getTranslations(),
                    EnderTranslate.getInstance().getEditorSecret()
                    ));
        } else jettySession.close();
    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }

}