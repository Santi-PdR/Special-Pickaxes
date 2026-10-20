package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.ArrayList;

/** A small shared Starfall ward: reduced projectile damage, with a quiet orbiting cue. */
public final class StarfallGuardEffect extends MobEffect {
    private static final DustParticleOptions STARDUST=new DustParticleOptions(new Vector3f(.92F,.68F,1F),.72F);

    public StarfallGuardEffect(){super(MobEffectCategory.BENEFICIAL,0xb69cff);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double angle=entity.tickCount*.09D;
        double radius=.38D;
        double y=entity.getY()+entity.getBbHeight()*.58D;
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(2);
        for(int i=0;i<2;i++){
            double phase=angle+i*Math.PI;
            particles.add(new ArtifactFeedback.AuraParticle(STARDUST,
                    new net.minecraft.world.phys.Vec3(entity.getX()+Math.cos(phase)*radius,y+Math.sin(phase*2D)*.06D,
                            entity.getZ()+Math.sin(phase)*radius),1,0,0,0,0));
        }
        ArtifactFeedback.entityAuraPulse(level,entity,particles);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
