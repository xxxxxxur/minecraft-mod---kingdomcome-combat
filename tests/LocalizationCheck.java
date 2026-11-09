import com.kingdomcomecombat.text.LocalizedText;
import com.kingdomcomecombat.item.SkillBookTexts;
import com.kingdomcomecombat.passive.PassiveSkillConfig;
import com.kingdomcomecombat.hardship.HardshipConfig;
import java.util.Locale;
import java.util.Map;

public final class LocalizationCheck {
    private static void expect(String expected, String actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + ", got " + actual);
    }

    public static void main(String[] args) {
        // Exercise production resolvers under a non-English JVM locale too.
        Locale.setDefault(Locale.forLanguageTag("tr-TR"));
        var book = new SkillBookTexts.Entry("Base", "Author", "Base page", "Learn", "art", Map.of(
                "en_us", new SkillBookTexts.EntryText("English", "English page", "Learn", ""),
                "zh_cn", new SkillBookTexts.EntryText("中文", "中文页面", "学习", ""),
                "fr", new SkillBookTexts.EntryText("Français", "", "", "")));
        for (String language : new String[] {null, "", "de_de", "ja_jp", "zh_tw"}) {
            expect("English", book.title(language));
            expect("English page", book.firstPage(language));
        }
        expect("中文", book.title(" ZH-CN "));
        expect("Français", book.title("fr_CA"));
        expect("English page", book.firstPage("fr_ca"));
        expect("art", book.illustration("de_de"));
        expect("Skill Book", SkillBookTexts.get("missing").title("de_de"));
        var passive = new PassiveSkillConfig("id", "Base", "Base description", "",
                PassiveSkillConfig.Source.BOOK, "", 0, Map.of(
                "en_us", new PassiveSkillConfig.Text("English", "English description"),
                "zh_cn", new PassiveSkillConfig.Text("中文", "")), Map.of());
        expect("English", passive.name("ru_ru"));
        expect("中文", passive.name("ZH-CN"));
        expect("English description", passive.description("zh_cn"));
        var hardship = new HardshipConfig("id", "Base", "Base description", "", "English", "English description",
                Map.of("zh_cn", new HardshipConfig.Translation("中文", "中文说明")), Map.of());
        expect("English", hardship.title(null));
        expect("English", hardship.title("ja_jp"));
        expect("English", hardship.title("zh_tw"));
        expect("中文", hardship.title("ZH-CN"));
        var partial = new HardshipConfig("id", "Base", "Base description", "", "Legacy English", "Legacy description",
                Map.of("en_us", new HardshipConfig.Translation("English", "English description"),
                       "fr", new HardshipConfig.Translation("Français", "")), Map.of());
        expect("English description", partial.description("fr_fr"));
        expect("English", LocalizedText.resolve(Map.of("en", "English"), null, value -> value, "Base"));
        System.out.println("Localization fallback checks passed.");
    }
}
