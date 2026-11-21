import pathlib
import unittest

ROOT = pathlib.Path('src/main/java/io/github/santipdr/specialpickaxes')
class CuriosActiveControls(unittest.TestCase):
    def test_equipped_packet_uses_live_slot_identity_before_dispatch(self):
        source = (ROOT/'network/RelicNetwork.java').read_text()
        start = source.index('if(intent.action()==RelicControl.Action.CURIO_ACTIVATE)')
        branch = source[start:source.index('var tool=p.getMainHandItem();',start)]
        for check in ['CuriosCompat.findFresh(p)', 'item.kind!=intent.kind()', 'equals(intent.toolIdentity())', 'CuriosCompat.pickInSlotFresh(p,equipped.slot())']:
            self.assertIn(check, branch)
        self.assertLess(branch.index('equals(intent.toolIdentity())'),branch.index('CuriosMiningActive.activate'))
    def test_client_sends_equipped_identity_and_never_swaps_hand(self):
        source = (ROOT/'client/RelicKeys.java').read_text()
        self.assertIn('GLFW.GLFW_KEY_H',source)
        self.assertIn('equipped.stack().getTag().getUUID("controlIdentity")', source)
        active = (ROOT/'artifact/CuriosMiningActive.java').read_text()
        self.assertNotIn('setItemInHand',active)
        self.assertIn('!lease.active()',active)
        self.assertIn('curiosActiveReady',active)
        self.assertIn('canSpendDurability(p,source,8)',active)

    def test_real_equipped_client_fixture_requires_keyboard_and_real_inventory(self):
        fixture = (ROOT/'test/EquippedClientValidation.java').read_text()
        self.assertIn('getCuriosInventory', fixture)
        self.assertIn('setStackInSlot', fixture)
        self.assertNotIn('CuriosMiningActive.activate', fixture)
        smoke = pathlib.Path('tools/client_smoke.py').read_text()
        self.assertIn("press_screen('curio')", smoke)
        self.assertIn("Missing real Curios slot and physical equipped-key verification", smoke)

    def test_tooltip_fixture_is_not_a_gameplay_hud(self):
        fixture = (ROOT/'test/ClientTooltipValidation.java').read_text()
        self.assertIn('Files.exists(java.nio.file.Path.of("client-tooltip-visible.flag"))', fixture)
        smoke = pathlib.Path('tools/client_smoke.py').read_text()
        self.assertIn("tooltip_flag.unlink(missing_ok=True)", smoke)
        self.assertIn("'action-'+id+'.png'", smoke)

    def test_curios_mixins_remap_only_in_development_smoke_client(self):
        gradle = pathlib.Path('build.gradle').read_text()
        client = gradle[gradle.index('        client {'):gradle.index('        server {')]
        self.assertIn("if (project.hasProperty('clientSmoke'))", client)
        self.assertIn("property 'mixin.env.remapRefMap', 'true'", client)
        self.assertIn("file('build/createSrgToMcp/output.srg').absolutePath", client)
        self.assertIn("dependsOn 'createSrgToMcp'", gradle)
        self.assertNotIn('disableRefMap', gradle)

    def test_equipped_default_avoids_curios_inventory_and_manual_uses_rebound_key(self):
        source = (ROOT/'client/RelicKeys.java').read_text()
        self.assertIn('GLFW.GLFW_KEY_X,GLFW.GLFW_KEY_H', source)
        self.assertNotIn('GLFW.GLFW_KEY_X,GLFW.GLFW_KEY_G', source)
        manual = (ROOT/'client/ArtifactManualScreen.java').read_text()
        self.assertIn('Component.translatable("curios.detail.specialpickaxes."+kind.id,RelicKeys.name(RelicControl.Action.CURIO_ACTIVATE))', manual)
        helper = pathlib.Path('tools/CaptureScreen.java').read_text()
        self.assertIn('case "curio" -> java.awt.event.KeyEvent.VK_H', helper)

    def test_equipped_active_lease_tracks_same_stack_copied_mode(self):
        source=(ROOT/'artifact/CuriosMiningActive.java').read_text()
        self.assertIn('var skill=HeldSkillLease.capture(source)',source)
        self.assertIn('current.get()==source&&skill.valid()&&WorldbreakerCuriosPassives.sourceKind(source)==kind',source)
        self.assertLess(source.index('HeldSkillLease.capture(source)'),source.index('WorldSafety.allowed(p,kind,at)'))

    def test_passive_copy_observation_keeps_original_mode_through_confirmation(self):
        source=(ROOT/'artifact/MiningObservations.java').read_text()
        self.assertIn('var skill=HeldSkillLease.capture(curio)',source)
        self.assertIn('current.get()==curio&&skill.valid()&&copiedMiningSource(curio)==source',source)

    def test_passive_streaks_never_reuse_revoked_equipment_records(self):
        for name in ('ResonantFault','ParadoxLattice'):
            source=(ROOT/'artifact'/f'{name}.java').read_text()
            self.assertIn('old!=null&&old.source.active()&&old.source.expected()==source.expected()',source)

    def test_synchronous_passive_scope_preserves_original_copied_mode(self):
        source=(ROOT/'artifact/WorldbreakerCuriosPassives.java').read_text()
        self.assertIn('current,HeldSkillLease.capture(stack)',source)
        self.assertIn('skill.valid()&&sourceKind(stack)==source',source)
