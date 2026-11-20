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
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var source=p.getMainHandItem();h.assertTrue(!CuriosMiningActive.activate(p,source,()->ItemStack.EMPTY)&&!WorkQueue.busy(p)&&source.getDamageValue()==0,"removed source rejected");source.getOrCreateTag().putString("copiedSkill",ArtifactKind.ICARUS.id);h.assertTrue(!CuriosMiningActive.activate(p,source,()->source)&&!WorkQueue.busy(p)&&source.getDamageValue()==0,"unsupported equipped control never falls back to native");finish(h,p);
    }
}
