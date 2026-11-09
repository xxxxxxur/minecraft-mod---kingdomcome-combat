package com.kingdomcomecombat.item;

import com.kingdomcomecombat.text.LocalizedText;

import java.util.HashMap;
import java.util.Map;

public final class SkillBookTexts {
    private static final Map<String, Entry> BY_ID = new HashMap<>();

    private SkillBookTexts() {
    }

    public static void clear() {
        BY_ID.clear();
    }

    public static void put(String id, Entry entry) {
        if (id == null || id.isBlank()) {
            return;
        }

        BY_ID.put(id, entry);
    }

    public static boolean contains(String id) {
        return BY_ID.containsKey(id);
    }

    public static Entry get(String id) {
        Entry entry = BY_ID.get(id);
        if (entry != null) {
            return entry;
        }

        return new Entry(
                "Skill Book",
                "Kingdom Come Combat",
                "This book records a combat technique.",
                "Turn to this page to learn the technique recorded in this book.",
                "",
                Map.of()
        );
    }

    public static Map<String, Entry> all() {
        return Map.copyOf(BY_ID);
    }

    public record Entry(
            String title,
            String author,
            String firstPage,
            String secondPage,
            String illustration,
            Map<String, EntryText> translations
    ) {
        public Entry(String title, String author, String firstPage, String secondPage, String illustration) {
            this(title, author, firstPage, secondPage, illustration, Map.of());
        }

        public Entry {
            title = title == null || title.isBlank() ? "Skill Book" : title;
            author = author == null || author.isBlank() ? "Kingdom Come Combat" : author;
            firstPage = firstPage == null || firstPage.isBlank()
                    ? "This book records a combat technique."
                    : firstPage;
            secondPage = secondPage == null || secondPage.isBlank()
                    ? "Turn to this page to learn the technique recorded in this book."
                    : secondPage;
            illustration = illustration == null ? "" : illustration;
            translations = translations == null ? Map.of() : Map.copyOf(translations);
        }

        public String title(String language) {
            return LocalizedText.resolve(translations, language, EntryText::title, title);
        }

        public String firstPage(String language) {
            return LocalizedText.resolve(translations, language, EntryText::firstPage, firstPage);
        }

        public String secondPage(String language) {
            return LocalizedText.resolve(translations, language, EntryText::secondPage, secondPage);
        }

        public String illustration(String language) {
            return LocalizedText.resolve(translations, language, EntryText::illustration, illustration);
        }
    }

    public record EntryText(
            String title,
            String firstPage,
            String secondPage,
            String illustration
    ) {
        public EntryText {
            title = title == null ? "" : title;
            firstPage = firstPage == null ? "" : firstPage;
            secondPage = secondPage == null ? "" : secondPage;
            illustration = illustration == null ? "" : illustration;
        }
    }
}
