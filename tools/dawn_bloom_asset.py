#!/usr/bin/env python3
"""Deterministic four-frame dawn bloom atlas for Palimpsest's Dawn Mending pulse.

The sprite is authored as code so the runtime texture, its animation metadata and
the validator all describe the same pixels. Frames are stacked vertically, the
layout the other animated particles in this mod use (32x32 each, 32x128 total).
"""
from pathlib import Path
import json, math, struct, zlib

ROOT = Path("src/main/resources/assets/specialpickaxes")
FRAME = 32
FRAMES = 4
OUTLINE = (63, 39, 23)
EMBER = (194, 108, 43)
GOLD = (250, 195, 83)
HIGHLIGHT = (255, 238, 166)


def blend(dst, src, alpha):
    """Composite src over dst with a 0..1 alpha in an 8-bit RGBA buffer."""
    src = (src[0], src[1], src[2], src[3] if len(src) > 3 else 255)
    sa = max(0.0, min(1.0, alpha)) * (src[3] / 255.0)
    if sa <= 0.0:
        return dst
    da = dst[3] / 255.0
    out_a = sa + da * (1.0 - sa)
    if out_a <= 0.0:
        return (0, 0, 0, 0)
    out = [
        (src[i] * sa + dst[i] * da * (1.0 - sa)) / out_a
        for i in range(3)
    ]
    return (int(round(out[0])), int(round(out[1])), int(round(out[2])), int(round(out_a * 255)))


def put(px, x, y, color, alpha=1.0):
    if 0 <= x < FRAME and 0 <= y < FRAME:
        i = y * FRAME + x
        px[i] = blend(px[i], color, alpha)


def frame(index):
    px = [(0, 0, 0, 0)] * (FRAME * FRAME)
    cx = cy = (FRAME - 1) / 2.0
    grow = index * 1.35
    # Growing corona: a soft ring that fades as it expands.
    ring_radius = 7.5 + grow
    ring_alpha = 0.55 - index * 0.10
    for y in range(FRAME):
        for x in range(FRAME):
            d = math.hypot(x - cx, y - cy)
            if abs(d - ring_radius) < 1.15:
                put(px, x, y, GOLD, ring_alpha)
    # Eight rays: four cardinal lances plus four shorter diagonals.
    for k in range(8):
        angle = k * math.pi / 4.0 + index * math.pi / 16.0
        length = (11.0 if k % 2 == 0 else 8.0) + grow
        for step in range(2, int(length) + 1):
            x = int(round(cx + math.cos(angle) * step))
            y = int(round(cy + math.sin(angle) * step))
            fade = 1.0 - (step - 2) / (length - 1)
            put(px, x, y, EMBER if k % 2 else GOLD, 0.35 + 0.55 * fade)
    # Solid core with a warm outline and a static highlight so the pulse reads as a sun.
    for y in range(FRAME):
        for x in range(FRAME):
            d = math.hypot(x - cx, y - cy)
            if d <= 4.6:
                put(px, x, y, OUTLINE, 1.0)
            if d <= 3.7:
                put(px, x, y, GOLD, 1.0)
            if d <= 2.2:
                put(px, x, y, HIGHLIGHT, 1.0)
    # Highlight leans toward the upper left in every frame; the rays carry the motion.
    for x, y in [(12, 12), (11, 13), (13, 11)]:
        put(px, x, y, HIGHLIGHT, 0.9)
    return px


def write_png(path, rows, width, height):
    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    raw = b"".join(b"\0" + bytes(sum((list(p) for p in row), [])) for row in rows)
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(b"\x89PNG\r\n\x1a\n"
                     + chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
                     + chunk(b"IDAT", zlib.compress(raw, 9))
                     + chunk(b"IEND", b""))


def main():
    rows = []
    for index in range(FRAMES):
        px = frame(index)
        for y in range(FRAME):
            rows.append(px[y * FRAME:(y + 1) * FRAME])
    write_png(ROOT / "textures/particle/dawn_mending_bloom.png", rows, FRAME, FRAME * FRAMES)
    (ROOT / "textures/particle/dawn_mending_bloom.png.mcmeta").write_text(
        json.dumps({"animation": {"frametime": 3, "frames": list(range(FRAMES))}}, indent=2) + "\n")
    (ROOT / "particles").mkdir(parents=True, exist_ok=True)
    (ROOT / "particles/dawn_mending_bloom.json").write_text(
        json.dumps({"textures": ["specialpickaxes:dawn_mending_bloom"]}, indent=2) + "\n")
    print("dawn_mending_bloom: %dx%d, %d frames" % (FRAME, FRAME * FRAMES, FRAMES))


if __name__ == "__main__":
    main()
