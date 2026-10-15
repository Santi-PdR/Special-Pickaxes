package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Worldloom's Curios counter heals in visible, deliberately spaced root pulses. */
public final class HeartwoodRenewalEffect extends MobEffect {
    private static final String NEXT_PULSE="specialpickaxesHeartwoodPulseAt";
    public HeartwoodRenewalEffect() {
        super(MobEffectCategory.BENEFICIAL,0x83d86e);
    }

    @Override
    public void applyEffectTick(LivingEntity entity,int amplifier) {
        if(entity.level().isClientSide)return;
        long now=entity.tickCount;
        var data=entity.getPersistentData();
        if(data.getLong(NEXT_PULSE)>now)return;
        data.putLong(NEXT_PULSE,now+20L);
        if(entity.getHealth()<entity.getMaxHealth())entity.heal(1.0F+Math.max(0,amplifier)*.5F);
        if(entity.level() instanceof ServerLevel level)
            level.sendParticles(ParticleTypes.COMPOSTER,entity.getX(),entity.getY()+.15D,entity.getZ(),3,.22D,.04D,.22D,.01D);
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%20==0;
    }
}
