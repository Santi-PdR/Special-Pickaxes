package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

/** Palimpsest turns its Curios counter into a short, visible reserve of food. */
public final class DawnReserveEffect extends MobEffect {
    private static final DustParticleOptions DAWN=new DustParticleOptions(new Vector3f(1F,.78F,.34F),1.1F);

    public DawnReserveEffect(){super(MobEffectCategory.BENEFICIAL,0xffd783);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity instanceof Player player)||!player.getFoodData().needsFood())return;
        player.getFoodData().eat(1,.35F);
        if(entity.level() instanceof ServerLevel level){
            double x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.62D,z=entity.getZ();
            level.sendParticles(ParticleTypes.WAX_ON,x,y,z,6,.26D,.2D,.26D,.025D);
            level.sendParticles(DAWN,x,y+.16D,z,3,.2D,.18D,.2D,0);
        }
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%20==0;}
}
