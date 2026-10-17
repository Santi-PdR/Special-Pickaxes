package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.BlockParticleOption;
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

/** Worldloom's Curios counter catches one hostile attacker in a brief, visible root drag. */
public final class HeartwoodGraspEffect extends MobEffect {
    private static final BlockParticleOption ROOTS=new BlockParticleOption(ParticleTypes.BLOCK,
            net.minecraft.world.level.block.Blocks.VINE.defaultBlockState());
    private static final DustParticleOptions HEARTWOOD=new DustParticleOptions(new Vector3f(.28F,.82F,.34F),.9F);

    public HeartwoodGraspEffect(){
        super(MobEffectCategory.HARMFUL,0x58bd62);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,UUID.fromString("15dfc8c2-cf3d-4313-9b31-779b94b2a6fa").toString(),-.25D,AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        Vec3 motion=entity.getDeltaMovement();
        double drag=amplifier>0?.42D:.58D;
        entity.setDeltaMovement(motion.x*drag,motion.y,motion.z*drag);
        entity.hasImpulse=true;
        if(entity.tickCount%10==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.16D;
            double radius=Math.max(.24D,entity.getBbWidth()*.48D);
            for(int i=0;i<4;i++){
                double angle=phase+i*Math.PI*.5D;
                double x=entity.getX()+Math.cos(angle)*radius;
                double z=entity.getZ()+Math.sin(angle)*radius;
                // Each mote climbs to the next band, making the four-pulse wrap legible as roots.
                double rise=((entity.tickCount/10L+i)%4L)*.16D;
                double y=entity.getY()+.1D+rise;
                level.sendParticles(ROOTS,x,y,z,1,.015D,.025D,.015D,.003D);
                if((i&1)==0)level.sendParticles(HEARTWOOD,x,y+.08D,z,1,0,.02D,0,0);
            }
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
