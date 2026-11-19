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
public final class AxiomReworkGameTests {
    @GameTest(template="empty",batch="axiom_613_01") public static void nullChamberExcavatesFacetsAndPreservesCoreOreAndPlayerBuilds(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var at=target(h,p);var level=h.getLevel();var far=at.east(16);var core=at.east();var ore=at.east(5);var outside=at.offset(7,6,6);var placed=at.west(4);
        for(var cell:java.util.List.of(far,core,outside,placed)){level.setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(ore);PlayerPlacedBlocks.get(level).mark(placed);
        try{h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"public null implosion starts");drain(p);
            h.assertTrue(level.getBlockState(far).isAir()&&level.getBlockState(at).is(Blocks.STONE)&&level.getBlockState(core).is(Blocks.STONE)&&level.getBlockState(ore).is(Blocks.DIAMOND_ORE)&&level.getBlockState(outside).is(Blocks.STONE)&&level.getBlockState(placed).is(Blocks.STONE),"massive facets excavate while core, ore, exterior and provenance survive");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="axiom_613_02") public static void nullChamberCanAnchorOnOreWithoutHarvestingIt(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var shell=at.above(4);h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());h.getLevel().setBlockAndUpdate(shell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(shell);
        p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,false),"copied null chamber targets natural ore");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(shell).isAir(),"copy preserves ore anchor and clears distant natural shell");finish(h,p);
    }
    @GameTest(template="empty",batch="axiom_613_03") public static void nullChamberRejectsReentrantOrRelocatedActivation(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!AxiomNullChamber.start(p,p.getMainHandItem()),"reentrant start refused");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,callback);
        try{h.assertTrue(!AxiomNullChamber.start(p,p.getMainHandItem())&&calls[0]==1&&!WorkQueue.busy(p),"permission callback cannot redirect origin");}finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="axiom_614_cross") public static void secondaryCutsThreePlanesButLeavesDiagonalRockAndOre(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var at=target(h,p);var plane=at.east(8);var diagonal=at.offset(3,3,3);var ore=at.above(4);
        for(var cell:java.util.List.of(plane,diagonal)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactSkills.use(p,p.getMainHandItem(),ArtifactKind.AXIOM),"public secondary null cross starts on rock");drain(p);
        h.assertTrue(h.getLevel().getBlockState(plane).isAir()&&h.getLevel().getBlockState(diagonal).is(Blocks.STONE)&&h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"cross planes excavate while diagonal, core and ore survive");finish(h,p);
    }
}
