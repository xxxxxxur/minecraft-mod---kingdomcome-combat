"""Run localization regression checks without Minecraft or downloaded build dependencies."""
import json
from pathlib import Path
import re
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
RESOURCES = ROOT / "src/main/resources"
DATA = RESOURCES / "data/kingdom_come_combat/kingdom_come_combat"
LANG = RESOURCES / "assets/kingdom_come_combat/lang"


def read(path):
    def unique(pairs):
        result = {}
        for key, value in pairs:
            assert key not in result, f"Duplicate key {key} in {path}"
            result[key] = value
        return result
    return json.loads(path.read_text(), object_pairs_hook=unique)


en, zh = (read(LANG / f"{language}.json") for language in ("en_us", "zh_cn"))
assert en.keys() == zh.keys(), "English and Chinese translation keys differ"
for key, value in en.items():
    assert not re.search(r"[\u4e00-\u9fff]", value), f"Chinese in English text: {key}"
    placeholders = lambda text: re.findall(r"%(?:\d+\$)?[sd]", text)
    assert placeholders(value) == placeholders(zh[key]), f"Placeholder mismatch: {key}"
for source in (ROOT / "src").rglob("*.java"):
    text = source.read_text()
    for key in re.findall(r'Text\.translatable\("((?:message|debug)\.kingdom_come_combat\.[^"]+)"', text):
        assert key in en, f"Missing translation: {key} ({source})"
    assert not re.search(r'Text\.literal\("[^"\n]*[\u4e00-\u9fff]', text), f"Hardcoded Chinese text in {source}"

count = 0
for path in (DATA / "skill_books").glob("*.json"):
    book = read(path)
    for field in ("title", "first_page", "second_page"):
        assert book[field] and not re.search(r"[\u4e00-\u9fff]", book[field]), (path, field)
        assert book["translations"]["zh_cn"][field], (path, field)
    count += 1
for skill in read(DATA / "passive_skills/default.json")["skills"].values():
    for field in ("name", "description"):
        assert not re.search(r"[\u4e00-\u9fff]", skill[field]), (skill["id"] if "id" in skill else skill["name"], field)
        assert skill["translations"]["zh_cn"][field]
for hardship in read(DATA / "hardships/default.json")["debuffs"]:
    for field in ("title", "description"):
        assert not re.search(r"[\u4e00-\u9fff]", hardship[field])
        assert hardship["translations"]["zh_cn"][field]

for path in [DATA / "combos/default.json", *(DATA / "executions").glob("*.json")]:
    data = read(path)
    for move in data.get("combos", data.get("executions", {})).values():
        name = move["display_name"]
        assert isinstance(name, dict) and name.get("en_us") and name.get("zh_cn"), path
        assert not re.search(r"[\u4e00-\u9fff]", name["en_us"]), path

package = ROOT / "src/main/java/com/kingdomcomecombat"
sources = [package / relative for relative in (
    "text/LocalizedText.java", "item/SkillBookTexts.java", "passive/PassiveSkillConfig.java", "hardship/HardshipConfig.java")]
with tempfile.TemporaryDirectory(prefix="kcc-localization-") as output:
    subprocess.run(["javac", "--release", "17", "-encoding", "UTF-8", "-d", output, *map(str, sources), str(ROOT / "tests/LocalizationCheck.java")], check=True)
    subprocess.run(["java", "-cp", output, "LocalizationCheck"], check=True)
print(f"Checked {len(en)} translation keys and {count} skill books, plus passive skills and hardships.")
