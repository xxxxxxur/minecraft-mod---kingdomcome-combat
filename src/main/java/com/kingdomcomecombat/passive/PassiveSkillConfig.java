package com.kingdomcomecombat.passive;

import com.kingdomcomecombat.text.LocalizedText;

import java.util.Map;

public record PassiveSkillConfig(
        String id,
        String name,
        String description,
        String icon,
        Source source,
        String advancement,
        int experienceCost,
        Map<String, Text> translations,
        Map<String, Double> perks
) {
    public enum Source {
        BOOK,
        ADVANCEMENT,
        EXPERIENCE
    }

    public PassiveSkillConfig(
            String id,
            String name,
            String description,
            String icon,
            Source source,
            String advancement,
            int experienceCost
    ) {
        this(id, name, description, icon, source, advancement, experienceCost, Map.of(), Map.of());
    }

    public PassiveSkillConfig {
        id = id == null ? "" : id;
        name = name == null || name.isBlank() ? id : name;
        description = description == null ? "" : description;
        icon = icon == null ? "" : icon;
        source = source == null ? Source.BOOK : source;
        advancement = advancement == null ? "" : advancement;
        experienceCost = Math.max(0, experienceCost);
        translations = translations == null ? Map.of() : Map.copyOf(translations);
        perks = perks == null ? Map.of() : Map.copyOf(perks);
    }

    public String name(String language) {
        return LocalizedText.resolve(translations, language, Text::name, name);
    }

    public String description(String language) {
        return LocalizedText.resolve(translations, language, Text::description, description);
    }

    public double perk(String key, double fallback) {
        return perks.getOrDefault(key, fallback);
    }

    public boolean hasPerk(String key) {
        return perks.containsKey(key);
    }

    public record Text(String name, String description) {
        public Text {
            name = name == null ? "" : name;
            description = description == null ? "" : description;
        }
    }
}
