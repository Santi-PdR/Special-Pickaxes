package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Iridium's successive ore breaks resonate into a short, capped mining-speed surge. */
public final class LodeSurgeEffect extends MobEffect {
    private static final DustParticleOptions MINT=new DustParticleOptions(new Vector3f(.34F,.96F,.78F),.78F);
    private static final DustParticleOptions AMBER=new DustParticleOptions(new Vector3f(1F,.72F,.32F),.68F);

    public LodeSurgeEffect(){super(MobEffectCategory.BENEFICIAL,0x68f2cb);}

    /** Local sparkle confirms each charge without broadcasting a mining trail to the whole server. */
    public static void oreBreakPulse(ServerPlayer player,BlockPos pos,int stacks){
        if(!(player.level() instanceof ServerLevel level))return;
        double x=pos.getX()+.5D,y=pos.getY()+.58D,z=pos.getZ()+.5D;
        level.sendParticles(player,MINT,false,x,y,z,3+stacks, .18D,.18D,.18D,.012D);
        if(stacks==3)level.sendParticles(player,ParticleTypes.GLOW,false,x,y,z,1,.05D,.08D,.05D,.008D);
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity instanceof ServerPlayer player)||!(entity.level() instanceof ServerLevel level))return;
        int charge=Math.min(2,amplifier),motes=2+charge;
        double phase=player.tickCount*.12D,radius=.3D+charge*.055D;
        double y=player.getY()+player.getBbHeight()*.55D;
        for(int i=0;i<motes;i++){
            double angle=phase+i*(Math.PI*2D/motes);
            level.sendParticles(player,(i&1)==0?MINT:AMBER,false,
                    player.getX()+Math.cos(angle)*radius,y+Math.sin(angle*2D)*.08D,
                    player.getZ()+Math.sin(angle)*radius,1,0,0,0,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%12==0;}
}
