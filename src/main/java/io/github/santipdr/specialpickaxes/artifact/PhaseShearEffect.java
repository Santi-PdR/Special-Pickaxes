package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

import java.util.UUID;

/** Crucible's Curios counter shears an attacker's tempo for a short window. */
public final class PhaseShearEffect extends MobEffect {
    private static final DustParticleOptions EMBER=new DustParticleOptions(new Vector3f(1F,.34F,.08F),.86F);
    private static final DustParticleOptions RIFT=new DustParticleOptions(new Vector3f(.18F,.88F,1F),.86F);

    public PhaseShearEffect(){
        super(MobEffectCategory.HARMFUL,0xef804e);
        addAttributeModifier(Attributes.ATTACK_SPEED,UUID.fromString("5c1d4e0f-f986-46b9-88ee-6e47860a21aa").toString(),-.25D,AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,UUID.fromString("ca915ea3-9d77-436d-a5fb-c65d80263e11").toString(),-.15D,AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    /** A crossed ember-and-rift cut marks the exact hit that disrupts an opponent. */
    public static void impactPulse(LivingEntity entity){
        if(!(entity.level() instanceof ServerLevel level))return;
        double facing=Math.toRadians(entity.getYRot()),radius=Math.max(.27D,entity.getBbWidth()*.58D);
        double centerY=entity.getY()+entity.getBbHeight()*.62D;
        var particles=new java.util.ArrayList<ArtifactFeedback.AuraParticle>(8);
        for(int cut=0;cut<2;cut++){
            double angle=facing+cut*Math.PI*.5D;
            for(int shard=0;shard<3;shard++){
                double offset=(shard-1D)*.14D;
                double x=entity.getX()+Math.cos(angle)*radius-Math.sin(angle)*offset;
                double z=entity.getZ()+Math.sin(angle)*radius+Math.cos(angle)*offset;
                particles.add(new ArtifactFeedback.AuraParticle(cut==0?EMBER:RIFT,
                        new net.minecraft.world.phys.Vec3(x,centerY+offset*.8D,z),1,0,0,0,0));
            }
        }
        particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.CRIT,
                new net.minecraft.world.phys.Vec3(entity.getX(),centerY,entity.getZ()),2,.12D,.16D,.12D,.008D));
        ArtifactFeedback.entityAuraPulse(level,entity,particles);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.22D;
        double centerY=entity.getY()+entity.getBbHeight()*.68D;
        double radius=Math.max(.26D,entity.getBbWidth()*.55D);
        var particles=new java.util.ArrayList<ArtifactFeedback.AuraParticle>(2);
        for(int i=0;i<2;i++){
            double angle=phase+i*Math.PI;
            double x=entity.getX()+Math.cos(angle)*radius;
            double z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(phase+i*Math.PI)*.1D;
            particles.add(new ArtifactFeedback.AuraParticle(i==0?EMBER:RIFT,
                    new net.minecraft.world.phys.Vec3(x,y,z),1,0,0,0,0));
        }
        ArtifactFeedback.entityAuraPulse(level,entity,particles);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
