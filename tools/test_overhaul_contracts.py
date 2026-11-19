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
                self.assertIn(record['external_repository'], {'Noaaan/MythicMetals', 'SammySemicolon/Malum-Mod'})
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

    def test_manual_has_two_ability_summaries_curios_and_live_controls(self):
        source = (ROOT / 'client/ArtifactManualScreen.java').read_text()
        sections = source.split('private List<Section> sections(', 1)[1].split('private void ensureCards', 1)[0]
        self.assertIn('guide.main.', sections)
        self.assertIn('guide.alt.', sections)
        self.assertIn('RelicKeys.name(action)', sections)
        self.assertIn('passive.specialpickaxes.', sections)
        self.assertIn('screen.specialpickaxes.curios', sections)
        self.assertIn('curios.detail.specialpickaxes."+kind.id', sections)
        for retired in ('manual4.limits', 'manual4.cost', 'manual4.tool_profile', 'melee.specialpickaxes.'):
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

    def test_expanded_eventide_instruction_keeps_both_rebindable_controls(self):
        for locale in ('en_us', 'es_es'):
            texts = json.loads((ASSETS / 'lang' / (locale + '.json')).read_text())
            text = texts['alternate.detail.specialpickaxes.eventide']
            self.assertIn(' %3$s ', text)
            self.assertIn(' %1$s ', text)

    def test_multimode_guides_explain_every_selected_mode_in_both_languages(self):
        modes = {
            'eventide': ['gravity_in', 'gravity_out'],
            'interregnum': ['domain', 'aura'],
            'icarus': ['forward', 'wide'],
            'worldbreaker': ['carve', 'fracture', 'cleave', 'core_drill', 'world_shatter', 'region_break'],
            'paradox_crucible': ['stone', 'deepslate', 'granite', 'diorite', 'andesite', 'dirt', 'basalt', 'obsidian', 'calcite', 'tuff', 'dripstone', 'gravel'],
        }
        for locale in ('en_us', 'es_es'):
            texts = json.loads((ASSETS / 'lang' / (locale + '.json')).read_text())
            for artifact, variants in modes.items():
                descriptions = [texts['guide.main.' + artifact + '.' + mode] for mode in variants]
                self.assertEqual(len(descriptions), len(set(descriptions)))
                self.assertTrue(all(15 < len(text) < 180 for text in descriptions))

    def test_manual_controls_explain_cancel_corners_and_field_toggles(self):
        for locale in ('en_us', 'es_es'):
            texts = json.loads((ASSETS / 'lang' / (locale + '.json')).read_text())
            for action in ('activate', 'region', 'alternate', 'corners', 'field'):
                self.assertTrue(15 < len(texts['guide.control.' + action]) < 150)
        source = (ROOT / 'client/ArtifactManualScreen.java').read_text()
        self.assertIn('if(regional)controls.add', source)
        self.assertIn('RelicKeys.name(action)', source)
        self.assertIn('abilityKind==ArtifactKind.INTERREGNUM', source)

    def test_seam_guides_no_longer_advertise_retired_limits_or_continuous_trails(self):
        for locale in ('en_us', 'es_es'):
            texts = json.loads((ASSETS / 'lang' / (locale + '.json')).read_text())
            for key, value in texts.items():
                if 'seam_ripper' in key:
                    self.assertNotIn('256', value, key)
                    self.assertNotIn('512', value, key)
                    self.assertNotIn('7×7', value, key)
            self.assertIn('1024', texts['guide.main.seam_ripper'])
            self.assertIn('15×15', texts['guide.alt.seam_ripper'])

    def test_worldloom_primary_documents_terraces_instead_of_the_retired_bore(self):
        for locale in ('en_us', 'es_es', 'es_uy'):
            texts = json.loads((ASSETS / 'lang' / (locale + '.json')).read_text())
            for key in ('guide.main.worldloom', 'mining.identity.worldloom.quarry', 'mining.how.worldloom.quarry'):
                self.assertIn('4164', texts[key])
                self.assertNotIn('7×7', texts[key])
                self.assertNotIn('48', texts[key])
                self.assertNotIn('outline', texts[key].lower())
                self.assertNotIn('contorno', texts[key].lower())
            self.assertIn('%1$s', texts['mining.how.worldloom.quarry'])
            self.assertIn('12', texts['guide.main.worldloom'])
            self.assertIn('64', texts['tooltip.specialpickaxes.secondary.worldloom'])
