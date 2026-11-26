package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Finite velocity-powered ascent. Clears its swept headroom before applying upward velocity. */
public final class IcarianFlight {
    public static final int DURATION=32,GRACE=120,MAX_JOBS=32;
    public static final double MAX_RISE=14;
    public static final int SURFACE_DURATION=1200,LANDING_TIMEOUT=4000,MAX_LANDING_GUARDS=128;
    private record Landing(ServerPlayer player,ServerLevel level,long until){}
    private static final Map<UUID,Landing> LANDING=new HashMap<>();
    private static final Map<UUID,Flight> JOBS=new HashMap<>();
    private static boolean ticking;
    private static final Set<UUID> STARTING=new HashSet<>();
    private static final class Flight {
        final ServerPlayer player;final ServerLevel level;final ItemStack tool;final EquipmentLease<ItemStack> source;final Vec3 origin;final long start;final HeldSkillLease skill;
        final boolean seekSurface;double exitY;
        Vec3 previous;long lastTick=Long.MIN_VALUE;
        Flight(ServerPlayer p,ItemStack tool,EquipmentLease<ItemStack> source){this.source=source;player=p;level=p.serverLevel();this.tool=tool;skill=HeldSkillLease.capture(tool);origin=p.position();previous=origin;start=ArtifactState.now(p);
            int surface=level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.blockPosition().getX(),p.blockPosition().getZ());
            seekSurface=surface>origin.y+2;exitY=Math.min(level.getMaxBuildHeight()-2,surface+3);
        }
        boolean valid(){return JOBS.get(player.getUUID())==this&&skill.valid()&&caster(player,tool,source)&&player.serverLevel()==level
                &&player.position().distanceToSqr(previous)<=16&&horizontalDistanceSqr(player.position(),origin)<=32*32
                &&player.getY()-origin.y<(seekSurface?level.getMaxBuildHeight()-origin.y:MAX_RISE)&&ArtifactState.now(player)>=start&&ArtifactState.now(player)-start<(seekSurface?SURFACE_DURATION:DURATION);}
    }
    private IcarianFlight(){}
    private static double horizontalDistanceSqr(Vec3 a,Vec3 b){double x=a.x-b.x,z=a.z-b.z;return x*x+z*z;}
    public static boolean protectsFall(ServerPlayer p){
        var guard=LANDING.get(p.getUUID());long now=ArtifactState.now(p);
        return guard!=null&&guard.player()==p&&guard.level()==p.serverLevel()&&p.isAlive()&&!p.isRemoved()
                &&now<=guard.until()&&guard.until()-now<=LANDING_TIMEOUT;
    }
    private static void tickLanding(){
        for(var guard:List.copyOf(LANDING.values())){
            var p=guard.player();
            if(!protectsFall(p)||!active(p)&&p.onGround()){LANDING.remove(p.getUUID(),guard);continue;}
            p.fallDistance=0;
            p.addEffect(new MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING,40,0,false,false,true));
        }
    }
    private static boolean caster(ServerPlayer p,ItemStack tool){
        return p.isAlive()&&!p.isRemoved()&&!p.isSpectator()&&p.mayBuild()&&!p.isPassenger()&&!p.isSleeping()&&!p.isFallFlying()
                &&p.getMainHandItem()==tool&&!tool.isEmpty()&&tool.getItem() instanceof ArtifactItem item
                &&ArtifactSkills.feedbackKind(tool,item.kind)==ArtifactKind.ICARUS;
    }
    private static boolean caster(ServerPlayer p,ItemStack tool,EquipmentLease<ItemStack> source){
        return source==null?caster(p,tool):source.active()&&!source.expected().isEmpty()&&WorldbreakerCuriosPassives.sourceKind(source.expected())==ArtifactKind.ICARUS
                &&p.isAlive()&&!p.isRemoved()&&!p.isSpectator()&&p.mayBuild()&&!p.isPassenger()&&!p.isSleeping()&&!p.isFallFlying()
                &&p.getMainHandItem()==tool&&!tool.isEmpty();
    }
    public static boolean startEquipped(ServerPlayer p,ItemStack tool,EquipmentLease<ItemStack> source){
        if(!STARTING.add(p.getUUID()))return false;
        try{return startOnce(p,tool,source);}finally{STARTING.remove(p.getUUID());}
    }
    public static double speed(int age){return Math.min(.70,.16+Math.max(0,age)*.035);}
    private static boolean clear(ServerPlayer p,double rise){
        // Sweep in short increments: checking only the destination could skip a thin obstruction.
        for(double dy=.1;dy<=rise+.1;dy+=.1)if(!WorldSafety.freeBody(p,p.position().add(0,Math.min(dy,rise),0)))return false;
        return true;
    }
    private static boolean safeStart(ServerPlayer p,ItemStack tool){
        var box=p.getBoundingBox().expandTowards(0,1.5,0).deflate(.001);
        for(var at:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){
            if(!p.serverLevel().hasChunkAt(at)||WorldSafety.barrier(p,at)||PlayerPlacedBlocks.get(p.serverLevel()).contains(at)
                    ||!p.serverLevel().getBlockState(at).isAir()&&!WorldSafety.harvestable(p,tool,at))return false;
        }
        return true;
    }
    private static boolean headroom(ServerPlayer p,double rise,Flight job){
        var box=p.getBoundingBox().expandTowards(0,rise+.25,0).deflate(.001);
        for(var cursor:BlockPos.betweenClosed(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(box.maxX,box.maxY,box.maxZ))){
            var at=cursor.immutable();var level=p.serverLevel();
            if(!job.valid()||!level.hasChunkAt(at)||level.isOutsideBuildHeight(at))return false;
            var state=level.getBlockState(at);if(state.isAir())continue;
            if(WorldSafety.barrier(p,at)||PlayerPlacedBlocks.get(level).contains(at))return false;
            if(!WorldSafety.mineNaturalLeased(p,job.tool,ArtifactKind.ICARUS,at,state,job::valid))return false;
        }
        return job.valid()&&clear(p,rise+.25);
    }
    public static boolean start(ServerPlayer p,ItemStack tool){
        if(!STARTING.add(p.getUUID()))return false;
        try{return startOnce(p,tool,null);}finally{STARTING.remove(p.getUUID());}
    }
    private static boolean startOnce(ServerPlayer p,ItemStack tool,EquipmentLease<ItemStack> source){
        if(!caster(p,tool,source)||active(p)||JOBS.size()>=MAX_JOBS
                ||!LANDING.containsKey(p.getUUID())&&LANDING.size()>=MAX_LANDING_GUARDS||!safeStart(p,tool))return false;
        var context=HeldUtilityContext.capture(p,tool);
        boolean allowed=WorldSafety.allowed(p,ArtifactKind.ICARUS,p.blockPosition().above());
        if(!allowed||!context.valid()||!caster(p,tool,source)||active(p)||JOBS.size()>=MAX_JOBS
                ||!LANDING.containsKey(p.getUUID())&&LANDING.size()>=MAX_LANDING_GUARDS||!safeStart(p,tool))return false;
        if(source!=null&&!ArtifactActions.canSpendDurability(p,source.expected(),8))return false;
        JOBS.put(p.getUUID(),new Flight(p,tool,source));return true;
    }
    public static boolean active(ServerPlayer p){return JOBS.containsKey(p.getUUID());}
    public static void stop(ServerPlayer p){JOBS.remove(p.getUUID());}
    /** Lifecycle teardown only. User cancellation deliberately retains landing protection. */
    public static void forget(ServerPlayer p){JOBS.remove(p.getUUID());LANDING.remove(p.getUUID());}
    public static void clear(){JOBS.clear();LANDING.clear();}
    public static void tick(){
        if(ticking)return;ticking=true;
        try{tickLanding();for(var job:List.copyOf(JOBS.values()))advance(job);}finally{ticking=false;}
    }
    private static void advance(Flight job){
        var p=job.player;
        if(!job.valid()){JOBS.remove(p.getUUID(),job);return;}
        long now=ArtifactState.now(p);if(job.lastTick==now)return;
        job.lastTick=now;
        if(job.seekSurface){
            int surface=p.serverLevel().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.blockPosition().getX(),p.blockPosition().getZ());
            job.exitY=Math.max(job.exitY,Math.min(job.level.getMaxBuildHeight()-2,surface+3));
            if(p.getY()>=job.exitY-.02&&job.level.canSeeSky(p.blockPosition())){JOBS.remove(p.getUUID(),job);return;}
        }
        double remaining=job.seekSurface?job.exitY-p.getY():MAX_RISE-(p.getY()-job.origin.y);
        double rise=Math.min(speed((int)(now-job.start)),remaining);
        var context=HeldUtilityContext.capture(p,job.tool);
        boolean allowed=WorldSafety.allowed(p,ArtifactKind.ICARUS,BlockPos.containing(p.position().add(0,rise+1,0)));
        if(!allowed||!context.valid()||!job.valid()||!headroom(p,rise,job)){JOBS.remove(p.getUUID(),job);return;}
        job.previous=p.position();
        p.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_FEATHERFALL.get(),GRACE,0,false,false,true));
        // Potion callbacks can change the actor too; never apply a stale impulse after them.
        if(!context.valid()||!job.valid()||!clear(p,rise+.25)){JOBS.remove(p.getUUID(),job);return;}
        var motion=p.getDeltaMovement();p.setDeltaMovement(motion.x,rise,motion.z);
        LANDING.put(p.getUUID(),new Landing(p,p.serverLevel(),now+LANDING_TIMEOUT));
        p.hasImpulse=true;p.fallDistance=0;
        p.connection.send(new ClientboundSetEntityMotionPacket(p));
        if((now-job.start)%3==0)AbilityVfx.ascent(p,(int)(now-job.start));
    }
}
