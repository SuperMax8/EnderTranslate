package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import org.eclipse.jetty.websocket.api.Session;

import java.util.UUID;

public class PlayerChangeLanguagePacket implements WsPacket {

    private final UUID playerId;
    private final String language;

    public PlayerChangeLanguagePacket(UUID playerId, String language) {
        this.playerId = playerId;
        this.language = language;
    }

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {
        TranslatePlayerManager.getInstance().setPlayerLanguage(playerId, language);
    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }

}