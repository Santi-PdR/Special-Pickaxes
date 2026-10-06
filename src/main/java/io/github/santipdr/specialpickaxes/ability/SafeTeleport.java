package io.github.santipdr.specialpickaxes.ability;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Swept full player volume, sampled at <= 0.2 blocks, with explicit chunk/border/fluid checks. */
public final class SafeTeleport {
    private SafeTeleport() {}
    public static boolean free(ServerPlayer player, Vec3 feet) {
        var level = player.serverLevel();
        AABB box = player.getBoundingBox().move(feet.subtract(player.position()));
        if (box.minY < level.getMinBuildHeight() || box.maxY >= level.getMaxBuildHeight()
                || !level.getWorldBorder().isWithinBounds(box)) return false;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ),
                BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            if (!level.hasChunkAt(pos) || !level.mayInteract(player, pos)
                    || !level.getFluidState(pos).isEmpty()) return false;
        }
        return level.noCollision(player, box);
    }
    public static boolean pathClear(ServerPlayer player, Vec3 destination, double range, String ability) {
        Vec3 delta = destination.subtract(player.position());
        double length = delta.length();
        if (!Double.isFinite(length) || length > range || player.isPassenger() || player.isSleeping()) return false;
        int steps = Math.max(1, (int) Math.ceil(length / 0.2));
        for (int i = 1; i <= steps; i++) {
            Vec3 point = player.position().add(delta.scale(i / (double) steps));
            if (!free(player, point) || !AbilityRuntime.allowed(player, ability, BlockPos.containing(point))) return false;
        }
        return true;
    }
    public static boolean move(ServerPlayer player, Vec3 destination, double range, String ability) {
        if (!pathClear(player, destination, range, ability)) return false;
        player.connection.teleport(destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        // Keep fallDistance: repeated teleportation is not a free fall-damage reset.
        return true;
    }
}
