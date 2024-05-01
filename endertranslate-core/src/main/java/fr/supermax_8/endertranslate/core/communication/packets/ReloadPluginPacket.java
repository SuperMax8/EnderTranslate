package fr.supermax_8.endertranslate.core.communication.packets;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacket;
import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.CompletableFuture;

public class ReloadPluginPacket implements WsPacket {


    @Override
    public void receiveFromClient(Session jettySession, WebSocketServer socket) {
        CompletableFuture.runAsync(() -> EnderTranslate.getInstance().reload());
    }

    @Override
    public void receiveFromServer(ServerWebSocketClient ws) {

    }


}