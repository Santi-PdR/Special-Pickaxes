package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Curios mining focus: the amplifier is translated into a bounded BreakSpeed bonus. */
public final class StrataFocusEffect extends MobEffect {
    private static final DustParticleOptions STRATA_GLEAM=new DustParticleOptions(new Vector3f(.32F,.78F,.95F),.58F);
    private static final DustParticleOptions MINING_GLEAM=new DustParticleOptions(new Vector3f(.48F,.92F,1F),1.05F);
    private static final String NEXT_MINING_PULSE="specialpickaxesStrataMiningPulseAt";

    public StrataFocusEffect(){super(MobEffectCategory.BENEFICIAL,0x52bdd8);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        double phase=entity.tickCount*.09D;
        double y=entity.getY()+entity.getBbHeight()*.68D;
        for(int i=0;i<3;i++){
            double angle=phase+i*Math.PI*2D/3D;
            double x=entity.getX()+Math.cos(angle)*.34D,z=entity.getZ()+Math.sin(angle)*.34D;
            level.sendParticles(STRATA_GLEAM,x,y+Math.sin(angle*2D)*.07D,z,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}

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
