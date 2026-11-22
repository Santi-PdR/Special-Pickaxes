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
    @GameTest(template="empty",batch="stasis_visibility_04") public static void legacyDomainComponentAnchorsAnExact33By17By33Domain(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);var at=target(h,p);
        ArtifactState.of(p,ArtifactKind.INTERREGNUM).putInt("mode",0);
        h.assertTrue(DomainFields.start(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,at,16),"expanded primary starts");
        h.assertTrue(DomainFields.contains(p,at.offset(16,8,16))&&!DomainFields.contains(p,at.offset(17,0,0))&&!DomainFields.contains(p,at.above(9)),"inclusive radius16 and half-height8 match boundary volume");
        DomainFields.stop(p);finish(h,p);
    }
    @GameTest(template="empty",batch="stasis_visibility_05") public static void copiedAuraIgnoresUnrelatedSourceModeAndFollowsItsCaster(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var tool=p.getMainHandItem();
        tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.INTERREGNUM.id);tool.getOrCreateTag().putInt("copiedMode",1);
        ArtifactState.of(p,ArtifactKind.INTERREGNUM).putInt("mode",0);
        h.assertTrue(DomainFields.start(p,tool,ArtifactKind.INTERREGNUM,p.blockPosition(),2),"copied aura starts despite source saved in domain mode");
        var destination=p.blockPosition().east(12);p.setPos(destination.getX()+.5,destination.getY(),destination.getZ()+.5);
        h.assertTrue(DomainFields.contains(p,destination)&&!DomainFields.contains(p,destination.east(3)),"copied mode remains a moving five-block-wide aura");
        DomainFields.stop(p);finish(h,p);
    }
    @GameTest(template="empty",batch="stasis_visibility_03") public static void hourglassSecondaryPreservesItsWaistGapAndPlacedPolarRock(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);var at=target(h,p);var level=h.getLevel();
        var gap=at.east(4);var pole=at.east(7).above(8);var placed=at.west(7).above(8);
        for(var cell:java.util.List.of(gap,pole,placed)){level.setBlockAndUpdate(cell,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        PlayerPlacedBlocks.get(level).mark(placed);
        try{
            h.assertTrue(MiningAlternates.use(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM),"public hourglass starts");drain(p);
            h.assertTrue(level.getBlockState(at).isAir()&&level.getBlockState(pole).isAir(),"waist and distant upper chamber excavated");
            h.assertTrue(level.getBlockState(gap).is(net.minecraft.world.level.block.Blocks.STONE)&&level.getBlockState(placed).is(net.minecraft.world.level.block.Blocks.STONE),"narrow waist gap and player provenance preserved");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
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
