#!/usr/bin/env python3
"""Package verified CI portraits and exact runtime icons, without launching Minecraft.
Requires Pillow for image/PDF rendering. Inputs must be extracted by fetch_ci_evidence.py.
"""
import argparse
import hashlib
import html
import io
import json
from pathlib import Path
import re
import zipfile
from gallery_provenance import ASSETS, validate_gallery


def version_from_release(filename):
    match = re.fullmatch(r'special-pickaxes-1\.20\.1-(\d+\.\d+\.\d+)\.jar', filename)
    if not match:
        raise ValueError('Unexpected release filename')
    return match[1]


def make_deliverable(captures, destination):
    # Validate every source before creating an output directory or opening any images.
    provenance = validate_gallery(captures)
    version = version_from_release(provenance['release_file'])
    from PIL import Image, ImageDraw, ImageFont
    captures, destination = Path(captures), Path(destination)
    if destination.exists() and any(destination.iterdir()):
        raise ValueError('Refusing to mix an existing gallery with another export')
    destination.mkdir(parents=True, exist_ok=True)
    font_path = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'
    def font(size):
        return ImageFont.truetype(font_path, size) if Path(font_path).exists() else ImageFont.load_default()
    pages, cards, files = [], [], {}
    for index, item in enumerate(provenance['items'], 1):
        name = item['artifact']
        texture = ASSETS / 'textures' / (item['texture'].split(':', 1)[1] + '.png')
        icon_bytes = texture.read_bytes()
        portrait_bytes = (captures / ('portrait-' + name + '.jpg')).read_bytes()
        files['icono-' + name + '.png'] = icon_bytes
        files['en-mano-' + name + '.jpg'] = portrait_bytes
        page = Image.new('RGB', (1440, 1080), '#111b28')
        draw = ImageDraw.Draw(page)
        draw.text((54, 38), f'{index:02d} / 13  {name.replace("_", " ").title()}', fill='white', font=font(38))
        draw.text((54, 94), f'Special Pickaxes {version} | Forge 1.20.1 | CI {provenance["run_id"]}', fill='#b4cfdf', font=font(21))
        with Image.open(io.BytesIO(icon_bytes)) as source:
            icon = source.convert('RGBA').resize((320, 320), Image.Resampling.NEAREST)
        page.paste(icon, (70, 252), icon)
        with Image.open(io.BytesIO(portrait_bytes)) as source:
            portrait = source.convert('RGB')
            portrait.thumbnail((960, 730), Image.Resampling.LANCZOS)
            page.paste(portrait, (430 + (960 - portrait.width) // 2, 178 + (730 - portrait.height) // 2))
        draw.text((72, 630), 'Icono real del recurso', fill='#d0e1ec', font=font(23))
        draw.text((430, 930), 'Captura real del cliente CI; no certifica toda la animacion.', fill='#d0e1ec', font=font(20))
        draw.text((54, 1003), 'Fuente: ' + provenance['commit'], fill='#9fb4c6', font=font(19))
        pages.append(page)
        safe = html.escape(name)
        cards.append(f'<article><h2>{safe.replace("_", " ")}</h2><img class="icon" src="icono-{safe}.png" alt="Icono de {safe}"><img class="hand" src="en-mano-{safe}.jpg" alt="Captura CI de {safe} en mano"></article>')
    pdf = io.BytesIO()
    pages[0].save(pdf, format='PDF', save_all=True, append_images=pages[1:], resolution=120)
    files['los-13-picos.pdf'] = pdf.getvalue()
    provenance['scope'] = 'Exact runtime icons and CI still portraits; not approval of complete animations or a full modpack.'
    provenance['version'] = version
    files['PROVENANCE.json'] = (json.dumps(provenance, indent=2, ensure_ascii=False) + '\n').encode()
    files['index.html'] = (f'<!doctype html><html lang="es"><meta charset="utf-8"><title>Los 13 picos — {version}</title><style>body{{background:#111b28;color:#e5eff8;font:18px system-ui;max-width:1200px;margin:2rem auto;padding:1rem}}article{{border-top:1px solid #536879;padding:1rem 0}}img{{max-width:100%;vertical-align:middle}}.icon{{width:192px;image-rendering:pixelated}}.hand{{width:900px}}h2{{text-transform:capitalize}}</style><h1>Los 13 picos — {version}</h1><p>Iconos del recurso y capturas reales CI. No es una certificacion de animaciones completas ni del pack Ghouls.</p>' + ''.join(cards) + '</html>').encode()
    sums = ''.join(hashlib.sha256(data).hexdigest() + '  ' + name + '\n' for name, data in sorted(files.items()))
    files['SHA256SUMS'] = sums.encode()
    for name, data in files.items():
        (destination / name).write_bytes(data)
    archive_path = destination / 'picos-iconos-y-en-mano.zip'
    with zipfile.ZipFile(archive_path, 'w', zipfile.ZIP_DEFLATED) as archive:
        for name, data in sorted(files.items()):
            archive.writestr(name, data)
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None:
            raise ValueError('Gallery ZIP failed CRC validation')
    return {'version': version, 'pages': len(pages), 'images': len(provenance['items']) * 2,
            'pdf': str(destination / 'los-13-picos.pdf'), 'zip': str(archive_path)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('captures', type=Path)
    parser.add_argument('destination', type=Path)
    args = parser.parse_args()
    print(json.dumps(make_deliverable(args.captures, args.destination), indent=2))


if __name__ == '__main__':
    main()
