package com.kingdomcomecombat.text;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/** Resolves each field separately so incomplete translations also fall back to English. */
public final class LocalizedText {
    private LocalizedText() {}

    public static String normalize(String language) {
        return language == null ? "" : language.trim().toLowerCase(Locale.ROOT).replace('-', '_');
    }

    public static <T> String resolve(Map<String, T> translations, String language,
                                     Function<T, String> field, String fallback) {
        String normalized = normalize(language);
        int separator = normalized.indexOf('_');
        String primary = separator > 0 ? normalized.substring(0, separator) : normalized;
        for (String candidate : new String[] {normalized, primary, "en_us", "en"}) {
            T entry = translations.get(candidate);
            String value = entry == null ? "" : field.apply(entry);
            if (value != null && !value.isBlank()) return value;
        }
        return fallback;
    }
}
