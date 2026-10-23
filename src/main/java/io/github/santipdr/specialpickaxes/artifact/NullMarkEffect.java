package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Axiom's Curios counter leaves a visible null sigil that suppresses healing. */
public final class NullMarkEffect extends MobEffect {
    public NullMarkEffect() {
        super(MobEffectCategory.HARMFUL,0x68d5bd);
    }

    @Override
    public void applyEffectTick(LivingEntity entity,int amplifier) {
        if(!entity.level().isClientSide&&entity.level() instanceof ServerLevel level){
            var particle=new ArtifactFeedback.AuraParticle(ParticleTypes.SCULK_SOUL,
                    new net.minecraft.world.phys.Vec3(entity.getX(),entity.getY()+entity.getBbHeight()*.55D,entity.getZ()),
                    2,.12D,.18D,.12D,.005D);
            ArtifactFeedback.entityAuraPulse(level,entity,java.util.List.of(particle));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%10==0;
    }
}
