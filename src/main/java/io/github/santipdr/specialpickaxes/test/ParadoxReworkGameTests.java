package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.ability.AbilityUseEvent;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
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
public final class ParadoxReworkGameTests {
    private static void select(GameTestHelper h,ServerPlayer p,BlockPos a,BlockPos b,ArtifactKind kind){
        ArtifactState.of(p,kind).remove("inputTick");h.assertTrue(RelicControl.execute(p,kind,RelicControl.Action.ACTIVATE),"primary arms selection");
        h.assertTrue(RelicControl.corner(p,a)&&RelicControl.corner(p,b),"two server-validated corners mark the causal volume");
        for(int tick=0;tick<600&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready"),"snapshot analysis awaits explicit detonation");
    }
    @GameTest(template="empty",batch="reform_606_01") public static void paradoxStoresRegionBeforeDetonatingNaturalOreWithNativeDrops(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h,p);var ore=at.east();var chest=at.east(2);
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(ore);h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
        select(h,p,at,chest,ArtifactKind.CRUCIBLE);h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.STONE)&&h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"recording never alters terrain");
        h.assertTrue(WorkQueue.togglePause(p),"explicit detonation starts");
        for(int tick=0;tick<20&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(ore).isAir()&&h.getLevel().getBlockState(chest).is(Blocks.CHEST),"confirmed fracture mines rock/ore while preserving container");
        h.assertTrue(!h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(ore).inflate(2),e->e.getItem().is(Items.DIAMOND)).isEmpty(),"native diamond loot, never a selected output material");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_606_02") public static void paradoxSnapshotSkipsChangedAndNewlyPlacedCells(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h,p);var b=at.east();h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        select(h,p,at,b,ArtifactKind.CRUCIBLE);h.getLevel().setBlockAndUpdate(at,Blocks.GRANITE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(b);
        try{h.assertTrue(WorkQueue.togglePause(p),"detonation starts");drain(p);h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.GRANITE)&&h.getLevel().getBlockState(b).is(Blocks.STONE),"exact snapshot changes and placed provenance veto release");}
        finally{PlayerPlacedBlocks.get(h.getLevel()).unmark(b);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_606_03") public static void worldbreakerCopiesParadoxSelectionOrderAndDestructiveSecondary(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var b=at.east();h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        var tag=p.getMainHandItem().getOrCreateTag();tag.putString("copiedSkill",ArtifactKind.CRUCIBLE.id);tag.putInt("copiedMode",8);
        select(h,p,at,b,ArtifactKind.WORLDBREAKER);ArtifactInteraction.display(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER);
        h.assertTrue(tag.getInt("copiedMode")==8&&tag.getString("artifactModeName").equals("calcite"),"selected copy order is not replaced by source's unrelated saved mode");
        WorkQueue.togglePause(p);drain(p);h.assertTrue(h.getLevel().getBlockState(at).isAir(),"copied fracture excavates");ArtifactInteraction.clear(p);
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER),"copied lattice starts");drain(p);h.assertTrue(h.getLevel().getBlockState(at).isAir(),"copied lattice excavates, never converts");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_606_04") public static void causalLatticePreservesGapsPlacedOreAndClaimedCells(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h,p);var included=at.offset(1,1,0);var gap=at.east();var placed=at.offset(-1,1,0);var denied=at.offset(1,-1,0);
        for(var cell:new BlockPos[]{included,gap,denied}){h.getLevel().setBlockAndUpdate(cell,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        h.getLevel().setBlockAndUpdate(placed,Blocks.DIAMOND_ORE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(placed);
        Consumer<AbilityUseEvent> veto=e->{if(e.player==p&&e.target.equals(denied))e.setCanceled(true);};MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,veto);
        try{h.assertTrue(ParadoxLattice.start(p,p.getMainHandItem()),"lattice starts");drain(p);h.assertTrue(h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(included).isAir()&&h.getLevel().getBlockState(gap).is(Blocks.STONE)&&h.getLevel().getBlockState(placed).is(Blocks.DIAMOND_ORE)&&h.getLevel().getBlockState(denied).is(Blocks.STONE),"causal geometry, provenance and claims all hold");}
        finally{MinecraftForge.EVENT_BUS.unregister(veto);PlayerPlacedBlocks.get(h.getLevel()).unmark(placed);finish(h,p);}
    }
    private static void scar(GameTestHelper h,ServerPlayer p,BlockPos at,ItemStack source,AtomicReference<ItemStack> slot){
        h.getLevel().setBlockAndUpdate(at,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).unmark(at);h.assertTrue(MiningObservations.captureCuriosParadox(p,source,at,Blocks.STONE.defaultBlockState(),slot::get),"live slot observes natural manual scar");
        h.getLevel().setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());MiningObservations.flush();
    }
    @GameTest(template="empty",batch="reform_606_05") public static void fourCuriosScarsReplayDestructiveEchoesAtTheirOriginalLocations(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h,p);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.CRUCIBLE).get());var slot=new AtomicReference<ItemStack>(source);
        var a=at;var b=at.east(2);var c=at.east(4);var d=at.east(6);for(var point:new BlockPos[]{a,b,c,d})h.getLevel().setBlockAndUpdate(point.above(),Blocks.STONE.defaultBlockState());
        scar(h,p,a,source,slot);scar(h,p,b,source,slot);scar(h,p,c,source,slot);h.assertTrue(!WorkQueue.busy(p),"three scars are stored without immediate excavation");scar(h,p,d,source,slot);
        h.assertTrue(WorkQueue.busy(p),"four scars trigger one connected aftershock");drain(p);
        h.assertTrue(h.getLevel().getBlockState(d.above()).isAir()&&h.getLevel().getBlockState(a.above()).is(Blocks.STONE),"aftershock starts at the latest scar and does not jump to a remote island");
        for(int i=0;i<4;i++)scar(h,p,a,source,slot);h.assertTrue(!WorkQueue.busy(p),"eight-second cooldown prevents endless echo chaining");finish(h,p);
    }
    @GameTest(template="empty",batch="reform_606_06") public static void removingCuriosInsideForgeCallbackStopsTheSameEchoCell(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h,p);var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.CRUCIBLE).get());var slot=new AtomicReference<ItemStack>(source);
        for(int i=0;i<4;i++)scar(h,p,at,source,slot);var adjacent=at.above();h.getLevel().setBlockAndUpdate(adjacent,Blocks.STONE.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> revoke=e->{if(e.getPlayer()==p&&e.getPos().equals(adjacent))slot.set(ItemStack.EMPTY);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,revoke);
        try{drain(p);h.assertTrue(slot.get().isEmpty()&&h.getLevel().getBlockState(adjacent).is(Blocks.STONE),"callback revocation protects current cell as well as future work");}
        finally{MinecraftForge.EVENT_BUS.unregister(revoke);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_606_07") public static void latticeActivationRejectsReentryAndMovedCaster(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);target(h,p);int[] calls={0};Consumer<AbilityUseEvent> callback=e->{if(e.player==p){calls[0]++;h.assertTrue(!ParadoxLattice.start(p,p.getMainHandItem()),"nested start refused");p.setPos(p.position().add(1,0,0));}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,callback);
        try{h.assertTrue(!ParadoxLattice.start(p,p.getMainHandItem())&&calls[0]==1&&!WorkQueue.busy(p),"claim callbacks cannot redirect an activation");}
        finally{MinecraftForge.EVENT_BUS.unregister(callback);finish(h,p);}
    }
    @GameTest(template="empty",batch="reform_606_08") public static void latticeCancelsThroughAlternateAndSkipsUnloadedChunks(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);target(h,p);h.assertTrue(ParadoxLattice.start(p,p.getMainHandItem()),"lattice starts");h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ALT_SKILL)&&!WorkQueue.busy(p),"alternate cancels work");
        var remote=new BlockPos(1200000,100,1200000);var plan=new ParadoxLattice(remote);h.assertTrue(!h.getLevel().hasChunkAt(remote)&&!plan.next(p).apply(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE)&&!h.getLevel().hasChunkAt(remote),"no chunk loading");finish(h,p);
    }
}
