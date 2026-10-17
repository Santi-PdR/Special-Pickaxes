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
    private static final String NEXT_MINING_PULSE="specialpickaxesStasisResonancePulseAt";
    private static final DustParticleOptions VIOLET_DUST=new DustParticleOptions(new Vector3f(.67F,.47F,1F),.82F);

    public StasisResonanceEffect(){super(MobEffectCategory.BENEFICIAL,0xa88bff);}

    /** A brief mining flash replaces idle particles; each ally gets at most one every 5 ticks. */
    public static boolean miningPulse(ServerPlayer player,BlockPos broken){
        if(player.level().isClientSide||!(player.level() instanceof ServerLevel level)
                ||!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_RESONANCE.get()))return false;
        long now=player.tickCount;var data=player.getPersistentData();
        if(data.getLong(NEXT_MINING_PULSE)>now)return false;
        data.putLong(NEXT_MINING_PULSE,now+5L);
        double x=broken.getX()+.5D,y=broken.getY()+.62D,z=broken.getZ()+.5D;
        level.sendParticles(player,VIOLET_DUST,false,x,y,z,5,.2D,.22D,.2D,.015D);
        level.sendParticles(player,ParticleTypes.REVERSE_PORTAL,false,x,y,z,2,.1D,.16D,.1D,.008D);
        return true;
    }

}
