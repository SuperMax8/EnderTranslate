package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;

public class EditorInfosPacket implements WsPacket {

    private List<String> translationFilesPaths;
    private List<String> languages;

    public EditorInfosPacket(List<String> translationFilesPaths, List<String> languages) {
        this.translationFilesPaths = translationFilesPaths;
        this.languages = languages;
    }

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {

    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }

}