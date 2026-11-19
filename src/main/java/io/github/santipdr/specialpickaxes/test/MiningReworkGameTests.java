package io.github.santipdr.specialpickaxes.test;

import com.mojang.authlib.GameProfile;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.network.RelicNetwork;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.*;
import java.util.UUID;

/** New contracts, serialized after the historical batch to keep server-wide job limits realistic. */
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class MiningReworkGameTests {
    private static ServerPlayer player(GameTestHelper h,ArtifactKind kind){
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"MiningRework"));
        p.connection=new ServerGamePacketListenerImpl(h.getLevel().getServer(),new Connection(PacketFlow.SERVERBOUND),p);
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var at=h.absolutePos(new BlockPos(5,3,3));p.setPos(at.getX()+.5,at.getY(),at.getZ()+.5);p.setYRot(0);p.setXRot(0);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(kind).get()));p.getMainHandItem().getOrCreateTag().putUUID("controlIdentity",UUID.randomUUID());return p;
    }
    private static void finish(GameTestHelper h,ServerPlayer p){DropRecall.stop(p);RelicNetwork.forget(p);WorkQueue.cancel(p);ArtifactInteraction.clear(p);h.succeed();}
    @GameTest(template="empty",batch="rework_each_318") public static void seamExpandedExtractionStopsAt1024AndPreservesProtectedCells(GameTestHelper h){
        var p=player(h,ArtifactKind.SEAM_RIPPER);var level=h.getLevel();
        var center=h.absolutePos(new BlockPos(5,60,7));
        p.setPos(center.getX()+.5,center.getY(),center.getZ()-7.5);
        var placed=PlayerPlacedBlocks.get(level);
        for(int x=-5;x<=5;x++)for(int y=-5;y<=5;y++)for(int z=-5;z<=5;z++){
            var at=center.offset(x,y,z);placed.unmark(at);level.setBlockAndUpdate(at,Blocks.GRANITE.defaultBlockState());
        }
        var protectedPos=center.above();var ore=center.below();
        placed.mark(protectedPos);level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        try{
            h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER,
                    new SeamMiningProgram(center,Blocks.GRANITE.defaultBlockState())),"expanded seam queues");
            for(int tick=0;tick<1000&&WorkQueue.busy(p);tick++){
                for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(center).inflate(10)))drop.discard();
                if(WorkQueue.status(p).equals("paused"))WorkQueue.togglePause(p);
                WorkQueue.tick();
            }
            h.assertTrue(!WorkQueue.busy(p),"bounded search and mining finish");
            int removed=0;
            for(int x=-5;x<=5;x++)for(int y=-5;y<=5;y++)for(int z=-5;z<=5;z++)
                if(level.getBlockState(center.offset(x,y,z)).isAir())removed++;
            h.assertTrue(removed==1024,"exact enlarged mining limit, not the old256 or the entire1331 volume: "+removed);
            h.assertTrue(level.getBlockState(protectedPos).is(Blocks.GRANITE)&&level.getBlockState(ore).is(Blocks.DIAMOND_ORE),
                    "placed rock and ore survive the expanded extraction");
        }finally{placed.unmark(protectedPos);WorkQueue.cancel(p);RelicNetwork.forget(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_319") public static void worldloomExpandedBorePreservesOresAndPlacedBlocks(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var level=h.getLevel();
        var origin=h.absolutePos(new BlockPos(5,60,7));var placed=PlayerPlacedBlocks.get(level);
        p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-4.5);
        for(int depth=0;depth<48;depth++)for(var at:DirectionalProgram.section(origin,Direction.SOUTH,DirectionalProgram.Shape.WORLDLOOM_BORE,depth)){
            placed.unmark(at);level.setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());
        }
        var ore=origin.south(40);var owned=origin.south(41);
        var beyond=origin.south(48);var outside=origin.offset(4,0,20);
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());placed.mark(owned);
        level.setBlockAndUpdate(beyond,Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(outside,Blocks.STONE.defaultBlockState());
        try{
            h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,
                    new DirectionalProgram(origin,Direction.SOUTH,DirectionalProgram.Shape.WORLDLOOM_BORE,ArtifactKind.WORLDLOOM)),"expanded natural bore queues");
            for(int tick=0;tick<1000&&WorkQueue.busy(p);tick++){
                for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(origin.south(24)).inflate(30)))drop.discard();
                if(WorkQueue.status(p).equals("paused"))WorkQueue.togglePause(p);
                WorkQueue.tick();
            }
            h.assertTrue(!WorkQueue.busy(p),"expanded bore finishes under normal budgets");
            int removed=0;
            for(int depth=0;depth<48;depth++)for(var at:DirectionalProgram.section(origin,Direction.SOUTH,DirectionalProgram.Shape.WORLDLOOM_BORE,depth))
                if(level.getBlockState(at).isAir())removed++;
            h.assertTrue(removed==2350,"2352 planned cells minus two preserved cells: "+removed);
            h.assertTrue(level.getBlockState(ore).is(Blocks.DIAMOND_ORE)&&level.getBlockState(owned).is(Blocks.STONE),"ore and placed geology remain intact");
            h.assertTrue(level.getBlockState(beyond).is(Blocks.STONE)&&level.getBlockState(outside).is(Blocks.STONE),"exact depth and width boundaries");
        }finally{placed.unmark(owned);WorkQueue.cancel(p);RelicNetwork.forget(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_320") public static void worldloomExpandedSectionStopsBeforeMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var origin=h.absolutePos(new BlockPos(5,60,7));var level=h.getLevel();
        for(var at:DirectionalProgram.section(origin,Direction.SOUTH,DirectionalProgram.Shape.WORLDLOOM_BORE,0)){
            PlayerPlacedBlocks.get(level).unmark(at);level.setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());
        }
        var machine=origin.offset(3,3,0);level.setBlockAndUpdate(machine,Blocks.CHEST.defaultBlockState());
        var behind=origin.south();level.setBlockAndUpdate(behind,Blocks.STONE.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,
                new DirectionalProgram(origin,Direction.SOUTH,DirectionalProgram.Shape.WORLDLOOM_BORE,ArtifactKind.WORLDLOOM)),"machine barrier fixture queues");
        for(int tick=0;tick<100&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p)&&level.getBlockState(machine).is(Blocks.CHEST),"machine terminates route intact");
        h.assertTrue(level.getBlockState(origin).is(Blocks.STONE)&&level.getBlockState(behind).is(Blocks.STONE),"whole-section preflight prevents partial excavation through machine barrier");
        finish(h,p);
    }

    private static BlockPos longBridgeFixture(GameTestHelper h,ServerPlayer p){
        var base=h.absolutePos(new BlockPos(5,100,7));p.setPos(base.getX()+.5,base.getY(),base.getZ()+.5);p.setYRot(0);
        var floor=p.blockPosition().below();
        for(int depth=1;depth<=65;depth++)h.getLevel().setBlockAndUpdate(floor.south(depth),Blocks.AIR.defaultBlockState());
        return floor;
    }
    @GameTest(template="empty",batch="rework_each_321") public static void worldloomBridgePaysExactlyOneFullOffhandStackFor64Blocks(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var floor=longBridgeFixture(h,p);var level=h.getLevel();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Blocks.COBBLESTONE,64));
        var reserve=new ItemStack(Blocks.COBBLESTONE,64);p.getInventory().setItem(10,reserve);
        h.assertTrue(PickaxeGameTests.legacyPaidBridge(p),"long bridge starts");
        for(int tick=0;tick<100&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p)&&p.getOffhandItem().isEmpty()&&reserve.getCount()==64,"exactly the offhand stack is consumed");
        for(int depth=1;depth<=64;depth++)h.assertTrue(level.getBlockState(floor.south(depth)).is(Blocks.COBBLESTONE),"paid bridge reaches cell "+depth);
        h.assertTrue(level.getBlockState(floor.south(65)).isAir(),"never builds past the 64-block cap");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_322") public static void worldloomLongBridgeStopsAtLateClaimWithoutPayingBeyondIt(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var floor=longBridgeFixture(h,p);var level=h.getLevel();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Blocks.COBBLESTONE,64));var denied=floor.south(40);
        java.util.function.Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> veto=event->{
            if(event.player==p&&event.target.equals(denied))event.setCanceled(true);
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST,false,
                io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,veto);
        try{
            h.assertTrue(PickaxeGameTests.legacyPaidBridge(p),"late claim fixture queues");
            for(int tick=0;tick<100&&WorkQueue.busy(p);tick++)WorkQueue.tick();
            h.assertTrue(!WorkQueue.busy(p)&&p.getOffhandItem().getCount()==25,"only 39 accepted blocks are paid");
            h.assertTrue(level.getBlockState(floor.south(39)).is(Blocks.COBBLESTONE),"build reaches the claim boundary");
            for(int depth=40;depth<=65;depth++)h.assertTrue(level.getBlockState(floor.south(depth)).isAir(),"claim cannot be skipped at cell "+depth);
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(veto);WorkQueue.cancel(p);RelicNetwork.forget(p);}
        h.succeed();
    }

    private static void expandedNaturalVolume(GameTestHelper h,ExcavationVolume.Shape shape){
        var kind=shape==ExcavationVolume.Shape.VAULT?ArtifactKind.CHOIR:ArtifactKind.HELLSPEC;
        var p=player(h,kind);var level=h.getLevel();var origin=h.absolutePos(new BlockPos(5,100,7));
        p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-3.5);
        for(int index=0;index<shape.volume;index++){
            var at=ExcavationVolume.cell(origin,Direction.SOUTH,shape,index);
            PlayerPlacedBlocks.get(level).unmark(at);level.setBlockAndUpdate(at,Blocks.GRANITE.defaultBlockState());
        }
        var placed=ExcavationVolume.cell(origin,Direction.SOUTH,shape,1);
        var denied=ExcavationVolume.cell(origin,Direction.SOUTH,shape,2);
        var machine=ExcavationVolume.cell(origin,Direction.SOUTH,shape,3);
        var bedrock=ExcavationVolume.cell(origin,Direction.SOUTH,shape,5);
        var ore=ExcavationVolume.cell(origin,Direction.SOUTH,shape,6);
        PlayerPlacedBlocks.get(level).mark(placed);level.setBlockAndUpdate(machine,Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(bedrock,Blocks.BEDROCK.defaultBlockState());level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        java.util.function.Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> veto=event->{
            if(event.player==p&&event.target.equals(denied))event.setCanceled(true);
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST,false,
                io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,veto);
        try{
            h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),kind,new NaturalExcavationProgram(origin,Direction.SOUTH,shape)),"streaming volume queues");
            h.assertTrue(WorkQueue.remaining(p)==shape.volume&&level.getBlockState(origin).is(Blocks.GRANITE),"activation does not eagerly excavate");
            WorkQueue.tick();
            h.assertTrue(WorkQueue.completed(p)<=EnchantmentScaling.budget(p.getMainHandItem()),"first turn respects player budget");
            for(int tick=0;tick<1000&&WorkQueue.busy(p);tick++){
                for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                        new net.minecraft.world.phys.AABB(origin).inflate(60)))drop.discard();
                if(WorkQueue.status(p).equals("paused"))WorkQueue.togglePause(p);
                WorkQueue.tick();
            }
            h.assertTrue(!WorkQueue.busy(p),"streamed volume finishes");
            int removed=0;
            for(int index=0;index<shape.volume;index++)if(level.getBlockState(ExcavationVolume.cell(origin,Direction.SOUTH,shape,index)).isAir())removed++;
            h.assertTrue(removed==shape.volume-4,"exact excavation with four safety exclusions: "+removed);
            h.assertTrue(level.getBlockState(placed).is(Blocks.GRANITE)&&level.getBlockState(denied).is(Blocks.GRANITE)
                    &&level.getBlockState(machine).is(Blocks.CHEST)&&level.getBlockState(bedrock).is(Blocks.BEDROCK),"placed, claim, machine and bedrock survive");
            h.assertTrue(level.getBlockState(ore).isAir(),"eligible ore is harvested with the rock");
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(veto);PlayerPlacedBlocks.get(level).unmark(placed);WorkQueue.cancel(p);RelicNetwork.forget(p);}
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_323") public static void choirVaultStreams2832CellsWithSafetyExclusions(GameTestHelper h){expandedNaturalVolume(h,ExcavationVolume.Shape.VAULT);}
    @GameTest(template="empty",batch="rework_each_324") public static void hellspecChimneyStreams5808CellsWithSafetyExclusions(GameTestHelper h){expandedNaturalVolume(h,ExcavationVolume.Shape.CHIMNEY);}
    @GameTest(template="empty",batch="rework_each_325") public static void naturalExcavationSkipsMissingChunksWithoutLoadingThem(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var remote=new BlockPos(1200000,80,1200000);var level=h.getLevel();
        h.assertTrue(!level.hasChunkAt(remote),"fixture is unloaded");
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CHOIR,new NaturalExcavationProgram(remote,Direction.SOUTH,ExcavationVolume.Shape.VAULT)),"unloaded scan queues");
        for(int tick=0;tick<500&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p)&&!level.hasChunkAt(remote),"bounded volume does not force chunks");finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_326") public static void naturalExcavationBackpressurePreservesCursorAndResumes(GameTestHelper h){
        for(var shape:ExcavationVolume.Shape.values()){
            var kind=shape==ExcavationVolume.Shape.VAULT?ArtifactKind.CHOIR:ArtifactKind.HELLSPEC;
            var p=player(h,kind);var level=h.getLevel();var origin=h.absolutePos(new BlockPos(5,70,7));
            p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-3.5);
            level.setBlockAndUpdate(origin,Blocks.GRANITE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(origin);
            var drops=new java.util.ArrayList<net.minecraft.world.entity.item.ItemEntity>();
            try{
                for(int index=0;index<256;index++){
                    var drop=new net.minecraft.world.entity.item.ItemEntity(level,origin.getX()+.5,origin.getY()+.5,origin.getZ()+.5,new ItemStack(Items.COBBLESTONE));
                    drops.add(drop);level.addFreshEntity(drop);
                }
                h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),kind,new NaturalExcavationProgram(origin,Direction.SOUTH,shape)),"backpressured plan queues");
                WorkQueue.tick();
                h.assertTrue(WorkQueue.status(p).equals("paused")&&WorkQueue.remaining(p)==shape.volume,"drop pressure pauses without consuming first cell");
                h.assertTrue(level.getBlockState(origin).is(Blocks.GRANITE),"drop pressure does not destroy target");
                h.assertTrue(!WorkQueue.togglePause(p),"cannot resume while drops remain");
                for(var drop:drops)drop.discard();
                h.assertTrue(WorkQueue.togglePause(p),"can resume after clearing drops");WorkQueue.tick();
                h.assertTrue(level.getBlockState(origin).isAir()&&WorkQueue.remaining(p)<shape.volume,"same target executes after resume");
            }finally{for(var drop:drops)drop.discard();WorkQueue.cancel(p);RelicNetwork.forget(p);}
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_327") public static void naturalExcavationRejectsToolSwapDuringClaimCallback(GameTestHelper h){
        for(var shape:ExcavationVolume.Shape.values()){
            var kind=shape==ExcavationVolume.Shape.VAULT?ArtifactKind.CHOIR:ArtifactKind.HELLSPEC;
            var p=player(h,kind);var level=h.getLevel();var origin=h.absolutePos(new BlockPos(5,75,7));
            p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-3.5);
            level.setBlockAndUpdate(origin,Blocks.GRANITE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(origin);
            var next=ExcavationVolume.cell(origin,Direction.SOUTH,shape,1);
            level.setBlockAndUpdate(next,Blocks.GRANITE.defaultBlockState());PlayerPlacedBlocks.get(level).unmark(next);
            java.util.function.Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> swap=event->{
                if(event.player==p&&event.target.equals(origin))p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            };
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST,false,
                    io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,swap);
            try{
                h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),kind,new NaturalExcavationProgram(origin,Direction.SOUTH,shape)),"tool swap plan queues");
                WorkQueue.tick();
                h.assertTrue(!WorkQueue.busy(p),"callback invalidates actor and cancels stream");
                h.assertTrue(level.getBlockState(origin).is(Blocks.GRANITE)&&level.getBlockState(next).is(Blocks.GRANITE),"neither target nor following cell is mined after tool swap");
            }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(swap);WorkQueue.cancel(p);RelicNetwork.forget(p);}
        }
        h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_328") public static void naturalExcavationClipsAtBuildCeilingWithoutLosingValidCells(GameTestHelper h){
        for(var shape:ExcavationVolume.Shape.values()){
            var kind=shape==ExcavationVolume.Shape.VAULT?ArtifactKind.CHOIR:ArtifactKind.HELLSPEC;
            var p=player(h,kind);var level=h.getLevel();
            var local=h.absolutePos(new BlockPos(5,0,7));var origin=new BlockPos(local.getX(),level.getMaxBuildHeight()-1,local.getZ());
            p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-3.5);
            int valid=0;
            for(int index=0;index<shape.volume;index++){
                var at=ExcavationVolume.cell(origin,Direction.SOUTH,shape,index);
                if(level.isOutsideBuildHeight(at))continue;
                valid++;PlayerPlacedBlocks.get(level).unmark(at);level.setBlockAndUpdate(at,Blocks.GRANITE.defaultBlockState());
            }
            h.assertTrue(valid==(shape==ExcavationVolume.Shape.VAULT?432:121),"fixture has exactly one valid layer");
            try{
                h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),kind,new NaturalExcavationProgram(origin,Direction.SOUTH,shape)),"ceiling plan queues");
                for(int tick=0;tick<500&&WorkQueue.busy(p);tick++){
                    for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                            new net.minecraft.world.phys.AABB(origin).inflate(60)))drop.discard();
                    WorkQueue.tick();
                }
                h.assertTrue(!WorkQueue.busy(p),"out-of-height cells cannot stall the job");
                for(int index=0;index<shape.volume;index++){
                    var at=ExcavationVolume.cell(origin,Direction.SOUTH,shape,index);
                    if(!level.isOutsideBuildHeight(at))h.assertTrue(level.getBlockState(at).isAir(),"every valid ceiling cell is mined");
                }
            }finally{WorkQueue.cancel(p);RelicNetwork.forget(p);}
        }
        h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_329") public static void radialSharedPlansKeepIndependentCursorsAndRespectConfiguredLimit(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);
        var origin=h.absolutePos(new BlockPos(5,60,7));
        for(var kind:new ArtifactKind[]{ArtifactKind.CRUCIBLE,ArtifactKind.WORLDLOOM,ArtifactKind.HELLSPEC,ArtifactKind.AXIOM}){
            var shape=kind==ArtifactKind.HELLSPEC?RadialVolume.Shape.BLOOM:kind==ArtifactKind.AXIOM?RadialVolume.Shape.SHELL:RadialVolume.Shape.SPHERE;
            var offsets=RadialVolume.offsets(shape,12);var mutable=origin.mutable();
            var first=new RadialMiningProgram(mutable,12,kind,0,null);
            var second=new RadialMiningProgram(origin,12,kind,0,null);mutable.set(0,0,0);
            int limit=Math.min(offsets.size(),ArtifactConfig.JOB_LIMIT.get());
            h.assertTrue(first.remaining()==limit&&second.remaining()==limit,"configured candidate count is exact");
            for(int index=0;index<limit;index++){
                h.assertTrue(!first.done(),"plan remains active through last candidate");
                h.assertTrue(first.next(p).pos().equals(origin.offset(offsets.get(index))),"historical center-out traversal is preserved");
                h.assertTrue(second.remaining()==limit,"another cast never shares mutable cursor state");
            }
            h.assertTrue(first.done()&&first.remaining()==0,"last candidate completes without a phantom scan step");
            h.assertTrue(second.next(p).pos().equals(origin.offset(offsets.get(0))),"independent cast starts at first offset");
        }
        finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_330") public static void hellspecExpandedBloomHarvests1679CellsWithSafetyExclusions(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var level=h.getLevel();var origin=h.absolutePos(new BlockPos(5,90,7));
        p.setPos(origin.getX()+.5,origin.getY(),origin.getZ()-15.5);
        var offsets=RadialVolume.offsets(RadialVolume.Shape.BLOOM,HellspecBloomShape.RADIUS);
        h.assertTrue(offsets.size()==1679,"expanded primary contains exactly 1679 cells");
        for(var offset:offsets){var at=origin.offset(offset);PlayerPlacedBlocks.get(level).unmark(at);level.setBlockAndUpdate(at,Blocks.GRANITE.defaultBlockState());}
        var placed=origin.east(12);var denied=origin.west(12);var machine=origin.south(12);var ore=origin.north(12);var bedrock=origin.above(3);var outside=origin.east(13);
        PlayerPlacedBlocks.get(level).mark(placed);level.setBlockAndUpdate(machine,Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());level.setBlockAndUpdate(bedrock,Blocks.BEDROCK.defaultBlockState());level.setBlockAndUpdate(outside,Blocks.GRANITE.defaultBlockState());
        java.util.function.Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> veto=event->{if(event.player==p&&event.target.equals(denied))event.setCanceled(true);};
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST,false,
                io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,veto);
        try{
            h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.HELLSPEC,
                    new RadialMiningProgram(origin,HellspecBloomShape.RADIUS,ArtifactKind.HELLSPEC,0,null)),"expanded bloom queues");
            for(int tick=0;tick<500&&WorkQueue.busy(p);tick++){
                for(var drop:level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(origin).inflate(20)))drop.discard();
                if(WorkQueue.status(p).equals("paused"))WorkQueue.togglePause(p);
                WorkQueue.tick();
            }
            h.assertTrue(!WorkQueue.busy(p),"expanded bloom completes within budgeted turns");
            int removed=0;for(var offset:offsets)if(level.getBlockState(origin.offset(offset)).isAir())removed++;
            h.assertTrue(removed==1675,"all 1679 cells except four safety exclusions are harvested: "+removed);
            h.assertTrue(level.getBlockState(placed).is(Blocks.GRANITE)&&level.getBlockState(denied).is(Blocks.GRANITE)
                    &&level.getBlockState(machine).is(Blocks.CHEST)&&level.getBlockState(bedrock).is(Blocks.BEDROCK),"placed, claims, machines and bedrock stay intact");
            h.assertTrue(level.getBlockState(ore).isAir()&&level.getBlockState(outside).is(Blocks.GRANITE),"outer petal ore is harvested but the next cell is untouched");
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(veto);PlayerPlacedBlocks.get(level).unmark(placed);WorkQueue.cancel(p);RelicNetwork.forget(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_331") public static void solarNovaGeometryCannotLoadOrDiscoverRemoteOre(GameTestHelper h){
        var remote=new BlockPos(1200000,80,1200000);var level=h.getLevel();
        var offsets=SolarRupture.offsets(SolarRupture.Form.NOVA);
        h.assertTrue(!level.hasChunkAt(remote)&&offsets.size()==7153&&!level.hasChunkAt(remote),"geometry has no level/ore query dependency");h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_332") public static void modeCatalogPreservesSavedOrderAndDefendsSharedKeys(GameTestHelper h){
        String[][] expected={
            {"palimpsest","solar_rupture"},{"fault_choir","fault_fan"},{"eventide","gravity_in,gravity_out"},
            {"paradox_crucible","stone,deepslate,granite,diorite,andesite,dirt,basalt,obsidian,calcite,tuff,dripstone,gravel"},
            {"interregnum","domain,aura"},{"worldloom","quarry"},{"icarus","forward,wide"},{"hollow_axiom","hollow_pulse"},
            {"exodium","starfall"},{"iridium","orefall"},{"hellspec","hellforge"},{"seam_ripper","connected"},
            {"worldbreaker","carve,fracture,cleave,core_drill,world_shatter,region_break"}
        };
        for(var record:expected){
            var kind=ArtifactKind.byId(record[0]);var keys=record[1].split(",");
            h.assertTrue(java.util.Arrays.equals(keys,ArtifactInteraction.modes(kind)),"saved mode order stays compatible: "+kind);
            h.assertTrue(ArtifactInteraction.modeCount(kind)==keys.length,"constant-time mode count is exact");
            var mutable=ArtifactInteraction.modes(kind);mutable[0]="corrupt";
            for(int mode:new int[]{0,-1,1,Integer.MIN_VALUE,Integer.MAX_VALUE})
                h.assertTrue(ArtifactInteraction.modeKey(kind,mode).equals(keys[Math.floorMod(mode,keys.length)]),"external mutation and corrupt mode integers cannot change catalog keys");
        }
        h.assertTrue(ArtifactInteraction.modeKey(ArtifactKind.MERIDIAN,0).equals("retired"),"retired modes stay retired");h.succeed();
    }

    private static void barrier(GameTestHelper h,BlockState barrier){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=h.absolutePos(new BlockPos(5,4,7));
        h.getLevel().setBlockAndUpdate(at,barrier);h.getLevel().setBlockAndUpdate(at.south(),Blocks.STONE.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new DirectionalProgram(at,Direction.SOUTH,DirectionalProgram.Shape.CARVE)),"route accepted");
        for(int i=0;i<20&&WorkQueue.busy(p);i++)WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p),"route terminates, not skips barrier");
        h.assertTrue(h.getLevel().getBlockState(at)==barrier,"barrier unchanged");
        h.assertTrue(h.getLevel().getBlockState(at.south()).is(Blocks.STONE),"stone behind barrier unchanged");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_01") public static void carveStopsAtBedrock(GameTestHelper h){barrier(h,Blocks.BEDROCK.defaultBlockState());}
    @GameTest(template="empty",batch="rework_each_02") public static void carveStopsAtWaterSource(GameTestHelper h){barrier(h,Blocks.WATER.defaultBlockState());}
    @GameTest(template="empty",batch="rework_each_03") public static void carveStopsAtFlowingWater(GameTestHelper h){barrier(h,Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL,5));}
    @GameTest(template="empty",batch="rework_each_04") public static void carveStopsAtLavaSource(GameTestHelper h){barrier(h,Blocks.LAVA.defaultBlockState());}
    @GameTest(template="empty",batch="rework_each_05") public static void carveStopsAtFlowingLava(GameTestHelper h){barrier(h,Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,3));}
    @GameTest(template="empty",batch="rework_each_06") public static void duplicateCornerDoesNotAnalyze(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=h.absolutePos(new BlockPos(5,4,7));
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT),"selection armed");
        RelicControl.corner(p,at);RelicControl.corner(p,at);
        h.assertTrue(ArtifactInteraction.cornerCount(p)==1&&!WorkQueue.busy(p),"duplicate first corner cannot submit region");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick");RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.MODE);
        h.assertTrue(!ArtifactInteraction.selecting(p)&&ArtifactState.mode(p,ArtifactKind.CRUCIBLE)==1,"mode change clears incompatible selection");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_07") public static void axiomPacketCannotExecuteTwice(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var at=h.absolutePos(new BlockPos(5,4,7));
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(at.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        var packet=new RelicNetwork.Intent(ArtifactKind.AXIOM,RelicControl.Action.ACTIVATE,1,p.getMainHandItem().getOrCreateTag().getUUID("controlIdentity"));
        h.assertTrue(RelicNetwork.accept(p,packet),"first press activates");
        WorkQueue.cancel(p);ArtifactState.of(p,ArtifactKind.AXIOM).remove("ready");ArtifactState.of(p,ArtifactKind.AXIOM).remove("inputTick");
        h.assertTrue(!RelicNetwork.accept(p,packet)&&!WorkQueue.busy(p),"duplicate stays rejected even after job cancellation and cooldown removal");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_08") public static void thirteenPlayableMiningArtifacts(GameTestHelper h){
        h.assertTrue(SpecialPickaxes.PICKS.size()==13,"exact final roster");
        for(var kind:ArtifactKind.values())h.assertTrue(SpecialPickaxes.PICKS.containsKey(kind)==kind.playable(),"retired artifact not registered: "+kind);
        for(int mode=0;mode<6;mode++)h.assertTrue(ArtifactInteraction.regional(ArtifactKind.WORLDBREAKER,mode)==(mode==5),"only region break selects corners");
        h.assertTrue(ArtifactInteraction.modeCount(ArtifactKind.WORLDLOOM)==1,"Worldloom has one mining mode");h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_09") public static void carveStopsAtForgeFluid(GameTestHelper h){
        h.assertTrue(TestFluids.BLOCK.defaultBlockState().getFluidState().getFluidType()==TestFluids.TYPE,"genuine foreign Forge FluidType");barrier(h,TestFluids.BLOCK.defaultBlockState());
    }
    @GameTest(template="empty",batch="rework_each_10") public static void carveStopsAtFlowingForgeFluid(GameTestHelper h){barrier(h,TestFluids.BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL,4));}
    @GameTest(template="empty",batch="rework_each_11") public static void everyArtifactRejectsPhysicalBarriers(GameTestHelper h){
        var at=h.absolutePos(new BlockPos(5,4,7));var other=at.east();
        for(var k:ArtifactKind.playableValues()){
            var p=player(h,k);var tool=p.getMainHandItem();p.getInventory().add(new ItemStack(Items.STONE,64));
            for(var state:java.util.List.of(Blocks.BEDROCK.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL,6),Blocks.LAVA.defaultBlockState(),Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,2),TestFluids.BLOCK.defaultBlockState())){
                h.getLevel().setBlockAndUpdate(at,state);h.getLevel().setBlockAndUpdate(other,Blocks.AIR.defaultBlockState());
                h.assertTrue(!new WorkStep.Mine(at,state).apply(p,tool,k),"cannot mine barrier: "+k);
                h.assertTrue(!new WorkStep.Rephase(at,state,Blocks.STONE.defaultBlockState()).apply(p,tool,k),"cannot transform barrier: "+k);
                h.assertTrue(!new WorkStep.Place(at,Blocks.STONE.defaultBlockState()).apply(p,tool,k),"cannot overwrite barrier: "+k);
                h.assertTrue(!new WorkStep.Place(other,state).apply(p,tool,k),"cannot copy barrier: "+k);
                h.assertTrue(!new WorkStep.Exchange(at,other,state,Blocks.AIR.defaultBlockState()).apply(p,tool,k),"cannot exchange barrier: "+k);
                h.assertTrue(h.getLevel().getBlockState(at)==state&&h.getLevel().getBlockState(other).isAir(),"world preserved: "+k);
            }WorkQueue.cancel(p);
        }h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_12") public static void activationRejectsForgeCallbackReentry(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var at=h.absolutePos(new BlockPos(5,4,7));
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(at.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,net.minecraft.world.phys.Vec3.atCenterOf(at));
        h.assertTrue(ArtifactActions.target(p).filter(at::equals).isPresent(),"test player has a clear line to the intended matrix block");
        h.assertTrue(WorldSafety.allowed(p,ArtifactKind.AXIOM,p.blockPosition())&&WorldSafety.harvestable(p,p.getMainHandItem(),at),"matrix seed passes range, claim and tool checks");
        int[] callbacks={0};java.util.function.Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> reenter=event->{
            if(callbacks[0]++==0)h.assertTrue(!ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"callback cannot activate recursively");
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,reenter);
        try{
            h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"outer activation succeeds");
            h.assertTrue(callbacks[0]>0&&WorkQueue.busy(p),"exactly one bounded survey accepted as scheduled work");
            h.assertTrue(p.getMainHandItem().getDamageValue()==EnchantmentScaling.activationCost(p.getMainHandItem(),ArtifactKind.AXIOM),"one activation cost");
            long ready=ArtifactState.of(p,ArtifactKind.AXIOM).getLong("ready");
            h.assertTrue(!ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false)&&ArtifactState.of(p,ArtifactKind.AXIOM).getLong("ready")==ready,"one cooldown deadline");
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(reenter);}finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_13") public static void allDirectionalShapesStopBehindBarrier(GameTestHelper h){
        for(var shape:DirectionalProgram.Shape.values()){
            var kind=shape==DirectionalProgram.Shape.ICARUS||shape==DirectionalProgram.Shape.ICARUS_WIDE?ArtifactKind.ICARUS:shape==DirectionalProgram.Shape.EXODIUM_LANCE?ArtifactKind.EXODIUM:ArtifactKind.WORLDBREAKER;
            var p=player(h,kind);var at=h.absolutePos(new BlockPos(8,30,8));p.setPos(at.getX()+.5,at.getY()-1,at.getZ()-1.5);
            for(int x=-20;x<=20;x+=4)h.getLevel().getChunkAt(at.offset(x,0,0));
            var dir=shape==DirectionalProgram.Shape.CORE_DRILL?Direction.DOWN:Direction.SOUTH;
            for(var obstacle:java.util.List.of(Blocks.BEDROCK.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL,5),Blocks.LAVA.defaultBlockState(),Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,3),TestFluids.BLOCK.defaultBlockState(),TestFluids.BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL,4))){
                // Large fixtures exceed the 12-block template: initialize every examined cell,
                // rather than inherit geometry from a neighboring completed test.
                for(int depth=0;depth<3;depth++)for(var pos:DirectionalProgram.section(at,dir,shape,depth))h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                p.setPos(at.getX()+.5,at.getY()-1,at.getZ()-1.5);
                h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());
                h.getLevel().setBlockAndUpdate(at.relative(dir),obstacle);h.getLevel().setBlockAndUpdate(at.relative(dir,2),Blocks.STONE.defaultBlockState());
                WorkQueue.startRegion(p,p.getMainHandItem(),kind,new DirectionalProgram(at,dir,shape));
                for(int tick=0;tick<200&&WorkQueue.busy(p);tick++)WorkQueue.tick();
                h.assertTrue(!WorkQueue.busy(p),"shape terminates: "+shape);
                h.assertTrue(h.getLevel().getBlockState(at).isAir(),"mines before barrier: "+shape);
                h.assertTrue(h.getLevel().getBlockState(at.relative(dir))==obstacle&&h.getLevel().getBlockState(at.relative(dir,2)).is(Blocks.STONE),"does not skip barrier: "+shape);
                h.assertTrue(WorldSafety.freeBody(p,p.position()),"player not embedded: "+shape);
                h.getLevel().setBlockAndUpdate(at.relative(dir),Blocks.AIR.defaultBlockState());h.getLevel().setBlockAndUpdate(at.relative(dir,2),Blocks.AIR.defaultBlockState());
            }WorkQueue.cancel(p);
        }h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_14") public static void worldloomQuarryIsBoundedAndMiningOnly(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var center=h.absolutePos(new BlockPos(5,3,7));p.setPos(p.getX(),center.getY()-1,p.getZ());
        var stone=center.east();var ore=center.above();var outside=center.east(13);
        var placed=PlayerPlacedBlocks.get(h.getLevel());for(var at:java.util.List.of(center,stone,ore,outside))placed.unmark(at);
        h.getLevel().setBlockAndUpdate(center,Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(stone,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());h.getLevel().setBlockAndUpdate(outside,Blocks.STONE.defaultBlockState());
        p.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,net.minecraft.world.phys.Vec3.atCenterOf(center));
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,false),"quarry starts");
        for(int tick=0;tick<1200&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(stone).isAir(),"quarry mines nearby stone");
        h.assertTrue(h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(outside).is(Blocks.STONE),"preserves ore and cells outside radius");finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_15") public static void crucibleSelectsGeologyAndProtectsPlacedBlocks(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=h.absolutePos(new BlockPos(5,4,7));var b=a.offset(2,0,0);
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",6);
        var granite=a;var dirt=a.east();var placed=a.east(2);var ore=a.above();var chest=a.west();
        for(var at:java.util.List.of(granite,dirt,placed,ore,chest))PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        h.getLevel().setBlockAndUpdate(granite,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(dirt,Blocks.DIRT.defaultBlockState());
        h.getLevel().setBlockAndUpdate(placed,Blocks.DIORITE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
        var plan=new RegionWork(new SelectionVolume(a,b),null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,6,Blocks.BASALT.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,plan),"selected transformation queued");
        for(int tick=0;tick<100&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready"),"analysis completes before applying changes");h.assertTrue(WorkQueue.togglePause(p),"explicitly confirm analyzed transformation");
        for(int tick=0;tick<100&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(granite).isAir()&&h.getLevel().getBlockState(dirt).isAir(),"natural stone and soil are excavated, not converted");
        h.assertTrue(h.getLevel().getBlockState(placed).is(Blocks.DIORITE),"marked player placement stays unchanged");
        h.assertTrue(h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST),"ore and inventory stay unchanged");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_34") public static void crucibleSelectionControlsTransformCalciteAndObsidian(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=h.absolutePos(new BlockPos(5,4,7));var b=a.east();
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",9); // Tuff
        PlayerPlacedBlocks.get(h.getLevel()).unmark(a);PlayerPlacedBlocks.get(h.getLevel()).unmark(b);
        h.getLevel().setBlockAndUpdate(a,Blocks.CALCITE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(b,Blocks.OBSIDIAN.defaultBlockState());
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"activate arms the real corner-selection flow");
        h.assertTrue(RelicControl.corner(p,a)&&RelicControl.corner(p,b),"both real selection clicks are accepted");
        for(int tick=0;tick<100&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready"),"Crucible analysis reaches its confirmation state");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick"); // GameTest advances work synchronously without advancing world time.
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CONFIRM),"the normal confirm control resumes the conversion");
        for(int tick=0;tick<100&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(a).isAir()&&h.getLevel().getBlockState(b).isAir(),"calcite and obsidian are harvested with native drops, never converted to tuff");
        finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_16") public static void staleSameKindToolIntentIsRejected(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var old=p.getMainHandItem().getTag().getUUID("controlIdentity");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.AXIOM).get()));p.getMainHandItem().getOrCreateTag().putUUID("controlIdentity",UUID.randomUUID());
        h.assertTrue(!RelicNetwork.accept(p,new RelicNetwork.Intent(ArtifactKind.AXIOM,RelicControl.Action.ACTIVATE,1,old))&&!WorkQueue.busy(p),"same kind is not the same tool");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_17") public static void selectionOwnershipAndIncompleteConfirmation(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=h.absolutePos(new BlockPos(5,4,7));
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT);RelicControl.corner(p,at);
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick");h.assertTrue(!RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CONFIRM),"incomplete selection cannot execute");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.CRUCIBLE).get()));
        h.assertTrue(!ArtifactInteraction.selecting(p)&&!WorkQueue.busy(p),"switching exact tool invalidates selection");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick");RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT);RelicControl.corner(p,at);RelicControl.corner(p,at.east());
        h.assertTrue(WorkQueue.busy(p),"second distinct corner submits one analysis");int pending=WorkQueue.remaining(p);RelicControl.corner(p,at.above());h.assertTrue(WorkQueue.remaining(p)==pending,"repeated click cannot add work");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick");h.assertTrue(!RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT),"cannot start another selection during active work");
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CANCEL);h.assertTrue(!WorkQueue.busy(p)&&!ArtifactInteraction.selecting(p),"cancel clears owned state");finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_18") public static void legacyModesCannotBecomeUnrelatedMiningOperations(GameTestHelper h){
        for(var kind:java.util.List.of(ArtifactKind.WORLDBREAKER,ArtifactKind.CRUCIBLE)){
            var p=player(h,kind);var data=ArtifactState.of(p,kind);data.remove("miningSchema");data.putInt("mode",3);
            h.assertTrue(ArtifactState.mode(p,kind)==0,"old mode resets safely: "+kind);
            ArtifactState.of(p,kind).putInt("mode",1);h.assertTrue(ArtifactState.mode(p,kind)==1,"new selections remain stable: "+kind);
        }h.succeed();
    }

    @GameTest(template="empty",batch="rework_each_19") public static void carveStopsAtForgeClaimWithoutMiningBehindIt(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=h.absolutePos(new BlockPos(8,30,8));
        p.setPos(at.getX()+.5,at.getY()-1,at.getZ()-1.5);
        for(int depth=0;depth<3;depth++)for(var pos:DirectionalProgram.section(at,Direction.SOUTH,DirectionalProgram.Shape.CARVE,depth))h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        for(int depth=0;depth<3;depth++)h.getLevel().setBlockAndUpdate(at.south(depth),Blocks.STONE.defaultBlockState());
        java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.BreakEvent> deny=e->{if(e.getPlayer()==p&&e.getPos().equals(at.south()))e.setCanceled(true);};
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGHEST,deny);
        try{WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new DirectionalProgram(at,Direction.SOUTH,DirectionalProgram.Shape.CARVE));for(int n=0;n<100&&WorkQueue.busy(p);n++)WorkQueue.tick();}
        finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(deny);}
        h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).isAir(),"mines before claim and terminates");
        h.assertTrue(h.getLevel().getBlockState(at.south()).is(Blocks.STONE)&&h.getLevel().getBlockState(at.south(2)).is(Blocks.STONE),"no skipped protection");
        h.assertTrue(p.getMainHandItem().getDamageValue()==1,"only actual successful harvest consumes durability");finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_20") public static void nativeBreakCallbacksCannotReplaceTargetWithFluid(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=h.absolutePos(new BlockPos(5,4,7));
        for(var barrier:java.util.List.of(Blocks.BEDROCK.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.LAVA.defaultBlockState(),TestFluids.BLOCK.defaultBlockState())){
            h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());
            java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.BreakEvent> change=e->{if(e.getPlayer()==p&&e.getPos().equals(at))h.getLevel().setBlockAndUpdate(at,barrier);};
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGH,change);
            try{h.assertTrue(!WorldSafety.mine(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,at,Blocks.STONE.defaultBlockState()),"changed target vetoed after callback");}
            finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(change);}
            h.assertTrue(h.getLevel().getBlockState(at)==barrier,"callback-introduced barrier never deleted");
        }finish(h,p);
    }

    @GameTest(template="empty",batch="rework_each_21") public static void rollbackNeverOverwritesCallbackIntroducedBarriers(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var at=h.absolutePos(new BlockPos(5,4,7));var other=at.east();p.getInventory().add(new ItemStack(Items.STONE,16));
        for(var barrier:java.util.List.of(Blocks.BEDROCK.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.LAVA.defaultBlockState(),TestFluids.BLOCK.defaultBlockState(),Blocks.CHEST.defaultBlockState())){
            java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent> change=e->{if(e.getPos().equals(at)){h.getLevel().setBlockAndUpdate(at,barrier);e.setCanceled(true);}};
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGH,change);
            try{
                PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
                h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
                h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,at,Blocks.STONE.defaultBlockState())&&h.getLevel().getBlockState(at)==barrier,"paid rollback preserves external barrier");
                h.assertTrue(p.getInventory().countItem(Items.STONE)==16,"denied placement not charged");
                h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());
                PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
                boolean transmuted=WorldSafety.transmute(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,at,Blocks.STONE.defaultBlockState(),Blocks.BASALT.defaultBlockState());
                var transmuteResult=h.getLevel().getBlockState(at);
                h.assertTrue(!transmuted&&transmuteResult==barrier,"transmutation rollback preserves external barrier: requested="+barrier+", actual="+transmuteResult+", accepted="+transmuted);
                h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(other,Blocks.BASALT.defaultBlockState());
                h.assertTrue(!WorldSafety.exchange(p,p.getMainHandItem(),at,other,Blocks.STONE.defaultBlockState(),Blocks.BASALT.defaultBlockState())&&h.getLevel().getBlockState(at)==barrier&&h.getLevel().getBlockState(other).is(Blocks.BASALT),"pair rollback preserves external barrier and restores its own other write");
            }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(change);}
        }finish(h,p);
    }

}
