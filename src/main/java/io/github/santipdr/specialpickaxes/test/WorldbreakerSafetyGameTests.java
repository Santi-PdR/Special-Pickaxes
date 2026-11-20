package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class WorldbreakerSafetyGameTests {
    @GameTest(template="empty",batch="worldbreaker_625_region") public static void regionBreakAcceptsBreakableMaterialsAndPreservesPlayerStone(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var placed=at.east();var wood=at.east(2);var ore=at.east(3);
        h.getLevel().setBlockAndUpdate(placed,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        h.getLevel().setBlockAndUpdate(wood,Blocks.OAK_PLANKS.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(ore);
        var region=new RegionWork(new SelectionVolume(at,ore),null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,5,Blocks.STONE.defaultBlockState());
        try{h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,region),"region analysis starts");WorkQueue.tick();h.assertTrue(region.awaiting(),"explicit confirmation still required");region.confirm();drain(p);
            h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(ore).isAir()&&h.getLevel().getBlockState(placed).is(Blocks.STONE)&&h.getLevel().getBlockState(wood).isAir(),"region harvests breakable materials and ore while preserving placed blocks");
        }finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_625_callback") public static void regionBreakRechecksProvenanceInsideCurrentBreakCallback(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var region=new RegionWork(new SelectionVolume(at,at),null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,5,Blocks.STONE.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,region),"analysis starts");WorkQueue.tick();region.confirm();
        Consumer<BlockEvent.BreakEvent> mark=e->{if(e.getPlayer()==p&&e.getPos().equals(at))PlayerPlacedBlocks.get(h.getLevel()).mark(at);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,mark);
        try{drain(p);h.assertTrue(PlayerPlacedBlocks.get(h.getLevel()).contains(at)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"late provenance revokes current natural mutation");}
        finally{MinecraftForge.EVENT_BUS.unregister(mark);PlayerPlacedBlocks.get(h.getLevel()).unmark(at);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_625_shatter") public static void worldShatterStopsBeforePlayerBuiltStone(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);PlayerPlacedBlocks.get(h.getLevel()).mark(at);
        try{h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new DirectionalProgram(at,Direction.SOUTH,DirectionalProgram.Shape.WORLD_SHATTER)),"shatter preflight queues");drain(p);
            h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.STONE),"wedge never harvests player-built stone");
        }finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(at);finish(h,p);}
    }
}
