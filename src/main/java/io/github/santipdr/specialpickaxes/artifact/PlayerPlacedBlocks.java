package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.BlockPos;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.WeakHashMap;

/** Persistent provenance for player-placed, harvestable cells that mining skills must preserve. */
public final class PlayerPlacedBlocks extends SavedData {
    private static final String DATA_NAME = "specialpickaxes_player_placed";
    private static final int CLEANUP_PER_TICK = 128;
    private static final int MAX_PENDING_CLEANUPS = 8192;
    private static final ArrayDeque<PendingCleanup> PENDING = new ArrayDeque<>();
    private static final Map<ServerLevel, PlayerPlacedBlocks> LOADED = new WeakHashMap<>();
    private static int sweepClock;
    private record PendingCleanup(ServerLevel level, long position, Block originalBlock) {}
    private final LongOpenHashSet positions = new LongOpenHashSet();
    private LongIterator staleScan;

    public static PlayerPlacedBlocks get(ServerLevel level) {
        return LOADED.computeIfAbsent(level,
                key -> key.getDataStorage().computeIfAbsent(PlayerPlacedBlocks::load, PlayerPlacedBlocks::new, DATA_NAME));
    }

    public static PlayerPlacedBlocks load(CompoundTag tag) {
        var data = new PlayerPlacedBlocks();
        for (long position : tag.getLongArray("positions")) data.positions.add(position);
        return data;
    }

    public void mark(BlockPos pos) {
        if (positions.add(pos.asLong())) { staleScan = null; setDirty(); }
    }
    public void unmark(BlockPos pos) {
        if (positions.remove(pos.asLong())) { staleScan = null; setDirty(); }
    }

    public boolean contains(BlockPos pos) { return positions.contains(pos.asLong()); }

    /** Schedule provenance cleanup after a break event has had time to mutate the world. */
    public static void scheduleCleanup(ServerLevel level, BlockPos pos) {
        var data = LOADED.get(level);
        if (data == null || !data.contains(pos) || PENDING.size() >= MAX_PENDING_CLEANUPS || !level.hasChunkAt(pos)) return;
        PENDING.addLast(new PendingCleanup(level, pos.asLong(), level.getBlockState(pos).getBlock()));
    }

    /** Bounded cleanup: cancelled breaks keep their protection; removed/replaced cells release saved data. */
    public static void tickCleanup() {
        for (int i = 0, count = Math.min(CLEANUP_PER_TICK, PENDING.size()); i < count; i++) {
            var pending = PENDING.removeFirst();
            var pos = BlockPos.of(pending.position());
            if (!pending.level().hasChunkAt(pos)) continue;
            var current = pending.level().getBlockState(pos);
            if (current.isAir() || current.getBlock() != pending.originalBlock())
                get(pending.level()).unmark(pos);
        }
        if (++sweepClock >= 20) {
            sweepClock = 0;
            for (var entry : LOADED.entrySet()) entry.getValue().pruneStale(entry.getKey(), 32);
        }
    }

    public static void clearPendingCleanup() { PENDING.clear(); LOADED.clear(); sweepClock = 0; }

    /** Slowly discard stale provenance without loading chunks or scanning an entire saved set at once. */
    private void pruneStale(ServerLevel level, int budget) {
        if (positions.isEmpty()) { staleScan = null; return; }
        if (staleScan == null || !staleScan.hasNext()) staleScan = positions.iterator();
        int checked = 0;
        while (checked++ < budget && staleScan.hasNext()) {
            long packed = staleScan.nextLong();
            var pos = BlockPos.of(packed);
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            if (state.isAir() || state.hasBlockEntity() || !state.getFluidState().isEmpty() || !ArtifactTools.effective(state)) {
                staleScan.remove();
                setDirty();
            }
        }
        if (!staleScan.hasNext()) { staleScan = null; positions.trim(); }
    }

    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("positions", positions.toLongArray());
        return tag;
    }
}
