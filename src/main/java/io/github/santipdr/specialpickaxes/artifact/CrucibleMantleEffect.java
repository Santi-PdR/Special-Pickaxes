package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Crucible's Curios lease turns nearby heat into a small orbiting cinder ward. */
public final class CrucibleMantleEffect extends MobEffect {
    private static final DustParticleOptions EMBER_DUST=new DustParticleOptions(new Vector3f(.98F,.36F,.08F),.78F);

    public CrucibleMantleEffect(){super(MobEffectCategory.BENEFICIAL,0xef7026);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        boolean inHeat=entity.isOnFire()||entity.isInLava();
        if(entity.isOnFire())entity.clearFire();
        if(!inHeat||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.12D,radius=.42D;
        double y=entity.getY()+entity.getBbHeight()*.58D;
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI*.5D;
            double x=entity.getX()+Math.cos(angle)*radius,z=entity.getZ()+Math.sin(angle)*radius;
            if(entity instanceof ServerPlayer wearer)
                level.sendParticles(wearer,EMBER_DUST,false,x,y+Math.sin(angle*2D)*.06D,z,1,0,0,0,0);
            else level.sendParticles(EMBER_DUST,x,y+Math.sin(angle*2D)*.06D,z,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
