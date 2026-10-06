package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

public final class MagnetAbility implements PickaxeAbility {
    public String id() { return "magnetic"; }
    public void tick(ServerPlayer player, ItemStack tool) {
        if (player.isShiftKeyDown() || player.tickCount % 2 != 0) return;
        int radius = PickaxeConfig.MAGNET_RADIUS.get();
        var level = player.serverLevel();
        var drops = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius),
            item -> item.isAlive() && !item.hasPickUpDelay() && item.distanceToSqr(player) <= radius * radius
                && (item.getOwner() == null || item.getOwner().equals(player.getUUID())));
        drops.sort(Comparator.comparingDouble(player::distanceToSqr));
        int moved = 0;
        for (ItemEntity item : drops) {
            if (moved >= PickaxeConfig.MAGNET_LIMIT.get()) break;
            Vec3 destination = player.position().add(0, 0.5, 0);
            if (level.clip(new ClipContext(item.position(), destination, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY, player)).getType() != HitResult.Type.MISS
                    || !AbilityRuntime.allowed(player, id(), item.blockPosition())) continue;
            // Normal item collision/pickup performs ownership, delay, inventory and Forge pickup checks.
            item.setDeltaMovement(destination.subtract(item.position()).normalize().scale(PickaxeConfig.MAGNET_SPEED.get()));
            item.hasImpulse = true;
            moved++;
        }
    }
}
