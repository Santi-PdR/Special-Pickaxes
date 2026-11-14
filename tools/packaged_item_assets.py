"""Prevent stale cached item models/textures from passing a merely structural JAR check."""
import hashlib
import json
from pathlib import Path, PurePosixPath
from mining_catalog import IDS

ASSETS = Path('src/main/resources/assets/specialpickaxes')
MANIFEST = Path('docs/mining-rework/ASSET-PROVENANCE.json')


def verify_packaged_item_assets(archive, assets=ASSETS, records=None):
    records = json.loads(MANIFEST.read_text()) if records is None else records
    by_id = {record['artifact']: record for record in records}
    if len(by_id) != len(records) or set(by_id) != set(IDS):
        raise ValueError('Packaged asset provenance must cover exactly thirteen picks')
    names = archive.namelist()
    if len(set(names)) != len(names):
        raise ValueError('Duplicate ZIP entries are not allowed in a release JAR')
    verified = []
    for artifact in IDS:
        relative_model = f'models/item/{artifact}.json'
        model_bytes = exact_resource(archive, assets, relative_model)
        model = json.loads(model_bytes)
        texture = model['textures']['layer0']
        if not texture.startswith('specialpickaxes:item/'):
            raise ValueError('Unexpected item texture namespace: ' + artifact)
        resource = texture.split(':', 1)[1]
        if PurePosixPath(resource).parts != ('item', PurePosixPath(resource).name) or '\\' in resource:
            raise ValueError('Unsafe packaged texture path: ' + artifact)
        relative_texture = 'textures/' + resource + '.png'
        texture_bytes = exact_resource(archive, assets, relative_texture)
        record = by_id[artifact]
        expected = record.get('runtime_sha256', record['sha256'])
        if hashlib.sha256(texture_bytes).hexdigest() != expected:
            raise ValueError('Packaged texture provenance mismatch: ' + artifact)
        metadata = relative_texture + '.mcmeta'
        entry = 'assets/specialpickaxes/' + metadata
        if (assets / metadata).is_file():
            exact_resource(archive, assets, metadata)
        elif entry in names:
            raise ValueError('Unexpected animation metadata in release: ' + artifact)
        verified.append(artifact)
    return verified


def exact_resource(archive, assets, relative):
    entry = 'assets/specialpickaxes/' + relative
    try:
        data = archive.read(entry)
    except KeyError as error:
        raise ValueError('Missing packaged item resource: ' + relative) from error
    if data != (assets / relative).read_bytes():
        raise ValueError('Stale or modified packaged item resource: ' + relative)
    return data
