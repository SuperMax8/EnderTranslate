package fr.supermax_8.endertranslate.core.communication;

import org.eclipse.jetty.websocket.api.Session;

public class WsServer extends WsSession {

    public WsServer(Session jettySession) {
        super(jettySession);
    }

}