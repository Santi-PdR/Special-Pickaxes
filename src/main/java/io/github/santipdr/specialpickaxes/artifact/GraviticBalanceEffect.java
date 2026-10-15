package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Eventide's Curios passive bends a fast fall into a controlled descent. */
public final class GraviticBalanceEffect extends MobEffect {
    public GraviticBalanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa67aff);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround()||entity.isFallFlying())return;
        var motion=entity.getDeltaMovement();
        if(motion.y<-.24D){entity.setDeltaMovement(motion.x,-.24D,motion.z);entity.hasImpulse=true;}
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level)
            level.sendParticles(ParticleTypes.REVERSE_PORTAL,entity.getX(),entity.getY()+entity.getBbHeight()*.5D,entity.getZ(),3,.18D,.28D,.18D,.01D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
