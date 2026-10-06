package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public final class SpeedAbility implements PickaxeAbility {
    public String id() { return "overdrive"; }
    public boolean active() { return true; }
    public boolean activate(ServerPlayer player, ItemStack tool) {
        int duration = PickaxeConfig.TIMINGS.get(id()).duration().get();
        if (duration <= 0 || player.hasEffect(SpecialPickaxes.OVERDRIVE.get())) return false;
        player.addEffect(new MobEffectInstance(SpecialPickaxes.OVERDRIVE.get(), duration, 0, false, true, true));
        AbilityRuntime.message(player, "overdrive", PickaxeConfig.SPEED.get());
        return true;
    }
}
