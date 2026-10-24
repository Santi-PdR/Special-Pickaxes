package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.UUID;

/** Worldloom's Curios counter catches one hostile attacker in a brief, visible root drag. */
public final class HeartwoodGraspEffect extends MobEffect {
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
            double phase=level.getGameTime()*.16D;
            Vec3 feet=new Vec3(entity.getX(),entity.getY(),entity.getZ());
            var particles=new ArrayList<ArtifactFeedback.AuraParticle>(4);
            for(int i=0;i<4;i++){
                Vec3 point=rootFootPoint(feet,phase,i);
                particles.add(new ArtifactFeedback.AuraParticle(
                        io.github.santipdr.specialpickaxes.SpecialPickaxes.WORLDLOOM_ROOT_CLAMP.get(),
                        point,1,0,0,0,0));
            }
            ArtifactFeedback.entityAuraPulse(level,entity,particles);
        }
    }

    /** Compact animated ring at the target's feet makes the root snare read as ground restraint. */
    public static Vec3 rootFootPoint(Vec3 feet,double phase,int root){
        double angle=phase+Math.floorMod(root,4)*Math.PI*.5D;
        double radius=.29D+.035D*Math.sin(phase+root*.4D);
        return feet.add(Math.cos(angle)*radius,.055D+.018D*Math.sin(phase*2D+root),Math.sin(angle)*radius);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
