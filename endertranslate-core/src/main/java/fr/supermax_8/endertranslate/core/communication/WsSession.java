package fr.supermax_8.endertranslate.core.communication;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import org.eclipse.jetty.websocket.api.Session;

public abstract class WsSession {

    protected final Session jettySession;


    public WsSession(Session jettySession) {
        this.jettySession = jettySession;
    }


    public void sendPacket(WsPacket packet) {
        try {
            jettySession.getRemote().sendString(EnderTranslate.getGson().toJson(new WsPacketWrapper(packet)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}