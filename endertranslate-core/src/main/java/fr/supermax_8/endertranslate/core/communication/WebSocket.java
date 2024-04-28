package fr.supermax_8.endertranslate.core.communication;

import com.google.gson.JsonSyntaxException;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import io.javalin.Javalin;
import lombok.Getter;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocket {

    @Getter
    private static WebSocket instance;

    @Getter
    private final ConcurrentHashMap<Session, Optional<WsSession>> sessions = new ConcurrentHashMap<>();
    private final Javalin server;

    public WebSocket(int port) {
        Javalin app = Javalin.create().start(port);

        instance = this;
        server = app.ws("/", wsConfig -> {
            wsConfig.onConnect(ctx -> {
                sessions.put(ctx.session, Optional.empty());
                System.out.println("New session connected, total: " + sessions.size());
            });
            wsConfig.onClose(ctx -> {
                sessions.remove(ctx.session);
                System.out.println("Session disconnected, total: " + sessions.size());
            });
            wsConfig.onMessage((ctx) -> {
                try {
                    PacketWrapper packet = EnderTranslate.getGson().fromJson(ctx.message(), PacketWrapper.class);
                    packet.getPacket().receivePacket(ctx.session, this);
                } catch (JsonSyntaxException e) {
                }
            });
        });
        server.start();
    }

    public void stop() {
        server.stop();
    }

}