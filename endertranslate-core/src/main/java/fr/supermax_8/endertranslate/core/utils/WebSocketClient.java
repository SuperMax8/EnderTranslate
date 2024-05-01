package fr.supermax_8.endertranslate.core.utils;

import fr.supermax_8.endertranslate.core.EnderTranslate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.*;

public abstract class WebSocketClient {

    private final String wsUrl;
    private ScheduledExecutorService scheduler;
    private WebSocket webSocket;

    public WebSocketClient(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    protected void onOpen(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    protected abstract void onText(String receivedText, boolean last);

    protected abstract void onBinary(ByteBuffer receivedBinary, boolean last);

    public void sendMessage(String message) {
        sendMessage(message, true);
    }

    public void sendMessage(String message, boolean last) {
        webSocket.sendText(message, last);
    }

    public void sendBinary(ByteBuffer binary) {
        sendBinary(binary, true);
    }

    public void sendBinary(ByteBuffer binary, boolean last) {
        webSocket.sendBinary(binary, last);
    }

    public void start() {
        if (scheduler == null) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(() -> {
                if (webSocket != null) {
                    ByteBuffer payload = ByteBuffer.wrap(new byte[]{1, 2, 3});
                    webSocket.sendPing(payload);
                }
            }, 4, 10, TimeUnit.SECONDS);
        }
        try {
            EnderTranslate.log("Trying to start WebSocket client...");
            HttpClient client = HttpClient.newHttpClient();
            WebSocket.Builder builder = client.newWebSocketBuilder();
            builder.buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    EnderTranslate.log("WebSocketClient started");
                    WebSocketClient.this.onOpen(webSocket);
                    webSocket.request(1);
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    WebSocketClient.this.onText(String.valueOf(data), last);
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }

                @Override
                public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                    WebSocketClient.this.onBinary(data, last);
                    return WebSocket.Listener.super.onBinary(webSocket, data, last);
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    error.printStackTrace();
                    WebSocket.Listener.super.onError(webSocket, error);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    EnderTranslate.log("WebSocketClient closed: " + statusCode + " " + reason);
                    scheduler.schedule(() -> start(), 2, TimeUnit.SECONDS);
                    return null;
                }
            }).join();
            if (webSocket == null) scheduler.schedule(() -> start(), 2, TimeUnit.SECONDS);
        } catch (Exception e) {
            scheduler.schedule(() -> start(), 2, TimeUnit.SECONDS);
        }
    }


    public void close() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Goodbye");
            scheduler.shutdown();
        }
    }

}