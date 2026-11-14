package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;

/** Eventide's Curios passive bends a fast fall into a controlled descent. */
public final class GraviticBalanceEffect extends MobEffect {
    private static final float MIN_IMPACT_FALL=7F;
    private static final double IMPACT_RADIUS=4.5D;
    private static final int IMPACT_TARGETS=3;
    private static final int IMPACT_COOLDOWN=200;
    private static final int IMPACT_SNARE_TICKS=60;
    private static final DustParticleOptions GRAVITY_DUST=new DustParticleOptions(new Vector3f(.63F,.42F,.96F),.78F);
    public GraviticBalanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa67aff);}

    /** A focused ground ripple shows that Eventide caught a dangerous fall without a generic hit burst. */
    public static void landingPulse(ServerPlayer player,float fallDistance){
        if(!Float.isFinite(fallDistance)||!(player.level() instanceof ServerLevel level)
                ||!ArtifactFeedback.reserveAuraParticles(level.getServer().getTickCount(),15,1))return;
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

    private static final java.util.Set<java.util.UUID> LANDING=new java.util.HashSet<>();

    /** Eventide turns a committed Curios landing into a brief, capped anti-chase gravity snare. */
    public static int landingSnare(ServerPlayer player,float fallDistance){
        if(!Float.isFinite(fallDistance)||fallDistance<MIN_IMPACT_FALL||!player.isAlive()||player.isRemoved()||player.isSpectator()
                ||!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get())
                ||!LANDING.add(player.getUUID()))return 0;
        try{
            var level=player.serverLevel();var center=player.position();
            var balance=player.getEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get());
            var state=ArtifactState.of(player,ArtifactKind.EVENTIDE);long now=ArtifactState.now(player);
            if(PersistedCooldown.active(now,state.getLong("eventideImpactReady"),IMPACT_COOLDOWN)
                    ||!WorldSafety.allowed(player,ArtifactKind.EVENTIDE,player.blockPosition())
                    ||!landingContext(player,level,center,balance))return 0;
            var targets=EntitySelection.nearest(level,LivingEntity.class,player.getBoundingBox().inflate(IMPACT_RADIUS),
                    target->landingTarget(player,target,center),center,IMPACT_TARGETS,128);
            int caught=0;var tether=io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_TETHER.get();
            for(var target:targets){
                if(!landingContext(player,level,center,balance))break;
                var targetPosition=target.position();
                if(!landingTarget(player,target,center)||!WorldSafety.allowed(player,ArtifactKind.EVENTIDE,target.blockPosition())
                        ||!landingContext(player,level,center,balance)||!target.position().equals(targetPosition)
                        ||!landingTarget(player,target,center))continue;
                if(caught==0&&PersistedCooldown.active(ArtifactState.now(player),state.getLong("eventideImpactReady"),IMPACT_COOLDOWN))break;
                if(!ArtifactPassives.grantCombatEffect(target,tether,0,IMPACT_SNARE_TICKS))continue;
                if(caught++==0)state.putLong("eventideImpactReady",ArtifactState.now(player)+IMPACT_COOLDOWN);
                Vec3 targetCenter=target.position().add(0,target.getBbHeight()*.5D,0);
                ArtifactFeedback.gravityTether(player,center.add(0,.35D,0),targetCenter);
            }
            return caught;
        }finally{LANDING.remove(player.getUUID());}
    }

    private static boolean landingContext(ServerPlayer player,ServerLevel level,Vec3 origin,net.minecraft.world.effect.MobEffectInstance balance){
        return player.serverLevel()==level&&player.isAlive()&&!player.isRemoved()&&!player.isSpectator()
                &&player.position().equals(origin)&&player.getEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get())==balance;
    }
    private static boolean landingTarget(ServerPlayer player,LivingEntity target,Vec3 center){
        return target!=player&&target.isAlive()&&!target.isRemoved()&&target.level()==player.level()
                &&EntitySelection.isHostileCandidate(target,player)&&!target.isAlliedTo(player)&&!player.isAlliedTo(target)
                &&target.distanceToSqr(center)<=IMPACT_RADIUS*IMPACT_RADIUS
                &&(!(target instanceof net.minecraft.world.entity.player.Player other)||player.canHarmPlayer(other));
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||entity.onGround()||entity.isFallFlying())return;
        var motion=entity.getDeltaMovement();
        if(motion.y<-.24D){entity.setDeltaMovement(motion.x,-.24D,motion.z);entity.hasImpulse=true;}
        if(entity.tickCount%12==0&&entity.level() instanceof ServerLevel level){
            double phase=entity.tickCount*.075D;
            double centerY=entity.getY()+entity.getBbHeight()*.5D;
            var particles=new ArrayList<ArtifactFeedback.AuraParticle>(4);
            for(int i=0;i<3;i++){
                double angle=phase+i*Math.PI*2D/3D;
                double x=entity.getX()+Math.cos(angle)*.34D;
                double z=entity.getZ()+Math.sin(angle)*.34D;
                double y=centerY+Math.sin(angle*2D)*.08D;
                particles.add(new ArtifactFeedback.AuraParticle(GRAVITY_DUST,new net.minecraft.world.phys.Vec3(x,y,z),1,0,0,0,0));
            }
            if(entity.tickCount%24==0)particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.REVERSE_PORTAL,
                    new net.minecraft.world.phys.Vec3(entity.getX(),centerY,entity.getZ()),1,.04D,.08D,.04D,.005D));
            ArtifactFeedback.entityAuraPulse(level,entity,particles);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%4==0;}
}
