package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Choir's custom stagger damps momentum without applying generic vanilla Slowness. */
public final class FaultResonanceEffect extends MobEffect {
    private static final DustParticleOptions RESONANCE=new DustParticleOptions(new Vector3f(.28F,.91F,.86F),1.15F);

    public FaultResonanceEffect(){super(MobEffectCategory.HARMFUL,0x46ede0);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        Vec3 motion=entity.getDeltaMovement();
        double damping=amplifier>0?.38D:.62D;
        entity.setDeltaMovement(motion.x*damping,motion.y,motion.z*damping);
        entity.hasImpulse=true;
        if(entity.tickCount%8==0&&entity.level() instanceof ServerLevel level){
            double y=entity.getY()+.12D;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,entity.getX(),y,entity.getZ(),3,.24D,.06D,.24D,.012D);
            level.sendParticles(RESONANCE,entity.getX(),y+.22D,entity.getZ(),2,.18D,.12D,.18D,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
