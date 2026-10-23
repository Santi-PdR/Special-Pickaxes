package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/** Choir's custom stagger damps momentum without applying generic vanilla Slowness. */
public final class FaultResonanceEffect extends MobEffect {
    private static final DustParticleOptions RESONANCE=new DustParticleOptions(new Vector3f(.28F,.91F,.86F),1.15F);

    public FaultResonanceEffect(){
        super(MobEffectCategory.HARMFUL,0x46ede0);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,UUID.fromString("b35f7545-e903-4d4a-8b54-a78b34fcb1e2").toString(),-.25D,AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        Vec3 motion=entity.getDeltaMovement();
        double damping=amplifier>0?.38D:.62D;
        entity.setDeltaMovement(motion.x*damping,motion.y,motion.z*damping);
        entity.hasImpulse=true;
        // A rotating torso ring is easier to read than sparks hidden at the feet,
        // while emitting four particles every 12 ticks keeps multi-target fights light.
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.17D;
            double centerY=entity.getY()+entity.getBbHeight()*.52D;
            double radius=Math.max(.28D,entity.getBbWidth()*.62D);
            var particles=new java.util.ArrayList<ArtifactFeedback.AuraParticle>(4);
            for(int i=0;i<3;i++){
                double angle=phase+i*Math.PI*2D/3D;
                double x=entity.getX()+Math.cos(angle)*radius;
                double z=entity.getZ()+Math.sin(angle)*radius;
                double y=centerY+Math.sin(angle*2D)*.08D;
                particles.add(new ArtifactFeedback.AuraParticle(RESONANCE,
                        new Vec3(x,y,z),1,0,0,0,0));
            }
            double sparkX=entity.getX()+Math.cos(phase)*radius;
            double sparkZ=entity.getZ()+Math.sin(phase)*radius;
            particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.ELECTRIC_SPARK,
                    new Vec3(sparkX,centerY+.1D,sparkZ),1,0,0,0,0));
            ArtifactFeedback.entityAuraPulse(level,entity,particles);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
