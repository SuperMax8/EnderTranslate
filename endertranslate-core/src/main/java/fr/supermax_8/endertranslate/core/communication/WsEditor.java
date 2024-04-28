package fr.supermax_8.endertranslate.core.communication;

import org.eclipse.jetty.websocket.api.Session;

public class WsEditor extends WsSession {

    public WsEditor(Session jettySession) {
        super(jettySession);
    }

}