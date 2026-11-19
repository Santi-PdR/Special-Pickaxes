"""User-directed invariants of the major reform, not claims of completed ability work."""
import hashlib
import json
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
LANG = ROOT / 'src/main/resources/assets/specialpickaxes/lang'

class MajorReformContracts(unittest.TestCase):
    def test_approved_item_art_and_protected_mechanics_are_byte_identical(self):
        frozen = json.loads((ROOT/'docs/mining-rework/REFORM-6-FROZEN.json').read_text())
        for name, digest in frozen['sha256'].items():
            with self.subTest(path=name):
                self.assertEqual(hashlib.sha256((ROOT/name).read_bytes()).hexdigest(), digest)

    def test_uruguay_has_a_complete_spanish_translation_and_all_locales_match_keys(self):
        es = json.loads((LANG/'es_es.json').read_text())
        uy = json.loads((LANG/'es_uy.json').read_text())
        en = json.loads((LANG/'en_us.json').read_text())
        self.assertEqual(es, uy)
        self.assertEqual(set(es), set(en))

    def test_failure_feedback_is_short_in_all_supported_locales(self):
        for locale in ('en_us','es_es','es_uy'):
            data = json.loads((LANG/(locale+'.json')).read_text())
            for key in ('cancelled','no_target','alternate_no_target','selection_invalid','chunk_pause','drop_pause','region_memory_limit','crucible_no_change'):
                with self.subTest(locale=locale,key=key):
                    self.assertLessEqual(len(data['message.specialpickaxes.'+key]), 42)

    def test_no_world_hud_renderer_is_registered(self):
        source = (ROOT/'src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactHud.java').read_text()
        self.assertNotIn('void overlay(', source)
        self.assertNotIn('void bar(', source)
        self.assertNotIn('RenderGuiOverlayEvent.Post event', source)

    def test_manual_has_explicit_curios_content_and_real_item_render(self):
        source = (ROOT/'src/main/java/io/github/santipdr/specialpickaxes/client/ArtifactManualScreen.java').read_text()
        self.assertIn('"passive.specialpickaxes."+kind.id', source)
        self.assertIn('g.renderItem(shownIcon,0,0)', source)
