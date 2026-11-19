package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class WorldloomHarvestGameTests {
    @GameTest(template="empty",batch="worldloom_harvest_01") public static void secondaryHarvestsExposedOreWithoutBuildingOrPaying(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var at=target(h,p);var level=h.getLevel();var ore=at.east();var buried=at.east(5);var placed=at.west();
        for(var cell:java.util.List.of(ore,buried,placed)){level.setBlockAndUpdate(cell,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(cell);}
        for(var direction:Direction.values())level.setBlockAndUpdate(buried.relative(direction),Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).mark(placed);
        var material=new ItemStack(Items.COBBLESTONE,31);material.setHoverName(net.minecraft.network.chat.Component.literal("Not payment"));p.setItemInHand(InteractionHand.OFF_HAND,material);
        try{h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM),"secondary accepts natural target independently of offhand");drain(p);
            h.assertTrue(level.getBlockState(ore).isAir()&&level.getBlockState(buried).is(Blocks.DIAMOND_ORE)&&level.getBlockState(placed).is(Blocks.DIAMOND_ORE)&&level.getBlockState(at).is(Blocks.STONE),"exposed ore only; buried ore, builds and matrix preserved");
            h.assertTrue(material.getCount()==31,"no construction payment");
        }finally{PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldloom_harvest_02") public static void exposureIsRecheckedAfterExternalBreakCallback(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var at=target(h,p);var level=h.getLevel();level.setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());
        for(var direction:Direction.values())level.setBlockAndUpdate(at.relative(direction),direction==Direction.UP?Blocks.AIR.defaultBlockState():Blocks.STONE.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,new WorldloomHarvest(at)),"harvest queued");
        Consumer<BlockEvent.BreakEvent> seal=e->{if(e.getPlayer()==p&&e.getPos().equals(at))level.setBlockAndUpdate(at.above(),Blocks.STONE.defaultBlockState());};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,seal);
        try{drain(p);h.assertTrue(level.getBlockState(at).is(Blocks.DIAMOND_ORE),"ore sealed during callback is not harvested");}finally{MinecraftForge.EVENT_BUS.unregister(seal);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldloom_harvest_03") public static void copiedHarvestNeverFallsBackToPaidBridge(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.WORLDLOOM.id);
        h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER),"copied harvest requires no building material");drain(p);h.assertTrue(h.getLevel().getBlockState(at).isAir(),"copied secondary harvests exposed ore");finish(h,p);
    }
}
