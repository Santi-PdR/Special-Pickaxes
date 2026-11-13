"""Bounded Checks export. Full images remain in the Actions artifact, not silently discarded."""
import hashlib
import io
import json
import pathlib
import zipfile

MAX_EXPORT_BYTES = 2_000_000
EXPORT_REPORT = 'build/evidence-export.json'
PRIORITY_IMAGES = {'gallery-clean.jpg', 'inventory.jpg', 'supreme-executing.jpg', 'supreme-complete.jpg'}


def make_bundle(contents, max_bytes=MAX_EXPORT_BYTES):
    files = dict(contents)
    omitted = []
    while True:
        report = {'scope': 'bounded_checks_subset', 'full_captures': 'forge-validation Actions artifact',
                  'max_compressed_bytes': max_bytes, 'omitted_images': sorted(omitted)}
        files[EXPORT_REPORT] = (json.dumps(report, indent=2) + '\n').encode()
        manifest = [{'path': name, 'bytes': len(data), 'sha256': hashlib.sha256(data).hexdigest()}
                    for name, data in sorted(files.items())]
        output = io.BytesIO()
        with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
            for name, data in sorted(files.items()):
                archive.writestr(zipfile.ZipInfo(name), data, compress_type=zipfile.ZIP_DEFLATED)
            archive.writestr(zipfile.ZipInfo('manifest.json'), json.dumps(manifest, indent=2), compress_type=zipfile.ZIP_DEFLATED)
        if len(output.getvalue()) <= max_bytes:
            return output.getvalue(), manifest, report
        images = [name for name in files if pathlib.PurePosixPath(name).suffix.lower() in ('.jpg', '.png')]
        if not images:
            raise ValueError('Non-image evidence exceeds export budget; no diagnostic text was truncated')
        candidates = [name for name in images if pathlib.PurePosixPath(name).name not in PRIORITY_IMAGES] or images
        remove = max(candidates, key=lambda name: (len(files[name]), name))
        omitted.append(remove)
        del files[remove]
