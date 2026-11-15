"""Static/resource regression checks for the event-only visual overhaul; not a substitute for client CI."""
import hashlib
import json
from pathlib import Path
import unittest

ROOT = Path('src/main/java/io/github/santipdr/specialpickaxes')
ASSETS = Path('src/main/resources/assets/specialpickaxes')


class OverhaulContracts(unittest.TestCase):
    def test_every_imported_pick_is_unmodified_and_bound_to_a_source_commit(self):
        records = json.loads(Path('docs/mining-rework/ASSET-PROVENANCE.json').read_text())
        self.assertEqual(len(records), 13)
        for record in records:
            with self.subTest(pick=record['artifact']):
                self.assertEqual(record['external_repository'], 'Noaaan/MythicMetals')
                self.assertRegex(record['external_commit'], r'^[0-9a-f]{40}$')
                self.assertTrue(record['external_entry'].endswith('_pickaxe.png'))
                data = (ASSETS / 'textures/item' / record['runtime_texture']).read_bytes()
                self.assertEqual(hashlib.sha256(data).hexdigest(), record['external_source_sha256'])
                self.assertEqual(record['runtime_sha256'], record['external_source_sha256'])
                model = json.loads((ASSETS / 'models/item' / (record['artifact'] + '.json')).read_text())
                self.assertTrue(model['textures']['layer0'].endswith('_external'))

    def test_only_the_event_emitter_can_send_particle_packets(self):
        emitters = [p.name for p in (ROOT / 'artifact').glob('*.java') if '.sendParticles(' in p.read_text()]
        self.assertEqual(emitters, ['AbilityVfx.java'])
        source = (ROOT / 'artifact/AbilityVfx.java').read_text()
        self.assertNotIn('ParticleTypes.', source)
        self.assertNotIn('@SubscribeEvent', source)
        self.assertNotIn('PacketDistributor', source)
        self.assertIn('tryReserve(tick,16)', source)

    def test_client_does_not_restore_ambient_auras_or_boundary_particles(self):
        aura = (ROOT / 'client/RelicAura.java').read_text()
        boundary = (ROOT / 'client/StasisBoundaryClient.java').read_text()
        self.assertNotIn('@SubscribeEvent', aura)
        self.assertNotIn('.addParticle(', aura)
        self.assertNotIn('.addParticle(', boundary)

    def test_manual_has_only_two_ability_summaries_and_live_controls(self):
        source = (ROOT / 'client/ArtifactManualScreen.java').read_text()
        sections = source.split('private List<Section> sections(', 1)[1].split('private void ensureCards', 1)[0]
        self.assertIn('guide.main.', sections)
        self.assertIn('guide.alt.', sections)
        self.assertIn('RelicKeys.name(action)', sections)
        for retired in ('manual4.limits', 'manual4.cost', 'manual4.tool_profile', 'curios.detail.', 'melee.specialpickaxes.', 'passive.specialpickaxes.'):
            self.assertNotIn(retired, sections)
        for locale in ('en_us', 'es_es'):
            texts = json.loads((ASSETS / 'lang' / (locale + '.json')).read_text())
            records = json.loads(Path('docs/mining-rework/ASSET-PROVENANCE.json').read_text())
            for record in records:
                for prefix in ('guide.main.', 'guide.alt.'):
                    text = texts[prefix + record['artifact']]
                    self.assertTrue(text.strip())
                    self.assertLess(len(text), 200)

    def test_personal_artwork_does_not_get_an_automatic_public_binary(self):
        workflow = Path('.github/workflows/verify.yml').read_text()
        self.assertIn("PUBLISH_BINARY: 'false'", workflow)
        publication = workflow.split('- name: Publish downloadable test build', 1)[1].split('env:', 1)[0]
        self.assertIn("vars.ALLOW_PUBLIC_ASSET_REDISTRIBUTION == 'true'", publication)
