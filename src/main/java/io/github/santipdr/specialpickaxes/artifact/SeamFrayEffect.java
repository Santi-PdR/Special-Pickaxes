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

/** Seam Ripper's Curios counter tears away a quarter of an attacker's armor briefly. */
public final class SeamFrayEffect extends MobEffect {
    private static final DustParticleOptions SCAR_DUST=new DustParticleOptions(new Vector3f(.96F,.48F,.28F),.82F);

    public SeamFrayEffect(){
        super(MobEffectCategory.HARMFUL,0xf5ac79);
        addAttributeModifier(Attributes.ARMOR,UUID.fromString("91c5a9b4-cf8a-4e87-b72d-a4d3c0c1f19e").toString(),-.25D,AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(!entity.level().isClientSide&&entity.level() instanceof ServerLevel level)
            level.sendParticles(SCAR_DUST,entity.getX(),entity.getY()+entity.getBbHeight()*.55D,entity.getZ(),3,.2D,.24D,.2D,.005D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){
        return duration%6==0;
    }
}
