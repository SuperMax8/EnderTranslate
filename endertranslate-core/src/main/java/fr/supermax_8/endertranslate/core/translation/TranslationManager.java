package fr.supermax_8.endertranslate.core.translation;

import com.google.gson.Gson;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import lombok.Getter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationManager {

    @Getter
    private static TranslationManager instance;

    private final ConcurrentHashMap<String, Translation> translations = new ConcurrentHashMap<>();
    private final File translationFolder;

    public TranslationManager(File translationFolder) {
        this.translationFolder = translationFolder;
        instance = this;
        EnderTranslate.log("Loading translations...");
        try {
            Files.walk(translationFolder.toPath()).filter(p -> p.toFile().getName().endsWith(".json")).forEach(path -> {
                try {
                    load(path.toFile());
                    EnderTranslate.log("Loading translation file: " + path.toFile().getName());
                } catch (Exception e) {
                    EnderTranslate.log("§cCan't load file " + path);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        EnderTranslate.log(translations.size() + " translation loaded");
        getAllFilesPaths();
    }

    public Translation getTranslation(String placeholder) {
        return translations.get(placeholder);
    }

    public List<String> getAllFilesPaths() {
        List<String> paths = new ArrayList<>();
        try {
            Files.walk(translationFolder.toPath()).filter(p -> p.toFile().getName().endsWith(".json") || p.toFile().isDirectory() && !p.equals(translationFolder.toPath())).forEach(path -> {
                String absolutePath = path.toFile().getAbsolutePath();
                try {
                    String cleanPath = absolutePath.substring(absolutePath.indexOf("translations") + "translations/".length());
                    paths.add(cleanPath);
                } catch (Exception e) {
                    System.out.println(absolutePath);
                    System.out.println(absolutePath.indexOf("translations"));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }

    private void load(File file) throws FileNotFoundException {
        TranslationFile translationFile = new Gson().fromJson(new FileReader(file), TranslationFile.class);
        translationFile.entries.forEach((translationId, entry) -> {
            // Fill the map with all languages
            HashMap<String, String> translationsMap = new LinkedHashMap<>();
            for (String lId : LanguageManager.getInstance().getLanguages()) translationsMap.put(lId, null);

            translationsMap.putAll(entry);

            // Get the translation and add them in the same order as the languages config
            ArrayList<String> translations = new ArrayList<>();
            for (String lId : LanguageManager.getInstance().getLanguages())
                translations.add(translationsMap.get(lId));

            Translation translation = new Translation(translations);
            this.translations.put(translationId, translation);
        });
    }

    public static class TranslationFile {
        LinkedHashMap<String, LinkedHashMap<String, String>> entries;

        public TranslationFile(LinkedHashMap<String, LinkedHashMap<String, String>> entries) {
            this.entries = entries;
        }
    }

}