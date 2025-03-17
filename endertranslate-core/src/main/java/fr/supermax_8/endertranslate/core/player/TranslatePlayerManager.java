package fr.supermax_8.endertranslate.core.player;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.PacketEventsHandler;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TranslatePlayerManager {

    @Getter
    private static TranslatePlayerManager instance;
    private static String defaultLanguage = EnderTranslateConfig.getInstance().getDefaultLanguage();
    private ConcurrentHashMap<UUID, TranslatePlayer> players = new ConcurrentHashMap<>();
    private File playerDataFolder;

    public TranslatePlayerManager() {
        instance = this;
    }

    public TranslatePlayerManager(File playerDataFolder) {
        this.playerDataFolder = playerDataFolder;
        try {
            LinkedList<UUID> toResetLanguage = new LinkedList<>();
            for (File jsonFile : playerDataFolder.listFiles(f -> f.getName().endsWith(".json"))) {
                TranslatePlayer player = EnderTranslate.getGson().fromJson(new FileReader(jsonFile), TranslatePlayer.class);
                UUID playerId = UUID.fromString(jsonFile.getName().replace(".json", ""));
                players.put(playerId, player);
                if (!(LanguageManager.getInstance().getLanguageMap().containsKey(player.getSelectedLanguage())))
                    toResetLanguage.add(playerId);
            }
            toResetLanguage.forEach(id -> setPlayerLanguage(id, null));
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance = this;
    }

    public void setPlayerLanguage(UUID playerId, String newLanguage) {
        TranslatePlayer player = players.computeIfAbsent(playerId, k -> new TranslatePlayer(null));
        player.setSelectedLanguage(newLanguage);

        if (playerDataFolder != null) {
            File playerDataFile = new File(playerDataFolder, playerId + ".json");
            // Update file
            try (FileWriter writer = new FileWriter(playerDataFile)) {
                writer.write(EnderTranslate.getGson().toJson(player));
            } catch (IOException e) {
                System.out.println("An error occurred while writing JSON to the file: " + e.getMessage());
                e.printStackTrace();
            }
        }

        PacketEventsHandler handler = PacketEventsHandler.getInstance();
        if (handler == null) return;
        handler.resendEntityMetaPackets(EnderTranslate.getInstance().getUuidToPlayerObjFunction().apply(playerId));
    }

    public String getPlayerLanguage(UUID playerId) {
        TranslatePlayer player = players.get(playerId);
        return player == null || player.getSelectedLanguage() == null ? defaultLanguage : player.getSelectedLanguage();
    }

    public boolean isPlayerLanguageSet(UUID playerId) {
        TranslatePlayer player = players.get(playerId);
        return !(player == null || player.getSelectedLanguage() == null);
    }

}