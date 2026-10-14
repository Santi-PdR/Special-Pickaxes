package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** A short, readable gravity snare used by Eventide's Curios-only counter. */
public final class GraviticTetherEffect extends MobEffect {
    private static final DustParticleOptions TETHER_DUST = new DustParticleOptions(new Vector3f(.56F,.34F,.92F),.8F);

    public GraviticTetherEffect() {
        super(MobEffectCategory.HARMFUL,0x8f58eb);
    }

    @Override
    public void applyEffectTick(LivingEntity entity,int amplifier) {
        if(entity.level().isClientSide)return;
        Vec3 motion=entity.getDeltaMovement();
        double drag=Math.max(.35D,.68D-amplifier*.08D);
        entity.setDeltaMovement(motion.x*drag,motion.y,motion.z*drag);
        entity.hasImpulse=true;
        if(entity.tickCount%8==0&&entity.level() instanceof ServerLevel level)
            level.sendParticles(TETHER_DUST,entity.getX(),entity.getY()+entity.getBbHeight()*.55D,entity.getZ(),2,.18D,.22D,.18D,.01D);
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%4==0;
    }
}
