"""Validate a bounded CI evidence ZIP completely before writing its declared files."""
import hashlib
import io
import json
import pathlib
import stat
import zipfile

MAX_FILES = 2048
MAX_UNPACKED_BYTES = 128 * 1024 * 1024
MAX_MANIFEST_BYTES = 1024 * 1024


def safe_name(name):
    if not isinstance(name, str) or not name or '\\' in name or ':' in name:
        raise ValueError('Unsafe evidence path')
    path = pathlib.PurePosixPath(name)
    if path.is_absolute() or '..' in path.parts or path.as_posix() != name or name == '.':
        raise ValueError(f'Unsafe evidence path: {name}')
    return name


def extract_verified(data, destination):
    root = pathlib.Path(destination).resolve()
    with zipfile.ZipFile(io.BytesIO(data)) as archive:
        members = archive.infolist()
        if len(members) > MAX_FILES or sum(info.file_size for info in members) > MAX_UNPACKED_BYTES:
            raise ValueError('Evidence exceeds extraction budget')
        names = []
        for info in members:
            safe_name(info.filename)
            if info.is_dir() or stat.S_ISLNK(info.external_attr >> 16):
                raise ValueError('Evidence must contain regular files only')
            if not (root / info.filename).resolve().is_relative_to(root):
                raise ValueError('Evidence destination escapes through a symlink')
            names.append(info.filename)
        if len(set(names)) != len(names) or 'manifest.json' not in names:
            raise ValueError('Duplicate files or missing evidence manifest')
        if archive.getinfo('manifest.json').file_size > MAX_MANIFEST_BYTES:
            raise ValueError('Evidence manifest is too large')
        manifest = json.loads(archive.read('manifest.json'))
        if not isinstance(manifest, list):
            raise ValueError('Invalid evidence manifest')
        declared = set()
        for entry in manifest:
            if not isinstance(entry, dict):
                raise ValueError('Invalid evidence entry')
            name = safe_name(entry.get('path'))
            if name == 'manifest.json' or name in declared or name not in names:
                raise ValueError('Duplicate or absent manifest entry')
            declared.add(name)
            info = archive.getinfo(name)
            if type(entry.get('bytes')) is not int or entry['bytes'] != info.file_size:
                raise ValueError(f'Evidence size mismatch: {name}')
            digest = hashlib.sha256()
            with archive.open(name) as source:
                for chunk in iter(lambda: source.read(1024 * 1024), b''):
                    digest.update(chunk)
            if digest.hexdigest() != entry.get('sha256'):
                raise ValueError(f'Evidence checksum mismatch: {name}')
        if declared != set(names) - {'manifest.json'}:
            raise ValueError('Evidence contains undeclared files')
        root.mkdir(parents=True, exist_ok=True)
        archive.extractall(root)
    return manifest
