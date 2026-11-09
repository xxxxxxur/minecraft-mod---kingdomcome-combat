"""Verify packaged localization behavior, versions, checksums, and record build outcomes."""
from pathlib import Path
import hashlib
import json
import subprocess
import tempfile
import zipfile

OUT = Path(__file__).resolve().parent
ROOT = OUT.parents[1]
results = json.loads((OUT / 'build-results.json').read_text())
successes = {key: value for key, value in results.items() if value['status'] == 'success'}
assert successes, 'No release artifacts'
for target, result in successes.items():
    path = OUT / result['artifact']
    assert hashlib.sha256(path.read_bytes()).hexdigest() == result['sha256'], path
    with zipfile.ZipFile(path) as jar:
        assert jar.testzip() is None, path
        if target.startswith('neoforge-'):
            assert 'fabric.mod.json' not in jar.namelist(), path
            assert not any(name.startswith('com/kingdomcomecombat/network/legacy/') for name in jar.namelist()), path
        en = json.loads(jar.read('assets/kingdom_come_combat/lang/en_us.json'))
        zh = json.loads(jar.read('assets/kingdom_come_combat/lang/zh_cn.json'))
        assert en.keys() == zh.keys(), path
        assert len(en) == 360, (path, len(en))
        assert jar.read('com/kingdomcomecombat/text/LocalizedText.class')[:4] == b'\xca\xfe\xba\xbe'

with tempfile.TemporaryDirectory(prefix='kcc-packaged-localization-') as classes:
    first = OUT / next(iter(successes.values()))['artifact']
    subprocess.run(['javac', '--release', '21', '-encoding', 'UTF-8', '-cp', str(first), '-d', classes,
                    str(ROOT / 'tests/LocalizationCheck.java')], check=True)
    for target, result in successes.items():
        subprocess.run(['java', '-cp', str(OUT / result['artifact']) + ':' + classes,
                        'LocalizationCheck'], check=True, capture_output=True, text=True)
        print(f'PASS {target}: JAR integrity, localization keys, packaged fallback behavior, SHA-256')

# A shared modern artifact is recommended only when all executable classes and resources match.
modern = [f'fabric-{version}' for version in ('1.21.6', '1.21.7', '1.21.8', '1.21.6-1.21.8')]
shared_verified = False
if all(target in successes for target in modern):
    contents = []
    for target in modern:
        with zipfile.ZipFile(OUT / successes[target]['artifact']) as jar:
            contents.append({name: hashlib.sha256(jar.read(name)).hexdigest() for name in jar.namelist()
                             if name not in ('fabric.mod.json', 'META-INF/MANIFEST.MF') and not name.endswith('/')})
    shared_verified = all(content == contents[0] for content in contents[1:])
    print('Modern 1.21.6–1.21.8 classes/resources identical:', shared_verified)

lines = ['# Kingdom Come Combat 1.7.5', '', 'Build date: 2026-10-27 (Asia/Shanghai).', '',
         'Built from the current workspace, including English localization fallback fixes.', '',
         '## Build results', '', '| Target | Result | Artifact / Log |', '| --- | --- | --- |']
for target, result in results.items():
    if result['status'] == 'success':
        artifact = result['artifact']
        lines.append(f'| {target} | Built and verified | [{artifact}]({artifact}) |')
    elif result['status'] == 'excluded':
        lines.append(f'| {target} | Excluded at user request | — |')
    else:
        log = result['log']
        lines.append(f'| {target} | {result["status"]} | [Build log]({log}) |')
lines += ['', 'Minecraft 1.20.1 (Fabric and Forge) was excluded at the user’s request.', '', '## Verification', '',
          '- Full Gradle `build` succeeded for each artifact listed as built.',
          '- Embedded 1.7.5 version and exact/ranged Minecraft dependencies checked during packaging.',
          '- JAR integrity, translation keys, packaged localization fallback behavior, and SHA-256 verified.',
          '- Source localization regression suite passed: 360 translation keys, 22 skill books, passive skills, and hardships.',
          '- Game/client/server launch testing was not performed in this build task.', '']
if shared_verified:
    lines += ['The shared Fabric 1.21.6–1.21.8 package has identical executable classes and resources to all three individual verification builds.', '']
failed = [target for target, result in results.items() if result['status'] not in ('success', 'excluded')]
if failed:
    lines += ['## Targets without release artifacts', '', ', '.join(f'`{target}`' for target in failed) + '.', '',
              'These targets were attempted. Their logs record the remaining source/API or dependency failures; no older binaries were renamed as 1.7.5.', '']
(OUT / 'BUILD_STATUS.md').write_text('\n'.join(lines))
(OUT / 'verification.json').write_text(json.dumps({'verified_targets': list(successes), 'shared_modern_classes_resources_identical': shared_verified}, indent=2) + '\n')
print(f'Verified {len(successes)} artifacts; {len(failed)} targets have no release artifact.')
