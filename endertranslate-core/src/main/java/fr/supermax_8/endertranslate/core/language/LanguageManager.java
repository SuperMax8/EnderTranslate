package fr.supermax_8.endertranslate.core.language;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    @Getter
    private static LanguageManager instance;

    private final ArrayList<String> languagesIds = new ArrayList<>();
    private final ConcurrentHashMap<String, Language> languages = new ConcurrentHashMap<>();

    public LanguageManager(Map<String, Language> languages) {
        languages.forEach((id, language) -> {
            languages.put(id, language);
            languagesIds.add(id);
        });
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

}