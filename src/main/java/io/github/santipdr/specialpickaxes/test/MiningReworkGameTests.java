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

/** New contracts, separate from historical 4.0 scenarios pending their migration. */
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
    private static void finish(GameTestHelper h,ServerPlayer p){RelicNetwork.forget(p);WorkQueue.cancel(p);ArtifactInteraction.clear(p);h.succeed();}
    private static void barrier(GameTestHelper h,BlockState barrier){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=h.absolutePos(new BlockPos(5,4,7));
        h.getLevel().setBlockAndUpdate(at,barrier);h.getLevel().setBlockAndUpdate(at.south(),Blocks.STONE.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new DirectionalProgram(at,Direction.SOUTH,DirectionalProgram.Shape.CARVE)),"route accepted");
        for(int i=0;i<20&&WorkQueue.busy(p);i++)WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p),"route terminates, not skips barrier");
        h.assertTrue(h.getLevel().getBlockState(at)==barrier,"barrier unchanged");
        h.assertTrue(h.getLevel().getBlockState(at.south()).is(Blocks.STONE),"stone behind barrier unchanged");finish(h,p);
    }
    @GameTest(template="empty") public static void carveStopsAtBedrock(GameTestHelper h){barrier(h,Blocks.BEDROCK.defaultBlockState());}
    @GameTest(template="empty") public static void carveStopsAtWaterSource(GameTestHelper h){barrier(h,Blocks.WATER.defaultBlockState());}
    @GameTest(template="empty") public static void carveStopsAtFlowingWater(GameTestHelper h){barrier(h,Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL,5));}
    @GameTest(template="empty") public static void carveStopsAtLavaSource(GameTestHelper h){barrier(h,Blocks.LAVA.defaultBlockState());}
    @GameTest(template="empty") public static void carveStopsAtFlowingLava(GameTestHelper h){barrier(h,Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL,3));}
    @GameTest(template="empty") public static void duplicateCornerDoesNotAnalyze(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=h.absolutePos(new BlockPos(5,4,7));
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT),"selection armed");
        RelicControl.corner(p,at);RelicControl.corner(p,at);
        h.assertTrue(ArtifactInteraction.cornerCount(p)==1&&!WorkQueue.busy(p),"duplicate first corner cannot submit region");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick");RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.MODE);
        h.assertTrue(!ArtifactInteraction.selecting(p)&&ArtifactState.mode(p,ArtifactKind.CRUCIBLE)==1,"mode change clears incompatible selection");finish(h,p);
    }
    @GameTest(template="empty") public static void axiomPacketCannotExecuteTwice(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var at=h.absolutePos(new BlockPos(5,4,7));
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(at.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        var packet=new RelicNetwork.Intent(ArtifactKind.AXIOM,RelicControl.Action.ACTIVATE,1,p.getMainHandItem().getOrCreateTag().getUUID("controlIdentity"));
        h.assertTrue(RelicNetwork.accept(p,packet),"first press activates");
        WorkQueue.cancel(p);ArtifactState.of(p,ArtifactKind.AXIOM).remove("ready");ArtifactState.of(p,ArtifactKind.AXIOM).remove("inputTick");
        h.assertTrue(!RelicNetwork.accept(p,packet)&&!WorkQueue.busy(p),"duplicate stays rejected even after job cancellation and cooldown removal");finish(h,p);
    }
    @GameTest(template="empty") public static void ninePlayableMiningArtifacts(GameTestHelper h){
        h.assertTrue(SpecialPickaxes.PICKS.size()==9,"exact final roster");
        for(var kind:ArtifactKind.values())h.assertTrue(SpecialPickaxes.PICKS.containsKey(kind)==kind.playable(),"retired artifact not registered: "+kind);
        for(int mode=0;mode<6;mode++)h.assertTrue(ArtifactInteraction.regional(ArtifactKind.WORLDBREAKER,mode)==(mode==5),"only region break selects corners");
        h.assertTrue(ArtifactInteraction.modeCount(ArtifactKind.WORLDLOOM)==1,"Worldloom has one mining mode");h.succeed();
    }
    @GameTest(template="empty") public static void carveStopsAtForgeFluid(GameTestHelper h){
        h.assertTrue(TestFluids.BLOCK.defaultBlockState().getFluidState().getFluidType()==TestFluids.TYPE,"genuine foreign Forge FluidType");barrier(h,TestFluids.BLOCK.defaultBlockState());
    }
    @GameTest(template="empty") public static void carveStopsAtFlowingForgeFluid(GameTestHelper h){barrier(h,TestFluids.BLOCK.defaultBlockState().setValue(LiquidBlock.LEVEL,4));}
    @GameTest(template="empty") public static void everyArtifactRejectsPhysicalBarriers(GameTestHelper h){
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
    @GameTest(template="empty") public static void activationRejectsForgeCallbackReentry(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var at=h.absolutePos(new BlockPos(5,4,7));
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(at.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        int[] callbacks={0};java.util.function.Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> reenter=event->{
            if(callbacks[0]++==0)h.assertTrue(!ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"callback cannot activate recursively");
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,reenter);
        try{
            h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"outer activation succeeds");
            h.assertTrue(callbacks[0]>0&&!WorkQueue.busy(p),"exactly one bounded survey accepted without a bulk job");
            h.assertTrue(p.getMainHandItem().getDamageValue()==EnchantmentScaling.activationCost(p.getMainHandItem(),ArtifactKind.AXIOM),"one activation cost");
            long ready=ArtifactState.of(p,ArtifactKind.AXIOM).getLong("ready");
            h.assertTrue(!ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false)&&ArtifactState.of(p,ArtifactKind.AXIOM).getLong("ready")==ready,"one cooldown deadline");
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(reenter);}finish(h,p);
    }

    @GameTest(template="empty") public static void allDirectionalShapesStopBehindBarrier(GameTestHelper h){
        for(var shape:DirectionalProgram.Shape.values()){
            var kind=shape==DirectionalProgram.Shape.ICARUS||shape==DirectionalProgram.Shape.ICARUS_WIDE?ArtifactKind.ICARUS:ArtifactKind.WORLDBREAKER;
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
    @GameTest(template="empty") public static void worldloomQuarryIsBoundedAndMiningOnly(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var center=h.absolutePos(new BlockPos(8,6,8));
        var stone=center.east();var ore=center.above();var outside=center.east(8);
        h.getLevel().setBlockAndUpdate(stone,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());h.getLevel().setBlockAndUpdate(outside,Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,false),"quarry starts");
        for(int tick=0;tick<200&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(stone).isAir(),"quarry mines nearby stone");
        h.assertTrue(h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(outside).is(Blocks.STONE),"preserves ore and cells outside radius");finish(h,p);
    }

    @GameTest(template="empty") public static void crucibleSelectsGeologyAndProtectsPlacedBlocks(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=h.absolutePos(new BlockPos(5,4,7));var b=a.offset(2,0,0);
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",6);
        var granite=a;var dirt=a.east();var placed=a.east(2);var ore=a.above();var chest=a.west();
        h.getLevel().setBlockAndUpdate(granite,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(dirt,Blocks.DIRT.defaultBlockState());
        h.getLevel().setBlockAndUpdate(placed,Blocks.DIORITE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
        var plan=new RegionWork(new SelectionVolume(a,b),null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,6,Blocks.BASALT.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,plan),"selected transformation queued");
        for(int tick=0;tick<100&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(granite).is(Blocks.BASALT)&&h.getLevel().getBlockState(dirt).is(Blocks.BASALT),"natural stone and soil transform");
        h.assertTrue(h.getLevel().getBlockState(placed).is(Blocks.DIORITE),"marked player placement stays unchanged");
        h.assertTrue(h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST),"ore and inventory stay unchanged");finish(h,p);
    }
    @GameTest(template="empty") public static void staleSameKindToolIntentIsRejected(GameTestHelper h){
        var p=player(h,ArtifactKind.AXIOM);var old=p.getMainHandItem().getTag().getUUID("controlIdentity");
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.AXIOM).get()));p.getMainHandItem().getOrCreateTag().putUUID("controlIdentity",UUID.randomUUID());
        h.assertTrue(!RelicNetwork.accept(p,new RelicNetwork.Intent(ArtifactKind.AXIOM,RelicControl.Action.ACTIVATE,1,old))&&!WorkQueue.busy(p),"same kind is not the same tool");finish(h,p);
    }
    @GameTest(template="empty") public static void selectionOwnershipAndIncompleteConfirmation(GameTestHelper h){
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

    @GameTest(template="empty") public static void legacyModesCannotBecomeUnrelatedMiningOperations(GameTestHelper h){
        for(var kind:java.util.List.of(ArtifactKind.WORLDBREAKER,ArtifactKind.CRUCIBLE)){
            var p=player(h,kind);var data=ArtifactState.of(p,kind);data.remove("miningSchema");data.putInt("mode",3);
            h.assertTrue(ArtifactState.mode(p,kind)==0,"old mode resets safely: "+kind);
            ArtifactState.of(p,kind).putInt("mode",1);h.assertTrue(ArtifactState.mode(p,kind)==1,"new selections remain stable: "+kind);
        }h.succeed();
    }

    @GameTest(template="empty") public static void carveStopsAtForgeClaimWithoutMiningBehindIt(GameTestHelper h){
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

    @GameTest(template="empty") public static void nativeBreakCallbacksCannotReplaceTargetWithFluid(GameTestHelper h){
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

    @GameTest(template="empty") public static void rollbackNeverOverwritesCallbackIntroducedBarriers(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDLOOM);var at=h.absolutePos(new BlockPos(5,4,7));var other=at.east();p.getInventory().add(new ItemStack(Items.STONE,16));
        for(var barrier:java.util.List.of(Blocks.BEDROCK.defaultBlockState(),Blocks.WATER.defaultBlockState(),Blocks.LAVA.defaultBlockState(),TestFluids.BLOCK.defaultBlockState(),Blocks.CHEST.defaultBlockState())){
            java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent> change=e->{if(e.getPos().equals(at)){h.getLevel().setBlockAndUpdate(at,barrier);e.setCanceled(true);}};
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.HIGH,change);
            try{
                h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
                h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,at,Blocks.STONE.defaultBlockState())&&h.getLevel().getBlockState(at)==barrier,"paid rollback preserves external barrier");
                h.assertTrue(p.getInventory().countItem(Items.STONE)==16,"denied placement not charged");
                h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());
                h.assertTrue(!WorldSafety.transmute(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,at,Blocks.STONE.defaultBlockState(),Blocks.BASALT.defaultBlockState())&&h.getLevel().getBlockState(at)==barrier,"transmutation rollback preserves external barrier");
                h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(other,Blocks.BASALT.defaultBlockState());
                h.assertTrue(!WorldSafety.exchange(p,p.getMainHandItem(),at,other,Blocks.STONE.defaultBlockState(),Blocks.BASALT.defaultBlockState())&&h.getLevel().getBlockState(at)==barrier&&h.getLevel().getBlockState(other).is(Blocks.BASALT),"pair rollback preserves external barrier and restores its own other write");
            }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(change);}
        }finish(h,p);
    }

}
