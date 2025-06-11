package fr.supermax_8.endertranslate.core.translation;

import fr.supermax_8.endertranslate.core.EnderTranslateConfig;
import fr.supermax_8.endertranslate.core.language.LanguageManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class Translation {

    private static final LanguageManager languageManager = LanguageManager.getInstance();

    private final ArrayList<TranslationValue> translations = new ArrayList<>();

    public Translation(List<String> translations) {
        EnderTranslateConfig config = EnderTranslateConfig.getInstance();
        this.translations.addAll(
                translations
                        .stream()
                        .map(s ->
                                s == null ? null : new TranslationValue(
                                        s,
                                        s.contains(config.getStartTag()) && s.contains(config.getEndTag())
                                )
                        )
                        .toList()
        );
    }

    public String getTranslation(String language) {
        TranslationValue value = getTranslationValue(language);
        return value == null ? null : value.getValue();
    }

    public TranslationValue getTranslationValue(String language) {
        int index = languageManager.languageIndex(language);
        return index >= translations.size() ? translations.get(0) : translations.get(index);
    }

}