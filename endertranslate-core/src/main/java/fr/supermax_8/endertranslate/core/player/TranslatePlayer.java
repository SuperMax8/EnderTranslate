package fr.supermax_8.endertranslate.core.player;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TranslatePlayer {

    private String selectedLanguage;

    public TranslatePlayer(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

}