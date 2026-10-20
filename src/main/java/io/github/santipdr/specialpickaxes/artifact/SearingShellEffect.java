package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

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
            var halo=heatHaloParticles(entity,entity.tickCount);
            if(entity instanceof ServerPlayer wearer)ArtifactFeedback.nearbyWardPulse(wearer,halo);
            else for(var sample:halo){var at=sample.position();level.sendParticles(sample.particle(),at.x,at.y,at.z,
                    sample.count(),sample.dx(),sample.dy(),sample.dz(),sample.speed());}
        }
    }

    /** Six flames and three lava sparks retain the halo silhouette with a bounded local audience. */
    public static ArrayList<ArtifactFeedback.AuraParticle> heatHaloParticles(LivingEntity entity,int tick){
        double phase=tick*.16D,radius=Math.max(.34D,entity.getBbWidth()*.52D);
        double centerY=entity.getY()+entity.getBbHeight()*.52D;
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(9);
        for(int i=0;i<6;i++){
            double angle=phase+Math.PI*2D*i/6D;
            double x=entity.getX()+Math.cos(angle)*radius,z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(angle*2D)*.1D;
            particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.FLAME,new Vec3(x,y,z),1,0,0,0,.001D));
            if((i&1)==0)particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.LAVA,new Vec3(x,y,z),1,.015D,.025D,.015D,.001D));
        }
        return particles;
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%10==0;
    }
}
