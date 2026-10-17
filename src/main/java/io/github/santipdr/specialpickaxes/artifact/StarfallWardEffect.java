package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Exodium's Curios passive catches projectiles in a short-lived stellar veil. */
public final class StarfallWardEffect extends MobEffect {
    private static final String NEXT_FLASH="specialpickaxesStarfallFlashAt";
    private static final DustParticleOptions STARDUST=new DustParticleOptions(new Vector3f(1F,.36F,.79F),.82F);

    public StarfallWardEffect(){super(MobEffectCategory.BENEFICIAL,0xf65dc0);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity instanceof ServerPlayer wearer)||!(entity.level() instanceof ServerLevel level))return;
        tryCatchIncoming(wearer,level);
        if(entity.tickCount%10!=0)return;
        double phase=entity.tickCount*.045D;
        double y=entity.getY()+entity.getBbHeight()*.56D;
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI*.5D;
            double radius=.38D+(i%2)*.08D;
            level.sendParticles(wearer,STARDUST,false,entity.getX()+Math.cos(angle)*radius,y+Math.sin(angle*2D)*.08D,
                    entity.getZ()+Math.sin(angle)*radius,1,0,0,0,0);
        }
        if(entity.tickCount%20==0)
            level.sendParticles(wearer,ParticleTypes.END_ROD,false,entity.getX(),y+.18D,entity.getZ(),1,.04D,.08D,.04D,.005D);
    }

    /** Starfall catches one hostile shot whose current path would pass through the wearer's body. */
    public static boolean tryCatchIncoming(ServerPlayer wearer,ServerLevel level){
        var state=ArtifactState.of(wearer,ArtifactKind.EXODIUM);long now=ArtifactState.now(wearer);
        if(state.getLong("starfallCatchReady")>now)return false;
        Vec3 center=wearer.position().add(0,wearer.getBbHeight()*.5D,0);
        var incoming=EntitySelection.nearest(level,Entity.class,wearer.getBoundingBox().inflate(8D),shot->
                isIncomingHostile(shot,wearer,center),center,1,256);
        if(incoming.isEmpty())return false;
        Entity shot=incoming.get(0);
        shot.discard();
        state.putLong("starfallCatchReady",now+60L);
        projectileImpact(wearer);
        return true;
    }

    private static boolean isIncomingHostile(Entity shot,ServerPlayer wearer,Vec3 center){
        if(!JegProjectileCompat.isProjectile(shot)||shot==wearer||!shot.isAlive())return false;
        Entity owner=JegProjectileCompat.owner(shot);
        if(owner==wearer||owner!=null&&owner.isAlliedTo(wearer))return false;
        if(owner instanceof Player other&&!wearer.canHarmPlayer(other))return false;
        Vec3 motion=shot.getDeltaMovement();
        if(motion.lengthSqr()<.01D)return false;
        Vec3 toward=center.subtract(shot.position()),direction=motion.normalize();
        double along=toward.dot(direction);
        if(along<=0D||along>8D)return false;
        return toward.subtract(direction.scale(along)).lengthSqr()<=1.25D*1.25D;
    }

    /** A brief, low-count flash confirms the Curios ward caught a hostile shot. */
    public static void projectileImpact(ServerPlayer wearer){
        if(wearer.level().isClientSide||!(wearer.level() instanceof ServerLevel level))return;
        long now=wearer.tickCount;var data=wearer.getPersistentData();
        if(data.getLong(NEXT_FLASH)>now)return;
        data.putLong(NEXT_FLASH,now+5L);
        double x=wearer.getX(),y=wearer.getY()+wearer.getBbHeight()*.56D,z=wearer.getZ();
        emitImpact(wearer,level,x,y,z);
        var center=wearer.position();
        var nearby=EntitySelection.nearest(level,ServerPlayer.class,wearer.getBoundingBox().inflate(8D),
                listener->listener!=wearer&&listener.isAlive()&&listener.distanceToSqr(center)<=64D,
                center,8,128);
        for(var listener:nearby)emitImpact(listener,level,x,y,z);
    }

    private static void emitImpact(ServerPlayer viewer,ServerLevel level,double x,double y,double z){
        level.sendParticles(viewer,ParticleTypes.END_ROD,false,x,y,z,3,.2D,.18D,.2D,.015D);
        level.sendParticles(viewer,STARDUST,false,x,y,z,5,.3D,.24D,.3D,.01D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%2==0;}
}
