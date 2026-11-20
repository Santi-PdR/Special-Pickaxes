package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.gametest.framework.*;
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
public final class CuriosActiveGameTests {
    @GameTest(template="empty",batch="curios_active_01") public static void equippedWorldbreakerActivatesWithoutSwappingHeldTool(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();var slot=new AtomicReference<ItemStack>(source);var held=new ItemStack(Items.DIAMOND_PICKAXE);p.setItemInHand(InteractionHand.MAIN_HAND,held);var rock=at.east(3);h.getLevel().setBlockAndUpdate(rock,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(rock);
        h.assertTrue(CuriosMiningActive.activate(p,source,slot::get)&&source.getDamageValue()==8&&p.getMainHandItem()==held,"equipped active charges source without swapping held mining tool");drain(p);
        h.assertTrue(h.getLevel().getBlockState(rock).isAir()&&h.getLevel().getBlockState(at).is(Blocks.STONE),"native small Curios geometry and core preserved");h.assertTrue(!CuriosMiningActive.activate(p,source,slot::get)&&source.getDamageValue()==8,"cooldown prevents duplicate charge");finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_02") public static void changingCopiedEquippedSourceDuringBreakRevokesActiveCell(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.HELLSPEC.id);var slot=new AtomicReference<ItemStack>(source);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var rock=at.east(3);h.getLevel().setBlockAndUpdate(rock,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(rock);
        h.assertTrue(CuriosMiningActive.activate(p,source,slot::get),"equipped copied Hellspec active starts");
        Consumer<BlockEvent.BreakEvent> mutate=e->{if(e.getPlayer()==p&&e.getPos().equals(rock))source.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,mutate);
        try{drain(p);h.assertTrue(h.getLevel().getBlockState(rock).is(Blocks.STONE)&&source.getTag().getString("copiedSkill").equals(ArtifactKind.AXIOM.id),"active uses copied geometry but rechecks source lease at mutation");}
        finally{MinecraftForge.EVENT_BUS.unregister(mutate);finish(h,p);}
    }
    @GameTest(template="empty",batch="curios_active_03") public static void removedOrUnsupportedEquippedSourceCannotStartOrCharge(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var source=p.getMainHandItem();h.assertTrue(!CuriosMiningActive.activate(p,source,()->ItemStack.EMPTY)&&!WorkQueue.busy(p)&&source.getDamageValue()==0,"removed source rejected");source.getOrCreateTag().putString("copiedSkill","invalid");h.assertTrue(!CuriosMiningActive.activate(p,source,()->source)&&!WorkQueue.busy(p)&&source.getDamageValue()==0,"unsupported equipped control never falls back to native");finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_remaining_mining") public static void remainingEquippedMiningSourcesStartTheirOwnPrograms(GameTestHelper h){
        for(var kind:java.util.List.of(ArtifactKind.CHOIR,ArtifactKind.EVENTIDE,ArtifactKind.CRUCIBLE,ArtifactKind.INTERREGNUM,ArtifactKind.SEAM_RIPPER)){
            var p=player(h,kind);target(h,p);var source=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
            h.assertTrue(CuriosMiningActive.activate(p,source,()->source)&&source.getDamageValue()==8&&WorkQueue.busy(p),"equipped source starts and charges once: "+kind);drain(p);WorkQueue.cancel(p);MiningObservations.forget(p);
        }h.succeed();
    }
    @GameTest(template="empty",batch="curios_active_icarus") public static void equippedIcarusUsesVelocityAndStopsWhenSlotIsRemoved(GameTestHelper h){
        var p=player(h,ArtifactKind.ICARUS);target(h,p);var source=p.getMainHandItem();var slot=new AtomicReference<ItemStack>(source);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var position=p.position();
        h.assertTrue(CuriosMiningActive.activate(p,source,slot::get)&&IcarianFlight.active(p),"equipped ascent starts");IcarianFlight.tick();
        h.assertTrue(p.position().equals(position)&&p.getDeltaMovement().y>0,"flight uses velocity, never teleport");slot.set(ItemStack.EMPTY);IcarianFlight.tick();h.assertTrue(!IcarianFlight.active(p),"removal stops further impulses");IcarianFlight.stop(p);finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_loot") public static void dawnLightsLootWhileIridiumOnlyPullsOwnedOreLoot(GameTestHelper h){
        var p=player(h,ArtifactKind.PALIMPSEST);var at=target(h,p);var dawn=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        var drop=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),at.getX()+.5,at.getY()+.25,at.getZ()+.5,new ItemStack(Items.DIAMOND));h.getLevel().addFreshEntity(drop);var motion=drop.getDeltaMovement();
        h.assertTrue(CuriosMiningActive.activate(p,dawn,()->dawn)&&drop.isCurrentlyGlowing()&&drop.getDeltaMovement().equals(motion),"Dawn lights drops without a magnet or mineral radar");
        var iridium=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.IRIDIUM).get());
        h.assertTrue(!CuriosMiningActive.activate(p,iridium,()->iridium)&&iridium.getDamageValue()==0,"Iridium never recalls unowned loot");OreDropHighlight.mark(drop,p);
        h.assertTrue(CuriosMiningActive.activate(p,iridium,()->iridium)&&!drop.getDeltaMovement().equals(motion),"owned ore loot receives bounded recall impulse");drop.discard();finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_copy_remaining") public static void worldbreakerCopyCanActivatePreviouslyUnsupportedSource(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var source=p.getMainHandItem();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.CRUCIBLE.id);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(CuriosMiningActive.activate(p,source,()->source)&&WorkQueue.remaining(p)==27,"Paradox copy activates small echoes, not transmutation or native fracture");drain(p);finish(h,p);
    }
}
