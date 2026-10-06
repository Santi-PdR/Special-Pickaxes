package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/** Two-stage personal anchor; same dimension, finite lifetime, range and unobstructed return. */
public final class VoidAnchorAbility implements PickaxeAbility {
    public String id() { return "void"; }
    public int cooldownTicks(ServerPlayer player) {
        return AbilityRuntime.data(player).contains("anchor") ? PickaxeConfig.VOID_ARM_COOLDOWN.get()
            : PickaxeAbility.super.cooldownTicks(player);
    }
    public boolean active() { return true; }
    public boolean activate(ServerPlayer player, ItemStack tool) {
        CompoundTag data = AbilityRuntime.data(player);
        CompoundTag anchor = data.getCompound("anchor");
        if (player.isShiftKeyDown()) {
            data.remove("anchor");
            AbilityRuntime.message(player, "anchor_cleared");
            return true;
        }
        if (anchor.getLong("expires") <= AbilityRuntime.now(player)) {
            if (!SafeTeleport.free(player, player.position())) return false;
            anchor = new CompoundTag();
            anchor.putDouble("x", player.getX()); anchor.putDouble("y", player.getY()); anchor.putDouble("z", player.getZ());
            anchor.putString("dimension", player.level().dimension().location().toString());
            anchor.putLong("expires", AbilityRuntime.now(player) + PickaxeConfig.TIMINGS.get(id()).duration().get());
            data.put("anchor", anchor);
            AbilityRuntime.message(player, "anchor_set");
            return true;
        }
        Vec3 target = new Vec3(anchor.getDouble("x"), anchor.getDouble("y"), anchor.getDouble("z"));
        if (!anchor.getString("dimension").equals(player.level().dimension().location().toString())
                || !SafeTeleport.move(player, target, PickaxeConfig.VOID_RANGE.get(), id())) {
            AbilityRuntime.message(player, "anchor_blocked");
            return false;
        }
        data.remove("anchor");
        AbilityRuntime.message(player, "anchor_return");
        return true;
    }
}
