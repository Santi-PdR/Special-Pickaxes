package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Stasis shares a named mining cadence with teammates instead of generic Haste. */
public final class StasisResonanceEffect extends MobEffect {
    private static final DustParticleOptions VIOLET_DUST=new DustParticleOptions(new Vector3f(.67F,.47F,1F),.82F);

    public StasisResonanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa88bff);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.065D;
        double y=entity.getY()+entity.getBbHeight()*.72D;
        for(int i=0;i<3;i++){
            double angle=phase+i*(Math.PI*2D/3D);
            level.sendParticles(VIOLET_DUST,entity.getX()+Math.cos(angle)*.32D,y+Math.sin(phase+i)*.08D,
                    entity.getZ()+Math.sin(angle)*.32D,1,0,0,0,0);
        }
        if(entity.tickCount%20==0)level.sendParticles(ParticleTypes.REVERSE_PORTAL,entity.getX(),y,entity.getZ(),1,.06D,.08D,.06D,.006D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
