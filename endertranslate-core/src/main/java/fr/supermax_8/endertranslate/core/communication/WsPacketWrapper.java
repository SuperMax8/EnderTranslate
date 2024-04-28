
package fr.supermax_8.endertranslate.core.communication;

import com.google.gson.*;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WsPacketWrapper {

    @Getter
    private WsPacket packet;

    public WsPacketWrapper(WsPacket packet) {
        this.packet = packet;
    }

    @Getter
    private static final List<String> packageNameOfPackets = new ArrayList<>() {{
        add("fr.supermax_8.endertranslate.core.communication.packets");
    }};

    public static class Adapter implements JsonSerializer<WsPacketWrapper>, JsonDeserializer<WsPacketWrapper> {

        @Override
        public WsPacketWrapper deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = null;
            String type = null;
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().asMap().entrySet()) {
                type = entry.getKey();
                jsonObject = entry.getValue().getAsJsonObject();
                break;
            }
            Class<? extends WsPacket> packetClass = getPacketClass(type);
            WsPacket packet = context.deserialize(jsonObject, packetClass);

            return new WsPacketWrapper(packet);
        }

        @Override
        public JsonElement serialize(WsPacketWrapper src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            WsPacket packet = src.packet;
            String packetName = packet.getClass().getSimpleName();
            JsonElement valueElement = context.serialize(packet);

            object.add(packetName, valueElement);
            return object;
        }

        private Class<? extends WsPacket> getPacketClass(String className) {
            for (String packageName : packageNameOfPackets) {
                try {
                    String fullClassName = packageName + "." + className;
                    return Class.forName(fullClassName).asSubclass(WsPacket.class);
                } catch (ClassNotFoundException ignored) {
                }
            }
            throw new JsonParseException("Unable to find Packet class: " + className + " Did you use packageNameOfPackets ?");
        }

    }


}