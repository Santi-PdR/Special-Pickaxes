package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Hellspec's Magma Wave leaves a short, visible brand that pulses with custom magma damage. */
public final class MagmaBrandEffect extends MobEffect {
    public MagmaBrandEffect(){super(MobEffectCategory.HARMFUL,0xff5428);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        // Keep the custom pulse distinct from vanilla fire ticks so already-burning targets still feel the mark.
        entity.invulnerableTime=0;
        entity.hurt(level.damageSources().magic(),1.5F+.5F*Math.max(0,amplifier));
        if(!entity.isAlive())return;
        entity.setSecondsOnFire(2);
        double x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.52D,z=entity.getZ();
        level.sendParticles(ParticleTypes.FLAME,x,y,z,4,.12D,.2D,.12D,.012D);
        level.sendParticles(ParticleTypes.LAVA,x,y-.18D,z,1,.08D,.08D,.08D,.005D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%20==0;}
}
