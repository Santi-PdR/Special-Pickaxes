package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import org.joml.Vector3f;

/** Stasis shares a named mining cadence with teammates instead of generic Haste. */
public final class StasisResonanceEffect extends MobEffect {
    private static final String NEXT_MINING_PULSE="specialpickaxesStasisResonancePulseAtWorldTime";
    private static final DustParticleOptions VIOLET_DUST=new DustParticleOptions(new Vector3f(.67F,.47F,1F),.82F);

    public StasisResonanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa88bff);}

    /** A brief mining flash replaces idle particles; each ally gets at most one every 5 ticks. */
    public static boolean miningPulse(ServerPlayer player,BlockPos broken){
        if(player.level().isClientSide||!(player.level() instanceof ServerLevel level)
                ||!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_RESONANCE.get()))return false;
        long now=ArtifactState.now(player);var data=player.getPersistentData();
        if(data.getLong(NEXT_MINING_PULSE)>now)return false;
        data.putLong(NEXT_MINING_PULSE,now+5L);
        ArtifactFeedback.nearbyMiningPulse(player,broken,VIOLET_DUST,5,ParticleTypes.REVERSE_PORTAL,2);
        return true;
    }

}
