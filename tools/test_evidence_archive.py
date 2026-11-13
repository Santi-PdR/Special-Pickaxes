import hashlib
import io
import json
import pathlib
import tempfile
import unittest
import zipfile
from unittest.mock import patch

import evidence_archive


def make_archive(files, manifest=None):
    if manifest is None:
        manifest = [{'path': name, 'bytes': len(data), 'sha256': hashlib.sha256(data).hexdigest()}
                    for name, data in files.items()]
    output = io.BytesIO()
    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
        for name, data in files.items():
            archive.writestr(name, data)
        archive.writestr('manifest.json', json.dumps(manifest))
    return output.getvalue()


class EvidenceArchiveValidation(unittest.TestCase):
    def test_valid_archive_extracts_verified_files(self):
        data = make_archive({'build/RELEASE.json': b'{}', 'build/client.log': b'clean'})
        with tempfile.TemporaryDirectory() as directory:
            manifest = evidence_archive.extract_verified(data, directory)
            self.assertEqual(len(manifest), 2)
            self.assertEqual((pathlib.Path(directory) / 'build/client.log').read_bytes(), b'clean')

    def test_unsafe_paths_are_rejected(self):
        for path in ['../secret', '/absolute', 'build/../../secret', 'C:\\secret', 'build\\secret', './ambiguous', '']:
            with self.subTest(path=path), self.assertRaises(ValueError):
                evidence_archive.safe_name(path)

    def test_checksum_failure_happens_before_any_file_is_written(self):
        data = make_archive({'build/log': b'bad'}, [{'path': 'build/log', 'bytes': 3, 'sha256': '0' * 64}])
        with tempfile.TemporaryDirectory() as directory:
            with self.assertRaises(ValueError):
                evidence_archive.extract_verified(data, directory)
            self.assertEqual(list(pathlib.Path(directory).iterdir()), [])

    def test_wrong_size_is_rejected(self):
        data = make_archive({'log': b'ok'}, [{'path': 'log', 'bytes': 100, 'sha256': hashlib.sha256(b'ok').hexdigest()}])
        with tempfile.TemporaryDirectory() as directory, self.assertRaises(ValueError):
            evidence_archive.extract_verified(data, directory)

    def test_unlisted_files_are_rejected(self):
        data = make_archive({'log': b'ok'}, [])
        with tempfile.TemporaryDirectory() as directory, self.assertRaises(ValueError):
            evidence_archive.extract_verified(data, directory)

    def test_duplicate_manifest_entries_are_rejected(self):
        entry = {'path': 'log', 'bytes': 2, 'sha256': hashlib.sha256(b'ok').hexdigest()}
        with tempfile.TemporaryDirectory() as directory, self.assertRaises(ValueError):
            evidence_archive.extract_verified(make_archive({'log': b'ok'}, [entry, entry]), directory)

    def test_unpacked_size_budget_is_enforced(self):
        data = make_archive({'log': b'x' * 100})
        with tempfile.TemporaryDirectory() as directory, patch.object(evidence_archive, 'MAX_UNPACKED_BYTES', 10):
            with self.assertRaises(ValueError):
                evidence_archive.extract_verified(data, directory)

    def test_existing_symlink_cannot_redirect_extraction(self):
        with tempfile.TemporaryDirectory() as directory:
            root = pathlib.Path(directory) / 'root'
            root.mkdir()
            (root / 'build').symlink_to(pathlib.Path(directory), target_is_directory=True)
            with self.assertRaises(ValueError):
                evidence_archive.extract_verified(make_archive({'build/log': b'ok'}), root)


if __name__ == '__main__':
    unittest.main()
