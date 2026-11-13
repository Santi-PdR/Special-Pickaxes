"""Keep authored runtime dawn sprites byte-identical to the deterministic source."""
import contextlib
import io
import pathlib
import tempfile
import unittest
from unittest.mock import patch

import dawn_bloom_asset


class DawnBloomAssets(unittest.TestCase):
    def test_regeneration_matches_all_runtime_files(self):
        runtime = pathlib.Path('src/main/resources/assets/specialpickaxes')
        with tempfile.TemporaryDirectory() as directory:
            generated = pathlib.Path(directory)
            with patch.object(dawn_bloom_asset, 'ROOT', generated), contextlib.redirect_stdout(io.StringIO()):
                dawn_bloom_asset.main()
            for relative in ['textures/particle/dawn_mending_bloom.png',
                             'textures/particle/dawn_mending_bloom.png.mcmeta',
                             'particles/dawn_mending_bloom.json']:
                with self.subTest(asset=relative):
                    self.assertEqual((generated / relative).read_bytes(), (runtime / relative).read_bytes())

    def test_frames_are_distinct_with_transparent_corners(self):
        frames = [dawn_bloom_asset.frame(index) for index in range(dawn_bloom_asset.FRAMES)]
        self.assertEqual(len({tuple(frame) for frame in frames}), dawn_bloom_asset.FRAMES)
        for pixels in frames:
            self.assertEqual(len(pixels), dawn_bloom_asset.FRAME ** 2)
            self.assertEqual(pixels[0][3], 0)
            self.assertTrue(any(pixel[3] == 255 for pixel in pixels), 'the sun core stays legible')


if __name__ == '__main__':
    unittest.main()
