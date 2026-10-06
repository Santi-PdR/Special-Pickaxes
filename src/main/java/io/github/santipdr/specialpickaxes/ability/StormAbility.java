package io.github.santipdr.specialpickaxes.ability;

import io.github.santipdr.specialpickaxes.PickaxeConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import java.util.Comparator;

public final class StormAbility implements PickaxeAbility {
    public String id() { return "storm"; }
    public boolean active() { return true; }
    public boolean activate(ServerPlayer player, ItemStack tool) {
        int radius = PickaxeConfig.STORM_RADIUS.get();
        var targets = player.serverLevel().getEntitiesOfClass(Monster.class,
            player.getBoundingBox().inflate(radius), mob -> mob.isAlive() && !mob.isAlliedTo(player)
                && mob.distanceToSqr(player) <= radius * radius && player.hasLineOfSight(mob));
        targets.sort(Comparator.comparingDouble(player::distanceToSqr));
        int hit = 0;
        for (Monster target : targets) {
            if (hit >= PickaxeConfig.STORM_TARGETS.get()) break;
            if (!AbilityRuntime.allowed(player, id(), target.blockPosition())) continue;
            // Player-attributed damage honours Forge attack/hurt events; never creates a lightning entity.
            if (!target.hurt(player.damageSources().playerAttack(player), PickaxeConfig.STORM_DAMAGE.get().floatValue())) continue;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, PickaxeConfig.STORM_SLOW.get(), 1));
            var start = player.getEyePosition();
            var delta = target.getEyePosition().subtract(start);
            for (int i = 0; i <= 12; i++) {
                var point = start.add(delta.scale(i / 12.0));
                player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK, point.x, point.y, point.z,
                    1, 0.02, 0.02, 0.02, 0);
            }
            hit++;
        }
        return hit > 0;
    }
}
