package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Forge 47 invokes Item.mineBlock BEFORE removeBlock. Confirm the resulting scar at end-of-tick. */
public final class MiningObservations {
    private record Observation(ServerPlayer player,ItemStack tool,ArtifactKind kind,BlockPos pos,
                               BlockState state,ResourceKey<Level> dimension,long time) {}
    private static final ArrayDeque<Observation> PENDING=new ArrayDeque<>();
    private MiningObservations() {}
    public static void capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state) {
        if(WorkQueue.running() || PENDING.size()>=1024) return;
        PENDING.addLast(new Observation(p,tool,kind,pos.immutable(),state,p.level().dimension(),ArtifactState.now(p)));
    }
    public static void flush() {
        int budget=512;
        while(budget-->0 && !PENDING.isEmpty()) {
            var o=PENDING.removeFirst();var p=o.player;
            if(!p.isAlive() || p.isRemoved() || p.level().dimension()!=o.dimension || p.getMainHandItem()!=o.tool
                    || o.tool.isEmpty() || ArtifactState.now(p)-o.time>2 || !p.serverLevel().hasChunkAt(o.pos)) continue;
            if(p.serverLevel().getBlockState(o.pos).isAir()) ArtifactActions.mined(p,o.tool,o.kind,o.pos,o.state);
        }
    }
    public static void forget(ServerPlayer p) { PENDING.removeIf(o -> o.player.getUUID().equals(p.getUUID())); }
    public static void clear() { PENDING.clear(); }
}
