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
        self.assertIn('GLFW.GLFW_KEY_G',source)
        self.assertIn('equipped.stack().getTag().getUUID("controlIdentity")', source)
        active = (ROOT/'artifact/CuriosMiningActive.java').read_text()
        self.assertNotIn('setItemInHand',active)
        self.assertIn('!lease.active()',active)
        self.assertIn('curiosActiveReady',active)
        self.assertIn('canSpendDurability(p,source,8)',active)
