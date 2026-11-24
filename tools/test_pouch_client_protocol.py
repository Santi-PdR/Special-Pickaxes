"""Protocol contracts; actual rendering remains mandatory in the Forge client job."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / 'src/main/java/io/github/santipdr/specialpickaxes/test'


class PouchClientProtocolTests(unittest.TestCase):
    def test_render_probe_reads_real_slots_and_requires_stable_post_render_frames(self):
        source = (JAVA / 'PouchClientRenderValidation.java').read_text()
        for required in ['ScreenEvent.Render.Post', 'AbstractContainerScreen<?>',
                         'menu.getSlot(0).getItem()', 'menu.getSlot(1).getItem()',
                         'diamond.getCount()!=20', 'gold.getCount()!=5',
                         '++stableFrames<10', 'StandardCopyOption.ATOMIC_MOVE']:
            self.assertIn(required, source)
        for forbidden in ['.setItem(', '.setCount(', 'PickaxePouch.insert', 'openMenu(']:
            self.assertNotIn(forbidden, source)

    def test_client_automation_does_not_close_before_render_acknowledgement(self):
        source = (ROOT / 'tools/client_smoke.py').read_text()
        initial = source.split("'POUCH_MENU_OPEN' in line:", 1)[1].split("'POUCH_MENU_UPDATED' in line:", 1)[0]
        self.assertIn('client-pouch-initial-captured.flag', initial)
        self.assertNotIn("press_screen('close')", initial)
        for required in ['Missing real rendered pouch live-update verification',
                         'Missing server acknowledgement of rendered pouch live update',
                         'Missing real live-updated pouch capture']:
            self.assertIn(required, source)

    def test_server_uses_storage_api_and_waits_for_actual_client_ack(self):
        source = (JAVA / 'PouchClientValidation.java').read_text()
        self.assertIn('PickaxePouch.insert(source,new ItemStack(Items.DIAMOND,3))', source)
        self.assertIn('PickaxePouch.insert(source,new ItemStack(Items.GOLD_INGOT,5))', source)
        self.assertIn('Files.readString(flag("live-ack")).equals("20,5")', source)
        self.assertLess(source.index('Files.exists(flag("initial-captured"))'),
                        source.index('PickaxePouch.insert(source,new ItemStack(Items.DIAMOND,3))'))
        self.assertNotIn('.setItem(', source)

    def test_live_capture_is_prioritized_in_bounded_evidence(self):
        source = (ROOT / 'tools/evidence_bundle.py').read_text()
        self.assertIn("'pouch-live-updated.jpg'", source)
