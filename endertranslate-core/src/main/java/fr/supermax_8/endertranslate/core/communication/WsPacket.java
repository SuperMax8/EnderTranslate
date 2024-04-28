package fr.supermax_8.endertranslate.core.communication;

import org.eclipse.jetty.websocket.api.Session;

public interface WsPacket {


    void receiveFromClient(Session jettySession, WebSocketServer socket);

    void receiveFromServer(ServerWebSocketClient ws);

}