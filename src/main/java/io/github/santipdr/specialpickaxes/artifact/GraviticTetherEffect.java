package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** A short, readable gravity snare used by Eventide and Stasis Curios counters. */
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
        // Two counter-rotating filaments read as a tether and cost fewer particles
        // than the old scattered burst (2 every 12 ticks instead of every 8).
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.19D;
            double centerY=entity.getY()+entity.getBbHeight()*.52D;
            double radius=Math.max(.26D,entity.getBbWidth()*.56D);
            for(int i=0;i<2;i++){
                double angle=phase+i*Math.PI;
                double x=entity.getX()+Math.cos(angle)*radius;
                double z=entity.getZ()+Math.sin(angle)*radius;
                double y=centerY+Math.sin(phase+i*Math.PI)*.18D;
                level.sendParticles(TETHER_DUST,x,y,z,1,0,0,0,0);
            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%4==0;
    }
}
