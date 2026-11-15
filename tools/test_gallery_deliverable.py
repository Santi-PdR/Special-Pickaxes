import pathlib
import tempfile
import unittest
from unittest.mock import patch
from gallery_deliverable import make_deliverable, version_from_release


class GalleryDeliverableTests(unittest.TestCase):
    def test_version_is_taken_only_from_a_canonical_jar_name(self):
        self.assertEqual(version_from_release('special-pickaxes-1.20.1-5.10.442.jar'), '5.10.442')
        for name in ('../../5.10.442.jar', 'special-pickaxes-1.20.1-5.10.442.jar.exe',
                     'special-pickaxes-1.20.1-latest.jar', 'special-pickaxes-1.21-5.10.442.jar'):
            with self.subTest(name=name), self.assertRaises(ValueError):
                version_from_release(name)

    def test_invalid_evidence_creates_no_deliverable(self):
        with tempfile.TemporaryDirectory() as root:
            output = pathlib.Path(root) / 'output'
            with patch('gallery_deliverable.validate_gallery', side_effect=ValueError('unverified')):
                with self.assertRaisesRegex(ValueError, 'unverified'):
                    make_deliverable(pathlib.Path(root) / 'captures', output)
            self.assertFalse(output.exists())

    def test_invalid_version_creates_no_deliverable(self):
        with tempfile.TemporaryDirectory() as root:
            output = pathlib.Path(root) / 'output'
            with patch('gallery_deliverable.validate_gallery', return_value={'release_file': 'latest.jar'}):
                with self.assertRaisesRegex(ValueError, 'filename'):
                    make_deliverable(pathlib.Path(root) / 'captures', output)
            self.assertFalse(output.exists())

    def test_rendered_export_has_thirteen_pages_and_twenty_six_exact_images(self):
        # Synthetic unit pixels are temporary fixtures, never presented as Minecraft evidence.
        try:
            from PIL import Image
        except ImportError:
            self.skipTest('Pillow is required only by the optional PDF renderer')
        import hashlib
        import io
        import json
        import zipfile
        from mining_catalog import IDS
        with tempfile.TemporaryDirectory() as root:
            root = pathlib.Path(root)
            assets, captures, output = root / 'assets', root / 'captures', root / 'output'
            (assets / 'textures/item').mkdir(parents=True)
            captures.mkdir()
            icon_buffer, portrait_buffer = io.BytesIO(), io.BytesIO()
            Image.new('RGBA', (32, 32), (40, 180, 220, 180)).save(icon_buffer, 'PNG')
            Image.new('RGB', (128, 72), '#142537').save(portrait_buffer, 'JPEG')
            icon, portrait = icon_buffer.getvalue(), portrait_buffer.getvalue()
            items = []
            for name in IDS:
                (assets / f'textures/item/{name}.png').write_bytes(icon)
                (captures / f'portrait-{name}.jpg').write_bytes(portrait)
                items.append({'artifact': name, 'texture': 'specialpickaxes:item/' + name})
            provenance = {'release_file': 'special-pickaxes-1.20.1-5.10.443.jar',
                          'commit': 'a' * 40, 'run_id': '123', 'items': items}
            with patch('gallery_deliverable.ASSETS', assets), patch('gallery_deliverable.validate_gallery', return_value=provenance):
                result = make_deliverable(captures, output)
                with self.assertRaisesRegex(ValueError, 'mix'):
                    make_deliverable(captures, output)
            self.assertEqual((result['pages'], result['images']), (13, 26))
            self.assertTrue(pathlib.Path(result['pdf']).read_bytes().startswith(b'%PDF'))
            self.assertIn(b'/Count 13', pathlib.Path(result['pdf']).read_bytes())
            with zipfile.ZipFile(result['zip']) as archive:
                self.assertIsNone(archive.testzip())
                self.assertEqual(sum(name.startswith(('icono-', 'en-mano-')) for name in archive.namelist()), 26)
                for name in IDS:
                    self.assertEqual(archive.read(f'icono-{name}.png'), icon)
                    self.assertEqual(archive.read(f'en-mano-{name}.jpg'), portrait)
                for line in archive.read('SHA256SUMS').decode().splitlines():
                    digest, name = line.split('  ', 1)
                    self.assertEqual(hashlib.sha256(archive.read(name)).hexdigest(), digest)
                self.assertEqual(json.loads(archive.read('PROVENANCE.json'))['commit'], 'a' * 40)
