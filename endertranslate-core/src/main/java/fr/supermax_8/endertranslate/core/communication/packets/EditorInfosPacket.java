package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import fr.supermax_8.endertranslate.core.language.Language;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;
import java.util.Map;

public class EditorInfosPacket implements WsPacket {

    private List<String> translationFilesPaths;
    private Map<String, Language> languages;

    public EditorInfosPacket(List<String> translationFilesPaths, Map<String, Language> languages) {
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