package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Curios mining focus: the amplifier is translated into a bounded BreakSpeed bonus. */
public final class StrataFocusEffect extends MobEffect {
    private static final net.minecraft.core.particles.DustParticleOptions MINING_GLEAM=
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(.48F,.92F,1F),1.05F);
    private static final String NEXT_MINING_PULSE="specialpickaxesStrataMiningPulseAtWorldTime";
    private static final String MINING_STREAK_AT="specialpickaxesStrataMiningStreakAt";
    private static final String MINING_STREAK="specialpickaxesStrataMiningStreak";
    private static final int STREAK_WINDOW_TICKS=80;

    public StrataFocusEffect(){super(MobEffectCategory.BENEFICIAL,0x52bdd8);}

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
