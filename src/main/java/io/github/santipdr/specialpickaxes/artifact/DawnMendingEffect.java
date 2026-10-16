package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

/** Palimpsest's passive gives controlled healing pulses instead of vanilla Regeneration. */
public final class DawnMendingEffect extends MobEffect {
    private static final String NEXT_PULSE="specialpickaxesDawnMendingAt";
    private static final DustParticleOptions DAWN_DUST=new DustParticleOptions(new Vector3f(1F,.72F,.28F),.82F);

    public DawnMendingEffect(){super(MobEffectCategory.BENEFICIAL,0xf3b950);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide)return;
        long now=entity.tickCount;
        var data=entity.getPersistentData();
        if(data.getLong(NEXT_PULSE)>now)return;
        data.putLong(NEXT_PULSE,now+40L);
        if(entity.getHealth()<entity.getMaxHealth())entity.heal(1F+.5F*Math.max(0,amplifier));
        if(entity.level() instanceof ServerLevel level){
            double x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.64D,z=entity.getZ();
            level.sendParticles(DAWN_DUST,x,y,z,2,.1D,.08D,.1D,0);
            level.sendParticles(ParticleTypes.WAX_ON,x,y+.08D,z,2,.12D,.1D,.12D,.006D);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%40==0;}
}
