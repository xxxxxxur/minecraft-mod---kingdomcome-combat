"""Build the declared Minecraft matrix sequentially to protect shared generated sources."""
from pathlib import Path
import datetime
import hashlib
import json
import os
import shutil
import subprocess
import sys
import time
import zipfile

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
VERSION = '1.7.5'
JDK21 = '/usr/local/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home'
JDK25 = '/usr/local/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home'
results_file = OUT / 'build-results.json'
results = json.loads(results_file.read_text()) if results_file.exists() else {}


def save():
    results_file.write_text(json.dumps(results, indent=2, ensure_ascii=False) + '\n')
    jars = sorted(OUT.glob('*.jar'))
    (OUT / 'SHA256SUMS').write_text(''.join(f'{hashlib.sha256(p.read_bytes()).hexdigest()}  {p.name}\n' for p in jars))


def build(loader, minecraft, module=None, ranged=False):
    key = f'{loader}-{minecraft}'
    if results.get(key, {}).get('status') in ('success', 'failed', 'excluded'):
        print(f'SKIP {key}: {results[key]["status"]}', flush=True)
        return
    command = [str(ROOT / 'gradlew')]
    if module:
        command += ['-p', module]
    elif not ranged:
        command += [f'-Ptarget_mc={minecraft}']
    command += ['build', '--console=plain', '--max-workers=4',
                '-Dorg.gradle.internal.http.connectionTimeout=15000',
                '-Dorg.gradle.internal.http.socketTimeout=15000']
    env = os.environ.copy()
    env['JAVA_HOME'] = JDK25 if minecraft == '26.2' else JDK21
    log = OUT / 'logs' / f'{key}.log'
    print(f'START {key}', flush=True)
    started = time.time()
    results[key] = {'status': 'running', 'command': command, 'log': str(log.relative_to(OUT))}
    save()
    with log.open('w') as stream:
        completed = subprocess.run(command, cwd=ROOT, env=env, stdout=stream, stderr=subprocess.STDOUT)
    result = results[key]
    result['exit_code'] = completed.returncode
    result['duration_seconds'] = round(time.time() - started, 1)
    result['status'] = 'failed' if completed.returncode else 'success'
    if completed.returncode == 0:
        folder = ROOT / (module or '') / 'build/libs'
        if loader == 'fabric':
            filename = f'kingdom_come_combat-{VERSION}.jar' if minecraft != '26.2' else f'kingdom_come_combat-26.2-{VERSION}.jar'
        elif loader == 'neoforge':
            filename = f'kingdom_come_combat-neoforge-{VERSION}.jar'
        else:
            filename = f'kingdom_come_combat-forge-1.20.1-{VERSION}.jar'
        artifact = folder / filename
        with zipfile.ZipFile(artifact) as jar:
            assert jar.testzip() is None, f'Corrupt JAR: {artifact}'
            if loader == 'fabric':
                metadata = json.loads(jar.read('fabric.mod.json'))
                assert metadata['version'] == VERSION, metadata
                expected = '>=1.21.6 <=1.21.8' if ranged else f'={minecraft}'
                assert metadata['depends']['minecraft'] == expected, metadata['depends']
            else:
                path = 'META-INF/neoforge.mods.toml' if loader == 'neoforge' else 'META-INF/mods.toml'
                assert f'version="{VERSION}"' in jar.read(path).decode()
            for path in ['assets/kingdom_come_combat/lang/en_us.json', 'assets/kingdom_come_combat/lang/zh_cn.json']:
                translations = json.loads(jar.read(path))
                assert 'message.kingdom_come_combat.learned_combo' in translations, path
            assert 'com/kingdomcomecombat/text/LocalizedText.class' in jar.namelist()
        name = f'kingdom_come_combat-{VERSION}+{loader}-mc{minecraft}.jar'
        shutil.copy2(artifact, OUT / name)
        result['artifact'] = name
        result['sha256'] = hashlib.sha256((OUT / name).read_bytes()).hexdigest()
    save()
    print(f'{result["status"].upper()} {key} ({result["duration_seconds"]}s)', flush=True)
    if completed.returncode:
        lines = log.read_text(errors='replace').splitlines()
        print('\n'.join(lines[-20:]), flush=True)


if __name__ == '__main__':
    for version in ['1.21.7', '1.21', '1.21.1', '1.21.6', '1.21.8', '1.21.10', '1.21.11',
                    '1.21.2', '1.21.3', '1.21.4', '1.21.5', '1.21.9']:
        build('fabric', version)
    build('fabric', '1.21.6-1.21.8', ranged=True)
    build('neoforge', '1.21.1', module='neoforge')
    build('fabric', '26.2', module='versions/26.2')
    print('FINISHED', flush=True)
