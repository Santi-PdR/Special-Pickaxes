package io.github.santipdr.specialpickaxes.artifact;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Event-only custom VFX. No tick subscriber, ambient emission, vanilla fallback or global broadcast. */
public final class AbilityVfx {
    private static final RelicParticleBudget BUDGET=new RelicParticleBudget(256);
    private static final Map<UUID,Integer> LAST_CAST=new HashMap<>();
    private AbilityVfx(){}

    public static SimpleParticleType particle(ArtifactKind kind){
        return particle(kind,false);
    }
    public static SimpleParticleType particle(ArtifactKind kind,boolean alternate){
        if(!kind.playable())throw new IllegalArgumentException("No cast signature for retired pickaxe");
        return (alternate?SpecialPickaxes.ALTERNATE_SIGNATURE_PARTICLES:SpecialPickaxes.SIGNATURE_PARTICLES).get(kind.name()).get();
    }
    /** Distinct custom cast silhouettes; at most one successful burst per player per tick. */
    public static boolean cast(ServerPlayer player,ArtifactKind kind){
        return cast(player,kind,false);
    }
    public static boolean cast(ServerPlayer player,ArtifactKind kind,boolean alternate){
        if(!kind.playable()||!player.isAlive()||player.isRemoved()||player.isSpectator())return false;
        int tick=player.server.getTickCount();
        if(LAST_CAST.getOrDefault(player.getUUID(),tick-1)==tick||!BUDGET.tryReserve(tick,16))return false;
        LAST_CAST.put(player.getUUID(),tick);
        var center=player.position().add(player.getLookAngle().multiply(1,0,1).normalize().scale(1.5)).add(0,.25,0);
        var shape=AbilityBurstShape.Shape.valueOf(kind.name());
        for(int i=0;i<AbilityBurstShape.COUNT;i++){
            var point=center.add(AbilityBurstShape.orient((alternate?AbilityBurstShape.alternateOffset(shape,i):AbilityBurstShape.offset(shape,i)),player.getLookAngle()));
            player.serverLevel().sendParticles(player,particle(kind,alternate),false,point.x,
                    point.y,point.z,1,0,.015,0,0);
        }
        return true;
    }
    /** Invoked only by an executing queue after successful work, never by held/Curios tick effects. */
    public static boolean excavation(ServerPlayer player,ArtifactKind kind,net.minecraft.core.BlockPos at,boolean complete){
        if(!kind.playable()||!player.isAlive()||player.isRemoved()||player.isSpectator())return false;
        var level=player.serverLevel();
        if(!level.hasChunkAt(at)||player.distanceToSqr(Vec3.atCenterOf(at))>48*48)return false;
        int tick=player.server.getTickCount(),count=complete?RelicWorkShape.FINISH_COUNT:RelicWorkShape.WORK_COUNT;
        if(!BUDGET.tryReserve(tick,count))return false;
        Vec3 center=Vec3.atCenterOf(at);
        // Pull surface impacts slightly toward the caster so opaque adjacent faces do not swallow every glyph.
        Vec3 toward=player.getEyePosition().subtract(center).normalize();center=center.add(toward.scale(.6));
        var shape=AbilityBurstShape.Shape.valueOf(kind.name());
        for(int i=0;i<count;i++){
            var point=center.add(AbilityBurstShape.orient(RelicWorkShape.point(shape,i,complete),toward));
            level.sendParticles(player,particle(kind,complete),false,point.x,point.y,point.z,1,0,0,0,0);
        }
        return true;
    }
    /** Two animated custom feather helices, only during the short active ascent. */
    public static boolean ascent(ServerPlayer player,int age){
        int tick=player.server.getTickCount();if(!BUDGET.tryReserve(tick,8))return false;
        for(int side=-1;side<=1;side+=2)for(int feather=0;feather<4;feather++){
            double angle=age*.24+feather*.65+(side<0?Math.PI:0),radius=.5+feather*.12;
            var at=player.position().add(Math.cos(angle)*radius,.1-feather*.18,Math.sin(angle)*radius);
            player.serverLevel().sendParticles(player,particle(ArtifactKind.ICARUS,true),false,at.x,at.y,at.z,1,0,.02,0,0);
        }return true;
    }
    /** Expanding solar rim; only an executing sunburst calls this, never item/Curios idle ticks. */
    public static boolean solarWave(ServerPlayer player,net.minecraft.core.BlockPos center,double radius,boolean nova){
        if(!player.isAlive()||player.isRemoved()||player.distanceToSqr(Vec3.atCenterOf(center))>48*48)return false;
        int tick=player.server.getTickCount();if(!BUDGET.tryReserve(tick,16))return false;
        for(int i=0;i<16;i++){
            double angle=Math.PI*2*i/16,r=Math.max(.6,radius);
            var at=Vec3.atCenterOf(center).add(Math.cos(angle)*r,nova?Math.sin(angle*2)*r*.35:.2,Math.sin(angle)*r);
            player.serverLevel().sendParticles(player,particle(ArtifactKind.PALIMPSEST,nova),false,at.x,at.y,at.z,1,0,.02,0,0);
        }return true;
    }
    /** Seismic comb: authored fault particles on the advancing edge, never an idle aura. */
    public static boolean faultWave(ServerPlayer player,net.minecraft.core.BlockPos origin,net.minecraft.core.Direction forward,net.minecraft.core.BlockPos front,boolean cascade){
        if(!player.isAlive()||player.isRemoved()||player.distanceToSqr(Vec3.atCenterOf(front))>64*64)return false;
        int tick=player.server.getTickCount();if(!BUDGET.tryReserve(tick,12))return false;
        int depth=(front.getX()-origin.getX())*forward.getStepX()+(front.getZ()-origin.getZ())*forward.getStepZ();
        var right=forward.getClockWise();double span=cascade?20:Math.min(24,2+depth);
        for(int i=0;i<12;i++){
            double side=(i/11D*2-1)*span;
            var at=Vec3.atCenterOf(origin.relative(forward,depth)).add(right.getStepX()*side,cascade?(i%3-1)*10:.25,right.getStepZ()*side);
            player.serverLevel().sendParticles(player,particle(ArtifactKind.CHOIR,cascade),false,at.x,at.y,at.z,1,0,.03,0,0);
        }return true;
    }
    /** Twin twilight rims, horizontal or vertical, on the currently executing destructive horizon. */
    public static boolean twilightHorizon(ServerPlayer p,net.minecraft.core.BlockPos center,net.minecraft.core.Direction forward,double radius,boolean vertical,boolean alternate){
        if(!p.isAlive()||p.isRemoved()||p.distanceToSqr(Vec3.atCenterOf(center))>64*64)return false;
        if(!BUDGET.tryReserve(p.server.getTickCount(),16))return false;
        var right=forward.getClockWise();double r=Math.max(1,radius);
        for(int i=0;i<16;i++){
            double a=Math.PI*2*i/16,x=Math.cos(a)*r,z=Math.sin(a)*r;
            var at=Vec3.atCenterOf(center).add(vertical?right.getStepX()*x:x,vertical?z:(i%2==0?.25:-.25),vertical?right.getStepZ()*x:z);
            p.serverLevel().sendParticles(p,particle(ArtifactKind.EVENTIDE,alternate),false,at.x,at.y,at.z,1,0,.015,0,0);
        }return true;
    }
    /** Three crossed causal loops. Server-authored, bounded, and emitted only during work. */
    public static boolean paradoxKnot(ServerPlayer p,net.minecraft.core.BlockPos center,boolean release){
        if(!p.isAlive()||p.isRemoved()||p.distanceToSqr(Vec3.atCenterOf(center))>64*64)return false;
        if(!BUDGET.tryReserve(p.server.getTickCount(),12))return false;
        double phase=p.server.getTickCount()*.17,r=release?1.8:.7;
        for(int i=0;i<12;i++){
            double a=phase+Math.PI*2*(i%4)/4,x=Math.cos(a)*r,y=Math.sin(a)*r;
            var at=Vec3.atCenterOf(center).add(i<4?x:0,i<4?y:i<8?x:y,i<4?0:i<8?y:x);
            p.serverLevel().sendParticles(p,particle(ArtifactKind.CRUCIBLE,release),false,at.x,at.y,at.z,1,0,0,0,0);
        }return true;
    }
    public static boolean stasisHourglass(ServerPlayer p,net.minecraft.core.BlockPos center){
        if(!p.isAlive()||p.isRemoved()||p.distanceToSqr(Vec3.atCenterOf(center))>64*64)return false;
        if(!BUDGET.tryReserve(p.server.getTickCount(),12))return false;
        double phase=p.server.getTickCount()*.12;
        for(int i=0;i<12;i++){
            double y=(i%6-2.5)*.5,r=.3+Math.abs(y),a=phase+(i<6?0:Math.PI);
            var at=Vec3.atCenterOf(center).add(Math.cos(a)*r,y,Math.sin(a)*r);
            p.serverLevel().sendParticles(p,particle(ArtifactKind.INTERREGNUM,true),false,at.x,at.y,at.z,1,0,0,0,0);
        }return true;
    }
    public static boolean worldloomStrands(ServerPlayer p,net.minecraft.core.BlockPos center){
        if(!p.isAlive()||p.isRemoved()||p.distanceToSqr(Vec3.atCenterOf(center))>64*64)return false;
        if(!BUDGET.tryReserve(p.server.getTickCount(),12))return false;
        for(int i=0;i<12;i++){
            double offset=(i%6-2.5)*.6,phase=p.server.getTickCount()*.12;
            var at=Vec3.atCenterOf(center).add(i<6?offset:Math.sin(phase+offset)*.2,.15+Math.cos(phase+offset)*.12,i<6?Math.sin(phase+offset)*.2:offset);
            p.serverLevel().sendParticles(p,particle(ArtifactKind.WORLDLOOM,true),false,at.x,at.y,at.z,1,0,0,0,0);
        }return true;
    }
    public static void forget(ServerPlayer player){LAST_CAST.remove(player.getUUID());}
    public static void clear(){LAST_CAST.clear();}

    /** Retired cosmetic API. Kept while old geometry/budget contracts remain tested; emits nothing. */
    public static boolean retired(ServerLevel level,ServerPlayer viewer,ParticleOptions particle,boolean force,
                                   double x,double y,double z,int count,double dx,double dy,double dz,double speed){return false;}
    public static int retired(ServerLevel level,ParticleOptions particle,double x,double y,double z,
                               int count,double dx,double dy,double dz,double speed){return 0;}
}
