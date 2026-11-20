package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.ability.AbilityUseEvent;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
/** Historical geometry regression tests. Live R/X routes are covered by ConnectedExcavationGameTests. */
public final class HellspecReworkGameTests {
    @GameTest(template="empty",batch="hellspec_617_01") public static void infernalRiftsExcavateEightArmsAndPreserveBuildsAndInterveningRock(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var at=target(h,p);var level=h.getLevel();
        var central=at.below(8);var satellite=at.offset(9,-6,9);var ore=at.west(12).below(4);var placed=at.north(12);var outside=at.offset(8,0,4);
        for(var cell:java.util.List.of(central,satellite,placed,outside)){level.setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(ore);PlayerPlacedBlocks.get(level).mark(placed);
        try{h.assertTrue(HellspecInfernalRifts.start(p,p.getMainHandItem()),"public infernal rifts starts");drain(p);
            h.assertTrue(level.getBlockState(at).isAir()&&level.getBlockState(central).isAir()&&level.getBlockState(satellite).isAir()&&level.getBlockState(ore).isAir(),"rift arms harvest natural rock and ore");
            h.assertTrue(level.getBlockState(placed).is(Blocks.STONE)&&level.getBlockState(outside).is(Blocks.STONE),"player builds and exterior remain intact");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="hellspec_617_02") public static void worldbreakerHandCopyUsesInfernalRifts(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var satellite=at.offset(9,-5,9);h.getLevel().setBlockAndUpdate(satellite,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(satellite);
        p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.HELLSPEC.id);
        h.assertTrue(HellspecInfernalRifts.start(p,p.getMainHandItem()),"copied rifts starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(satellite).isAir(),"copy excavates diagonal rift arm");finish(h,p);
    }
    @GameTest(template="empty",batch="hellspec_617_03") public static void infernalStartRejectsCallbackReentryAndRelocation(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!HellspecInfernalRifts.start(p,p.getMainHandItem()),"recursive rifts refused");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,callback);
        try{h.assertTrue(!HellspecInfernalRifts.start(p,p.getMainHandItem())&&calls[0]==1&&!WorkQueue.busy(p),"permission callback cannot redirect rift origin");}finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="hellspec_618_eruption") public static void secondaryWidensUpwardWithoutRemovingLowExterior(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var at=target(h,p);var flare=at.offset(11,18,0);var outside=at.east(11);var ore=at.above(10);
        for(var cell:java.util.List.of(flare,outside)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(ore);
        h.assertTrue(HellspecEruption.start(p,p.getMainHandItem()),"public eruption starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(flare).isAir()&&h.getLevel().getBlockState(ore).isAir()&&h.getLevel().getBlockState(outside).is(Blocks.STONE),"eruption harvests rising flare and ore but preserves low exterior");finish(h,p);
    }
}
