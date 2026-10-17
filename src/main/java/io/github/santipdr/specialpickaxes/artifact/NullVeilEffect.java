package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.Comparator;

/** Axiom's Curios veil erases one harmful effect in a restrained eight-second pulse. */
public final class NullVeilEffect extends MobEffect {
    private static final int COOLDOWN=160;
    private static final DustParticleOptions NULL_DUST=new DustParticleOptions(new Vector3f(.48F,.95F,.72F),.8F);

    public NullVeilEffect(){super(MobEffectCategory.BENEFICIAL,0x9df5c2);}

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity instanceof ServerPlayer player)pulse(player);
    }

    public static void pulse(ServerPlayer player){
        var state=ArtifactState.of(player,ArtifactKind.AXIOM);long now=ArtifactState.now(player);
        if(state.getLong("nullVeilReady")>now)return;
        var harmful=player.getActiveEffects().stream()
                .filter(effect->effect.getEffect().getCategory()==MobEffectCategory.HARMFUL)
                .min(Comparator.comparingInt((MobEffectInstance effect)->priority(effect.getEffect())).reversed()
                        .thenComparing(Comparator.comparingInt(MobEffectInstance::getAmplifier).reversed())
                        .thenComparing(Comparator.comparingInt(MobEffectInstance::getDuration).reversed())).orElse(null);
        if(harmful==null||!player.removeEffect(harmful.getEffect()))return;
        state.putLong("nullVeilReady",now+COOLDOWN);
        ServerLevel level=player.serverLevel();double phase=player.tickCount*.11D,cx=player.getX(),cz=player.getZ(),cy=player.getY()+player.getBbHeight()*.52D;
        for(int i=0;i<8;i++){
            double angle=phase+i*Math.PI/4D,radius=.32D+i*.018D;
            level.sendParticles(NULL_DUST,cx+Math.cos(angle)*radius,cy+(i%2)*.16D,cz+Math.sin(angle)*radius,1,0,0,0,0);
        }
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,cx,cy,cz,4,.12D,.18D,.12D,.015D);
    }

    private static int priority(MobEffect effect){
        if(effect==MobEffects.WITHER)return 100;
        if(effect==MobEffects.POISON)return 95;
        if(effect==MobEffects.MOVEMENT_SLOWDOWN||effect==MobEffects.BLINDNESS)return 80;
        if(effect==MobEffects.WEAKNESS||effect==MobEffects.DIG_SLOWDOWN)return 60;
        if(effect==MobEffects.HUNGER)return 40;
        if(effect==MobEffects.LEVITATION)return 30;
        return 20;
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%10==0;}
}
