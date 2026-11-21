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
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);
        sealedFallingFixture(h,at);
        h.assertTrue(ConnectedExcavation.start(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,true),"front starts");for(int i=0;i<64;i++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&WorkQueue.busy(p),"cleared cell remains watched during settling");
        long started=ArtifactState.now(p);var trace=new StringBuilder();
        for(int tick:new int[]{10,25,50,80,109})h.runAtTickTime(tick,()->trace.append(" tick=").append(tick)
                .append(" age=").append(ArtifactState.now(p)-started).append(" busy=").append(WorkQueue.busy(p))
                .append(" block=").append(h.getLevel().getBlockState(at))
                .append(" falling=").append(h.getLevel().getEntitiesOfClass(FallingBlockEntity.class,new AABB(at).inflate(8)).stream()
                        .limit(8).map(e->e.getBlockState()+"@"+e.position()).toList()));
        h.runAtTickTime(5,()->{h.getLevel().setBlock(at.above(5),Blocks.SAND.defaultBlockState(),2);FallingBlockEntity.fall(h.getLevel(),at.above(5),Blocks.SAND.defaultBlockState());});
        h.runAtTickTime(30,()->{h.getLevel().setBlock(at.above(5),Blocks.GRAVEL.defaultBlockState(),2);FallingBlockEntity.fall(h.getLevel(),at.above(5),Blocks.GRAVEL.defaultBlockState());});
        h.runAtTickTime(110,()->{
            h.assertTrue(h.getLevel().getBlockState(at).isAir(),"landed sand and gravel have both been excavated; "+trace+" finalAge="+(ArtifactState.now(p)-started)+" busy="+WorkQueue.busy(p)+" block="+h.getLevel().getBlockState(at));
            var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(7));
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
    @GameTest(template="empty",batch="front_07") public static void liveFrontContinuesThroughDenseLootWithoutHighlightingOrdinaryDrops(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var level=h.getLevel();
        var drops=new java.util.ArrayList<ItemEntity>();
        for(int x=0;x<=4;x++)level.setBlockAndUpdate(at.east(x),Blocks.TERRACOTTA.defaultBlockState());
        try{
            for(int i=0;i<256;i++){
                var drop=new ItemEntity(level,at.getX()+.5,at.getY()+.5,at.getZ()+.5,new ItemStack(Items.COBBLESTONE));
                drops.add(drop);level.addFreshEntity(drop);
            }
            h.assertTrue(WorldSafety.dropPressure(p,at),"fixture actually reaches the dense loot threshold");
            h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.EVENTIDE),"live alternate starts despite dense loot");
            for(int i=0;i<64;i++)WorkQueue.tick();
            h.assertTrue(level.getBlockState(at).isAir()&&level.getBlockState(at.east(4)).isAir(),"connected bank excavates while all existing loot remains");
            h.assertTrue(!WorkQueue.pauseReason(p).equals("drops")&&drops.stream().allMatch(ItemEntity::isAlive),"no loot pause or deletion");
            var nativeDrops=level.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(8));
            h.assertTrue(nativeDrops.stream().anyMatch(d->d.getItem().is(Items.TERRACOTTA)),"native terracotta loot is produced");
            h.assertTrue(nativeDrops.stream().noneMatch(ItemEntity::isCurrentlyGlowing),"neither fresh ordinary drops nor existing loot are highlighted");
        }finally{
            WorkQueue.cancel(p);MiningObservations.forget(p);
            for(var drop:level.getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(8)))drop.discard();
        }
        finish(h,p);
    }

    @GameTest(template="empty",batch="front_08") public static void saturatedFrontStillHarvestsArrivalsWithoutExpandingOrIgnoringProvenance(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var level=h.getLevel();
        var terrain=new java.util.ArrayList<BlockPos>();
        for(int x=0;x<5;x++)for(int y=0;y<3;y++)for(int z=0;z<4;z++){
            var cell=at.offset(x,y,z);terrain.add(cell);PlayerPlacedBlocks.get(level).unmark(cell);
            level.setBlock(cell,Blocks.STONE.defaultBlockState(),2);
        }
        int previous=ArtifactConfig.JOB_LIMIT.get();
        try{
            ArtifactConfig.JOB_LIMIT.set(32);
            h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.EVENTIDE),"live front starts with a finite terrain budget");
        }finally{ArtifactConfig.JOB_LIMIT.set(previous);}
        try{
            for(int i=0;i<64;i++)WorkQueue.tick();
            h.assertTrue(terrain.stream().filter(cell->level.getBlockState(cell).isAir()).count()==32,"terrain budget is enforced exactly");
            h.assertTrue(WorkQueue.busy(p)&&level.getBlockState(at).isAir(),"saturated front still watches its cleared origin");
            level.setBlock(at,Blocks.GRAVEL.defaultBlockState(),2);
            for(int i=0;i<64;i++)WorkQueue.tick();
            h.assertTrue(level.getBlockState(at).isAir(),"arrival in a cleared cell is harvested after terrain budget exhaustion");
            h.assertTrue(terrain.stream().filter(cell->level.getBlockState(cell).is(Blocks.STONE)).count()==28,"settling never resumes untouched terrain expansion");
            level.setBlock(at,Blocks.GRAVEL.defaultBlockState(),2);PlayerPlacedBlocks.get(level).mark(at);
            for(int i=0;i<64;i++)WorkQueue.tick();
            h.assertTrue(level.getBlockState(at).is(Blocks.GRAVEL),"settling still protects newly placed blocks");
            PlayerPlacedBlocks.get(level).unmark(at);drain(p);
            h.assertTrue(!WorkQueue.busy(p),"settling remains finite after the front saturates");
        }finally{PlayerPlacedBlocks.get(level).unmark(at);WorkQueue.cancel(p);MiningObservations.forget(p);}
        finish(h,p);
    }

    @GameTest(template="empty",batch="front_09") public static void liveFrontRevisitsArrivalsBeforeFinishingWithoutExceedingTickBudget(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var level=h.getLevel();
        for(int x=0;x<8;x++)for(int y=0;y<4;y++)for(int z=0;z<8;z++){
            var cell=at.offset(x,y,z);PlayerPlacedBlocks.get(level).unmark(cell);
            level.setBlock(cell,Blocks.STONE.defaultBlockState(),2);
        }
        int previous=ArtifactConfig.GLOBAL.get();
        try{
            ArtifactConfig.GLOBAL.set(1);
            h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.EVENTIDE),"live alternate starts on a large connected bank");
            WorkQueue.tick();
            h.assertTrue(level.getBlockState(at).isAir(),"first budgeted attempt clears the close origin");
            level.setBlock(at,Blocks.GRAVEL.defaultBlockState(),2);
            for(int tick=0;tick<16;tick++){
                WorkQueue.tick();h.assertTrue(WorkQueue.lastAttempts()<=1,"revisits share the one-attempt scheduler budget");
            }
            h.assertTrue(level.getBlockState(at).isAir(),"arrival is harvested while the excavation frontier is still live");
            h.assertTrue(WorkQueue.busy(p)&&level.getBlockState(at.offset(7,3,7)).is(Blocks.STONE),"front has not finished or jumped to distant terrain");
        }finally{ArtifactConfig.GLOBAL.set(previous);WorkQueue.cancel(p);MiningObservations.forget(p);}
        finish(h,p);
    }

    @GameTest(template="empty",batch="front_10") public static void smallSettlingJobLeavesSameTickBudgetForAnotherPlayersLiveFront(GameTestHelper h){
        var idle=player(h,ArtifactKind.EVENTIDE);target(h,idle);
        var miner=player(h,ArtifactKind.HELLSPEC);miner.setPos(miner.position().add(20,0,0));var at=target(h,miner);
        for(int x=0;x<=4;x++)h.getLevel().setBlockAndUpdate(at.east(x),Blocks.TERRACOTTA.defaultBlockState());
        int previous=ArtifactConfig.GLOBAL.get();
        try{
            ArtifactConfig.GLOBAL.set(16);
            h.assertTrue(ArtifactActions.useAlternate(idle,idle.getMainHandItem(),ArtifactKind.EVENTIDE),"first player starts a small front");
            for(int tick=0;tick<4;tick++)WorkQueue.tick();
            h.assertTrue(WorkQueue.busy(idle),"first player is still watching for falling arrivals");
            h.assertTrue(ArtifactActions.useAlternate(miner,miner.getMainHandItem(),ArtifactKind.HELLSPEC),"second player starts a connected bank");
            WorkQueue.tick();
            h.assertTrue(h.getLevel().getBlockState(at).isAir(),"second player mines in the same tick after the idle watch");
            h.assertTrue(WorkQueue.busy(idle),"sharing budget does not cancel the first player's settling watch");
            h.assertTrue(WorkQueue.lastAttempts()<=16,"both players share the existing global bound");
        }finally{
            ArtifactConfig.GLOBAL.set(previous);WorkQueue.cancel(idle);WorkQueue.cancel(miner);
            MiningObservations.forget(idle);MiningObservations.forget(miner);
        }
        finish(h,idle);
    }

    @GameTest(template="empty",batch="front_11",timeoutTicks=280) public static void delayedFirstInspectionCannotMineAfterHardDeadline(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);
        // Direct program: deliberately withhold all scheduler/progress calls until expiry.
        var program=new ConnectedExcavation(p,at,ArtifactKind.EVENTIDE,true,null);
        h.runAfterDelay(241,()->{
            try{
                boolean mined=program.next(p).apply(p,p.getMainHandItem(),ArtifactKind.EVENTIDE);
                h.assertTrue(!mined&&h.getLevel().getBlockState(at).is(Blocks.STONE),"stale done clock cannot authorize one late terrain break");
                h.assertTrue(program.done(),"expired program is finished after refreshing time");
                finish(h,p);
            }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        });
    }

    @GameTest(template="empty",batch="front_12",timeoutTicks=180) public static void delayedSettlingInspectionCannotHarvestAfterItsWindow(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);
        var program=new ConnectedExcavation(p,at,ArtifactKind.EVENTIDE,true,null);
        // Seed and six neighbors, then hold a revisit across the final deadline.
        for(int i=0;i<7;i++)program.next(p).apply(p,p.getMainHandItem(),ArtifactKind.EVENTIDE);
        var delayed=program.next(p);
        h.assertTrue(delayed.pos().equals(at),"delayed inspection targets the mined seed");
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&!program.done(),"seed was cleared and observation is live");
        h.runAfterDelay(121,()->{
            try{
                h.getLevel().setBlock(at,Blocks.GRAVEL.defaultBlockState(),2);
                boolean mined=delayed.apply(p,p.getMainHandItem(),ArtifactKind.EVENTIDE);
                h.assertTrue(!mined&&h.getLevel().getBlockState(at).is(Blocks.GRAVEL),"expired observation cannot harvest a late arrival");
                h.assertTrue(program.done(),"final window expiry finishes program");
                finish(h,p);
            }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        });
    }

    private static void sealedFallingFixture(GameTestHelper h,BlockPos at){
        // This high fixture can still intersect worldgen water/gravel depending on the
        // batch's world position. Seal outside fluids and falling terrain out; do not
        // change the ability's fluid barriers, landing physics or settling deadline.
        for(int x=-8;x<=8;x++)for(int y=-1;y<=8;y++)for(int z=-8;z<=8;z++){
            var cell=at.offset(x,y,z);boolean shell=Math.abs(x)==8||Math.abs(z)==8||y==-1||y==8;
            h.getLevel().setBlock(cell,shell?Blocks.BEDROCK.defaultBlockState():Blocks.AIR.defaultBlockState(),2);
            PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);
        }
        h.getLevel().setBlock(at,Blocks.STONE.defaultBlockState(),2);
    }
    private static void verifyOtherFallingArrival(GameTestHelper h,boolean equipped,boolean anvil){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);sealedFallingFixture(h,at);
        var source=p.getMainHandItem();var slot=new AtomicReference<ItemStack>(source);
        if(equipped){
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(anvil?Items.DIAMOND_PICKAXE:Items.DIAMOND_SHOVEL));
            // The equipped route keeps the real held tool: a shovel starts on dirt,
            // rather than asking it to harvest a stone seed before observing powder.
            if(!anvil)h.getLevel().setBlock(at,Blocks.DIRT.defaultBlockState(),2);
        }
        boolean started=equipped?CuriosMiningActive.activate(p,source,slot::get):ConnectedExcavation.start(p,source,ArtifactKind.EVENTIDE,true);
        h.assertTrue(started,"falling arrival observation starts in requested held/equipped route");
        for(int i=0;i<64;i++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&WorkQueue.busy(p),"origin is cleared and watched before the arrival exists");
        var falling=anvil?Blocks.ANVIL.defaultBlockState():Blocks.RED_CONCRETE_POWDER.defaultBlockState();
        h.runAtTickTime(5,()->{
            h.getLevel().setBlock(at.above(5),falling,2);
            FallingBlockEntity.fall(h.getLevel(),at.above(5),falling);
        });
        h.runAtTickTime(80,()->{
            try{
                h.assertTrue(h.getLevel().getBlockState(at).isAir(),"arrived falling material was harvested");
                var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(7));
                var harvested=drops.stream().filter(d->anvil?(d.getItem().is(Items.ANVIL)||d.getItem().is(Items.CHIPPED_ANVIL)||d.getItem().is(Items.DAMAGED_ANVIL)):d.getItem().is(Items.RED_CONCRETE_POWDER)).toList();
                h.assertTrue(!harvested.isEmpty(),"native harvested drops exist for the falling material");
                h.assertTrue(harvested.stream().noneMatch(ItemEntity::isCurrentlyGlowing),"ordinary falling-block drops never receive extractor highlighting");
                h.assertTrue(h.getLevel().getEntitiesOfClass(FallingBlockEntity.class,new AABB(at).inflate(7)).isEmpty(),"assertion observes completed fall, not a still airborne block");
                finish(h,p);
            }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        });
    }
    @GameTest(template="empty",batch="front_falling_powder_held",timeoutTicks=150) public static void heldExcavationHarvestsRealFallingConcretePowder(GameTestHelper h){verifyOtherFallingArrival(h,false,false);}
    @GameTest(template="empty",batch="front_falling_anvil_held",timeoutTicks=150) public static void heldExcavationHarvestsRealFallingAnvil(GameTestHelper h){verifyOtherFallingArrival(h,false,true);}
    @GameTest(template="empty",batch="front_falling_powder_equipped",timeoutTicks=150) public static void equippedExcavationWithShovelHarvestsRealFallingConcretePowder(GameTestHelper h){verifyOtherFallingArrival(h,true,false);}
    @GameTest(template="empty",batch="front_falling_anvil_equipped",timeoutTicks=150) public static void equippedExcavationWithPickHarvestsRealFallingAnvil(GameTestHelper h){verifyOtherFallingArrival(h,true,true);}

    private static void verifySettlingPreservesNewFluid(GameTestHelper h,boolean lava){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);sealedFallingFixture(h,at);
        h.assertTrue(ConnectedExcavation.start(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,true),"connected front starts");
        for(int i=0;i<64;i++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&WorkQueue.busy(p),"cleared cell remains under observation");
        var fluid=lava?Blocks.LAVA:Blocks.WATER;
        h.getLevel().setBlockAndUpdate(at,fluid.defaultBlockState());
        h.runAfterDelay(40,()->{
            try{
                h.assertTrue(h.getLevel().getBlockState(at).is(fluid)&&h.getLevel().getFluidState(at).isSource(),"settling never harvests or replaces newly arrived fluid source");
                h.assertTrue(WorkQueue.busy(p),"fluid barrier does not cancel the finite observation job");
                h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(7)).stream()
                        .noneMatch(d->d.getItem().is(lava?Items.LAVA_BUCKET:Items.WATER_BUCKET)),"ordinary mining does not synthesize fluid loot");
                finish(h,p);
            }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        });
    }
    @GameTest(template="empty",batch="front_arrival_water",timeoutTicks=100) public static void settlingPreservesWaterArrivingAfterExcavation(GameTestHelper h){verifySettlingPreservesNewFluid(h,false);}
    @GameTest(template="empty",batch="front_arrival_lava",timeoutTicks=100) public static void settlingPreservesLavaArrivingAfterExcavation(GameTestHelper h){verifySettlingPreservesNewFluid(h,true);}

}
