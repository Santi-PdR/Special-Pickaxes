package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.UUID;

/** Interregnum compresses a target's movement and attack cadence into a visible stasis lock. */
public final class StasisCrushEffect extends MobEffect {
    private static final DustParticleOptions STASIS=new DustParticleOptions(new Vector3f(.48F,.76F,1F),1.05F);
    private static final DustParticleOptions VIOLET=new DustParticleOptions(new Vector3f(.68F,.39F,.96F),.92F);

    public StasisCrushEffect(){
        super(MobEffectCategory.HARMFUL,0x8ae5ff);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,UUID.fromString("01bca78e-7b1d-4e39-9c6c-7f6d5224cfc0").toString(),-.65D,AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,UUID.fromString("9ce09b7a-e5c0-4b8f-856e-3e1fb5a69c5e").toString(),-.45D,AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        Vec3 motion=entity.getDeltaMovement();
        double drag=amplifier>0?.22D:.38D;
        entity.setDeltaMovement(motion.x*drag,motion.y,motion.z*drag);
        entity.hasImpulse=true;
        if(entity.tickCount%8==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.13D;
            double y=entity.getY()+entity.getBbHeight()*.42D;
            double radius=Math.max(.25D,entity.getBbWidth()*.55D);
            for(int i=0;i<3;i++){
                double angle=phase+i*Math.PI*2D/3D;
                double x=entity.getX()+Math.cos(angle)*radius,z=entity.getZ()+Math.sin(angle)*radius;
                level.sendParticles(i==0?VIOLET:STASIS,x,y+Math.sin(angle*2D)*.07D,z,1,0,.015D,0,0);
            }
            if(entity.tickCount%24==0)level.sendParticles(ParticleTypes.REVERSE_PORTAL,entity.getX(),y,entity.getZ(),2,.08D,.12D,.08D,.004D);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
