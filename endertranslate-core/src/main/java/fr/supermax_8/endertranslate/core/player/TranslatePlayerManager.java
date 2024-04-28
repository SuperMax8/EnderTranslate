package fr.supermax_8.endertranslate.core.player;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import lombok.Getter;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.UUID;

public class TranslatePlayerManager {

    @Getter
    private static TranslatePlayerManager instance;
    private HashMap<UUID, TranslatePlayer> players = new HashMap<>();


    public TranslatePlayerManager(File playerDataFolder) {
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

}
