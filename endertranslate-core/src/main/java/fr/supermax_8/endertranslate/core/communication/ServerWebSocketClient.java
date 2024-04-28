package fr.supermax_8.endertranslate.core.communication;

import com.google.gson.JsonSyntaxException;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.utils.WebSocketClient;
import lombok.Getter;

import java.nio.ByteBuffer;

public class ServerWebSocketClient extends WebSocketClient {

    @Getter
    private static ServerWebSocketClient instance;

    public ServerWebSocketClient(String wsUrl) {
        super(wsUrl, true);
        start();
        instance = this;
    }

    @Override
    protected void onText(String receivedText, boolean last) {
        try {
            WsPacketWrapper packet = EnderTranslate.getGson().fromJson(receivedText, WsPacketWrapper.class);
            packet.getPacket().receiveFromServer(this);
        } catch (JsonSyntaxException e) {
        }
    }

    @Override
    protected void onBinary(ByteBuffer receivedBinary, boolean last) {
    }

    public void sendPacket(WsPacket packet) {
        try {
            sendMessage(EnderTranslate.getGson().toJson(new WsPacketWrapper(packet)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}