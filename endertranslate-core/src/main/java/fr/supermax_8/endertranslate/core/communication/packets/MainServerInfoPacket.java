package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import fr.supermax_8.endertranslate.core.language.Language;
import org.eclipse.jetty.websocket.api.Session;

import java.util.LinkedHashMap;

public class MainServerInfoPacket implements WsPacket {

    private final LinkedHashMap<String, Language> languages;
    private final String editorSecret;

    public MainServerInfoPacket(LinkedHashMap<String, Language> languages, String editorSecret) {
        this.languages = languages;
        this.editorSecret = editorSecret;
    }

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {

    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {
        EnderTranslate.log("§aMain server info received, connection established !");
        EnderTranslate.getInstance().initLanguages(languages);
        EnderTranslate.getInstance().setEditorSecret(editorSecret);
    }

}