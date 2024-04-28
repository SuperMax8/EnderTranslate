package fr.supermax_8.endertranslate.core.communication;

import org.eclipse.jetty.websocket.api.Session;

public interface Packet {


    void receivePacket(Session jettySession, WebSocket socket);

}