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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import org.joml.Vector3f;
import java.util.ArrayList;

/** Exodium's Curios passive catches projectiles in a short-lived stellar veil. */
public final class StarfallWardEffect extends MobEffect {
    private static final double SCAN_RADIUS=10D;
    private static final double PREDICTION_DISTANCE=10D;
    private static final int CATCH_COOLDOWN_TICKS=30;
    private static final String NEXT_FLASH="specialpickaxesStarfallFlashAtWorldTime";
    private static final DustParticleOptions STARDUST=new DustParticleOptions(new Vector3f(1F,.36F,.79F),.82F);
    private static final DustParticleOptions RECHARGING_DUST=new DustParticleOptions(new Vector3f(.58F,.38F,.88F),.64F);
    private static final DustParticleOptions GUARD_DUST=new DustParticleOptions(new Vector3f(.96F,.72F,1F),.72F);

    public StarfallWardEffect(){super(MobEffectCategory.BENEFICIAL,0xf65dc0);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity instanceof ServerPlayer wearer)||!(entity.level() instanceof ServerLevel level))return;
        if(shouldPredictiveScan(entity.tickCount,entity.getId()))tryCatchIncoming(wearer,level);
        if(entity.tickCount%12!=0)return;
        long now=ArtifactState.now(wearer),readyAt=ArtifactState.of(wearer,ArtifactKind.EXODIUM).getLong("starfallCatchReady");
        double charge=chargeProgress(now,readyAt);
        ArtifactFeedback.nearbyWardPulse(wearer,orbitParticles(entity,entity.tickCount,charge));
    }

    /** Starfall catches one hostile shot whose current path would pass through the wearer's body. */
    public static boolean tryCatchIncoming(ServerPlayer wearer,ServerLevel level){
        var state=ArtifactState.of(wearer,ArtifactKind.EXODIUM);long now=ArtifactState.now(wearer);
        if(state.getLong("starfallCatchReady")>now)return false;
        Vec3 center=wearer.position().add(0,wearer.getBbHeight()*.5D,0);
        var incoming=JegProjectileCompat.nearest(level,wearer.getBoundingBox().inflate(SCAN_RADIUS),shot->
                isIncomingHostile(shot,wearer,center),center,1,256);
        if(incoming.isEmpty())return false;
        return consumeCatch(wearer,incoming.get(0));
    }

    /** Catch exact entity impacts as a final guard for bullets that cross the scan between ticks. */
    public static boolean interceptImpact(ProjectileImpactEvent event){
        if(!(event.getRayTraceResult() instanceof EntityHitResult hit)
                ||!(hit.getEntity() instanceof ServerPlayer wearer)
                ||!wearer.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get()))return false;
        Projectile shot=event.getProjectile();
        if(!isHostileTo(shot,wearer))return false;
        var state=ArtifactState.of(wearer,ArtifactKind.EXODIUM);long now=ArtifactState.now(wearer);
        if(state.getLong("starfallCatchReady")>now)return false;
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        return consumeCatch(wearer,shot);
    }

    private static boolean consumeCatch(ServerPlayer wearer,Entity shot){
        var state=ArtifactState.of(wearer,ArtifactKind.EXODIUM);long now=ArtifactState.now(wearer);
        if(state.getLong("starfallCatchReady")>now)return false;
        shot.discard();state.putLong("starfallCatchReady",now+CATCH_COOLDOWN_TICKS);
        projectileImpact(wearer);return true;
    }

    private static boolean isHostileTo(Entity shot,ServerPlayer wearer){
        if(!JegProjectileCompat.isProjectile(shot)||shot==wearer||!shot.isAlive())return false;
        Entity owner=JegProjectileCompat.owner(shot);
        if(owner==wearer||owner!=null&&owner.isAlliedTo(wearer))return false;
        return !(owner instanceof Player other)||wearer.canHarmPlayer(other);
    }

    /** Visual charge fraction: the orbit contracts after a catch and expands as the ward recharges. */
    public static double chargeProgress(long now,long readyAt){
        return readyAt<=now?1D:1D-Math.min(1D,(readyAt-now)/(double)CATCH_COOLDOWN_TICKS);
    }

    /** The four-point charge orbit is visible to the bearer and nearby teammates who share the ward. */
    public static ArrayList<ArtifactFeedback.AuraParticle> orbitParticles(LivingEntity wearer,int tick,double charge){
        double boundedCharge=Math.max(0D,Math.min(1D,charge));
        double phase=tick*.045D,y=wearer.getY()+wearer.getBbHeight()*.56D;
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(5);
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI*.5D;
            double radius=.22D+boundedCharge*.16D+(i%2)*.035D;
            particles.add(new ArtifactFeedback.AuraParticle(boundedCharge>=1D?STARDUST:RECHARGING_DUST,
                    new Vec3(wearer.getX()+Math.cos(angle)*radius,y+Math.sin(angle*2D)*.08D,
                            wearer.getZ()+Math.sin(angle)*radius),1,0,0,0,0));
        }
        if(boundedCharge>=1D&&tick%24==0)
            particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.END_ROD,
                    new Vec3(wearer.getX(),y+.18D,wearer.getZ()),1,.04D,.08D,.04D,.005D));
        return particles;
    }

    /** Stagger prediction scans across bearers; exact ProjectileImpactEvent interception remains per-impact. */
    public static boolean shouldPredictiveScan(int tickCount,int entityId){return ((tickCount+entityId)&1)==0;}

    private static boolean isIncomingHostile(Entity shot,ServerPlayer wearer,Vec3 center){
        if(!JegProjectileCompat.isProjectile(shot)||shot==wearer||!shot.isAlive())return false;
        Entity owner=JegProjectileCompat.owner(shot);
        if(owner==wearer||owner!=null&&owner.isAlliedTo(wearer))return false;
        if(owner instanceof Player other&&!wearer.canHarmPlayer(other))return false;
        Vec3 motion=shot.getDeltaMovement();
        if(motion.lengthSqr()<.01D)return false;
        Vec3 toward=center.subtract(shot.position()),direction=motion.normalize();
        double along=toward.dot(direction);
        if(along<=0D||along>PREDICTION_DISTANCE)return false;
        return toward.subtract(direction.scale(along)).lengthSqr()<=1.25D*1.25D;
    }

    /** A brief, low-count flash confirms the Curios ward caught a hostile shot. */
    public static void projectileImpact(ServerPlayer wearer){
        if(wearer.level().isClientSide||!(wearer.level() instanceof ServerLevel level))return;
        long now=ArtifactState.now(wearer);var data=wearer.getPersistentData();
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
        // One bounded spark burst gives the catch a readable, star-shaped snap without a particle loop.
        level.sendParticles(viewer,ParticleTypes.ELECTRIC_SPARK,false,x,y,z,6,.38D,.32D,.38D,.025D);
    }

    /** The shared damage-reduction ward gets a distinct, smaller flash under the same per-player throttle. */
    public static void guardImpact(ServerPlayer protectedPlayer){
        if(protectedPlayer.level().isClientSide||!(protectedPlayer.level() instanceof ServerLevel level))return;
        long now=ArtifactState.now(protectedPlayer);var data=protectedPlayer.getPersistentData();
        if(data.getLong(NEXT_FLASH)>now)return;
        data.putLong(NEXT_FLASH,now+5L);
        double x=protectedPlayer.getX(),y=protectedPlayer.getY()+protectedPlayer.getBbHeight()*.55D,z=protectedPlayer.getZ();
        double phase=level.getGameTime()*.18D;
        for(var viewer:ArtifactFeedback.nearbyWardAudience(protectedPlayer)){
            for(int i=0;i<6;i++){
                double angle=phase+i*Math.PI/3D;
                level.sendParticles(viewer,GUARD_DUST,false,x+Math.cos(angle)*.48D,y+Math.sin(angle*2D)*.08D,
                        z+Math.sin(angle)*.48D,1,0,0,0,0);
            }
            level.sendParticles(viewer,ParticleTypes.END_ROD,false,x,y,z,2,.09D,.12D,.09D,.008D);
        }
    }

    /** Check trajectories every server tick so fast modded rounds cannot cross the ward between scans. */
    @Override public boolean isDurationEffectTick(int duration,int amplifier){return true;}
}
