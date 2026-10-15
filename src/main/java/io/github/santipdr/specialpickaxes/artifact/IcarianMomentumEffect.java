package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Icarus' Curios passive turns a sprint-jump into a controlled, visible aerial glide. */
public final class IcarianMomentumEffect extends MobEffect {
    public IcarianMomentumEffect(){super(MobEffectCategory.BENEFICIAL,0xffc057);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround())return;
        if(entity instanceof Player player&&player.isSprinting()){
            Vec3 look=player.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);
            if(forward.lengthSqr()>.001D){
                Vec3 motion=player.getDeltaMovement();Vec3 horizontal=new Vec3(motion.x,0,motion.z).add(forward.normalize().scale(.06D));
                if(horizontal.lengthSqr()>.64D)horizontal=horizontal.normalize().scale(.8D);
                player.setDeltaMovement(horizontal.x,motion.y,horizontal.z);
            }
        }
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            Vec3 look=entity.getLookAngle();double sideX=-look.z*.55D,sideZ=look.x*.55D;
            level.sendParticles(ParticleTypes.END_ROD,entity.getX()+sideX,entity.getY()+entity.getBbHeight()*.62D,entity.getZ()+sideZ,1,0,0,0,0);
            level.sendParticles(ParticleTypes.END_ROD,entity.getX()-sideX,entity.getY()+entity.getBbHeight()*.62D,entity.getZ()-sideZ,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
