package fr.supermax_8.endertranslate.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacketWrapper;
import fr.supermax_8.endertranslate.core.language.Language;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import lombok.Getter;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class EnderTranslate {

    @Getter
    private static EnderTranslate instance;
    @Getter
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(WsPacketWrapper.class, new WsPacketWrapper.Adapter())
            .create();

    @Getter
    private File pluginDir;
    @Getter
    private Function<Object, UUID> playerObjectToUUIDFunction;
    private static Consumer<String> log;

    private EnderTranslateConfig config;
    private LanguageManager languageManager;
    private TranslationManager translationManager;
    private TranslatePlayerManager playerManager;
    private WebSocketServer webSocketServer;
    private ServerWebSocketClient webSocketClient;
    private PacketEventsHandler packetEventsHandler;

    public EnderTranslate(File pluginDir, Function<Object, UUID> playerObjectToUUIDFunction, Consumer<String> log) {
        this.log = log;
        log("Loading...");
        this.pluginDir = pluginDir;
        this.playerObjectToUUIDFunction = playerObjectToUUIDFunction;
        instance = this;
        config = new EnderTranslateConfig(pluginDir);

        if (config.isMainServer()) {
            languageManager = new LanguageManager(config.getLanguages());
            File translationFolder = new File(pluginDir, "translations");
            if (!translationFolder.exists()) translationFolder.mkdirs();
            translationManager = new TranslationManager(translationFolder);

            File playerDataFolder = new File(pluginDir, "players");
            if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
            playerManager = new TranslatePlayerManager(playerDataFolder);

            webSocketServer = new WebSocketServer(config.getWsPort());
            packetEventsHandler = new PacketEventsHandler(playerObjectToUUIDFunction);
        } else {
            webSocketClient = new ServerWebSocketClient(config.getWsUrl());
        }

    }

    public void shutdown() {
        if (webSocketServer != null) webSocketServer.stop();
        if (webSocketClient != null) webSocketClient.stop();
    }

    public void initLanguages(Map<String, Language> languages) {
        languageManager = new LanguageManager(languages);
    }


    public static void log(String message) {
        log.accept("§8[§d§lEnderTranslate§8] §r§f" + message);
    }

}