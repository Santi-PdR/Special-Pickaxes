package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Icarus' Curios passive turns a sprint-jump into a controlled, visible aerial glide. */
public final class IcarianMomentumEffect extends MobEffect {
    private static final DustParticleOptions WING_GLEAM=new DustParticleOptions(new Vector3f(.98F,.82F,.48F),.72F);
    private static final String LANDING_READY="specialpickaxesIcarianLandingReadyAtWorldTime";
    public IcarianMomentumEffect(){super(MobEffectCategory.BENEFICIAL,0xffc057);}

    /** Turns a meaningful protected landing into a short forward rebound, once every two seconds. */
    public static boolean convertLanding(ServerPlayer player,float fallDistance){
        long now=ArtifactState.now(player);
        if(fallDistance<5F||player.getPersistentData().getLong(LANDING_READY)>now)return false;
        Vec3 look=player.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);
        if(forward.lengthSqr()<.001D){Vec3 motion=player.getDeltaMovement();forward=new Vec3(motion.x,0,motion.z);}
        if(forward.lengthSqr()<.001D)return false;
        forward=forward.normalize();double kick=Math.min(.62D,.38D+(fallDistance-5F)*.025D);
        Vec3 motion=player.getDeltaMovement();
        player.setDeltaMovement(motion.x+forward.x*kick,Math.max(.12D,motion.y),motion.z+forward.z*kick);
        player.hasImpulse=true;player.getPersistentData().putLong(LANDING_READY,now+40L);
        return true;
    }

    /** A compact pair of feather arcs makes the landing rebound visible without a large particle burst. */
    public static void landingPulse(ServerPlayer player){
        if(!(player.level() instanceof ServerLevel level))return;
        Vec3 look=player.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);
        if(forward.lengthSqr()<.001D)forward=new Vec3(0,0,1);else forward=forward.normalize();
        Vec3 right=new Vec3(-forward.z,0,forward.x);double y=player.getY()+.18D;
        for(int side=-1;side<=1;side+=2)for(int feather=0;feather<3;feather++){
            double span=.22D+feather*.18D,trail=.08D+feather*.12D;
            double x=player.getX()+right.x*side*span-forward.x*trail;
            double z=player.getZ()+right.z*side*span-forward.z*trail;
            level.sendParticles(WING_GLEAM,x,y+feather*.025D,z,1,0,.015D,0,0);
            if(feather==2)level.sendParticles(ParticleTypes.END_ROD,x,y+.04D,z,1,0,.02D,0,0);
        }
    }

    /** Keeps the feather wake aligned with actual travel, even when the wearer looks aside mid-glide. */
    public static Vec3 glideTrailForward(Vec3 motion,Vec3 look){
        Vec3 travel=new Vec3(motion.x,0,motion.z);
        if(travel.lengthSqr()>.0025D)return travel.normalize();
        Vec3 facing=new Vec3(look.x,0,look.z);
        return facing.lengthSqr()>.001D?facing.normalize():new Vec3(0,0,1);
    }

    /** Sprinting while looking up turns Icarus' glide into a slow, capped ascent. */
    public static Vec3 guidedLift(Vec3 motion,Vec3 look,boolean sprinting,boolean fallFlying){
        if(!sprinting||fallFlying||look.y<.55D)return motion;
        double lift=.30D+Math.min(.10D,(look.y-.55D)*.22D);
        return new Vec3(motion.x,Math.min(.22D,motion.y+lift),motion.z);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround())return;
        Vec3 motion=entity.getDeltaMovement();
        if(!entity.isFallFlying()&&motion.y<-.36D){entity.setDeltaMovement(motion.x,-.36D,motion.z);entity.hasImpulse=true;}
        if(entity instanceof Player player&&player.isSprinting()){
            Vec3 look=player.getLookAngle();Vec3 forward=new Vec3(look.x,0,look.z);
            if(forward.lengthSqr()>.001D){
                motion=player.getDeltaMovement();Vec3 horizontal=new Vec3(motion.x,0,motion.z).add(forward.normalize().scale(.06D));
                if(horizontal.lengthSqr()>.64D)horizontal=horizontal.normalize().scale(.8D);
                player.setDeltaMovement(horizontal.x,motion.y,horizontal.z);
            }
        }
        if(entity instanceof Player player){
            Vec3 before=player.getDeltaMovement();Vec3 lifted=guidedLift(before,player.getLookAngle(),player.isSprinting(),player.isFallFlying());
            if(lifted.y!=before.y){player.setDeltaMovement(lifted);player.hasImpulse=true;}
        }
        if(entity instanceof Player player&&player.isSprinting()&&entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            Vec3 forward=glideTrailForward(player.getDeltaMovement(),player.getLookAngle());
            Vec3 right=new Vec3(-forward.z,0,forward.x);
            double speed=Math.min(.8D,player.getDeltaMovement().horizontalDistance());
            double shoulderY=entity.getY()+entity.getBbHeight()*.56D;
            for(int side=-1;side<=1;side+=2)for(int feather=0;feather<3;feather++){
                double span=(.24D+feather*.19D)*(1D+speed*.22D),trail=.08D+feather*.13D+speed*.12D;
                double x=entity.getX()+right.x*side*span-forward.x*trail;
                double z=entity.getZ()+right.z*side*span-forward.z*trail;
                double y=shoulderY+(2-feather)*.035D;
                level.sendParticles(WING_GLEAM,x,y,z,1,0,0,0,0);
                if(feather==2)level.sendParticles(ParticleTypes.END_ROD,x,y,z,1,0,0,0,0);
            }
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
