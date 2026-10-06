package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import io.github.santipdr.specialpickaxes.mining.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import java.util.Arrays;

public final class VeinMiningAbility implements PickaxeAbility {
    public String id() { return "vein_miner"; }
    @Override public void afterMine(ServerPlayer player, ItemStack tool, BlockPos pos, BlockState state) {
        if (MiningSafety.running() || !state.is(Tags.Blocks.ORES) || !tool.isCorrectToolForDrops(state)
                || !AbilityRuntime.ready(player, id())) return;
        int limit = PickaxeConfig.VEIN_LIMIT.get();
        int radius = PickaxeConfig.VEIN_RADIUS.get();
        float hardness = state.getDestroySpeed(player.serverLevel(), pos);
        int count = MiningSafety.guarded(() -> BoundedTraversal.walk(pos.immutable(), limit, limit * 6 + 1,
            p -> Arrays.stream(Direction.values()).map(p::relative).toList(),
            p -> p.equals(pos) || (player.serverLevel().hasChunkAt(p)
                && player.serverLevel().getBlockState(p).is(state.getBlock())
                && MiningSafety.breakOne(player, tool, id(), pos, p, radius, hardness))).size() - 1);
        if (count > 0) AbilityRuntime.cooldown(player, tool, id());
    }
}
