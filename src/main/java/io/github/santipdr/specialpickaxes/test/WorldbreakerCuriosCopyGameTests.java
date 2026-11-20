package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class WorldbreakerCuriosCopyGameTests {
    private static void observe(GameTestHelper h,ServerPlayer p,BlockPos scar,ItemStack source,AtomicReference<ItemStack> slot){
        h.getLevel().setBlockAndUpdate(scar,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(scar);
        h.assertTrue(MiningObservations.captureCuriosWorldbreaker(p,source,scar,Blocks.STONE.defaultBlockState(),slot::get),"manual natural rock observation accepted");
    }
    private static ItemStack source(){var stack=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get());stack.getOrCreateTag().putString("copiedSkill",ArtifactKind.HELLSPEC.id);return stack;}
    @GameTest(template="empty",batch="worldbreaker_curios_copy_01") public static void confirmedNaturalScarCutsACinderRing(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var at=target(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var source=source();var slot=new AtomicReference<ItemStack>(source);
        var side=at.east();var above=at.west(4);var placed=at.west(3);
        for(var cell:java.util.List.of(side,above,placed)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}PlayerPlacedBlocks.get(h.getLevel()).mark(placed);h.getLevel().setBlockAndUpdate(above,Blocks.DIAMOND_ORE.defaultBlockState());
        try{
            observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
            h.assertTrue(WorkQueue.busy(p),"bounded cinder ring queues after confirmation");drain(p);
            h.assertTrue(h.getLevel().getBlockState(side).isAir()&&h.getLevel().getBlockState(above).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(placed).is(Blocks.STONE),"annular geometry and provenance respected");
            observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();h.assertTrue(!WorkQueue.busy(p),"eight-second cooldown prevents chaining");
        }finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_curios_copy_02") public static void unconfirmedOrRemovedCurioCannotTriggerACut(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);
        observe(h,p,at,source,slot);MiningObservations.flush();h.assertTrue(!WorkQueue.busy(p),"unbroken rock does not trigger");
        observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());slot.set(ItemStack.EMPTY);MiningObservations.flush();
        h.assertTrue(!WorkQueue.busy(p),"original slot must remain equipped until confirmation");finish(h,p);
    }
    @GameTest(template="empty",batch="worldbreaker_curios_copy_03") public static void sameCellCallbackRevocationStopsTheTemporalCut(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);var side=at.east();h.getLevel().setBlockAndUpdate(side,Blocks.STONE.defaultBlockState());
        observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
        Consumer<BlockEvent.BreakEvent> revoke=e->{if(e.getPlayer()==p&&e.getPos().equals(side))slot.set(ItemStack.EMPTY);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,revoke);
        try{drain(p);h.assertTrue(slot.get().isEmpty()&&h.getLevel().getBlockState(side).is(Blocks.STONE),"lease rechecked after Forge callback before current mutation");}
        finally{MinecraftForge.EVENT_BUS.unregister(revoke);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_curios_copy_04") public static void changingTheCopiedSourceBeforeConfirmationInvalidatesObservation(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);
        observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());source.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);MiningObservations.flush();
        h.assertTrue(!WorkQueue.busy(p),"same stack with a different copied source cannot confirm old mining");finish(h,p);
    }
    @GameTest(template="empty",batch="worldbreaker_curios_copy_05") public static void sourceMutationInsideBreakCallbackCancelsTheCurrentCell(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);var side=at.east();
        h.getLevel().setBlockAndUpdate(side,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(side);
        observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
        Consumer<BlockEvent.BreakEvent> mutate=e->{if(e.getPlayer()==p&&e.getPos().equals(side))source.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,mutate);
        try{drain(p);h.assertTrue(MiningObservations.copiedMiningSource(source)==ArtifactKind.AXIOM&&h.getLevel().getBlockState(side).is(Blocks.STONE),"copy-source lease rechecked before current mutation");}
        finally{MinecraftForge.EVENT_BUS.unregister(mutate);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_curios_copy_06") public static void onlySupportedNonRecursiveMiningSourcesAreResolved(GameTestHelper h){
        var source=source();
        for(var kind:java.util.List.of(ArtifactKind.PALIMPSEST,ArtifactKind.CHOIR,ArtifactKind.EVENTIDE,ArtifactKind.CRUCIBLE,ArtifactKind.INTERREGNUM,ArtifactKind.WORLDLOOM,ArtifactKind.AXIOM,ArtifactKind.EXODIUM,ArtifactKind.HELLSPEC,ArtifactKind.IRIDIUM)){
            source.getOrCreateTag().putString("copiedSkill",kind.id);h.assertTrue(MiningObservations.copiedMiningSource(source)==kind,"explicit mining source resolves: "+kind);
        }
        for(var id:java.util.List.of(ArtifactKind.WORLDBREAKER.id,ArtifactKind.ICARUS.id,ArtifactKind.SEAM_RIPPER.id,"invalid")){
            source.getOrCreateTag().putString("copiedSkill",id);h.assertTrue(MiningObservations.copiedMiningSource(source)==null,"unsupported/self/invalid source rejected");
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="worldbreaker_curios_copy_07") public static void copiedWorldloomUsesItsOreTriggerAndDescendingRoots(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=source();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.WORLDLOOM.id);var slot=new AtomicReference<ItemStack>(source);var below=at.below();
        h.getLevel().setBlockAndUpdate(below,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(below);
        h.assertTrue(!MiningObservations.captureCuriosWorldbreaker(p,source,at,Blocks.STONE.defaultBlockState(),slot::get),"Worldloom copy does not borrow Hellspec's stone trigger");
        h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        h.assertTrue(MiningObservations.captureCuriosWorldbreaker(p,source,at,Blocks.DIAMOND_ORE.defaultBlockState(),slot::get),"Worldloom copy observes ore");
        h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();h.assertTrue(WorkQueue.busy(p),"copied source queues its own geometry");drain(p);
        h.assertTrue(h.getLevel().getBlockState(below).isAir(),"copied roots harvest downward");finish(h,p);
    }
    @GameTest(template="empty",batch="worldbreaker_curios_native_01") public static void uncopiedWorldbreakerHasItsOwnConfirmedOreFracture(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get());var slot=new AtomicReference<ItemStack>(source);
        var rock=at.east();var ore=at.west();var core=at.above();var placed=at.east(3);
        for(var cell:java.util.List.of(rock,core,placed)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(ore);PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        try{h.assertTrue(MiningObservations.captureCuriosWorldbreaker(p,source,at,Blocks.DIAMOND_ORE.defaultBlockState(),slot::get),"native Curios observes manual ore");h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
            h.assertTrue(WorkQueue.busy(p),"native fracture has its own geometry");drain(p);
            h.assertTrue(h.getLevel().getBlockState(rock).isAir()&&h.getLevel().getBlockState(ore).isAir()&&h.getLevel().getBlockState(core).isAir()&&h.getLevel().getBlockState(placed).is(Blocks.STONE),"native Curios harvests surrounding geology but preserves core/builds");
            h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());MiningObservations.captureCuriosWorldbreaker(p,source,at,Blocks.DIAMOND_ORE.defaultBlockState(),slot::get);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();h.assertTrue(!WorkQueue.busy(p),"native eight-second cooldown cannot chain");
        }finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_curios_native_02") public static void selectingACopyCancelsTheCurrentNativeFractureCell(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get());var slot=new AtomicReference<ItemStack>(source);var rock=at.east();
        h.getLevel().setBlockAndUpdate(rock,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(rock);h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        h.assertTrue(MiningObservations.captureCuriosWorldbreaker(p,source,at,Blocks.DIAMOND_ORE.defaultBlockState(),slot::get),"native ore observation");h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
        Consumer<BlockEvent.BreakEvent> mutate=e->{if(e.getPlayer()==p&&e.getPos().equals(rock))source.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,mutate);
        try{drain(p);h.assertTrue(source.hasTag()&&source.getTag().contains("copiedSkill")&&h.getLevel().getBlockState(rock).is(Blocks.STONE),"selecting a copy revokes native current-cell lease");}
        finally{MinecraftForge.EVENT_BUS.unregister(mutate);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_curios_iridium_01") public static void copiedIridiumConfirmsOreSurgeAndNewLootOwnership(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var source=source();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.IRIDIUM.id);var slot=new AtomicReference<ItemStack>(source);var ore=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(at,ore);PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        h.assertTrue(MiningObservations.captureCuriosWorldbreaker(p,source,at,ore,slot::get),"copied Iridium observes ore");h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());
        var drop=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),at.getX()+.5,at.getY()+.25,at.getZ()+.5,new ItemStack(Items.DIAMOND));h.getLevel().addFreshEntity(drop);MiningObservations.flush();
        h.assertTrue(p.hasEffect(SpecialPickaxes.LODE_SURGE.get())&&drop.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop")&&drop.getPersistentData().getUUID("specialpickaxesIridiumOwner").equals(p.getUUID()),"original Iridium surge and owned loot semantics copied");drop.discard();finish(h,p);
    }
    @GameTest(template="empty",batch="worldbreaker_curios_iridium_02") public static void copiedIridiumRejectsRevokedSourceBeforeOreConfirmation(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var at=target(h,p);var source=source();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.IRIDIUM.id);var slot=new AtomicReference<ItemStack>(source);var ore=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(at,ore);PlayerPlacedBlocks.get(h.getLevel()).unmark(at);
        h.assertTrue(MiningObservations.captureCuriosWorldbreaker(p,source,at,ore,slot::get),"copied ore observation admitted");h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());source.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);MiningObservations.flush();
        h.assertTrue(!p.hasEffect(SpecialPickaxes.LODE_SURGE.get()),"changed source cannot claim old ore surge");finish(h,p);
    }
}
