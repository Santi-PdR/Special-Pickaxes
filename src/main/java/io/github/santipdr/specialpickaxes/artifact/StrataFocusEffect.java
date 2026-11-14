package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

import java.util.ArrayDeque;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/** Curios mining focus: the amplifier is translated into a bounded BreakSpeed bonus. */
public final class StrataFocusEffect extends MobEffect {
    private static final String NEXT_MINING_PULSE="specialpickaxesStrataMiningPulseAtWorldTime";
    private static final String MINING_STREAK_AT="specialpickaxesStrataMiningStreakAt";
    private static final String MINING_STREAK="specialpickaxesStrataMiningStreak";
    private static final int STREAK_WINDOW_TICKS=80;
    private static final int MAX_PENDING_BREAKS=1024;
    private record PendingBreak(ServerPlayer player,BlockPos pos,BlockState expected,
                                ResourceKey<Level> dimension,int time) {}
    private static final Set<PendingBreak> PENDING_KEYS=new HashSet<>();
    private static final ArrayDeque<PendingBreak> PENDING_BREAKS=new ArrayDeque<>();

    public StrataFocusEffect(){super(MobEffectCategory.BENEFICIAL,0x52bdd8);}

    /** Defer Curios mining feedback until Forge and other mods have finished this block break. */
    public static boolean observeBreak(ServerPlayer player,BlockPos pos){
        if(!player.serverLevel().hasChunkAt(pos))return false;
        return observeBreak(player,pos,player.serverLevel().getBlockState(pos));
    }

    /** Only natural mineable geology, including ores, contributes to the Curios mining rhythm. */
    public static boolean observeBreak(ServerPlayer player,BlockPos pos,BlockState expected){
        if((!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get())
                &&!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_RESONANCE.get()))
                ||!player.isAlive()||player.isRemoved()||!player.serverLevel().hasChunkAt(pos)
                ||PENDING_BREAKS.size()>=MAX_PENDING_BREAKS||!mineableGeology(expected)
                ||PlayerPlacedBlocks.get(player.serverLevel()).contains(pos))return false;
        var pending=new PendingBreak(player,pos.immutable(),expected,player.level().dimension(),player.server.getTickCount());
        if(!PENDING_KEYS.add(pending))return false;
        PENDING_BREAKS.addLast(pending);return true;
    }

    /** Only successful, still-loaded breaks advance the tempo; canceled attempts grant nothing. */
    public static int flushObservedBreaks(){
        int processed=0,budget=512;
        while(budget-->0&&!PENDING_BREAKS.isEmpty()){
            var pending=PENDING_BREAKS.removeFirst();var player=pending.player();
            PENDING_KEYS.remove(pending);
            if(!player.isAlive()||player.isRemoved()||!player.level().dimension().equals(pending.dimension())
                    ||!ObservationWindow.fresh(player.server.getTickCount(),pending.time())||!player.serverLevel().hasChunkAt(pending.pos()))continue;
            if(mineableGeology(pending.expected())
                    &&!PlayerPlacedBlocks.get(player.serverLevel()).contains(pending.pos())
                    &&player.serverLevel().getBlockState(pending.pos()).isAir()){
                miningPulse(player,pending.pos());
                StasisResonanceEffect.miningPulse(player,pending.pos());
                processed++;
            }
        }
        return processed;
    }

    public static void forget(ServerPlayer player){
        UUID id=player.getUUID();PENDING_BREAKS.removeIf(pending->pending.player().getUUID().equals(id));
        PENDING_KEYS.removeIf(pending->pending.player().getUUID().equals(id));
    }

    public static void clearPending(){PENDING_BREAKS.clear();PENDING_KEYS.clear();}

    /** A bounded, wearer-only strike pulse makes the Curios mining bonus readable in use. */
    public static boolean miningPulse(ServerPlayer player,BlockPos pos){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get())
                ||!(player.level() instanceof ServerLevel level))return false;
        long now=ArtifactState.now(player);var data=player.getPersistentData();
        long last=data.getLong(MINING_STREAK_AT);int previous=Math.max(0,Math.min(3,data.getInt(MINING_STREAK)));
        int streak=previous>0&&RecordedAge.ticks(now,last,1)<=STREAK_WINDOW_TICKS
                ?Math.min(3,previous+1):1;
        data.putInt(MINING_STREAK,streak);data.putLong(MINING_STREAK_AT,now);
        if(previous<3&&streak==3)ArtifactFeedback.strataTempoPeak(player);
        if(PersistedCooldown.active(now,data.getLong(NEXT_MINING_PULSE),6))return false;
        data.putLong(NEXT_MINING_PULSE,now+6L);
        ArtifactFeedback.strataTempoPulse(player,pos,streak);
        return true;
    }

    /** Consecutive natural mining gains up to 24% more speed for four seconds. */
    public static double miningTempoMultiplier(ServerPlayer player){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get()))return 1D;
        var data=player.getPersistentData();long last=data.getLong(MINING_STREAK_AT),now=ArtifactState.now(player);
        int streak=data.getInt(MINING_STREAK);
        if(RecordedAge.ticks(now,last,1)>STREAK_WINDOW_TICKS||streak<=0)return 1D;
        return 1D+.08D*Math.min(3,streak);
    }

    private static boolean mineableGeology(BlockState state){
        return !state.hasBlockEntity()&&state.getFluidState().isEmpty()
                &&(MiningDesigns.matrix(state)||ArtifactOres.isOre(state));
    }
}
