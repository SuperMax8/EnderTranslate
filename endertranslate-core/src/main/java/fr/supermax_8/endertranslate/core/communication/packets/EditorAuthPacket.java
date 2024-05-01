package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.communication.*;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Optional;

public class EditorAuthPacket implements WsPacket {

    private final String secret;

    public EditorAuthPacket(String secret) {
        this.secret = secret;
    }

    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {
        if (EnderTranslate.getInstance().getEditorSecret().equals(secret)) {
            // Auth server
            WsEditor editor = new WsEditor(jettySession);
            socket.getSessions().put(jettySession, Optional.of(editor));

            // Send the main server config to him
            editor.sendPacket(new EditorInfosPacket(
                    TranslationManager.getInstance().getAllFilesPaths(),
                    EnderTranslateConfig.getInstance().getLanguages()
            ));
        } else jettySession.close();
    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }

}