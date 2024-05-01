package fr.supermax_8.endertranslate.core.language;

import fr.supermax_8.endertranslate.core.EnderTranslate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    @Getter
    private static LanguageManager instance;

    private final ArrayList<String> languagesIds;
    private final ConcurrentHashMap<String, Language> languages;

    public LanguageManager(Map<String, Language> languages) {
        this.languages = new ConcurrentHashMap<>(languages);
        languagesIds = new ArrayList<>(languages.keySet());
        EnderTranslate.log("Languages initialized");
        instance = this;
    }

    public int languageIndex(String language) {
        return languagesIds.indexOf(language);
    }

    public Language getLanguage(String language) {
        return languages.get(language);
    }

    public List<String> getLanguages() {
        return languagesIds;
    }

    public ConcurrentHashMap<String, Language> getLanguageMap() {
        return languages;
    }

}