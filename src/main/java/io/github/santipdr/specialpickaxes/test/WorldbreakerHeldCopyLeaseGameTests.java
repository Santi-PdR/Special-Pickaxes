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
        try{h.assertTrue(!ExodiumMeteorShower.start(p,tool)&&!WorkQueue.busy(p),"claim callback cannot retag the source and start stale work");}
        finally{MinecraftForge.EVENT_BUS.unregister(mutate);finish(h,p);}
    }
}
