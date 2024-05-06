package fr.supermax_8.endertranslate.core.language;

import lombok.Data;

import java.util.List;

@Data
public class Language {

    private final String id;
    private final String title;
    private final String material;
    private final List<String> patterns;

    public Language(String id, String title, String material, List<String> patterns) {
        this.id = id;
        this.title = title;
        this.material = material;
        this.patterns = patterns;
    }


    @Override
    public String toString() {
        return "Language{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", patterns=" + patterns +
                '}';
    }
}