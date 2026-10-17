package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** A small shared Starfall ward: reduced projectile damage, with a quiet orbiting cue. */
public final class StarfallGuardEffect extends MobEffect {
    private static final DustParticleOptions STARDUST=new DustParticleOptions(new Vector3f(.92F,.68F,1F),.72F);

    public StarfallGuardEffect(){super(MobEffectCategory.BENEFICIAL,0xb69cff);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double angle=entity.tickCount*.09D;
        double radius=.38D;
        double y=entity.getY()+entity.getBbHeight()*.58D;
        for(int i=0;i<2;i++){
            double phase=angle+i*Math.PI;
            level.sendParticles(STARDUST,entity.getX()+Math.cos(phase)*radius,y+Math.sin(phase*2D)*.06D,
                    entity.getZ()+Math.sin(phase)*radius,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
