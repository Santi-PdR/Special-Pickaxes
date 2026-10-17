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
    private static final String NEXT_MINING_PULSE="specialpickaxesStrataMiningPulseAt";

    public StrataFocusEffect(){super(MobEffectCategory.BENEFICIAL,0x52bdd8);}

    /** A bounded, wearer-only strike pulse makes the Curios mining bonus readable in use. */
    public static boolean miningPulse(ServerPlayer player,BlockPos pos){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get())
                ||!(player.level() instanceof ServerLevel level))return false;
        long now=player.tickCount;var data=player.getPersistentData();
        if(data.getLong(NEXT_MINING_PULSE)>now)return false;
        data.putLong(NEXT_MINING_PULSE,now+6L);
        double x=pos.getX()+.5D,y=pos.getY()+.58D,z=pos.getZ()+.5D;
        level.sendParticles(player,MINING_GLEAM,false,x,y,z,4,.22D,.2D,.22D,.015D);
        level.sendParticles(player,ParticleTypes.GLOW,false,x,y,z,1,.08D,.12D,.08D,.01D);
        return true;
    }
}
