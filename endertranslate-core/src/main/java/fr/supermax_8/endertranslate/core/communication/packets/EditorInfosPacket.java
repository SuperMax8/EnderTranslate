package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import lombok.AllArgsConstructor;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;

@AllArgsConstructor
public class EditorInfosPacket implements WsPacket {

    private final List<String> translationFilesPaths;
    private final List<String> languages;
    private final String startTag;
    private final String endTag;

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {

    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }

}