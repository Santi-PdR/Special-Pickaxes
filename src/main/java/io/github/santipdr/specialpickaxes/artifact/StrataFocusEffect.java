package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Curios mining focus: the amplifier is translated into a bounded BreakSpeed bonus. */
public final class StrataFocusEffect extends MobEffect {
    private static final DustParticleOptions STRATA_GLEAM=new DustParticleOptions(new Vector3f(.32F,.78F,.95F),.58F);

    public StrataFocusEffect(){super(MobEffectCategory.BENEFICIAL,0x52bdd8);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.09D;
        double y=entity.getY()+entity.getBbHeight()*.68D;
        for(int i=0;i<3;i++){
            double angle=phase+i*Math.PI*2D/3D;
            double x=entity.getX()+Math.cos(angle)*.34D,z=entity.getZ()+Math.sin(angle)*.34D;
            level.sendParticles(STRATA_GLEAM,x,y+Math.sin(angle*2D)*.07D,z,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
