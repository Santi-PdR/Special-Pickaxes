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
public final class ExodiumCuriosGameTests {
    private static void observe(GameTestHelper h,ServerPlayer p,BlockPos scar,ItemStack source,AtomicReference<ItemStack> slot){
        h.getLevel().setBlockAndUpdate(scar,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(scar);
        h.assertTrue(MiningObservations.captureCuriosExodium(p,source,scar,Blocks.DIAMOND_ORE.defaultBlockState(),slot::get),"manual ore observation accepted");
    }
    private static ItemStack source(){return new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EXODIUM).get());}
    @GameTest(template="empty",batch="exodium_curios_01") public static void confirmedOreScarOpensASmallImpactBowl(GameTestHelper h){
        var p=player(h,ArtifactKind.EXODIUM);var at=target(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var source=source();var slot=new AtomicReference<ItemStack>(source);
        var side=at.east(3);var above=at.below(2);var placed=at.west(3);
        for(var cell:java.util.List.of(side,above,placed)){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}PlayerPlacedBlocks.get(h.getLevel()).mark(placed);h.getLevel().setBlockAndUpdate(above,Blocks.DIAMOND_ORE.defaultBlockState());
        try{
            observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
            h.assertTrue(WorkQueue.remaining(p)==201,"bounded falling star queues after confirmation");drain(p);
            h.assertTrue(h.getLevel().getBlockState(side).isAir()&&h.getLevel().getBlockState(above).isAir()&&h.getLevel().getBlockState(placed).is(Blocks.STONE),"impact geometry and provenance respected");
            observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();h.assertTrue(!WorkQueue.busy(p),"eight-second cooldown prevents chaining");
        }finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    @GameTest(template="empty",batch="exodium_curios_02") public static void unconfirmedOrRemovedCurioCannotTriggerACut(GameTestHelper h){
        var p=player(h,ArtifactKind.EXODIUM);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);
        observe(h,p,at,source,slot);MiningObservations.flush();h.assertTrue(!WorkQueue.busy(p),"unbroken ore does not trigger");
        observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());slot.set(ItemStack.EMPTY);MiningObservations.flush();
        h.assertTrue(!WorkQueue.busy(p),"original slot must remain equipped until confirmation");finish(h,p);
    }
    @GameTest(template="empty",batch="exodium_curios_03") public static void sameCellCallbackRevocationStopsTheTemporalCut(GameTestHelper h){
        var p=player(h,ArtifactKind.EXODIUM);var at=target(h,p);var source=source();var slot=new AtomicReference<ItemStack>(source);var side=at.east(3);h.getLevel().setBlockAndUpdate(side,Blocks.STONE.defaultBlockState());
        observe(h,p,at,source,slot);h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
        Consumer<BlockEvent.BreakEvent> revoke=e->{if(e.getPlayer()==p&&e.getPos().equals(side))slot.set(ItemStack.EMPTY);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,revoke);
        try{drain(p);h.assertTrue(slot.get().isEmpty()&&h.getLevel().getBlockState(side).is(Blocks.STONE),"lease rechecked after Forge callback before current mutation");}
        finally{MinecraftForge.EVENT_BUS.unregister(revoke);finish(h,p);}
    }
}
