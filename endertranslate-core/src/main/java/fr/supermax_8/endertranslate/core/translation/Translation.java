package fr.supermax_8.endertranslate.core.translation;

import fr.supermax_8.endertranslate.core.language.LanguageManager;

import java.util.ArrayList;
import java.util.List;

public class Translation {

    private static final LanguageManager languageManager = LanguageManager.getInstance();

    private final ArrayList<String> translations = new ArrayList<>();

    public Translation(List<String> translations) {
        this.translations.addAll(translations);
    }

    public String getTranslation(String language) {
        return translations.get(languageManager.languageIndex(language));
    }

}