package fr.supermax_8.endertranslate.core.translation;

import com.google.gson.Gson;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationManager {

    @Getter
    private static TranslationManager instance;

    private final ConcurrentHashMap<String, Translation> translations = new ConcurrentHashMap<>();

    public TranslationManager(File translationFolder) {
        instance = this;
        try {
            Files.find(translationFolder.toPath(), Integer.MAX_VALUE, (path, att) -> path.toFile().getName().endsWith(".json")).forEach(path -> {
                try {
                    load(path.toFile());
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
        }
    }

    private void load(File file) throws FileNotFoundException {
        TranslationFile translationFile = new Gson().fromJson(new FileReader(file), TranslationFile.class);
        translationFile.translationEntries.forEach((translationId, entry) -> {
            // Fill the map with all languages
            HashMap<String, String> translationsMap = new LinkedHashMap<>();
            for (String lId : LanguageManager.getInstance().getLanguages()) translationsMap.put(lId, null);

            translationsMap.putAll(entry.translations);

            // Get the translation and add them in the same order as the languages config
            ArrayList<String> translations = new ArrayList<>();
            for (String lId : LanguageManager.getInstance().getLanguages())
                translations.add(translationsMap.get(lId));

            Translation translation = new Translation(translations);
            this.translations.put(translationId, translation);
        });
    }

    private static class TranslationFile {
        LinkedHashMap<String, TranslationEntry> translationEntries;


        private static class TranslationEntry {
            LinkedHashMap<String, String> translations;
        }
    }

}