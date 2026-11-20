package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class ConnectedExcavationGameTests {
    @GameTest(template="empty",batch="front_01") public static void everyReworkedNativeStartsCloseAndExcavatesConnectedTerrain(GameTestHelper h){
        for(var kind:ArtifactKind.playableValues()){
            if(!ConnectedExcavation.handles(kind))continue;
            var p=player(h,kind);var at=target(h,p);
            for(int x=0;x<=4;x++)h.getLevel().setBlockAndUpdate(at.east(x),Blocks.TERRACOTTA.defaultBlockState());
            var island=at.east(8);h.getLevel().setBlockAndUpdate(island,Blocks.DIAMOND_ORE.defaultBlockState());
            h.assertTrue(ArtifactActions.primary(p,p.getMainHandItem(),kind),"native front starts: "+kind);
            for(int i=0;i<12;i++)WorkQueue.tick();
            h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(at.east(4)).isAir(),"front and connected bank are cleared: "+kind);
            h.assertTrue(h.getLevel().getBlockState(island).is(Blocks.DIAMOND_ORE),"no jump through air to a remote island: "+kind);
            WorkQueue.cancel(p);MiningObservations.forget(p);
        }h.succeed();
    }
    @GameTest(template="empty",batch="front_02") public static void frontalSkillsRefuseToBeginTwentyBlocksAway(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
        var far=at.south(16);h.getLevel().setBlockAndUpdate(far,Blocks.STONE.defaultBlockState());
        h.assertTrue(!ConnectedExcavation.start(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,true)&&!WorkQueue.busy(p),"no twenty-block remote activation");finish(h,p);
    }
    @GameTest(template="empty",batch="front_03",timeoutTicks=200) public static void activeExcavationBreaksSandAndGravelFallingIntoPreviouslyClearedCells(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);h.getLevel().setBlock(at.below(),Blocks.BEDROCK.defaultBlockState(),2);
        h.assertTrue(ConnectedExcavation.start(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,true),"front starts");for(int i=0;i<64;i++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&WorkQueue.busy(p),"cleared cell remains watched during settling");
        h.runAtTickTime(5,()->{h.getLevel().setBlock(at.above(5),Blocks.SAND.defaultBlockState(),2);FallingBlockEntity.fall(h.getLevel(),at.above(5),Blocks.SAND.defaultBlockState());});
        h.runAtTickTime(30,()->{h.getLevel().setBlock(at.above(5),Blocks.GRAVEL.defaultBlockState(),2);FallingBlockEntity.fall(h.getLevel(),at.above(5),Blocks.GRAVEL.defaultBlockState());});
        h.runAtTickTime(110,()->{
            h.assertTrue(h.getLevel().getBlockState(at).isAir(),"landed sand and gravel have both been excavated");
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(3));
            h.assertTrue(drops.stream().anyMatch(d->d.getItem().is(Items.SAND)),"falling sand generated native harvested loot");
            h.assertTrue(drops.stream().anyMatch(d->d.getItem().is(Items.GRAVEL)||d.getItem().is(Items.FLINT)),"falling gravel generated native harvested loot; busy="+WorkQueue.busy(p)+" blocks="+h.getLevel().getBlockState(at)+" falling="+h.getLevel().getEntitiesOfClass(FallingBlockEntity.class,new AABB(at).inflate(8)).size()+" drops="+drops.stream().map(d->d.getItem().toString()).toList());
            finish(h,p);
        });
    }
    @GameTest(template="empty",batch="front_04") public static void equippedSourceRemovalCancelsSettlingAndProtectsNewBlocks(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var source=p.getMainHandItem();var slot=new AtomicReference<ItemStack>(source);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(CuriosMiningActive.activate(p,source,slot::get),"equipped connected excavation starts");for(int i=0;i<64;i++)WorkQueue.tick();
        slot.set(ItemStack.EMPTY);h.getLevel().setBlock(at,Blocks.GRAVEL.defaultBlockState(),2);for(int i=0;i<64;i++)WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).is(Blocks.GRAVEL),"revoked settling job cannot harvest a newly arrived block");finish(h,p);
    }
    @GameTest(template="empty",batch="front_05") public static void frontRetainsClaimProvenanceInventoryAndFluidSafety(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var at=target(h,p);var placed=at.east();var chest=at.west();var water=at.above();var denied=at.south();
        h.getLevel().setBlockAndUpdate(placed,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());h.getLevel().setBlockAndUpdate(water,Blocks.WATER.defaultBlockState());h.getLevel().setBlockAndUpdate(denied,Blocks.STONE.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> veto=e->{if(e.getPlayer()==p&&e.getPos().equals(denied))e.setCanceled(true);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,veto);
        try{
            h.assertTrue(ConnectedExcavation.start(p,p.getMainHandItem(),ArtifactKind.HELLSPEC,true),"front starts");for(int i=0;i<64;i++)WorkQueue.tick();
            h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(placed).is(Blocks.STONE)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST)
                    &&h.getLevel().getBlockState(water).is(Blocks.WATER)&&h.getLevel().getBlockState(denied).is(Blocks.STONE),"all mutation boundaries remain enforced");
        }finally{MinecraftForge.EVENT_BUS.unregister(veto);PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="front_06") public static void paradoxAlternateClearsStaleSelectionStatusAndKeepsFastManualMining(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h,p);var tool=p.getMainHandItem();ArtifactInteraction.use(p,tool,ArtifactKind.CRUCIBLE,false);
        h.assertTrue(ArtifactInteraction.selecting(p),"corner selection armed before switching action");
        h.assertTrue(ConnectedExcavation.start(p,tool,ArtifactKind.CRUCIBLE,true)&&!ArtifactInteraction.selecting(p),"accepted alternate clears server selection");
        WorkQueue.tick();ArtifactInteraction.display(p,tool,ArtifactKind.CRUCIBLE);
        h.assertTrue(tool.getDestroySpeed(Blocks.STONE.defaultBlockState())>=72,"executing alternate does not leave item at speed one");
        WorkQueue.cancel(p);ArtifactInteraction.display(p,tool,ArtifactKind.CRUCIBLE);
        h.assertTrue(tool.getDestroySpeed(Blocks.STONE.defaultBlockState())>=72,"finished alternate keeps full manual speed");finish(h,p);
    }
}
