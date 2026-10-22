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
    private static final RelicParticleBudget VISUAL_BUDGET=new RelicParticleBudget(256);
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
        var level=owner.serverLevel();long now=level.getGameTime();
        Binding existing=BINDINGS.get(id);
        if(existing!=null){
            boolean active=existing.level==level&&existing.target.isAlive()&&!existing.target.isRemoved()
                    &&existing.owner.isAlive()&&!existing.owner.isRemoved()&&existing.owner.serverLevel()==level
                    &&existing.target.level()==level&&(existing.expires==0||now<existing.expires);
            if(active)return false;
            BINDINGS.remove(id);
        }
        if(!BINDINGS.containsKey(id)&&BINDINGS.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
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
        if(!reserveVisualParticles(binding.level.getServer().getTickCount(),progress>.55D?10:8))return;
        double radius=.22D+.48D*progress,angle=age*.16D;
        double x=binding.target.getX(),y=binding.target.getY()+.08D,z=binding.target.getZ();
        // Four moving anchors make the closing circle readable from every side while
        // keeping the same pulse interval and bounded particle count per target.
        for(int i=0;i<4;i++){
            double spoke=angle+i*Math.PI*.5D;
            emit(binding,x+Math.cos(spoke)*radius,y,z+Math.sin(spoke)*radius,2,.055D,.035D,.055D,.004D);
        }
        if(progress>.55D){
            double rise=(progress-.55D)*.42D;
            emit(binding,x+Math.cos(angle)*radius*.72D,y+rise,z+Math.sin(angle)*radius*.72D,2,.035D,.04D,.035D,.002D);
        }
    }

    private static void emitCatch(Binding binding){
        if(!reserveVisualParticles(binding.level.getServer().getTickCount(),24))return;
        double x=binding.anchor.x,y=binding.anchor.y+.12D,z=binding.anchor.z;
        for(int i=0;i<8;i++){
            double angle=i*Math.PI/4D;
            emit(binding,x+Math.cos(angle)*.34D,y,z+Math.sin(angle)*.34D,2,.035D,.12D,.035D,.006D);
        }
        // Four short vine columns make the catch read as roots closing around a body,
        // while keeping this one-shot cue small even when several targets are caught.
        for(int i=0;i<4;i++){
            var point=catchVinePoint(binding.anchor,i);
            emit(binding,point.x,point.y,point.z,2,.025D,.48D,.025D,.006D);
        }
    }

    /** Center point for one of the four brief vertical vine columns on a successful catch. */
    public static Vec3 catchVinePoint(Vec3 anchor,int quadrant){
        double angle=Math.floorMod(quadrant,4)*Math.PI*.5D;
        return anchor.add(Math.cos(angle)*.34D,.58D,Math.sin(angle)*.34D);
    }

    private static void emitCaughtPulse(Binding binding,long age){
        if(!reserveVisualParticles(binding.level.getServer().getTickCount(),ROOT_PARTICLES))return;
        double angle=age*.22D,x=binding.anchor.x,y=binding.anchor.y+.08D,z=binding.anchor.z;
        emit(binding,x+Math.cos(angle)*.12D,y,z+Math.sin(angle)*.12D,ROOT_PARTICLES,.18D,.035D,.18D,.006D);
    }

    /** Whole root signatures share a strict per-server-tick cosmetic budget; gameplay never depends on particles. */
    public static boolean reserveVisualParticles(long serverTick,int count){
        return VISUAL_BUDGET.tryReserve(serverTick,count);
    }

    private static void emit(Binding binding,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
        // The server's normal tracking range delivers the effect to nearby viewers
        // without iterating every player in the level or sending it across dimensions.
        binding.level.sendParticles(VINE_PARTICLE,x,y,z,count,dx,dy,dz,speed);
    }

    public static void forget(ServerPlayer owner){BINDINGS.values().removeIf(binding->binding.owner==owner);}
    public static void clear(){BINDINGS.clear();}
}
