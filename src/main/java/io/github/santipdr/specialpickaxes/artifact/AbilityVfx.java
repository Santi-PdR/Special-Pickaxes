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
        return switch(kind){
            case INTERREGNUM,CHOIR -> SpecialPickaxes.STASIS_SHARD.get();
            case WORLDLOOM -> SpecialPickaxes.WORLDLOOM_ROOT_CLAMP.get();
            case ICARUS -> SpecialPickaxes.ICARIAN_FEATHER.get();
            case IRIDIUM,SEAM_RIPPER -> SpecialPickaxes.IRIDIUM_LODE_SHARD.get();
            case HELLSPEC,CRUCIBLE -> SpecialPickaxes.HELLSPEC_BRAND_FLARE.get();
            case PALIMPSEST -> SpecialPickaxes.DAWN_MENDING_BLOOM.get();
            default -> SpecialPickaxes.EXODIUM_STARFALL.get();
        };
    }
    /** A compact ring below the crosshair; at most one successful cast burst per player per tick. */
    public static boolean cast(ServerPlayer player,ArtifactKind kind){
        if(!kind.playable()||!player.isAlive()||player.isRemoved()||player.isSpectator())return false;
        int tick=player.server.getTickCount();
        if(LAST_CAST.getOrDefault(player.getUUID(),tick-1)==tick||!BUDGET.tryReserve(tick,16))return false;
        LAST_CAST.put(player.getUUID(),tick);
        var center=player.position().add(player.getLookAngle().multiply(1,0,1).normalize().scale(1.5)).add(0,.25,0);
        for(int i=0;i<16;i++){
            double angle=i*Math.PI/8,radius=.35+(i%2)*.2;
            player.serverLevel().sendParticles(player,particle(kind),false,center.x+Math.cos(angle)*radius,
                    center.y,center.z+Math.sin(angle)*radius,1,0,.015,0,0);
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
