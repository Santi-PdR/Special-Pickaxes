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
public final class WorldbreakerReworkGameTests {
    @GameTest(template="empty",batch="worldbreaker_619_01") public static void nativeCataclysmHarvestsABroadChamberAndPreservesItsExterior(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var level=h.getLevel();
        var central=at.below(8);var satellite=at.offset(12,-6,4);var ore=at.west(12).below(4);var placed=at.north(12);var outside=at.offset(16,0,16);
        for(var cell:java.util.List.of(central,satellite,placed,outside)){level.setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(ore);PlayerPlacedBlocks.get(level).mark(placed);
        try{h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,false),"public cataclysm starts");drain(p);
            h.assertTrue(level.getBlockState(at).isAir()&&level.getBlockState(central).isAir()&&level.getBlockState(satellite).isAir()&&level.getBlockState(ore).isAir(),"chamber harvest natural rock and ore");
            h.assertTrue(level.getBlockState(placed).is(Blocks.STONE)&&level.getBlockState(outside).is(Blocks.STONE),"player builds and exterior remain intact");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_619_03") public static void cataclysmStartRejectsCallbackReentryAndRelocation(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!WorldbreakerCataclysm.start(p,p.getMainHandItem()),"recursive rifts refused");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,callback);
        try{h.assertTrue(!WorldbreakerCataclysm.start(p,p.getMainHandItem())&&calls[0]==1&&!WorkQueue.busy(p),"permission callback cannot redirect cataclysm origin");}finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_620_shear") public static void nativeAlternateCutsThreeStrataButPreservesRockBetweenThem(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var upper=at.offset(20,6,0);var lower=at.offset(-20,-6,0);var between=at.offset(5,1,0);var placed=at.east(8);
        for(var cell:java.util.List.of(upper,between,placed)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        h.getLevel().setBlockAndUpdate(lower,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(lower);PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        try{h.assertTrue(ArtifactSkills.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER),"public native shear starts");drain(p);
            h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(upper).isAir()&&h.getLevel().getBlockState(lower).isAir(),"all three broad planes harvest natural rock and ore");
            h.assertTrue(h.getLevel().getBlockState(between).is(Blocks.STONE)&&h.getLevel().getBlockState(placed).is(Blocks.STONE),"intervening strata and builds preserved");
        }finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_620_copy") public static void copiedAlternateDoesNotFallBackToNativeShear(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var cross=at.above(10);var nativeOnly=at.offset(20,6,0);
        for(var cell:java.util.List.of(cross,nativeOnly)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);
        h.assertTrue(ArtifactSkills.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER),"copied cross starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(cross).isAir()&&h.getLevel().getBlockState(nativeOnly).is(Blocks.STONE)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"copied Axiom cross retains its core and geometry instead of native shear");finish(h,p);
    }
}
