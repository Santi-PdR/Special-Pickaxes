package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.UUID;

/** Iridium's Curios counter hardens its wearer in a visible, short-lived crystal ward. */
public final class LodeguardEffect extends MobEffect {
    private static final DustParticleOptions CRYSTAL_DUST=new DustParticleOptions(new Vector3f(.32F,.88F,.82F),.9F);

    public LodeguardEffect(){
        super(MobEffectCategory.BENEFICIAL,0x55d7c8);
        addAttributeModifier(Attributes.ARMOR,UUID.fromString("4a8b1439-e768-4fe6-a5de-6577137933b8").toString(),4D,AttributeModifier.Operation.ADDITION);
        addAttributeModifier(Attributes.KNOCKBACK_RESISTANCE,UUID.fromString("f0fb48f2-efc5-48d4-8354-e4c2fd4d2f9a").toString(),.2D,AttributeModifier.Operation.ADDITION);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(!entity.level().isClientSide&&entity.level() instanceof ServerLevel level){
            // The three-mote orbit stays light; one Blockbench crystal punctuates each second.
            double phase=entity.tickCount*.038D;
            double radius=.38D;
            double centerY=entity.getY()+entity.getBbHeight()*.56D;
            var particles=new ArrayList<ArtifactFeedback.AuraParticle>(4);
            for(int i=0;i<3;i++){
                double angle=phase+i*(Math.PI*2D/3D);
                double x=entity.getX()+Math.cos(angle)*radius;
                double z=entity.getZ()+Math.sin(angle)*radius;
                double y=centerY+Math.sin(angle*2D)*.09D;
                particles.add(new ArtifactFeedback.AuraParticle(CRYSTAL_DUST,new net.minecraft.world.phys.Vec3(x,y,z),1,0,0,0,0));
            }
            if(entity.tickCount%20==0){
                particles.add(new ArtifactFeedback.AuraParticle(
                        io.github.santipdr.specialpickaxes.SpecialPickaxes.IRIDIUM_LODE_SHARD.get(),
                        new net.minecraft.world.phys.Vec3(entity.getX(),centerY+.18D,entity.getZ()),1,0,0,0,0));
                // Twin foot-level marks make the new anti-knockback anchor readable
                // without turning a four-second ward into a continuous particle cloud.
                particles.add(new ArtifactFeedback.AuraParticle(CRYSTAL_DUST,
                        new net.minecraft.world.phys.Vec3(entity.getX()-.22D,entity.getY()+.08D,entity.getZ()),1,0,0,0,0));
                particles.add(new ArtifactFeedback.AuraParticle(CRYSTAL_DUST,
                        new net.minecraft.world.phys.Vec3(entity.getX()+.22D,entity.getY()+.08D,entity.getZ()),1,0,0,0,0));
            }
            ArtifactFeedback.entityAuraPulse(level,entity,particles);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){
        return duration%10==0;
    }
}
