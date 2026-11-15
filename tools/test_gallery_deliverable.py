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
