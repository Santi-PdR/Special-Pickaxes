package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.UUID;

/** Curios mining focus: the amplifier is translated into a bounded BreakSpeed bonus. */
public final class StrataFocusEffect extends MobEffect {
    private static final net.minecraft.core.particles.DustParticleOptions MINING_GLEAM=
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.48F,.92F,1F),1.05F);
    private static final String NEXT_MINING_PULSE="specialpickaxesStrataMiningPulseAtWorldTime";
    private static final String MINING_STREAK_AT="specialpickaxesStrataMiningStreakAt";
    private static final String MINING_STREAK="specialpickaxesStrataMiningStreak";
    private static final int STREAK_WINDOW_TICKS=80;
    private static final int MAX_PENDING_BREAKS=1024;
    private record PendingBreak(ServerPlayer player,BlockPos pos,ResourceKey<Level> dimension,long time) {}
    private static final ArrayDeque<PendingBreak> PENDING_BREAKS=new ArrayDeque<>();

    public StrataFocusEffect(){super(MobEffectCategory.BENEFICIAL,0x52bdd8);}

    /** Defer Curios streak credit until Forge and other mods have finished this block break. */
    public static boolean observeBreak(ServerPlayer player,BlockPos pos){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get())
                ||PENDING_BREAKS.size()>=MAX_PENDING_BREAKS)return false;
        PENDING_BREAKS.addLast(new PendingBreak(player,pos.immutable(),player.level().dimension(),ArtifactState.now(player)));
        return true;
    }

    /** Only successful, still-loaded breaks advance the tempo; canceled attempts grant nothing. */
    public static int flushObservedBreaks(){
        int processed=0,budget=512;
        while(budget-->0&&!PENDING_BREAKS.isEmpty()){
            var pending=PENDING_BREAKS.removeFirst();var player=pending.player();
            long now=ArtifactState.now(player);
            if(!player.isAlive()||player.isRemoved()||!player.level().dimension().equals(pending.dimension())
                    ||now<pending.time()||now-pending.time()>2L||!player.serverLevel().hasChunkAt(pending.pos()))continue;
            if(player.serverLevel().getBlockState(pending.pos()).isAir()
                    &&miningPulse(player,pending.pos()))processed++;
        }
        return processed;
    }

    public static void forget(ServerPlayer player){
        UUID id=player.getUUID();PENDING_BREAKS.removeIf(pending->pending.player().getUUID().equals(id));
    }

    public static void clearPending(){PENDING_BREAKS.clear();}

    /** A bounded, wearer-only strike pulse makes the Curios mining bonus readable in use. */
    public static boolean miningPulse(ServerPlayer player,BlockPos pos){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get())
                ||!(player.level() instanceof ServerLevel level))return false;
        long now=ArtifactState.now(player);var data=player.getPersistentData();
        long last=data.getLong(MINING_STREAK_AT);int previous=data.getInt(MINING_STREAK);
        int streak=previous>0&&now>=last&&now-last<=STREAK_WINDOW_TICKS
                ?Math.min(3,previous+1):1;
        data.putInt(MINING_STREAK,streak);data.putLong(MINING_STREAK_AT,now);
        if(data.getLong(NEXT_MINING_PULSE)>now)return false;
        data.putLong(NEXT_MINING_PULSE,now+6L);
        ArtifactFeedback.nearbyMiningPulse(player,pos,MINING_GLEAM,4,ParticleTypes.GLOW,streak);
        return true;
    }

    /** Consecutive natural mining gains up to 24% more speed for four seconds. */
    public static double miningTempoMultiplier(ServerPlayer player){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get()))return 1D;
        var data=player.getPersistentData();long last=data.getLong(MINING_STREAK_AT),now=ArtifactState.now(player);
        int streak=data.getInt(MINING_STREAK);
        if(now<last||now-last>STREAK_WINDOW_TICKS||streak<=0)return 1D;
        return 1D+.08D*Math.min(3,streak);
    }
}
