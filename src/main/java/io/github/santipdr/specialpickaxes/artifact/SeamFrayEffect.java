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

/** Seam Ripper's Curios counter tears away a quarter of an attacker's armor with orbiting fracture marks. */
public final class SeamFrayEffect extends MobEffect {
    private static final DustParticleOptions SCAR_DUST=new DustParticleOptions(new Vector3f(.96F,.48F,.28F),.82F);

    public SeamFrayEffect(){
        super(MobEffectCategory.HARMFUL,0xf5ac79);
        addAttributeModifier(Attributes.ARMOR,UUID.fromString("91c5a9b4-cf8a-4e87-b72d-a4d3c0c1f19e").toString(),-.25D,AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.17D;
        double radius=Math.max(.24D,entity.getBbWidth()*.52D);
        double centerY=entity.getY()+entity.getBbHeight()*.55D;
        for(int shard=0;shard<3;shard++){
            double angle=phase+shard*Math.PI*2D/3D;
            double x=entity.getX()+Math.cos(angle)*radius,z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(angle*2D)*.11D;
            level.sendParticles(SCAR_DUST,x,y,z,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){
        return duration%6==0;
    }
}
