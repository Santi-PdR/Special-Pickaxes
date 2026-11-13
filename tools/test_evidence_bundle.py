import pathlib
import random
import tempfile
import unittest

from evidence_archive import extract_verified
from evidence_bundle import EXPORT_REPORT, make_bundle


class EvidenceBundleTests(unittest.TestCase):
    def test_small_bundle_keeps_every_file_and_verifies(self):
        contents = {'build/client.log': b'clean', 'build/gallery.jpg': b'image'}
        data, manifest, report = make_bundle(contents)
        self.assertEqual(report['omitted_images'], [])
        with tempfile.TemporaryDirectory() as directory:
            self.assertEqual(extract_verified(data, directory), manifest)
            self.assertEqual((pathlib.Path(directory) / 'build/client.log').read_bytes(), b'clean')

    def test_images_are_omitted_explicitly_without_losing_logs(self):
        contents = {'build/client.log': b'FULL LOG\n' * 300,
                    'build/client-smoke/gallery-clean.jpg': random.Random(1).randbytes(2000),
                    'build/client-smoke/other.jpg': random.Random(2).randbytes(5000)}
        data, manifest, report = make_bundle(contents, max_bytes=4000)
        self.assertLessEqual(len(data), 4000)
        self.assertIn('build/client-smoke/other.jpg', report['omitted_images'])
        names = {entry['path'] for entry in manifest}
        self.assertIn('build/client-smoke/gallery-clean.jpg', names)
        self.assertIn(EXPORT_REPORT, names)
        with tempfile.TemporaryDirectory() as directory:
            extract_verified(data, directory)
            self.assertEqual((pathlib.Path(directory) / 'build/client.log').read_bytes(), contents['build/client.log'])

    def test_oversize_text_is_rejected_never_truncated(self):
        with self.assertRaises(ValueError):
            make_bundle({'build/client.log': random.Random(4).randbytes(5000)}, max_bytes=1000)

    def test_identical_input_is_reproducible_independent_of_dict_order(self):
        contents = {'build/b.log': b'b', 'build/a.log': b'a'}
        first, _, _ = make_bundle(contents)
        second, _, _ = make_bundle(dict(reversed(list(contents.items()))))
        self.assertEqual(first, second)

    def test_default_budget_fits_at_most_56_check_chunks(self):
        import base64
        import math
        from evidence_bundle import MAX_EXPORT_BYTES
        self.assertLessEqual(math.ceil(4 * math.ceil(MAX_EXPORT_BYTES / 3) / 48000), 56)


if __name__ == '__main__':
    unittest.main()
