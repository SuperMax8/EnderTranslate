package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsEditor;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import org.eclipse.jetty.websocket.api.Session;

import java.io.File;

public class EditorPagePacket implements WsPacket {

    private String pageRelativePath;
    private TranslationManager.TranslationFile file;

    public EditorPagePacket(String pageRelativePath, TranslationManager.TranslationFile file) {
        this.pageRelativePath = pageRelativePath;
        this.file = file;
    }

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {
        try {
            WsEditor editor = (WsEditor) socket.getSessions().get(jettySession).get();
            editor.sendPacket(new EditorPagePacket(pageRelativePath, TranslationManager.getInstance().getTranslationFileFromRelativePath(pageRelativePath.replaceAll("/", File.separator))));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }

}