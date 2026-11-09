package com.kingdomcomecombat.hardship;

import com.kingdomcomecombat.text.LocalizedText;

import java.util.Map;

public record HardshipConfig(
        String id,
        String title,
        String description,
        String icon,
        String translatedTitle,
        String translatedDescription,
        Map<String, Translation> translations,
        Map<String, Double> preks
) {
    public HardshipConfig {
        id = id == null ? "" : id;
        title = title == null ? "" : title;
        description = description == null ? "" : description;
        icon = icon == null ? "" : icon;
        translatedTitle = translatedTitle == null ? "" : translatedTitle;
        translatedDescription = translatedDescription == null ? "" : translatedDescription;
        translations = translations == null ? Map.of() : Map.copyOf(translations);
        preks = preks == null ? Map.of() : Map.copyOf(preks);
    }

    public String title(String language) {
        String fallback = !isChinese(language) && !translatedTitle.isBlank() ? translatedTitle : title;
        return LocalizedText.resolve(translations, language, Translation::title, fallback);
    }

    public String description(String language) {
        String fallback = !isChinese(language) && !translatedDescription.isBlank() ? translatedDescription : description;
        return LocalizedText.resolve(translations, language, Translation::description, fallback);
    }

    public boolean hasPrek(String key) { return preks.containsKey(key); }
    public double prek(String key, double fallback) { return preks.getOrDefault(key, fallback); }

    private static boolean isChinese(String language) {
        return LocalizedText.normalize(language).equals("zh_cn") || LocalizedText.normalize(language).equals("zh");
    }

    public record Translation(String title, String description) {
        public Translation {
            title = title == null ? "" : title;
            description = description == null ? "" : description;
        }
    }
}
