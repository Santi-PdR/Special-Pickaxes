package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class TeleportAbility implements PickaxeAbility {
    public String id() { return "ender"; }
    public boolean active() { return true; }
    public boolean activate(ServerPlayer player, ItemStack tool) {
        double range = PickaxeConfig.ENDER_RANGE.get();
        Vec3 origin = player.position();
        Vec3 direction = player.getLookAngle();
        Vec3 last = origin;
        for (double d = 0.2; d <= range; d += 0.2) {
            Vec3 next = origin.add(direction.scale(d));
            if (!SafeTeleport.free(player, next)
                    || !AbilityRuntime.allowed(player, id(), net.minecraft.core.BlockPos.containing(next))) break;
            last = next;
        }
        return last.distanceToSqr(origin) >= 1 && SafeTeleport.move(player, last, range, id());
    }
}
