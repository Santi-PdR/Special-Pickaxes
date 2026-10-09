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
    private record Observation(ServerPlayer player,ItemStack tool,ArtifactKind kind,BlockPos pos,
                               BlockState state,ResourceKey<Level> dimension,long time,Map<UUID,ItemStack> dropsBefore,
                               boolean applyHeldMiningEffects) {}
    private static final ArrayDeque<Observation> PENDING=new ArrayDeque<>();
    private MiningObservations() {}
    public static void capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state) {
        capture(p,tool,kind,pos,state,true);
    }
    public static void capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state,boolean applyHeldMiningEffects) {
        if(WorkQueue.running() || PENDING.size()>=1024) return;
        Map<UUID,ItemStack> dropsBefore=kind==ArtifactKind.IRIDIUM&&ArtifactOres.isOre(state)?dropSnapshot(p,pos):Map.of();
        PENDING.addLast(new Observation(p,tool,kind,pos.immutable(),state,p.level().dimension(),ArtifactState.now(p),dropsBefore,applyHeldMiningEffects));
    }
    public static void flush() {
        int budget=512;
        while(budget-->0 && !PENDING.isEmpty()) {
            var o=PENDING.removeFirst();var p=o.player;
            if(!p.isAlive() || p.isRemoved() || p.level().dimension()!=o.dimension || p.getMainHandItem()!=o.tool
                    || o.tool.isEmpty() || ArtifactState.now(p)-o.time>2 || !p.serverLevel().hasChunkAt(o.pos)) continue;
            if(p.serverLevel().getBlockState(o.pos).isAir()) {
                if(o.kind==ArtifactKind.IRIDIUM&&!o.dropsBefore.isEmpty())highlightNewOreDrops(p,o);
                if(o.applyHeldMiningEffects)ArtifactActions.mined(p,o.tool,o.kind,o.pos,o.state);
            }
        }
    }
    private static Map<UUID,ItemStack> dropSnapshot(ServerPlayer p,BlockPos pos) {
        var snapshot=new HashMap<UUID,ItemStack>();
        for(var drop:p.serverLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(2)))
            snapshot.put(drop.getUUID(),drop.getItem().copy());
        return snapshot;
    }
    private static void highlightNewOreDrops(ServerPlayer p,Observation o) {
        for(var drop:p.serverLevel().getEntitiesOfClass(ItemEntity.class,new AABB(o.pos).inflate(2))) {
            var previous=o.dropsBefore.get(drop.getUUID());var stack=drop.getItem();
            if(previous==null||stack.getCount()>previous.getCount()||!ItemStack.isSameItemSameTags(previous,stack)) {
                drop.setGlowingTag(true);drop.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
                drop.getPersistentData().putUUID("specialpickaxesIridiumOwner",p.getUUID());
            }
        }
    }
    public static void forget(ServerPlayer p) { PENDING.removeIf(o -> o.player.getUUID().equals(p.getUUID())); }
    public static void clear() { PENDING.clear(); }
}
