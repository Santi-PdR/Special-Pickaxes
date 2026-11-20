package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.ability.AbilityUseEvent;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class EventideReworkGameTests {
    static void changedContext(GameTestHelper h,int mutation){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var tool=p.getMainHandItem();
        Consumer<AbilityUseEvent> change=e->{if(e.player==p&&e.target.equals(at))switch(mutation){
            case 0->p.setPos(p.position().add(.25,0,0));case 1->PlayerPlacedBlocks.get(h.getLevel()).mark(at);
            case 2->p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));default->p.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        }};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,AbilityUseEvent.class,change);
        try{h.assertTrue(!ArtifactSkills.use(p,tool,ArtifactKind.EVENTIDE)&&!WorkQueue.busy(p)&&!ArtifactState.of(p,ArtifactKind.EVENTIDE).contains("alternateReady")&&tool.getDamageValue()==0,"changed actor/tool/provenance rejects horizon without spending cooldown or durability");}
        finally{MinecraftForge.EVENT_BUS.unregister(change);PlayerPlacedBlocks.get(h.getLevel()).unmark(at);finish(h,p);}
    }
    static void destructiveEclipse(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var edge=at.east(12);h.getLevel().setBlockAndUpdate(edge,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(edge);
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(at.getX(),at.getY()+6,at.getZ());h.getLevel().addFreshEntity(mob);mob.setDeltaMovement(Vec3.ZERO);float health=mob.getHealth();
        h.assertTrue(EventideRupture.start(p,p.getMainHandItem(),EventideRupture.Form.ECLIPSE),"eclipse starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(edge).isAir()&&!DomainFields.active(p)&&mob.getHealth()==health&&mob.getDeltaMovement().equals(Vec3.ZERO),"massive excavation is no longer a mining-speed field or entity pull");mob.discard();finish(h,p);
    }
    static void copiedHorizon(GameTestHelper h){horizon(h,ArtifactKind.WORLDBREAKER);}
    static void nativeHorizon(GameTestHelper h){horizon(h,ArtifactKind.EVENTIDE);}
    private static void horizon(GameTestHelper h,ArtifactKind kind){
        var p=player(h,kind);var at=target(h,p);var edge=at.east(16);h.getLevel().setBlockAndUpdate(edge,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(edge);
        var tool=p.getMainHandItem();if(kind==ArtifactKind.WORLDBREAKER)tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.EVENTIDE.id);
        var drop=new ItemEntity(h.getLevel(),at.getX()+4,at.getY(),at.getZ(),new ItemStack(Items.DIAMOND));drop.setDeltaMovement(Vec3.ZERO);h.getLevel().addFreshEntity(drop);
        h.assertTrue(EventideRupture.start(p,tool,EventideRupture.Form.HORIZON)&&drop.getDeltaMovement().equals(Vec3.ZERO),"copied secondary starts destructive horizon without pulling items");drain(p);
        h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.STONE)&&h.getLevel().getBlockState(edge).isAir(),"copied horizon excavates ring and preserves the central core");drop.discard();finish(h,p);
    }
    static void claimedHorizon(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var denied=at.east(16);h.getLevel().setBlockAndUpdate(denied,Blocks.STONE.defaultBlockState());
        var drop=new ItemEntity(h.getLevel(),denied.getX(),denied.getY(),denied.getZ(),new ItemStack(Items.DIAMOND));drop.setDeltaMovement(Vec3.ZERO);var tag=new net.minecraft.nbt.CompoundTag();drop.saveWithoutId(tag);tag.putUUID("Owner",java.util.UUID.randomUUID());drop.load(tag);h.getLevel().addFreshEntity(drop);
        Consumer<AbilityUseEvent> veto=e->{if(e.player==p&&e.target.equals(denied))e.setCanceled(true);};MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,veto);
        try{h.assertTrue(ArtifactSkills.use(p,p.getMainHandItem(),ArtifactKind.EVENTIDE)&&drop.getDeltaMovement().equals(Vec3.ZERO),"horizon does not move reserved items");drain(p);h.assertTrue(h.getLevel().getBlockState(denied).is(Blocks.STONE),"execution-time claim veto protects ring cells");}
        finally{MinecraftForge.EVENT_BUS.unregister(veto);drop.discard();finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_605_07") public static void secondSecondaryCancelsHorizonBeforeItExcavates(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.EVENTIDE)&&WorkQueue.busy(p),"horizon starts");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.EVENTIDE,RelicControl.Action.ALT_SKILL)&&!WorkQueue.busy(p),"semantic alternate cancels");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_605_01") public static void selectedCrescentAndCopiedCrescentCutVertically(GameTestHelper h){
        for(var kind:new ArtifactKind[]{ArtifactKind.EVENTIDE,ArtifactKind.WORLDBREAKER}){
            var p=player(h,kind);var at=target(h,p);var high=at.above(12);var outside=at.south(5);
            h.getLevel().setBlockAndUpdate(high,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(high);h.getLevel().setBlockAndUpdate(outside,Blocks.STONE.defaultBlockState());
            if(kind==ArtifactKind.WORLDBREAKER){p.getMainHandItem().getOrCreateTag().putString("copiedSkill",ArtifactKind.EVENTIDE.id);p.getMainHandItem().getTag().putInt("copiedMode",1);}else ArtifactState.of(p,kind).putInt("mode",1);
            h.assertTrue(EventideRupture.start(p,p.getMainHandItem(),EventideRupture.Form.CRESCENT),"vertical mode starts");drain(p);
            h.assertTrue(h.getLevel().getBlockState(high).isAir()&&h.getLevel().getBlockState(outside).is(Blocks.STONE),"native and copied selected mode use vertical, not horizontal plane");WorkQueue.cancel(p);MiningObservations.forget(p);
        }h.succeed();
    }
    @GameTest(template="empty",batch="reform_605_02") public static void twilightPreservesPlacedOreContainersFluidsAndClaims(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var placed=at.east();var chest=at.west();var fluid=at.above();var denied=at.below();
        h.getLevel().setBlockAndUpdate(placed,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());h.getLevel().setBlockAndUpdate(fluid,Blocks.LAVA.defaultBlockState());h.getLevel().setBlockAndUpdate(denied,Blocks.STONE.defaultBlockState());
        Consumer<AbilityUseEvent> veto=e->{if(e.player==p&&e.target.equals(denied))e.setCanceled(true);};MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,veto);
        try{h.assertTrue(ArtifactActions.primary(p,p.getMainHandItem(),ArtifactKind.EVENTIDE),"eclipse starts");drain(p);h.assertTrue(h.getLevel().getBlockState(placed).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST)&&h.getLevel().getBlockState(fluid).is(Blocks.LAVA)&&h.getLevel().getBlockState(denied).is(Blocks.STONE),"protected cells survive twilight");}
        finally{MinecraftForge.EVENT_BUS.unregister(veto);PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    private static void oreScar(GameTestHelper h,ServerPlayer p,BlockPos at,ItemStack source,AtomicReference<ItemStack> slot){
        var ore=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(at,ore);
        h.assertTrue(MiningObservations.captureCuriosEventide(p,source,at,ore,slot::get),"equipped Eventide observes manual ore");
        h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
    }
    @GameTest(template="empty",batch="reform_605_03") public static void curiosOreCreates312CellAfterglowWithoutDetectingOrAttractingOre(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());var slot=new AtomicReference<ItemStack>(source);
        var near=at.east(2);h.getLevel().setBlockAndUpdate(at.east(),Blocks.STONE.defaultBlockState());var core=at.east();h.getLevel().setBlockAndUpdate(near,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(core,Blocks.STONE.defaultBlockState());
        h.assertTrue(!MiningObservations.captureCuriosEventide(p,source,at,Blocks.STONE.defaultBlockState(),slot::get),"ordinary stone cannot trigger ore afterglow");oreScar(h,p,at,source,slot);
        h.assertTrue(WorkQueue.busy(p),"one confirmed ore triggers a connected aftershock");drain(p);
        h.assertTrue(h.getLevel().getBlockState(near).isAir()&&h.getLevel().getBlockState(core).isAir(),"afterglow starts beside the scar and follows connected terrain");oreScar(h,p,at,source,slot);h.assertTrue(!WorkQueue.busy(p),"eight-second cooldown prevents replay");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_605_04") public static void removingOrReplacingCuriosCancelsAfterglow(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());var slot=new AtomicReference<ItemStack>(source);oreScar(h,p,at,source,slot);
        var near=at.east(2);h.getLevel().setBlockAndUpdate(at.east(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(near,Blocks.STONE.defaultBlockState());slot.set(source.copy());WorkQueue.tick();h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(near).is(Blocks.STONE),"identical replacement cannot own the original queued annulus");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_605_05") public static void removingCuriosInsideForgeCallbackProtectsSameAfterglowCell(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);var at=target(h,p);var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());var slot=new AtomicReference<ItemStack>(source);oreScar(h,p,at,source,slot);
        var near=at.east(2);h.getLevel().setBlockAndUpdate(at.east(),Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(near,Blocks.STONE.defaultBlockState());Consumer<BlockEvent.BreakEvent> revoke=e->{if(e.getPlayer()==p&&e.getPos().equals(near))slot.set(ItemStack.EMPTY);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,revoke);
        try{drain(p);h.assertTrue(slot.get().isEmpty()&&h.getLevel().getBlockState(near).is(Blocks.STONE),"same-cell native guard rejects revoked equipment");}finally{MinecraftForge.EVENT_BUS.unregister(revoke);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_605_06") public static void twilightRejectsClaimReentryAndNeverLoadsRemoteChunks(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> recurse=e->{if(e.player==p){calls[0]++;h.assertTrue(!EventideRupture.start(p,p.getMainHandItem(),EventideRupture.Form.HORIZON),"claim callback cannot reenter twilight activation");}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,recurse);
        try{h.assertTrue(EventideRupture.start(p,p.getMainHandItem(),EventideRupture.Form.ECLIPSE)&&calls[0]==1,"one outer activation");WorkQueue.cancel(p);
            var remote=new BlockPos(1200000,100,1200000);var program=new EventideRupture(remote,Direction.SOUTH,EventideRupture.Form.ECLIPSE);h.assertTrue(!h.getLevel().hasChunkAt(remote)&&!program.next(p).apply(p,p.getMainHandItem(),ArtifactKind.EVENTIDE)&&!h.getLevel().hasChunkAt(remote),"missing chunks remain unloaded");
        }finally{MinecraftForge.EVENT_BUS.unregister(recurse);finish(h,p);}
    }
}
