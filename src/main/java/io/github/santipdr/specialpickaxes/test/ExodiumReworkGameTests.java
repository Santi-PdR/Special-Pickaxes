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
public final class ExodiumReworkGameTests {
    @GameTest(template="empty",batch="exodium_615_01") public static void meteorShowerCutsCentralAndSatelliteBowlsButNotBuildsOrExterior(GameTestHelper h){
        var p=player(h,ArtifactKind.EXODIUM);var at=target(h,p);var level=h.getLevel();
        var central=at.below(10);var satellite=at.east(12).below(6);var ore=at.west(12).below(4);var placed=at.north(12);var outside=at.offset(12,0,12);
        for(var cell:java.util.List.of(central,satellite,placed,outside)){level.setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(ore);PlayerPlacedBlocks.get(level).mark(placed);
        try{h.assertTrue(ExodiumMeteorShower.start(p,p.getMainHandItem()),"public meteor shower starts");drain(p);
            h.assertTrue(level.getBlockState(at).isAir()&&level.getBlockState(central).isAir()&&level.getBlockState(satellite).isAir()&&level.getBlockState(ore).isAir(),"central and satellite impacts harvest natural rock and ore");
            h.assertTrue(level.getBlockState(placed).is(Blocks.STONE)&&level.getBlockState(outside).is(Blocks.STONE),"player builds and exterior remain intact");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="exodium_615_02") public static void worldbreakerHandCopyUsesMeteorBowlsRatherThanALance(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var satellite=at.east(12).below(5);h.getLevel().setBlockAndUpdate(satellite,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(satellite);
        p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.EXODIUM.id);
        h.assertTrue(ExodiumMeteorShower.start(p,p.getMainHandItem()),"copied shower starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(satellite).isAir(),"copy excavates off-axis satellite impact");finish(h,p);
    }
    @GameTest(template="empty",batch="exodium_615_03") public static void meteorStartRejectsCallbackReentryAndRelocation(GameTestHelper h){
        var p=player(h,ArtifactKind.EXODIUM);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!ExodiumMeteorShower.start(p,p.getMainHandItem()),"recursive shower refused");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,callback);
        try{h.assertTrue(!ExodiumMeteorShower.start(p,p.getMainHandItem())&&calls[0]==1&&!WorkQueue.busy(p),"permission callback cannot redirect meteor origin");}finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="exodium_616_basin") public static void secondaryOpensABroadDeepBasinAndPreservesItsExterior(GameTestHelper h){
        var p=player(h,ArtifactKind.EXODIUM);var at=target(h,p);var deep=at.below(16);var wide=at.east(12).below(3);var outside=at.east(16).below();
        for(var cell:java.util.List.of(deep,wide,outside)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        h.assertTrue(ExodiumImpactBasin.start(p,p.getMainHandItem()),"public impact basin starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(deep).isAir()&&h.getLevel().getBlockState(wide).isAir()&&h.getLevel().getBlockState(outside).is(Blocks.STONE),"wide paraboloid opens deeply without becoming a shaft or removing exterior");finish(h,p);
    }
}
