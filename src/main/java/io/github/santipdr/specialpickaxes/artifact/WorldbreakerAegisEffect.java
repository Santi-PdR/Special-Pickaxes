package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/** Worldbreaker's Curios passive anchors its wearer with a visible orbit of slate shards. */
public final class WorldbreakerAegisEffect extends MobEffect {
    private static final BlockParticleOption SLATE_SHARD=new BlockParticleOption(ParticleTypes.BLOCK,
            net.minecraft.world.level.block.Blocks.DEEPSLATE.defaultBlockState());

    public WorldbreakerAegisEffect(){
        super(MobEffectCategory.BENEFICIAL,0x71627f);
        addAttributeModifier(Attributes.ARMOR,UUID.fromString("c9f3b44a-86e2-45fa-a3eb-7d410d96838f").toString(),4D,AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,UUID.fromString("cb640f7b-1233-4a81-b23a-0aa6ff1326d5").toString(),.3D,AttributeModifier.Operation.ADDITION);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.045D;
        double centerY=entity.getY()+entity.getBbHeight()*.52D;
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI*.5D;
            double radius=.43D+Math.sin(angle*2D)*.035D;
            double x=entity.getX()+Math.cos(angle)*radius;
            double z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(angle*2D)*.11D;
            level.sendParticles(SLATE_SHARD,x,y,z,1,0,0,0,.012D);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
