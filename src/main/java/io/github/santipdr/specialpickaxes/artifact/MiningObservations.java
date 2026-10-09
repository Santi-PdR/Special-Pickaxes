package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Forge 47 invokes Item.mineBlock BEFORE removeBlock. Confirm the resulting scar at end-of-tick. */
public final class MiningObservations {
    private static final int DROP_QUERY_LIMIT=256;
    private record Observation(ServerPlayer player,ItemStack tool,ArtifactKind kind,BlockPos pos,
                               BlockState state,ResourceKey<Level> dimension,long time,Map<UUID,ItemStack> dropsBefore,
                               boolean dropsBeforeComplete,boolean applyHeldMiningEffects) {}
    private record DropSnapshot(Map<UUID,ItemStack> stacks,boolean complete) {}
    private static final ArrayDeque<Observation> PENDING=new ArrayDeque<>();
    private MiningObservations() {}
    public static void capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state) {
        capture(p,tool,kind,pos,state,true);
    }
    public static void capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state,boolean applyHeldMiningEffects) {
        boolean trackOreDrops=kind==ArtifactKind.IRIDIUM&&ArtifactOres.isOre(state);
        // Queued Iridium breaks still need drop snapshots, but never chained held-mining effects.
        boolean queuedIridiumOre=WorkQueue.running()&&trackOreDrops;
        if((WorkQueue.running()&&!queuedIridiumOre) || PENDING.size()>=1024) return;
        DropSnapshot snapshot=trackOreDrops?dropSnapshot(p,pos):new DropSnapshot(Map.of(),true);
        PENDING.addLast(new Observation(p,tool,kind,pos.immutable(),state,p.level().dimension(),ArtifactState.now(p),snapshot.stacks(),snapshot.complete(),
                applyHeldMiningEffects&&!queuedIridiumOre));
    }
    public static void flush() {
        int budget=512;
        while(budget-->0 && !PENDING.isEmpty()) {
            var o=PENDING.removeFirst();var p=o.player;
            if(!p.isAlive() || p.isRemoved() || p.level().dimension()!=o.dimension
                    || (o.applyHeldMiningEffects&&(p.getMainHandItem()!=o.tool||o.tool.isEmpty()))
                    || ArtifactState.now(p)-o.time>2 || !p.serverLevel().hasChunkAt(o.pos)) continue;
            if(p.serverLevel().getBlockState(o.pos).isAir()) {
                if(o.kind==ArtifactKind.IRIDIUM&&ArtifactOres.isOre(o.state)&&o.dropsBeforeComplete)highlightNewOreDrops(p,o);
                if(o.applyHeldMiningEffects)ArtifactActions.mined(p,o.tool,o.kind,o.pos,o.state);
            }
        }
    }
    private static DropSnapshot dropSnapshot(ServerPlayer p,BlockPos pos) {
        var snapshot=new HashMap<UUID,ItemStack>();
        int[] inspected={0};
        p.serverLevel().getEntities().get(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
                new AABB(pos).inflate(2),drop->{
            if(++inspected[0]>=DROP_QUERY_LIMIT)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if(drop.isAlive())snapshot.put(drop.getUUID(),drop.getItem().copy());
            return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });
        return new DropSnapshot(snapshot,inspected[0]<DROP_QUERY_LIMIT);
    }
    private static void highlightNewOreDrops(ServerPlayer p,Observation o) {
        int[] inspected={0};
        p.serverLevel().getEntities().get(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
                new AABB(o.pos).inflate(2),drop->{
            if(++inspected[0]>DROP_QUERY_LIMIT)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if(!drop.isAlive())return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
            var previous=o.dropsBefore.get(drop.getUUID());var stack=drop.getItem();
            if(previous==null||stack.getCount()>previous.getCount()||!ItemStack.isSameItemSameTags(previous,stack)) {
                drop.setGlowingTag(true);drop.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
                drop.getPersistentData().putUUID("specialpickaxesIridiumOwner",p.getUUID());
            }
            return inspected[0]>=DROP_QUERY_LIMIT?net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT
                    :net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
    public static void forget(ServerPlayer p) { PENDING.removeIf(o -> o.player.getUUID().equals(p.getUUID())); }
    public static void clear() { PENDING.clear(); }
}
