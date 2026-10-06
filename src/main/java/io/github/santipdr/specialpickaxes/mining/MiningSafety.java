package io.github.santipdr.specialpickaxes.mining;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import io.github.santipdr.specialpickaxes.ability.AbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.IntSupplier;

public final class MiningSafety {
    private static final ThreadLocal<Boolean> RUNNING = ThreadLocal.withInitial(() -> false);
    private MiningSafety() {}
    public static boolean running() { return RUNNING.get(); }
    public static int guarded(IntSupplier work) {
        if (running()) return 0;
        RUNNING.set(true);
        try { return work.getAsInt(); }
        finally { RUNNING.remove(); }
    }
    public static boolean eligible(ServerPlayer player, ItemStack tool, BlockPos pos, float baseHardness) {
        var level = player.serverLevel();
        if (tool.isEmpty() || player.getMainHandItem() != tool || !player.mayBuild()
                || !level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)
                || !level.getWorldBorder().isWithinBounds(pos) || !level.mayInteract(player, pos)) return false;
        BlockState state = level.getBlockState(pos);
        float hardness = state.getDestroySpeed(level, pos);
        return !state.isAir() && !state.hasBlockEntity() && state.getFluidState().isEmpty()
            && hardness >= 0 && hardness <= Math.max(1, baseHardness) * PickaxeConfig.HARDNESS.get()
            && state.is(BlockTags.MINEABLE_WITH_PICKAXE) && tool.isCorrectToolForDrops(state);
    }
    /** Never call Level.destroyBlock/dropResources here: gameMode handles claims, XP, loot, tool and hooks. */
    public static boolean breakOne(ServerPlayer player, ItemStack tool, String ability,
                                   BlockPos center, BlockPos pos, int radius, float baseHardness) {
        if (pos.distSqr(center) > (double) radius * radius
                || !eligible(player, tool, pos, baseHardness)
                || !AbilityRuntime.allowed(player, ability, pos)) return false;
        return player.gameMode.destroyBlock(pos);
    }
}
