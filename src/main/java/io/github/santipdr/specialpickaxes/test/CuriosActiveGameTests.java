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
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();var slot=new AtomicReference<ItemStack>(source);var held=new ItemStack(Items.DIAMOND_PICKAXE);p.setItemInHand(InteractionHand.MAIN_HAND,held);var rock=at.east(3);for(int x=1;x<=3;x++)h.getLevel().setBlockAndUpdate(at.east(x),Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(rock);
        h.assertTrue(CuriosMiningActive.activate(p,source,slot::get)&&source.getDamageValue()==8&&p.getMainHandItem()==held,"equipped active charges source without swapping held mining tool");drain(p);
        h.assertTrue(h.getLevel().getBlockState(rock).isAir()&&h.getLevel().getBlockState(at).isAir(),"equipped mining clears the front first and follows its connected bank");h.assertTrue(!CuriosMiningActive.activate(p,source,slot::get)&&source.getDamageValue()==8,"cooldown prevents duplicate charge");finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_02") public static void changingCopiedEquippedSourceDuringBreakRevokesActiveCell(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.HELLSPEC.id);var slot=new AtomicReference<ItemStack>(source);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var rock=at.east(3);for(int x=1;x<=3;x++)h.getLevel().setBlockAndUpdate(at.east(x),Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(rock);
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
    @GameTest(template="empty",batch="curios_active_loot") public static void dawnBoostsMiningWhileOnlyIridiumHighlightsOreLoot(GameTestHelper h){
        var p=player(h,ArtifactKind.PALIMPSEST);var at=target(h,p);var dawn=p.getMainHandItem();p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        var drop=new net.minecraft.world.entity.item.ItemEntity(h.getLevel(),at.getX()+.5,at.getY()+.25,at.getZ()+.5,new ItemStack(Items.DIAMOND));h.getLevel().addFreshEntity(drop);var motion=drop.getDeltaMovement();
        h.assertTrue(CuriosMiningActive.activate(p,dawn,()->dawn)&&!drop.isCurrentlyGlowing()&&p.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED)&&drop.getDeltaMovement().equals(motion),"Dawn boosts the miner and leaves ordinary drops unchanged");
        var iridium=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.IRIDIUM).get());
        h.assertTrue(!CuriosMiningActive.activate(p,iridium,()->iridium)&&iridium.getDamageValue()==0,"Iridium never recalls unowned loot");OreDropHighlight.mark(drop,p);
        h.assertTrue(CuriosMiningActive.activate(p,iridium,()->iridium)&&!drop.getDeltaMovement().equals(motion),"owned ore loot receives bounded recall impulse");drop.discard();finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_copy_remaining") public static void worldbreakerCopyCanActivatePreviouslyUnsupportedSource(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var source=p.getMainHandItem();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.CRUCIBLE.id);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        h.assertTrue(CuriosMiningActive.activate(p,source,()->source)&&WorkQueue.busy(p),"Paradox copy starts a connected equipped excavation");drain(p);finish(h,p);
    }
    @GameTest(template="empty",batch="feedback_terrain") public static void reworkedMiningAcceptsSandstoneTerracottaSandAndModdedStyleMaterials(GameTestHelper h){
        var p=player(h,ArtifactKind.PALIMPSEST);var at=target(h,p);var level=h.getLevel();
        var blocks=java.util.List.of(Blocks.SANDSTONE,Blocks.RED_SANDSTONE,Blocks.TERRACOTTA,Blocks.RED_TERRACOTTA,Blocks.SAND,Blocks.GRAVEL,Blocks.GLASS);
        for(int i=0;i<blocks.size();i++){
            var cell=at.east(i);level.setBlock(cell.below(),Blocks.BEDROCK.defaultBlockState(),2);
            level.setBlock(cell,blocks.get(i).defaultBlockState(),2);PlayerPlacedBlocks.get(level).unmark(cell);
            h.assertTrue(NaturalExcavationProgram.eligible(level.getBlockState(cell)),"breakable material accepted: "+blocks.get(i));
        }
        h.assertTrue(!NaturalExcavationProgram.eligible(Blocks.WATER.defaultBlockState())&&!NaturalExcavationProgram.eligible(Blocks.LAVA.defaultBlockState())
                &&!NaturalExcavationProgram.eligible(Blocks.BEDROCK.defaultBlockState()),"fluids and unbreakable blocks remain outside excavation");
        h.assertTrue(SolarRupture.start(p,p.getMainHandItem(),SolarRupture.Form.NOVA),"excavation starts on sandstone");drain(p);
        for(int i=0;i<blocks.size();i++)h.assertTrue(level.getBlockState(at.east(i)).isAir(),"material actually harvested: "+blocks.get(i));
        finish(h,p);
    }
    @GameTest(template="empty",batch="curios_active_mode_start") public static void copiedEquippedModeChangeDuringPermissionCannotStartOrCharge(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();
        source.getOrCreateTag().putString("copiedSkill",ArtifactKind.HELLSPEC.id);source.getOrCreateTag().putInt("copiedMode",0);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        Consumer<io.github.santipdr.specialpickaxes.ability.AbilityUseEvent> mutate=e->{if(e.player==p)source.getOrCreateTag().putInt("copiedMode",1);};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,io.github.santipdr.specialpickaxes.ability.AbilityUseEvent.class,mutate);
        try{
            h.assertTrue(!CuriosMiningActive.activate(p,source,()->source),"mode changed by claim callback rejects stale activation");
            h.assertTrue(!WorkQueue.busy(p)&&source.getDamageValue()==0&&h.getLevel().getBlockState(at).is(Blocks.STONE),"no job, charge or break after stale permission");
            h.assertTrue(ArtifactState.of(p,ArtifactKind.HELLSPEC).getLong("curiosActiveReady")==0,"rejected activation never starts cooldown");
            finish(h,p);
        }finally{MinecraftForge.EVENT_BUS.unregister(mutate);WorkQueue.cancel(p);}
    }

    @GameTest(template="empty",batch="curios_active_mode_mining") public static void copiedEquippedModeChangeCancelsQueuedMiningOnSameStack(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();
        source.getOrCreateTag().putString("copiedSkill",ArtifactKind.HELLSPEC.id);source.getOrCreateTag().putInt("copiedMode",0);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        try{
            h.assertTrue(CuriosMiningActive.activate(p,source,()->source),"equipped mining starts");
            source.getOrCreateTag().putInt("copiedMode",1);WorkQueue.tick();
            h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"mode change revokes queued mining before its first break");
            h.assertTrue(source.getDamageValue()==8,"successful original activation charges only once");finish(h,p);
        }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
    }

    @GameTest(template="empty",batch="curios_active_mode_flight") public static void copiedEquippedModeChangeCancelsAscentWithoutRemovingGrace(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var source=p.getMainHandItem();
        source.getOrCreateTag().putString("copiedSkill",ArtifactKind.ICARUS.id);source.getOrCreateTag().putInt("copiedMode",0);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        try{
            h.assertTrue(CuriosMiningActive.activate(p,source,()->source),"equipped copied ascent starts");IcarianFlight.tick();
            h.assertTrue(p.getDeltaMovement().y>0&&p.hasEffect(SpecialPickaxes.ICARIAN_FEATHERFALL.get()),"initial thrust grants finite grace");
            var motion=p.getDeltaMovement();var position=p.position();source.getOrCreateTag().putInt("copiedMode",1);IcarianFlight.tick();
            h.assertTrue(!IcarianFlight.active(p)&&p.getDeltaMovement().equals(motion)&&p.position().equals(position),"source mode change stops old ascent without overwriting physics");
            h.assertTrue(p.hasEffect(SpecialPickaxes.ICARIAN_FEATHERFALL.get()),"mode change preserves fall grace");finish(h,p);
        }finally{IcarianFlight.stop(p);}
    }

}
