import pathlib
import unittest

BASE = pathlib.Path('src/main/java/io/github/santipdr/specialpickaxes/artifact')

class FeedbackRevisionTests(unittest.TestCase):
    def test_only_extractor_highlights_drops(self):
        safety = (BASE/'WorldSafety.java').read_text()
        marking = safety.split('public static void tagIridiumDrop', 1)[1].split('public static boolean inert', 1)[0]
        self.assertNotIn('setGlowingTag', marking)
        self.assertIn('context.kind()!=ArtifactKind.IRIDIUM||!ArtifactOres.isOre', marking)
        self.assertNotIn('setGlowingTag', (BASE/'MiningObservations.java').read_text())

    def test_loot_density_does_not_block_native_break(self):
        safety = (BASE/'WorldSafety.java').read_text()
        self.assertNotIn('if(checkDropPressure&&dropPressure', safety)
        gate = safety.split('static boolean backpressuredMine(', 1)[1].split('private static boolean breakPermission', 1)[0]
        self.assertIn('return false;', gate)
        self.assertNotIn('dropPressure(p,pos)', gate)

    def test_lift_mines_headroom_with_a_live_lease(self):
        flight = (BASE/'IcarianFlight.java').read_text()
        self.assertIn('WorldSafety.mineNaturalLeased(p,job.tool,ArtifactKind.ICARUS,at,state,job::valid)', flight)
        self.assertLess(flight.index('!headroom(p,rise,job)'), flight.index('p.setDeltaMovement'))
        self.assertNotIn('.teleport(', flight)

    def test_alternate_paradox_clears_corner_selection_only_after_queue_acceptance(self):
        source = (BASE/'ParadoxLattice.java').read_text().split('public static boolean start(',1)[1].split('public static boolean observed',1)[0]
        self.assertLess(source.index('if(!WorkQueue.startRegion'),source.index('ArtifactInteraction.clear(p)'))
