package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Eventide's Curios passive bends a fast fall into a controlled descent. */
public final class GraviticBalanceEffect extends MobEffect {
    private static final DustParticleOptions GRAVITY_DUST=new DustParticleOptions(new Vector3f(.63F,.42F,.96F),.78F);
    public GraviticBalanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa67aff);}

    /** A focused ground ripple shows that Eventide caught a dangerous fall without a generic hit burst. */
    public static void landingPulse(ServerPlayer player,float fallDistance){
        if(!(player.level() instanceof ServerLevel level))return;
        double intensity=Math.max(0D,Math.min(1D,fallDistance/12D));
        double phase=level.getGameTime()*.12D,radius=.52D+intensity*.62D;
        double x=player.getX(),y=player.getY()+.12D,z=player.getZ();
        var violet=new DustParticleOptions(new Vector3f(.68F,.48F,1F),.9F+(.35F*(float)intensity));
        for(int i=0;i<8;i++){
            double angle=phase+i*Math.PI/4D;
            double px=x+Math.cos(angle)*radius,pz=z+Math.sin(angle)*radius;
            level.sendParticles(player,violet,false,px,y,pz,1,0,.035D,0,.002D);
            if((i&1)==0)level.sendParticles(player,ParticleTypes.REVERSE_PORTAL,false,
                    x+Math.cos(angle)*radius*.58D,y+.08D,z+Math.sin(angle)*radius*.58D,1,0,0,0,0);
        }
        level.sendParticles(player,ParticleTypes.REVERSE_PORTAL,false,x,y+.04D,z,3,.12D,.015D,.12D,.012D);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround()||entity.isFallFlying())return;
        var motion=entity.getDeltaMovement();
        if(motion.y<-.24D){entity.setDeltaMovement(motion.x,-.24D,motion.z);entity.hasImpulse=true;}
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.075D;
            double centerY=entity.getY()+entity.getBbHeight()*.5D;
            for(int i=0;i<3;i++){
                double angle=phase+i*Math.PI*2D/3D;
                double x=entity.getX()+Math.cos(angle)*.34D;
                double z=entity.getZ()+Math.sin(angle)*.34D;
                double y=centerY+Math.sin(angle*2D)*.08D;
                level.sendParticles(GRAVITY_DUST,x,y,z,1,0,0,0,0);
            }
            if(entity.tickCount%24==0)
                level.sendParticles(ParticleTypes.REVERSE_PORTAL,entity.getX(),centerY,entity.getZ(),1,.04D,.08D,.04D,.005D);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
