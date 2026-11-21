package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.ability.AbilityUseEvent;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class WorldbreakerHeldCopyLeaseGameTests {
    @GameTest(template="empty",batch="worldbreaker_held_lease_01") public static void changingSourceOnSameHeldStackCancelsOldQueuedMining(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var tool=p.getMainHandItem();tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.EXODIUM.id);
        h.assertTrue(ArtifactActions.primary(p,tool,ArtifactKind.WORLDBREAKER),"copied shower queued");tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);drain(p);
        h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"same-stack source mutation cancels old work before mining");finish(h,p);
    }
    @GameTest(template="empty",batch="worldbreaker_held_lease_02") public static void modeMutationInCurrentBreakCallbackRevokesMutation(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var tool=p.getMainHandItem();tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.EXODIUM.id);tool.getOrCreateTag().putInt("copiedMode",0);
        h.assertTrue(ArtifactActions.primary(p,tool,ArtifactKind.WORLDBREAKER),"copy queued");int[] calls={0};Consumer<BlockEvent.BreakEvent> mutate=e->{if(e.getPlayer()==p&&e.getPos().equals(at)){calls[0]++;tool.getOrCreateTag().putInt("copiedMode",1);}};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,BlockEvent.BreakEvent.class,mutate);
        try{drain(p);h.assertTrue(calls[0]==1&&!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).is(Blocks.STONE),"changed copied mode revokes even the current block");}
        finally{MinecraftForge.EVENT_BUS.unregister(mutate);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_held_lease_03") public static void sourceMutationDuringActivationPermissionCannotStartOldAbility(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);target(h,p);var tool=p.getMainHandItem();tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.EXODIUM.id);
        Consumer<AbilityUseEvent> mutate=e->{if(e.player==p)tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);};MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,mutate);
        try{h.assertTrue(!ArtifactActions.primary(p,tool,ArtifactKind.WORLDBREAKER)&&!WorkQueue.busy(p),"claim callback cannot retag the source and start stale work");}
        finally{MinecraftForge.EVENT_BUS.unregister(mutate);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_held_lease_04") public static void copiedAscentModeChangeBeforeFirstTickCannotMineOrThrust(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var tool=p.getMainHandItem();
        for(int y=0;y<5;y++)h.getLevel().setBlockAndUpdate(p.blockPosition().above(y),Blocks.AIR.defaultBlockState());
        tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.ICARUS.id);
        tool.getOrCreateTag().putInt("copiedMode",0);
        try{
            h.assertTrue(IcarianFlight.start(p,tool),"copied ascent starts with original mode");
            var roof=p.blockPosition().above(2);h.getLevel().setBlockAndUpdate(roof,Blocks.STONE.defaultBlockState());
            tool.getOrCreateTag().putInt("copiedMode",1);IcarianFlight.tick();
            h.assertTrue(!IcarianFlight.active(p),"same-stack mode change invalidates original ascent");
            h.assertTrue(h.getLevel().getBlockState(roof).is(Blocks.STONE)&&p.getDeltaMovement().y==0,"stale ascent neither mines roof nor applies thrust");
            finish(h,p);
        }finally{IcarianFlight.stop(p);}
    }

    @GameTest(template="empty",batch="worldbreaker_held_lease_05") public static void copiedAscentModeChangeAfterThrustPreservesFallGrace(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var tool=p.getMainHandItem();
        for(int y=0;y<5;y++)h.getLevel().setBlockAndUpdate(p.blockPosition().above(y),Blocks.AIR.defaultBlockState());
        tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.ICARUS.id);
        tool.getOrCreateTag().putInt("copiedMode",0);
        try{
            h.assertTrue(IcarianFlight.start(p,tool),"copied ascent starts");IcarianFlight.tick();
            h.assertTrue(p.getDeltaMovement().y>0&&p.hasEffect(SpecialPickaxes.ICARIAN_FEATHERFALL.get()),"original impulse grants fall grace");
            // Cancellation must not overwrite existing physics, even within the same server tick.
            var motion=p.getDeltaMovement();var position=p.position();
            tool.getOrCreateTag().putInt("copiedMode",1);IcarianFlight.tick();
            h.assertTrue(!IcarianFlight.active(p),"changed mode ends old job before duplicate-tick shortcut");
            h.assertTrue(p.getDeltaMovement().equals(motion)&&p.position().equals(position),"cancellation does not teleport or overwrite motion");
            h.assertTrue(p.hasEffect(SpecialPickaxes.ICARIAN_FEATHERFALL.get()),"finite safety grace survives cancellation");
            finish(h,p);
        }finally{IcarianFlight.stop(p);}
    }

}
