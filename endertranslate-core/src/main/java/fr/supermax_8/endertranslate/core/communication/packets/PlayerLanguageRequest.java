package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerLanguageRequest implements WsPacket {

    private final UUID playerId;
    private final String language;

    static HashMap<UUID, Consumer<String>> waitingRequests = new HashMap<>();

    public PlayerLanguageRequest(UUID playerId, String language, Consumer<String> whenGet) {
        this.playerId = playerId;
        this.language = language;
        if (whenGet != null) waitingRequests.put(playerId, whenGet);
    }


    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {
        socket.getSessions().get(jettySession).ifPresent(wsSession -> {
            wsSession.sendPacket(new PlayerLanguageRequest(playerId, TranslatePlayerManager.getInstance().getPlayerLanguage(playerId), null));
        });
    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {
        waitingRequests.get(playerId).accept(language);
    }

}