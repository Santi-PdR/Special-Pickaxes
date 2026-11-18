"""Static contracts complement (not replace) Forge/GameTest and graphical checks."""
from pathlib import Path
import unittest

ROOT=Path('src/main/java/io/github/santipdr/specialpickaxes/artifact')

class ExcavationFeedbackTest(unittest.TestCase):
    def test_impacts_share_cast_budget_and_are_private_loaded_and_nearby(self):
        method=(ROOT/'AbilityVfx.java').read_text().split('public static boolean excavation(',1)[1].split('public static void forget(',1)[0]
        for contract in ('hasChunkAt(at)','>48*48','BUDGET.tryReserve(tick,count)','level.sendParticles(player,','false,point.x'):
            self.assertIn(contract,method)
        self.assertNotIn('ParticleTypes.',method)
    def test_only_active_queues_emit_progress_and_successful_completion(self):
        queue=(ROOT/'WorkQueue.java').read_text()
        self.assertIn('now-job.impactTick>=10',queue)
        self.assertIn('if(!job.aborted&&job.succeeded>0&&job.lastImpact!=null)',queue)
        self.assertIn('AbilityVfx.excavation(p,job.kind,feedback,false)',queue)
        self.assertIn('AbilityVfx.excavation(p,job.kind,job.lastImpact,true)',queue)
        for source in ('ArtifactPassives.java','RelicControl.java'):
            self.assertNotIn('AbilityVfx.excavation(', (ROOT/source).read_text())

if __name__=='__main__':unittest.main()
