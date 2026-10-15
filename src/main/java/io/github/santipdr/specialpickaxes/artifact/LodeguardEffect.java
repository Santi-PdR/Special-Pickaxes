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

/** Iridium's Curios counter hardens its wearer in a visible, short-lived crystal ward. */
public final class LodeguardEffect extends MobEffect {
    private static final DustParticleOptions CRYSTAL_DUST=new DustParticleOptions(new Vector3f(.32F,.88F,.82F),.9F);

    public LodeguardEffect(){
        super(MobEffectCategory.BENEFICIAL,0x55d7c8);
        addAttributeModifier(Attributes.ARMOR,UUID.fromString("4a8b1439-e768-4fe6-a5de-6577137933b8").toString(),4D,AttributeModifier.Operation.ADDITION);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(!entity.level().isClientSide&&entity.level() instanceof ServerLevel level){
            // Keep the same three-particle budget, but arrange it as a turning
            // crystal guard so the passive reads as a deliberate ward, not a puff.
            double phase=entity.tickCount*.038D;
            double radius=.38D;
            double centerY=entity.getY()+entity.getBbHeight()*.56D;
            for(int i=0;i<3;i++){
                double angle=phase+i*(Math.PI*2D/3D);
                double x=entity.getX()+Math.cos(angle)*radius;
                double z=entity.getZ()+Math.sin(angle)*radius;
                double y=centerY+Math.sin(angle*2D)*.09D;
                level.sendParticles(CRYSTAL_DUST,x,y,z,1,0,0,0,0);
            }
            if(entity.tickCount%20==0)level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,entity.getX(),centerY+.18D,entity.getZ(),1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){
        return duration%10==0;
    }
}
