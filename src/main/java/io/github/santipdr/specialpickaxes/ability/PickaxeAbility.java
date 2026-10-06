package io.github.santipdr.specialpickaxes.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Stateless, composable behaviour. All callbacks run on the logical server. */
public interface PickaxeAbility {
    String id();
    default int cooldownTicks(ServerPlayer player) {
        return io.github.santipdr.specialpickaxes.PickaxeConfig.TIMINGS.get(id()).cooldown().get();
    }
    default boolean active() { return false; }
    default boolean activate(ServerPlayer player, ItemStack tool) { return false; }
    default void afterMine(ServerPlayer player, ItemStack tool, BlockPos pos, BlockState state) {}
    default void tick(ServerPlayer player, ItemStack tool) {}
}
