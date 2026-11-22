"""Bounded Checks export. Full images remain in the Actions artifact, not silently discarded."""
import hashlib
import io
import json
import pathlib
import zipfile

MAX_EXPORT_BYTES = 2_000_000
EXPORT_REPORT = 'build/evidence-export.json'
from mining_catalog import IDS
PRIORITY_IMAGES = {'icarus-ascent.jpg', 'survey-guide.jpg', 'manual-palimpsest-solar_rupture.jpg', 'manual-fault_choir-fault_fan.jpg', 'manual-eventide-gravity_in.jpg', 'manual-paradox_crucible-stone.jpg', 'manual-interregnum-domain.jpg', 'stasis-domain-active.jpg', 'manual-worldloom-quarry.jpg', 'worldloom-quarry-active.jpg', 'manual-worldbreaker-carve.jpg', 'ux-shift-fault_choir-fault_fan.jpg', 'ux-shift-worldbreaker-carve.jpg', 'gallery-clean.jpg', 'inventory.jpg', 'supreme-executing.jpg', 'supreme-complete.jpg'} | {'portrait-'+pick+'.jpg' for pick in IDS}

# Current milestone's review must survive before historical priority screenshots.
ACTION_REVIEW_IMAGES = {'action-'+scene+'.jpg' for scene in (
    'palimpsest-solar_rupture', 'fault_choir-fault_fan', 'eventide-gravity_in',
    'paradox_crucible-stone', 'hollow_axiom-hollow_pulse', 'exodium-starfall',
    'hellspec-hellforge')} | {'supreme-executing.jpg', 'supreme-complete.jpg',
                           'curios-key-before.jpg', 'curios-key-after.jpg', 'pouch-menu.jpg'}
PRIORITY_IMAGES |= ACTION_REVIEW_IMAGES
FOCUS_IMAGES = {'manual-worldloom-quarry.jpg', 'worldloom-quarry-active.jpg'} | ACTION_REVIEW_IMAGES


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
        candidates = ([name for name in images if pathlib.PurePosixPath(name).name not in PRIORITY_IMAGES]
                      or [name for name in images if pathlib.PurePosixPath(name).name not in FOCUS_IMAGES]
                      or images)
        remove = max(candidates, key=lambda name: (len(files[name]), name))
        omitted.append(remove)
        del files[remove]
