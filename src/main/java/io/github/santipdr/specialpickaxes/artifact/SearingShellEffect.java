package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Hellspec's fireproof passive with a small ember tell while the wearer burns or swims in lava. */
public final class SearingShellEffect extends MobEffect {
    public SearingShellEffect() {
        super(MobEffectCategory.BENEFICIAL,0xf35a39);
    }

    @Override
    public void applyEffectTick(LivingEntity entity,int amplifier) {
        if(entity.level().isClientSide)return;
        boolean inHeat=entity.isOnFire()||entity.isInLava();
        if(entity.isOnFire())entity.clearFire();
        if(inHeat&&entity.level() instanceof ServerLevel level){
            level.sendParticles(ParticleTypes.FLAME,entity.getX(),entity.getY()+entity.getBbHeight()*.55D,entity.getZ(),4,.28D,.22D,.28D,.015D);
            level.sendParticles(ParticleTypes.SMOKE,entity.getX(),entity.getY()+.1D,entity.getZ(),1,.2D,.03D,.2D,.005D);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%10==0;
    }
}
