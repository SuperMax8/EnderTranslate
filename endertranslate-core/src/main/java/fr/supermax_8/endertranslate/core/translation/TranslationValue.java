package fr.supermax_8.endertranslate.core.translation;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TranslationValue {

    private final String value;
    private final boolean containsLangPlaceholder;

}
