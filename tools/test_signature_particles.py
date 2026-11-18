"""Contracts for cast-only signatures; independent of Pillow and the Minecraft client."""
import hashlib
import json
from pathlib import Path
import struct
import unittest

ROOT=Path(__file__).resolve().parent.parent
ASSETS=ROOT/'src/main/resources/assets/specialpickaxes'
KINDS='palimpsest choir eventide crucible interregnum worldloom icarus axiom worldbreaker exodium iridium hellspec seam_ripper'.split()

class SignatureParticlesTest(unittest.TestCase):
    def test_every_relic_has_four_existing_distinct_frames(self):
        all_hashes=set()
        for kind in KINDS+[k+'_alternate' for k in KINDS]:
            data=json.loads((ASSETS/'particles'/f'{kind}_signature.json').read_text())
            self.assertEqual(4,len(data['textures']))
            hashes=set()
            for texture in data['textures']:
                self.assertTrue(texture.startswith('specialpickaxes:signature/'))
                image=(ASSETS/'textures/particle'/(texture.split(':')[1]+'.png')).read_bytes()
                self.assertEqual(b'\x89PNG\r\n\x1a\n',image[:8])
                self.assertEqual((32,32),struct.unpack('>II',image[16:24]))
                hashes.add(hashlib.sha256(image).hexdigest())
            self.assertEqual(4,len(hashes),kind)
            self.assertFalse(all_hashes & hashes,kind)
            all_hashes.update(hashes)
    def test_renderer_is_client_only_and_has_no_collision(self):
        source=(ROOT/'src/main/java/io/github/santipdr/specialpickaxes/client/RelicSignatureParticle.java').read_text()
        self.assertIn('hasPhysics=false',source)
        self.assertIn('age>=lifetime',source)
        self.assertIn('ParticleEnvelope.alpha',source)
        self.assertIn('setSpriteFromAge',source)
    def test_cast_uses_signature_registry_not_shared_fallback(self):
        source=(ROOT/'src/main/java/io/github/santipdr/specialpickaxes/artifact/AbilityVfx.java').read_text()
        self.assertIn('SIGNATURE_PARTICLES).get(kind.name())',source)
        self.assertNotIn('return switch(kind)',source)

if __name__=='__main__':unittest.main()
