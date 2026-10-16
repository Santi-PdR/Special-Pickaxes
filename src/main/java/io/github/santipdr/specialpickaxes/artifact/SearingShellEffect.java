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
            double phase=entity.tickCount*.16D;
            double radius=Math.max(.34D,entity.getBbWidth()*.52D);
            for(int i=0;i<6;i++){
                double angle=phase+Math.PI*2D*i/6D;
                double x=entity.getX()+Math.cos(angle)*radius;
                double y=entity.getY()+entity.getBbHeight()*.52D+Math.sin(angle*2D)*.1D;
                double z=entity.getZ()+Math.sin(angle)*radius;
                level.sendParticles(ParticleTypes.FLAME,x,y,z,1,0,0,0,.001D);
                if((i&1)==0)level.sendParticles(ParticleTypes.LAVA,x,y,z,1,.015D,.025D,.015D,.001D);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%10==0;
    }
}
