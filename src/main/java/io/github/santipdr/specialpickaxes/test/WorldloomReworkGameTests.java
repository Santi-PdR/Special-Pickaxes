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
public final class WorldloomReworkGameTests {
    @GameTest(template="empty",batch="worldloom_610_01") public static void primaryOpensTerracesDownwardAndExposesNaturalOre(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var at=target(h,p);var level=h.getLevel();
        var deep=at.below(10);var edge=at.east(12);var gap=edge.below(3);var ore=at.east().below();var placed=at.west();var roof=at.above(3);
        for(var cell:java.util.List.of(deep,edge,gap,placed,roof)){level.setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(ore);PlayerPlacedBlocks.get(level).mark(placed);
        try{h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,false),"public terraced quarry starts");drain(p);
            h.assertTrue(level.getBlockState(at).isAir()&&level.getBlockState(deep).isAir()&&level.getBlockState(edge).isAir(),"surface and deep terraces excavate");
            h.assertTrue(level.getBlockState(gap).is(Blocks.STONE)&&level.getBlockState(roof).is(Blocks.STONE)&&level.getBlockState(ore).is(Blocks.DIAMOND_ORE)&&level.getBlockState(placed).is(Blocks.STONE),"terrace walls, ceiling, ore and player builds remain");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldloom_610_02") public static void permissionCallbackCannotReenterOrRedirectQuarryStart(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!WorldloomQuarry.start(p,p.getMainHandItem()),"nested start refused");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,callback);
        try{h.assertTrue(!WorldloomQuarry.start(p,p.getMainHandItem())&&calls[0]==1&&!WorkQueue.busy(p),"changed actor context refuses quarry");}
        finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldloom_610_03") public static void worldbreakerHandCopyUsesTheTerracedQuarry(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var deep=at.below(8);h.getLevel().setBlockAndUpdate(deep,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(deep);
        p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.WORLDLOOM.id);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,false),"copied quarry starts");drain(p);h.assertTrue(h.getLevel().getBlockState(deep).isAir(),"copy excavates downward instead of boring ahead");finish(h,p);
    }
}
