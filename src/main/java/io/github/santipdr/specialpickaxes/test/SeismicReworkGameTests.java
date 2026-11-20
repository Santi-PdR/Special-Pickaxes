package io.github.santipdr.specialpickaxes.test;

import com.mojang.authlib.GameProfile;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.network.RelicNetwork;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@GameTestHolder(SpecialPickaxes.ID)
@net.minecraftforge.gametest.PrefixGameTestTemplate(false)
public final class SeismicReworkGameTests {
    static ServerPlayer player(GameTestHelper h,ArtifactKind kind){
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),"SeismicTester"));
        p.connection=new ServerGamePacketListenerImpl(h.getLevel().getServer(),new Connection(PacketFlow.SERVERBOUND),p);
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);var base=h.absolutePos(new BlockPos(5,100,3));
        p.setPos(base.getX()+.5,base.getY(),base.getZ()+.5);p.setYRot(0);p.setXRot(0);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(kind).get()));p.getMainHandItem().getOrCreateTag().putUUID("controlIdentity",UUID.randomUUID());return p;
    }
    static BlockPos target(GameTestHelper h,ServerPlayer p){
        var at=p.blockPosition().above().south(4);
        for(var cell:BlockPos.betweenClosed(at.offset(-8,-4,-8),at.offset(8,4,8))){h.getLevel().setBlockAndUpdate(cell,Blocks.AIR.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        for(var drop:h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(12)))drop.discard();
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(at));return at;
    }
    static void drain(ServerPlayer p){
        SynchronousWorkDrain.drain(p,1000);
    }
    static void finish(GameTestHelper h,ServerPlayer p){WorkQueue.cancel(p);MiningObservations.forget(p);RelicNetwork.forget(p);h.succeed();}
    private static ItemStack source(){return new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.CHOIR).get());}
    private static void scar(GameTestHelper h,ServerPlayer p,BlockPos at,ItemStack source,AtomicReference<ItemStack> slot){
        var state=Blocks.STONE.defaultBlockState();h.getLevel().setBlockAndUpdate(at,state);
        h.assertTrue(MiningObservations.captureCuriosChoir(p,source,at,state,slot::get),"live Curios source observes manual natural mining");
        h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
    }
    @GameTest(template="empty",batch="reform_604_01") public static void seismicFanBreaksBroadNaturalRockInsteadOfATunnel(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var lateral=at.south(10).east(10);var outside=at.east(4);
        h.getLevel().setBlockAndUpdate(lateral,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(lateral);
        h.getLevel().setBlockAndUpdate(outside,Blocks.STONE.defaultBlockState());
        h.assertTrue(ResonantFault.start(p,p.getMainHandItem(),ResonantFault.Form.FAN),"real primary starts fan");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(lateral).isAir()&&h.getLevel().getBlockState(outside).is(Blocks.STONE),"wide fan reaches lateral ore but respects narrow root");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_604_02") public static void cascadeCutsSeparateTallFaultsAndLeavesTheirGaps(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var fault=at.south(6).above(10);var gap=at.south(5).above(10);
        h.getLevel().setBlockAndUpdate(fault,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(gap,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(fault);
        h.assertTrue(ResonantFault.start(p,p.getMainHandItem(),ResonantFault.Form.CASCADE),"secondary starts cascade");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(fault).isAir()&&h.getLevel().getBlockState(gap).is(Blocks.STONE),"vertical sheets: center="+h.getLevel().getBlockState(at)+" fault="+h.getLevel().getBlockState(fault)+" gap="+h.getLevel().getBlockState(gap)+" status="+WorkQueue.status(p)+" remaining="+WorkQueue.remaining(p)+" reason="+WorkQueue.pauseReason(p));finish(h,p);
    }
    @GameTest(template="empty",batch="reform_604_03") public static void worldbreakerCopiesBothSeismicGeometries(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var tool=p.getMainHandItem();tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.CHOIR.id);
        h.assertTrue(ArtifactActions.primary(p,tool,ArtifactKind.WORLDBREAKER),"copied fan starts");drain(p);h.assertTrue(h.getLevel().getBlockState(at).isAir(),"copied fan executes");
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.assertTrue(ArtifactSkills.use(p,tool,ArtifactKind.WORLDBREAKER),"copied cascade starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir(),"copied cascade executes");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_604_04") public static void seismicWorkPreservesPlacedBlocksContainersFluidsAndClaims(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var placed=at.east();var chest=at.west();var fluid=at.above();var denied=at.below();
        h.getLevel().setBlockAndUpdate(placed,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());h.getLevel().setBlockAndUpdate(fluid,Blocks.WATER.defaultBlockState());h.getLevel().setBlockAndUpdate(denied,Blocks.STONE.defaultBlockState());
        Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> veto=e->{if(e.player==p&&e.target.equals(denied))e.setCanceled(true);};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,veto);
        try{h.assertTrue(ResonantFault.start(p,p.getMainHandItem(),ResonantFault.Form.CASCADE),"cascade starts");drain(p);
            h.assertTrue(h.getLevel().getBlockState(placed).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST)&&h.getLevel().getBlockState(fluid).is(Blocks.WATER)&&h.getLevel().getBlockState(denied).is(Blocks.STONE),"all protected cells survive");
        }finally{MinecraftForge.EVENT_BUS.unregister(veto);PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_604_05") public static void sixConfirmedCuriosBreaksCreateOneBoundedReplica(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        var source=source();var slot=new AtomicReference<ItemStack>(source);var adjacent=at.east();h.getLevel().setBlockAndUpdate(adjacent,Blocks.STONE.defaultBlockState());
        for(int i=0;i<5;i++)scar(h,p,at,source,slot);h.assertTrue(!WorkQueue.busy(p),"five manual scars do not proc");scar(h,p,at,source,slot);
        h.assertTrue(WorkQueue.busy(p),"sixth scar schedules one connected aftershock");drain(p);
        h.assertTrue(h.getLevel().getBlockState(adjacent).isAir(),"replica actually harvests surrounding natural rock with the held tool");
        for(int i=0;i<6;i++)scar(h,p,at,source,slot);h.assertTrue(!WorkQueue.busy(p),"eight-second cooldown prevents immediate replay");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_604_06") public static void replacingCuriosSourceResetsItsStreakAndCancelsQueuedReplica(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);
        for(int i=0;i<5;i++)scar(h,p,at,source,slot);var replacement=source.copy();slot.set(replacement);scar(h,p,at,replacement,slot);
        h.assertTrue(!WorkQueue.busy(p),"identical replacement does not inherit old streak");for(int i=0;i<5;i++)scar(h,p,at,replacement,slot);
        var adjacent=at.east();h.getLevel().setBlockAndUpdate(adjacent,Blocks.STONE.defaultBlockState());slot.set(ItemStack.EMPTY);WorkQueue.tick();
        h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(adjacent).is(Blocks.STONE),"removing source revokes already queued replica");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_604_07") public static void curiosRemovalInsideAbilityCallbackCannotAuthorizeAReplicaBreak(GameTestHelper h){curiosRevocation(h,false);}
    @GameTest(template="empty",batch="reform_604_08") public static void curiosRemovalInsideForgeBreakCallbackCannotAuthorizeAReplicaBreak(GameTestHelper h){curiosRevocation(h,true);}
    private static void curiosRevocation(GameTestHelper h,boolean forge){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);
        for(int i=0;i<6;i++)scar(h,p,at,source,slot);var adjacent=at.east();h.getLevel().setBlockAndUpdate(adjacent,Blocks.STONE.defaultBlockState());
        Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> ability=e->{if(e.player==p&&e.target.equals(adjacent))slot.set(ItemStack.EMPTY);};
        Consumer<BlockEvent.BreakEvent> nativeBreak=e->{if(e.getPlayer()==p&&e.getPos().equals(adjacent))slot.set(ItemStack.EMPTY);};
        if(forge)MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,nativeBreak);
        else MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,ability);
        try{drain(p);h.assertTrue(slot.get().isEmpty()&&h.getLevel().getBlockState(adjacent).is(Blocks.STONE),"source revoked inside callback stops the same cell, not merely the next tick");}
        finally{if(forge)MinecraftForge.EVENT_BUS.unregister(nativeBreak);else MinecraftForge.EVENT_BUS.unregister(ability);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_604_09") public static void seismicActivationRejectsCallbackReentryAndCasterTeleport(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);target(h,p);int[] calls={0};
        Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!ResonantFault.start(p,p.getMainHandItem(),ResonantFault.Form.FAN),"nested start denied");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,callback);
        try{h.assertTrue(!ResonantFault.start(p,p.getMainHandItem(),ResonantFault.Form.CASCADE)&&calls[0]==1&&!WorkQueue.busy(p),"moved caster cannot start stale job");}
        finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_604_10") public static void seismicAlternateCanCancelAndNeverLoadsRemoteChunks(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);
        h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.CHOIR)&&WorkQueue.busy(p),"alternate starts through server orchestration");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CHOIR,RelicControl.Action.ALT_SKILL)&&!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"second alternate cancels before execution");
        var remote=new BlockPos(1200000,100,1200000);var program=new ResonantFault(remote,Direction.SOUTH,ResonantFault.Form.FAN);
        h.assertTrue(!h.getLevel().hasChunkAt(remote)&&!program.next(p).apply(p,p.getMainHandItem(),ArtifactKind.CHOIR)&&!h.getLevel().hasChunkAt(remote),"missing chunks stay unloaded");finish(h,p);
    }
}
