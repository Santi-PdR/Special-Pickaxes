"""Bind a photo gallery to one CI release, verified portraits and exact runtime assets.
Only local git object reads: never checks out another branch or compiles the mod.
"""
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import subprocess
from mining_catalog import IDS

ASSETS = Path('src/main/resources/assets/specialpickaxes')


def digest(data):
    return hashlib.sha256(data).hexdigest()


def git_blob(commit, path):
    return subprocess.check_output(['git', 'show', f'{commit}:{path.as_posix()}'])


def validate_gallery(captures, release_path=None, read_at_commit=git_blob, assets=ASSETS):
    captures = Path(captures)
    release_path = Path(release_path) if release_path else captures.parent / 'RELEASE.json'
    release = json.loads(release_path.read_text())
    commit = release.get('commit', '')
    if not re.fullmatch(r'[0-9a-f]{40}', commit):
        raise ValueError('Gallery requires a full source commit in RELEASE.json')
    if set(release.get('artifacts', [])) != set(IDS):
        raise ValueError('Gallery roster does not match the release')
    if not re.fullmatch(r'[0-9]+', str(release.get('run_id', ''))):
        raise ValueError('Gallery requires the CI run identity')
    # fetch_ci_evidence preserves this verified archive manifest on extraction.
    evidence_root = captures.parent.parent
    manifest = json.loads((evidence_root / 'manifest.json').read_text())
    entries = {entry['path']: entry for entry in manifest}
    if len(entries) != len(manifest):
        raise ValueError('Duplicate gallery evidence entries')
    release_relative = release_path.resolve().relative_to(evidence_root.resolve()).as_posix()
    verify_evidence(release_path, entries.get(release_relative))
    output = {'commit': commit, 'run_id': str(release['run_id']), 'release_file': release['file'], 'items': []}
    for name in IDS:
        model_path = assets / 'models/item' / (name + '.json')
        model_bytes = model_path.read_bytes()
        if model_bytes != read_at_commit(commit, model_path):
            raise ValueError('Local model does not match captured release: ' + name)
        texture = json.loads(model_bytes)['textures']['layer0']
        if not texture.startswith('specialpickaxes:item/'):
            raise ValueError('Unexpected runtime texture namespace: ' + name)
        texture_part = texture.split(':', 1)[1]
        if PurePosixPath(texture_part).parts != ('item', PurePosixPath(texture_part).name) or '\\' in texture_part:
            raise ValueError('Unsafe runtime texture path: ' + name)
        texture_path = assets / 'textures' / (texture_part + '.png')
        texture_bytes = texture_path.read_bytes()
        if texture_bytes != read_at_commit(commit, texture_path):
            raise ValueError('Local icon does not match captured release: ' + name)
        portrait = captures / ('portrait-' + name + '.jpg')
        relative = portrait.resolve().relative_to(evidence_root.resolve()).as_posix()
        portrait_hash = verify_evidence(portrait, entries.get(relative))
        output['items'].append({'artifact': name, 'model_sha256': digest(model_bytes),
                                'texture': texture, 'texture_sha256': digest(texture_bytes),
                                'portrait_sha256': portrait_hash})
    return output


def verify_evidence(path, entry):
    if not isinstance(entry, dict):
        raise ValueError('Missing verified gallery evidence: ' + str(path))
    data = path.read_bytes()
    if entry.get('bytes') != len(data) or entry.get('sha256') != digest(data):
        raise ValueError('Modified gallery evidence: ' + str(path))
    return digest(data)
