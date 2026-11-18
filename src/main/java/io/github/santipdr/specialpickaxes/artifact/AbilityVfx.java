package io.github.santipdr.specialpickaxes.artifact;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
    public static void forget(ServerPlayer player){LAST_CAST.remove(player.getUUID());}
    public static void clear(){LAST_CAST.clear();}

    /** Retired cosmetic API. Kept while old geometry/budget contracts remain tested; emits nothing. */
    public static boolean retired(ServerLevel level,ServerPlayer viewer,ParticleOptions particle,boolean force,
                                   double x,double y,double z,int count,double dx,double dy,double dz,double speed){return false;}
    public static int retired(ServerLevel level,ParticleOptions particle,double x,double y,double z,
                               int count,double dx,double dy,double dz,double speed){return 0;}
}
