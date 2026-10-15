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
            level.sendParticles(CRYSTAL_DUST,entity.getX(),entity.getY()+entity.getBbHeight()*.58D,entity.getZ(),3,.24D,.3D,.24D,.006D);
            if(entity.tickCount%20==0)level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,entity.getX(),entity.getY()+entity.getBbHeight()*.7D,entity.getZ(),1,.15D,.2D,.15D,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){
        return duration%10==0;
    }
}
