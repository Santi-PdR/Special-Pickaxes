package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.UUID;

/** Interregnum's Curios ward anchors its bearer and nearby allies in a calm field. */
public final class StasisWardEffect extends MobEffect {
    private static final DustParticleOptions STASIS_DUST=new DustParticleOptions(new Vector3f(.63F,.47F,.94F),.72F);
    private static final DustParticleOptions STASIS_CORE=new DustParticleOptions(new Vector3f(.48F,.86F,1F),.62F);

    public StasisWardEffect(){
        super(MobEffectCategory.BENEFICIAL,0x9f78ef);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,UUID.fromString("7ad59033-6f29-465b-a95b-8fedb5d866c4").toString(),.15D,AttributeModifier.Operation.ADDITION);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.08D;
        double centerY=entity.getY()+entity.getBbHeight()*.48D;
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(4);
        for(int i=0;i<3;i++){
            double angle=phase+i*Math.PI*2D/3D;
            double radius=.36D+Math.sin(phase+i*1.9D)*.045D;
            double x=entity.getX()+Math.cos(angle)*radius,z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(phase*1.6D+i*Math.PI*2D/3D)*.12D;
            particles.add(new ArtifactFeedback.AuraParticle((i&1)==0?STASIS_CORE:STASIS_DUST,new net.minecraft.world.phys.Vec3(x,y,z),1,0,0,0,0));
        }
        if(entity.tickCount%30==0)particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.REVERSE_PORTAL,
                new net.minecraft.world.phys.Vec3(entity.getX(),centerY,entity.getZ()),1,.08D,.08D,.08D,.003D));
        if(entity instanceof ServerPlayer wearer)ArtifactFeedback.nearbyWardPulse(wearer,particles);
        else for(var sample:particles){var at=sample.position();level.sendParticles(sample.particle(),at.x,at.y,at.z,
                sample.count(),sample.dx(),sample.dy(),sample.dz(),sample.speed());}
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
