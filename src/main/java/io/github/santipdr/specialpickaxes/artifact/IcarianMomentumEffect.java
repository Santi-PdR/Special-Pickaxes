package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Icarus' Curios passive turns a sprint-jump into a controlled, visible aerial glide. */
public final class IcarianMomentumEffect extends MobEffect {
    private static final DustParticleOptions WING_GLEAM=new DustParticleOptions(new Vector3f(.98F,.82F,.48F),.72F);
    public IcarianMomentumEffect(){super(MobEffectCategory.BENEFICIAL,0xffc057);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround())return;
        Vec3 motion=entity.getDeltaMovement();
        if(motion.y<-.36D){entity.setDeltaMovement(motion.x,-.36D,motion.z);entity.hasImpulse=true;}
        if(entity instanceof Player player&&player.isSprinting()){
            Vec3 look=player.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);
            if(forward.lengthSqr()>.001D){
                motion=player.getDeltaMovement();Vec3 horizontal=new Vec3(motion.x,0,motion.z).add(forward.normalize().scale(.06D));
                if(horizontal.lengthSqr()>.64D)horizontal=horizontal.normalize().scale(.8D);
                player.setDeltaMovement(horizontal.x,motion.y,horizontal.z);
            }
        }
        if(entity instanceof Player player&&player.isSprinting()&&entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            Vec3 look=player.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);
            if(forward.lengthSqr()>.001D){
                forward=forward.normalize();Vec3 right=new Vec3(-forward.z,0,forward.x);
                double shoulderY=entity.getY()+entity.getBbHeight()*.56D;
                for(int side=-1;side<=1;side+=2)for(int feather=0;feather<3;feather++){
                    double span=.24D+feather*.19D,trail=.08D+feather*.13D;
                    double x=entity.getX()+right.x*side*span-forward.x*trail;
                    double z=entity.getZ()+right.z*side*span-forward.z*trail;
                    double y=shoulderY+(2-feather)*.035D;
                    level.sendParticles(WING_GLEAM,x,y,z,1,0,0,0,0);
                    if(feather==2)level.sendParticles(ParticleTypes.END_ROD,x,y,z,1,0,0,0,0);
                }
            }
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
