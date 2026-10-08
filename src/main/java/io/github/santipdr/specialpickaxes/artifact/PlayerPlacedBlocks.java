package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.core.BlockPos;
import java.util.HashSet;
import java.util.Set;

/** Persistent provenance for player-placed cells, used to protect geology from Crucible edits. */
public final class PlayerPlacedBlocks extends SavedData {
    private static final String DATA_NAME = "specialpickaxes_player_placed";
    private final Set<Long> positions = new HashSet<>();

    public static PlayerPlacedBlocks get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PlayerPlacedBlocks::load, PlayerPlacedBlocks::new, DATA_NAME);
    }

    public static PlayerPlacedBlocks load(CompoundTag tag) {
        var data = new PlayerPlacedBlocks();
        for (long position : tag.getLongArray("positions")) data.positions.add(position);
        return data;
    }

    public void mark(BlockPos pos) {
        if (positions.add(pos.asLong())) setDirty();
    }

    public boolean contains(BlockPos pos) { return positions.contains(pos.asLong()); }

    @Override public CompoundTag save(CompoundTag tag) {
        tag.putLongArray("positions", positions.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }
}
