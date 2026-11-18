#!/usr/bin/env python3
"""Compile and execute actual pure-Java manual models, without Forge or Maven.
Requires JDK 17+. This validates models only, not rendering or the complete mod.
"""
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent.parent
SOURCES = [
    'src/main/java/io/github/santipdr/specialpickaxes/artifact/ManualChoiceNavigation.java',
    'src/main/java/io/github/santipdr/specialpickaxes/artifact/ManualScreenLayout.java',
    'src/main/java/io/github/santipdr/specialpickaxes/artifact/ManualScrollModel.java',
    'src/main/java/io/github/santipdr/specialpickaxes/artifact/ManualWheelScroll.java',
    'tools/ManualModelsSmoke.java',
]


def main():
    output = ROOT / 'build/manual-models'
    output.mkdir(parents=True, exist_ok=True)
    report = output / 'report.json'
    report.unlink(missing_ok=True)  # A stale success cannot survive a failed compilation.
    classes = output / 'classes'
    classes.mkdir(exist_ok=True)
    subprocess.run(['javac', '--release', '17', '-encoding', 'UTF-8', '-d', str(classes),
                    *[str(ROOT / source) for source in SOURCES]], check=True, cwd=ROOT)
    result = subprocess.run(['java', '-cp', str(classes), 'ManualModelsSmoke'],
                            check=False, capture_output=True, text=True, cwd=ROOT)
    print(result.stdout, end='')
    if result.stderr:
        print(result.stderr, end='', file=sys.stderr)
    result.check_returncode()
    marker = 'MANUAL_MODELS_OK checks='
    lines = [line for line in result.stdout.splitlines() if line.startswith(marker)]
    if len(lines) != 1 or not lines[0].endswith(' scope=pure_java_only'):
        raise RuntimeError('Missing exact pure-model completion marker')
    checks = int(lines[0].split('checks=', 1)[1].split()[0])
    if checks < 10000:
        raise RuntimeError('Manual model coverage unexpectedly incomplete')
    report.write_text(json.dumps({
        'scope': 'Pure production Java models only; not rendering, Forge, JUnit or GameTests.',
        'commit': os.environ.get('GITHUB_SHA') or subprocess.check_output(
            ['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
        'checks': checks,
        'sources': {source: hashlib.sha256((ROOT / source).read_bytes()).hexdigest() for source in SOURCES},
    }, indent=2) + '\n')


if __name__ == '__main__':
    main()
