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
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(kind).get()));return p;
    }
    private static void finish(GameTestHelper h,ServerPlayer p){WorkQueue.cancel(p);ArtifactInteraction.clear(p);h.succeed();}
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
        var packet=new RelicNetwork.Intent(ArtifactKind.AXIOM,RelicControl.Action.ACTIVATE,1);
        h.assertTrue(RelicNetwork.accept(p,packet),"first press activates");
        WorkQueue.cancel(p);ArtifactState.of(p,ArtifactKind.AXIOM).remove("ready");ArtifactState.of(p,ArtifactKind.AXIOM).remove("inputTick");
        h.assertTrue(!RelicNetwork.accept(p,packet)&&!WorkQueue.busy(p),"duplicate stays rejected even after job cancellation and cooldown removal");finish(h,p);
    }
    @GameTest(template="empty") public static void ninePlayableMiningArtifacts(GameTestHelper h){
        h.assertTrue(SpecialPickaxes.PICKS.size()==9,"exact final roster");
        for(var kind:ArtifactKind.values())h.assertTrue(SpecialPickaxes.PICKS.containsKey(kind)==kind.playable(),"retired artifact not registered: "+kind);
        for(int mode=0;mode<5;mode++)h.assertTrue(!ArtifactInteraction.regional(ArtifactKind.WORLDBREAKER,mode),"all Worldbreaker modes directional");
        h.assertTrue(ArtifactInteraction.modeCount(ArtifactKind.WORLDLOOM)==3,"three Worldloom modes");h.succeed();
    }
}
