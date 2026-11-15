import json
import os
from pathlib import Path
import tempfile
import unittest
from mining_catalog import IDS, apply, keybound_how


class MiningCatalogTests(unittest.TestCase):
    def test_instruction_action_letters_become_indexed_key_arguments(self):
        self.assertEqual(keybound_how('Press R. C switches modes; X recalls drops.'),
                         'Press %1$s. %2$s switches modes; %3$s recalls drops.')

    def test_key_replacement_never_alters_names_or_existing_arguments(self):
        text = 'CARVE and CORE DRILL remain names; %1$s is already bound.'
        self.assertEqual(keybound_how(text), text)

    def test_text_refresh_never_deletes_runtime_or_provenance_assets(self):
        # Use a disposable resource tree, never mutate the real source assets during tests.
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            assets = root / 'src/main/resources/assets/specialpickaxes'
            for folder in ('lang', 'models/item', 'textures/item'):
                (assets / folder).mkdir(parents=True)
            for locale in ('en_us', 'es_es'):
                (assets / f'lang/{locale}.json').write_text('{}')
            tags = root / 'src/main/resources/data/minecraft/tags/items'
            tags.mkdir(parents=True)
            originals = {}
            for relative in ('textures/item/seam_ripper_v2.png', 'textures/item/worldbreaker_v2.png',
                             'textures/item/reference_backup.png', 'textures/item/exodium_v2.png.mcmeta',
                             'models/item/reference_backup.json'):
                path = assets / relative
                originals[path] = b'unit fixture: retain exact source bytes'
                path.write_bytes(originals[path])
            previous = Path.cwd()
            try:
                os.chdir(root)
                apply()
            finally:
                os.chdir(previous)
            for path, expected in originals.items():
                self.assertEqual(path.read_bytes(), expected)
            self.assertEqual(json.loads((tags / 'pickaxes.json').read_text())['values'],
                             ['specialpickaxes:' + name for name in IDS])
            self.assertIn('mining.identity.seam_ripper.connected',
                          json.loads((assets / 'lang/es_es.json').read_text()))
