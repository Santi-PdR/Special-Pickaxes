package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Telegraphing root snares created by the Worldloom's held area skill. */
public final class WorldloomSnare {
    private static final int WINDUP_TICKS=80;
    private static final int ROOT_DURATION_TICKS=80;
    private static final int ROOT_PULSE_TICKS=10;
    private static final int ROOT_PARTICLES=4;
    private static final net.minecraft.world.level.block.state.BlockState VINE_STATE=
            net.minecraft.world.level.block.Blocks.VINE.defaultBlockState();
    private static final net.minecraft.core.particles.BlockParticleOption VINE_PARTICLE=
            new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,VINE_STATE);
    private static final class Binding {
        final LivingEntity target;
        final ServerPlayer owner;
        final ServerLevel level;
        final long started;
        Vec3 anchor;
        long expires;

        Binding(LivingEntity target,ServerPlayer owner,ServerLevel level,long started){
            this.target=target;this.owner=owner;this.level=level;this.started=started;
        }
    }
    private static final Map<UUID,Binding> BINDINGS=new HashMap<>();
    private WorldloomSnare() {}

    public static boolean bind(ServerPlayer owner,LivingEntity target){
        if(!target.isAlive()||target==owner||target.isAlliedTo(owner)||DomainFields.frozen(target)
                ||!WorldSafety.allowed(owner,ArtifactKind.WORLDLOOM,target.blockPosition()))return false;
        if(target instanceof Player other&&!owner.canHarmPlayer(other))return false;
        UUID id=target.getUUID();
        if(!BINDINGS.containsKey(id)&&BINDINGS.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
        var level=owner.serverLevel();
        long now=level.getGameTime();
        BINDINGS.put(id,new Binding(target,owner,level,now));
        return true;
    }

    public static void tick(){
        if(BINDINGS.isEmpty())return;
        var iterator=BINDINGS.entrySet().iterator();
        while(iterator.hasNext()){
            var binding=iterator.next().getValue();var target=binding.target;var owner=binding.owner;var level=binding.level;
            long now=level.getGameTime();
            if(!target.isAlive()||target.isRemoved()||owner.isRemoved()||!owner.isAlive()||owner.serverLevel()!=level||target.level()!=level
                    ||target.isAlliedTo(owner)||target instanceof Player other&&!owner.canHarmPlayer(other)
                    ||binding.expires>0&&now>=binding.expires){iterator.remove();continue;}
            if(now%5==0&&!WorldSafety.allowed(owner,ArtifactKind.WORLDLOOM,target.blockPosition())){iterator.remove();continue;}
            long age=now-binding.started;
            if(binding.anchor==null){
                // Roots cast together would send every telegraph in the same server tick.
                // Spread each target's pulse across the interval without reducing its cadence.
                if(pulseDue(binding,now))emitWindup(binding,age);
                if(age>=WINDUP_TICKS){
                    binding.anchor=target.position();
                    binding.expires=now+ROOT_DURATION_TICKS;
                    emitCatch(binding);
                }
                continue;
            }
            BlockPos at=BlockPos.containing(binding.anchor);
            if(level.hasChunkAt(at)){
                var box=target.getBoundingBox().move(binding.anchor.subtract(target.position()));
                if(level.noCollision(target,box))target.setPos(binding.anchor);
            }
            target.setDeltaMovement(Vec3.ZERO);target.hasImpulse=true;
            if(pulseDue(binding,now))emitCaughtPulse(binding,age);
        }
    }

    private static boolean pulseDue(Binding binding,long now){
        return visualPulseAt(now,binding.target.getId());
    }

    /** Deterministic entity phase spreads simultaneous root visuals across the pulse interval. */
    public static boolean visualPulseAt(long now,int entityId){
        return Math.floorMod(Math.floorMod(now,ROOT_PULSE_TICKS)+Math.floorMod(entityId,ROOT_PULSE_TICKS),ROOT_PULSE_TICKS)==0;
    }

    /** The ankle ring tightens over four seconds, giving targets a readable escape window. */
    private static void emitWindup(Binding binding,long age){
        double progress=Math.min(1D,Math.max(0D,age/(double)WINDUP_TICKS));
        double radius=.18D+.48D*progress,angle=age*.16D;
        double x=binding.target.getX(),y=binding.target.getY()+.08D,z=binding.target.getZ();
        emit(binding,x+Math.cos(angle)*radius,y,z+Math.sin(angle)*radius,ROOT_PARTICLES,.08D,.025D,.08D,.004D);
        emit(binding,x+Math.cos(angle+Math.PI)*radius,y,z+Math.sin(angle+Math.PI)*radius,ROOT_PARTICLES,.08D,.025D,.08D,.004D);
        if(progress>.55D){
            double rise=(progress-.55D)*.42D;
            emit(binding,x+Math.cos(angle)*radius*.72D,y+rise,z+Math.sin(angle)*radius*.72D,2,.035D,.04D,.035D,.002D);
        }
    }

    private static void emitCatch(Binding binding){
        double x=binding.anchor.x,y=binding.anchor.y+.12D,z=binding.anchor.z;
        for(int i=0;i<8;i++){
            double angle=i*Math.PI/4D;
            emit(binding,x+Math.cos(angle)*.34D,y,z+Math.sin(angle)*.34D,2,.035D,.12D,.035D,.006D);
        }
    }

    private static void emitCaughtPulse(Binding binding,long age){
        double angle=age*.22D,x=binding.anchor.x,y=binding.anchor.y+.08D,z=binding.anchor.z;
        emit(binding,x+Math.cos(angle)*.12D,y,z+Math.sin(angle)*.12D,ROOT_PARTICLES,.18D,.035D,.18D,.006D);
    }

    private static void emit(Binding binding,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
        // One bounded particle packet per nearby viewer keeps several simultaneous roots inexpensive.
        binding.level.sendParticles(binding.owner,VINE_PARTICLE,false,x,y,z,count,dx,dy,dz,speed);
        if(binding.target instanceof ServerPlayer viewer&&viewer!=binding.owner)
            binding.level.sendParticles(viewer,VINE_PARTICLE,false,x,y,z,count,dx,dy,dz,speed);
    }

    public static void forget(ServerPlayer owner){BINDINGS.values().removeIf(binding->binding.owner==owner);}
    public static void clear(){BINDINGS.clear();}
}
