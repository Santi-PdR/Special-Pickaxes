package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import io.github.santipdr.specialpickaxes.mining.MiningSafety;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class AreaMiningAbility implements PickaxeAbility {
    public String id() { return "excavator"; }
    @Override public void afterMine(ServerPlayer player, ItemStack tool, BlockPos pos, BlockState state) {
        if (MiningSafety.running() || !AbilityRuntime.ready(player, id()) || !tool.isCorrectToolForDrops(state)) return;
        int radius = PickaxeConfig.AREA_RADIUS.get();
        var look = player.getLookAngle();
        var axis = Direction.getNearest(look.x, look.y, look.z).getAxis();
        float hardness = state.getDestroySpeed(player.serverLevel(), pos);
        int count = MiningSafety.guarded(() -> {
            int broken = 0;
            for (int a = -radius; a <= radius; a++) for (int b = -radius; b <= radius; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos target = switch (axis) {
                    case X -> pos.offset(0, a, b);
                    case Y -> pos.offset(a, 0, b);
                    case Z -> pos.offset(a, b, 0);
                };
                if (MiningSafety.breakOne(player, tool, id(), pos, target, radius * 2, hardness)) broken++;
            }
            return broken;
        });
        if (count > 0) AbilityRuntime.cooldown(player, tool, id());
    }
}
