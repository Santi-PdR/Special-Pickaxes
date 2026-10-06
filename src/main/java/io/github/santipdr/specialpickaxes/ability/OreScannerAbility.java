package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import java.util.ArrayList;
import java.util.Comparator;

public final class OreScannerAbility implements PickaxeAbility {
    public String id() { return "scanner"; }
    public boolean active() { return true; }
    public boolean activate(ServerPlayer player, ItemStack tool) {
        int radius = PickaxeConfig.SCAN_RADIUS.get();
        BlockPos center = player.blockPosition();
        var level = player.serverLevel();
        var found = new ArrayList<BlockPos>();
        // Hard upper bound: 33^3 states, no chunk loads; only bounded results leave the server.
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) <= radius * radius && level.hasChunkAt(pos)
                    && level.getBlockState(pos).is(Tags.Blocks.ORES)
                    && AbilityRuntime.allowed(player, id(), pos)) found.add(pos.immutable());
        }
        found.sort(Comparator.comparingDouble(center::distSqr));
        int count = Math.min(found.size(), PickaxeConfig.SCAN_LIMIT.get());
        for (int i = 0; i < count; i++) {
            BlockPos pos = found.get(i);
            level.sendParticles(player, ParticleTypes.END_ROD, false, pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0);
        }
        if (count == 0) AbilityRuntime.message(player, "scan_empty");
        else {
            BlockPos pos = found.get(0);
            AbilityRuntime.message(player, "scan", count, level.getBlockState(pos).getBlock().getName(),
                pos.getX() - center.getX(), pos.getY() - center.getY(), pos.getZ() - center.getZ());
        }
        return true; // Empty scans also consume cooldown: no free repeated probing.
    }
}
