package fr.supermax_8.endertranslate.core.translation;

import com.google.gson.Gson;
import fr.supermax_8.endertranslate.core.EnderTranslate;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import fr.supermax_8.endertranslate.core.utils.ResourceUtils;
import lombok.Getter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TranslationManager {

    @Getter
    private static TranslationManager instance;

    @Getter
    private final ConcurrentHashMap<String, Translation> translations = new ConcurrentHashMap<>();
    private File translationFolder;

    public TranslationManager() {
        instance = this;
    }

    public TranslationManager(File translationFolder) {
        instance = this;
        this.translationFolder = translationFolder;
        File enderTranslate = new File(translationFolder, "EnderTranslate.json");
        if (!enderTranslate.exists()) {
            EnderTranslate.log("Create default plugin translation file");
            try {
                writeResourceToFile("EnderTranslate.json", enderTranslate.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        if (!EnderTranslate.isVerified() && translations.size() > 15) {
            EnderTranslate.log("§cYou have exceeded the limit of the FREE version only the first 15 translations will be loaded ! Consider buying the plugin");
            int count = 0;
            HashMap<String, Translation> first15 = new HashMap<>();
            for (Map.Entry<String, Translation> entry : translations.entrySet()) {
                if (count > 15) break;
                first15.put(entry.getKey(), entry.getValue());
                count++;
            }
            translations.clear();
            translations.putAll(first15);
        } else
            EnderTranslate.log(translations.size() + " translation loaded");
    }

    public void writeResourceToFile(String resourcePath, String outputPath) {
        byte[] buffer = new byte[1024];
        int bytesRead;

        // Charger la ressource
        try (InputStream resourceStream = ResourceUtils.getResourceAsStream(resourcePath);
             OutputStream fileOutputStream = new FileOutputStream(outputPath)) {

            // Lire les données de la ressource et les écrire dans le fichier
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load(File file) throws FileNotFoundException {
        TranslationFile translationFile = new Gson().fromJson(new FileReader(file), TranslationFile.class);
        translationFile.entries.forEach((entry) -> {
            // Fill the map with all languages
            HashMap<String, String> translationsMap = new LinkedHashMap<>();
            for (String lId : LanguageManager.getInstance().getLanguages()) translationsMap.put(lId, null);

            translationsMap.putAll(entry.values);

            // Get the translation and add them in the same order as the languages config
            ArrayList<String> translations = new ArrayList<>();
            for (String lId : LanguageManager.getInstance().getLanguages())
                translations.add(translationsMap.get(lId));

            Translation translation = new Translation(translations);
            this.translations.put(entry.id, translation);
        });
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

    public TranslationFile getTranslationFileFromRelativePath(String pageRelativePath) throws FileNotFoundException {
        File f = new File(translationFolder, pageRelativePath);
        if (f.exists())
            return EnderTranslate.getGson().fromJson(new FileReader(f), TranslationFile.class);
        f.getParentFile().mkdirs();
        TranslationFile tf = new TranslationFile(List.of());
        String json = EnderTranslate.getGson().toJson(tf);
        try (FileWriter writer = new FileWriter(f)) {
            writer.write(json);
        } catch (Exception e) {
        }
        return tf;
    }

    public void saveFile(String relativePath, String data) {
        File f = new File(translationFolder, relativePath);
        if (!f.getParentFile().exists()) f.getParentFile().mkdirs();

        try (FileWriter writer = new FileWriter(f)) {
            writer.write(data);
        } catch (Exception e) {
        }
    }

    public void renameFile(String path, String newPath) {
        File f = new File(translationFolder, path);
        f.renameTo(new File(translationFolder, newPath));
    }

    public void moveFile(String fromPath, String toPath) {
        File fromPathFile = new File(translationFolder, fromPath);
        File toPathFile = new File(translationFolder, toPath);
        try {
            Files.move(fromPathFile.toPath(), toPathFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteFile(String relativePath) {
        File f = new File(translationFolder, relativePath);
        f.delete();
    }

    public void createFile(String relativePath) {
        File f = new File(translationFolder, relativePath);
        if (f.getName().endsWith(".json")) {
            try (FileWriter writer = new FileWriter(f)) {
                writer.write(EnderTranslate.getGson().toJson(new TranslationFile(List.of())));
            } catch (Exception e) {
            }
        } else f.mkdirs();
    }

    public static class TranslationFile {
        private final List<TranslationEntry> entries;

        public TranslationFile(List<TranslationEntry> entries) {
            this.entries = entries;
        }


        public static class TranslationEntry {
            private String id;
            private Map<String, String> values;

            public TranslationEntry(String id, Map<String, String> values) {
                this.id = id;
                this.values = values;
            }
        }
    }

}