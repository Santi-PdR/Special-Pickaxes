package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
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
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.22D;
        double centerY=entity.getY()+entity.getBbHeight()*.68D;
        double radius=Math.max(.26D,entity.getBbWidth()*.55D);
        for(int i=0;i<2;i++){
            double angle=phase+i*Math.PI;
            double x=entity.getX()+Math.cos(angle)*radius;
            double z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(phase+i*Math.PI)*.1D;
            level.sendParticles(i==0?EMBER:RIFT,x,y,z,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
