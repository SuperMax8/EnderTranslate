package fr.supermax_8.endertranslate.core;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.supermax_8.endertranslate.core.communication.ServerWebSocketClient;
import fr.supermax_8.endertranslate.core.communication.WebSocketServer;
import fr.supermax_8.endertranslate.core.communication.WsPacketWrapper;
import fr.supermax_8.endertranslate.core.language.Language;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import fr.supermax_8.endertranslate.core.player.TranslatePlayerManager;
import fr.supermax_8.endertranslate.core.translation.TranslationManager;
import fr.supermax_8.endertranslate.core.utils.Base64Utils;
import fr.supermax_8.endertranslate.core.utils.CryptographyUtils;
import fr.supermax_8.endertranslate.core.utils.ResourceUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.*;
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
    @Getter
    private static final boolean verified;

    static {
        // Yes this is overkill
        String premiumSign = "swLOvTa0S4DWMLpX3SYW9GexN8xemo5+vgPaRDYBUK7ZyVH51ZGy401TumyZFqad59+6LLV8hlqGrm5XXDM7PuiGNgeQzhn26gEtin++ysXmfqqtJ++MBM6YyfsypsOFJUDX+nU+WxGKhs0mC8zANsTs2O95q+qKdkcZ3TsACdjPax+HjWmpq7tD9h7u5eORNC3vq+yXOluk04g8mmovJXCXt9s4Iz2vN8Kijc5n7X174nmvwMatAj9Di42STmwhh/9bIzKsfJAwhwECMHr2kDUVBz22lnFbgA3qsfwVUeWwRigSTNOtNujUaLf2VRGDUtzxWDAM2imz/GhsRn9WOg==";
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuGFtWUIqf4oQ4mECJ3MTQq1dW0+hrFcRNR/xcohAqFeyD7X8wyX/3aexQck6VFUexG3u/vGfqfItc/vHCTcuTwxMvRaueLRZP17EMUynD9K7wFZXgU2jyV9ffd+mTZ6XmSlCpU238JqLYHztL4tq2iLEzhYjKftkxCbaURhf1fY56TvIueOKBDzSqgmfWjiuTomeYwWAaq3bJy35HkwMDSye3KW+BfPDamMBS1ph1b54MOUte2opZBhc2igo8BI/73TsYIcpEcaRZb20419N3Nng+fsDcAEJIvRNV+V0jRnInmo8qA36hs4/v37V6YfnRSeMCBSkpHaF/7Z7jl4f7wIDAQAB";

        boolean verif = false;
        String value;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(ResourceUtils.getResourceAsStream("PREMIUM")))){
            value = br.readLine();
            verif = CryptographyUtils.verify(value, premiumSign, publicKey);
        } catch (Exception e) {
        }
        verified = verif;
    }

    @Getter
    @Setter
    private String editorSecret = Base64Utils.generateSecuredToken(8);

    public EnderTranslate(File pluginDir, Function<Object, UUID> playerObjectToUUIDFunction, Consumer<String> log) {
        EnderTranslate.log = log;
        this.pluginDir = pluginDir;
        this.playerObjectToUUIDFunction = playerObjectToUUIDFunction;
        log("§7Loading...");
        /*editorSecret = "dev";*/
        load();
        log("§aLoaded");
        instance = this;
    }

    public void reload() {
        log("§7Reloading...");
        try {
            config = new EnderTranslateConfig(pluginDir);

            if (config.isMainServer()) {
                languageManager = new LanguageManager(config.getLanguages());
                File translationFolder = new File(pluginDir, "translations");
                if (!translationFolder.exists()) translationFolder.mkdirs();
                translationManager = new TranslationManager(translationFolder);

                File playerDataFolder = new File(pluginDir, "players");
                if (!playerDataFolder.exists()) playerDataFolder.mkdirs();
                playerManager = new TranslatePlayerManager(playerDataFolder);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        log("§aReloaded !");
    }

    public void load() {
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
            packetEventsHandler = new PacketEventsHandler();
        } else
            webSocketClient = new ServerWebSocketClient(config.getWsUrl());
    }

    public void shutdown() {
        if (webSocketServer != null) webSocketServer.stop();
        try {
            if (webSocketClient != null) webSocketClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PacketEvents.getAPI().getEventManager().unregisterAllListeners();
    }

    public void initLanguages(Map<String, Language> languages) {
        languageManager = new LanguageManager(languages);
        languageManager.getLanguageMap().values().forEach(l -> {
            System.out.println(l);
        });
    }


    public static void log(String message) {
        log.accept("§8[§d§lEnderTranslate§8] §r§f" + message);
    }

}