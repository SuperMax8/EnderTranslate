package fr.supermax_8.endertranslate.core.utils;

import fr.supermax_8.endertranslate.core.EnderTranslate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public abstract class WebSocketClient {

    private final String wsUrl;
    private final boolean autoReconnect;
    private WebSocket webSocket;

    public WebSocketClient(String wsUrl, boolean autoReconnect) {
        this.wsUrl = wsUrl;
        this.autoReconnect = autoReconnect;
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
        try {
            HttpClient client = HttpClient.newHttpClient();
            WebSocket.Builder builder = client.newWebSocketBuilder();
            webSocket = builder.buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                @Override
                public void onOpen(WebSocket webSocket) {
                    EnderTranslate.log("WebSocketClient started");
                    java.net.http.WebSocket.Listener.super.onOpen(webSocket);
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    WebSocketClient.this.onText((String) data, last);
                    return WebSocket.Listener.super.onText(webSocket, data, last);
                }

                @Override
                public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                    WebSocketClient.this.onBinary(data, last);
                    return WebSocket.Listener.super.onBinary(webSocket, data, last);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    EnderTranslate.log("WebSocketClient closed: " + statusCode + " " + reason);
                    if (autoReconnect) restart();
                    return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
                }
            }).join();
        } catch (Exception e) {
            restart();
        }
    }

    private void restart() {
        CompletableFuture.runAsync(() -> {
            EnderTranslate.log("WebSocketClient restart...");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            start();
        });
    }

    public void stop() {
        webSocket.abort();
    }

}