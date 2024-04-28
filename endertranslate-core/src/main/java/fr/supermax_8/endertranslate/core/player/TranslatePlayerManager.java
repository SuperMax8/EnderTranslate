package fr.supermax_8.endertranslate.core.player;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TranslatePlayerManager {

    @Getter
    private static TranslatePlayerManager instance;
    private static String defaultLanguage = EnderTranslateConfig.getInstance().getDefaultLanguage();
    private ConcurrentHashMap<UUID, TranslatePlayer> players = new ConcurrentHashMap<>();
    private final File playerDataFolder;

    public TranslatePlayerManager(File playerDataFolder) {
        this.playerDataFolder = playerDataFolder;
        try {
            for (File jsonFile : playerDataFolder.listFiles(f -> f.getName().endsWith(".json"))) {
                TranslatePlayer player = EnderTranslate.getGson().fromJson(new FileReader(jsonFile), TranslatePlayer.class);
                players.put(UUID.fromString(jsonFile.getName().replace(".json", "")), player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        instance = this;
    }

    public void setPlayerLanguage(UUID playerId, String newLanguage) {
        TranslatePlayer player = players.get(playerId);
        File playerDataFile = new File(playerDataFolder, playerId + ".json");
        if (player == null) {
            player = new TranslatePlayer(defaultLanguage);
            players.put(playerId, player);
        }
        player.setSelectedLanguage(newLanguage);

        // Update file
        try (FileWriter writer = new FileWriter(playerDataFile)) {
            writer.write(EnderTranslate.getGson().toJson(player));
        } catch (IOException e) {
            System.out.println("An error occurred while writing JSON to the file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String getPlayerLanguage(UUID playerId) {
        TranslatePlayer player = players.get(playerId);
        return player == null ? defaultLanguage : player.getSelectedLanguage();
    }

}