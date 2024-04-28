package fr.supermax_8.endertranslate.core.communication;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import org.eclipse.jetty.websocket.api.Session;

public abstract class WsSession {

    private final Session jettySession;


    public WsSession(Session jettySession) {
        this.jettySession = jettySession;
    }


    public void sendPacket(Packet packet) {
        try {
            jettySession.getRemote().sendString(EnderTranslate.getGson().toJson(new PacketWrapper(packet)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}