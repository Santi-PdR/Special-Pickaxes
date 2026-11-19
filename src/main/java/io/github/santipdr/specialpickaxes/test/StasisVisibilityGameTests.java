package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class StasisVisibilityGameTests {
    @GameTest(template="empty",batch="stasis_visibility_01") public static void boundaryReportsConfiguredRemainingLifetimeAndClearsOnStop(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);var tool=p.getMainHandItem();
        h.assertTrue(DomainFields.start(p,tool,ArtifactKind.INTERREGNUM,p.blockPosition(),8,321),"field starts");
        h.assertTrue(DomainFields.remainingStasisTicks(p)==321,"server reports actual duration rather than hardcoded guide value");
        DomainFields.stop(p);h.assertTrue(DomainFields.remainingStasisTicks(p)==0,"stopped boundary has no remaining lifetime");finish(h,p);
    }
    @GameTest(template="empty",batch="stasis_visibility_02") public static void replacingHeldStackImmediatelyInvalidatesBoundaryTelemetry(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);var tool=p.getMainHandItem();DomainFields.start(p,tool,ArtifactKind.INTERREGNUM,p.blockPosition(),8,200);
        p.setItemInHand(InteractionHand.MAIN_HAND,tool.copy());h.assertTrue(DomainFields.remainingStasisTicks(p)==0,"same-kind replacement cannot sustain stale visible domain");
        DomainFields.stop(p);finish(h,p);
    }
}
