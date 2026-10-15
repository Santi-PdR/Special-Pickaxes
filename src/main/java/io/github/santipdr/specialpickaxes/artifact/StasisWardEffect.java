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

/** Interregnum's Curios ward anchors its bearer and nearby allies in a calm field. */
public final class StasisWardEffect extends MobEffect {
    private static final DustParticleOptions STASIS_DUST=new DustParticleOptions(new Vector3f(.63F,.47F,.94F),.72F);

    public StasisWardEffect(){
        super(MobEffectCategory.BENEFICIAL,0x9f78ef);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,UUID.fromString("7ad59033-6f29-465b-a95b-8fedb5d866c4").toString(),.15D,AttributeModifier.Operation.ADDITION);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.055D;
        double y=entity.getY()+entity.getBbHeight()*.42D;
        for(int i=0;i<2;i++){
            double angle=phase+i*Math.PI;
            double x=entity.getX()+Math.cos(angle)*.34D,z=entity.getZ()+Math.sin(angle)*.34D;
            level.sendParticles(STASIS_DUST,x,y+Math.sin(angle*2D)*.06D,z,1,0,0,0,0);
        }
        if(entity.tickCount%20==0)level.sendParticles(ParticleTypes.REVERSE_PORTAL,entity.getX(),y,entity.getZ(),1,.12D,.1D,.12D,.005D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
