package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Eventide's Curios passive bends a fast fall into a controlled descent. */
public final class GraviticBalanceEffect extends MobEffect {
    private static final DustParticleOptions GRAVITY_DUST=new DustParticleOptions(new Vector3f(.63F,.42F,.96F),.78F);
    public GraviticBalanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa67aff);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround()||entity.isFallFlying())return;
        var motion=entity.getDeltaMovement();
        if(motion.y<-.24D){entity.setDeltaMovement(motion.x,-.24D,motion.z);entity.hasImpulse=true;}
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.075D;
            double centerY=entity.getY()+entity.getBbHeight()*.5D;
            for(int i=0;i<3;i++){
                double angle=phase+i*Math.PI*2D/3D;
                double x=entity.getX()+Math.cos(angle)*.34D;
                double z=entity.getZ()+Math.sin(angle)*.34D;
                double y=centerY+Math.sin(angle*2D)*.08D;
                level.sendParticles(GRAVITY_DUST,x,y,z,1,0,0,0,0);
            }
            if(entity.tickCount%24==0)
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,entity.getX(),centerY,entity.getZ(),1,.04D,.08D,.04D,.005D);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
