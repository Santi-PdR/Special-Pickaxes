package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Exodium's Curios passive catches projectiles in a short-lived stellar veil. */
public final class StarfallWardEffect extends MobEffect {
    private static final DustParticleOptions STARDUST=new DustParticleOptions(new Vector3f(1F,.36F,.79F),.82F);

    public StarfallWardEffect(){super(MobEffectCategory.BENEFICIAL,0xf65dc0);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.045D;
        double y=entity.getY()+entity.getBbHeight()*.56D;
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI*.5D;
            double radius=.38D+(i%2)*.08D;
            level.sendParticles(STARDUST,entity.getX()+Math.cos(angle)*radius,y+Math.sin(angle*2D)*.08D,
                    entity.getZ()+Math.sin(angle)*radius,1,0,0,0,0);
        }
        if(entity.tickCount%20==0)
            level.sendParticles(ParticleTypes.END_ROD,entity.getX(),y+.18D,entity.getZ(),1,.04D,.08D,.04D,.005D);
    }

    /** A brief, low-count flash confirms the Curios ward caught a hostile shot. */
    public static void projectileImpact(LivingEntity entity){
        if(entity.level().isClientSide||entity.tickCount%5!=0||!(entity.level() instanceof ServerLevel level))return;
        double y=entity.getY()+entity.getBbHeight()*.56D;
        level.sendParticles(ParticleTypes.END_ROD,entity.getX(),y,entity.getZ(),3,.2D,.18D,.2D,.015D);
        level.sendParticles(STARDUST,entity.getX(),y,entity.getZ(),5,.3D,.24D,.3D,.01D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
