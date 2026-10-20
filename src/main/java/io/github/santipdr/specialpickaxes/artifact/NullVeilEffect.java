package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;

/** Axiom's Curios veil erases one harmful effect in a restrained eight-second pulse. */
public final class NullVeilEffect extends MobEffect {
    private static final int COOLDOWN=160;
    private static final DustParticleOptions NULL_DUST=new DustParticleOptions(new Vector3f(.48F,.95F,.72F),.8F);
    private static final DustParticleOptions NULL_HIGHLIGHT=new DustParticleOptions(new Vector3f(.72F,1F,.9F),.58F);

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
        double phase=player.tickCount*.11D,cx=player.getX(),cz=player.getZ(),baseY=player.getY();
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(31);
        // Three interleaved strands make the cleanse read as a rising veil instead of a flat spark.
        for(int i=0;i<24;i++){
            double progress=i/23D,angle=phase+progress*Math.PI*3D,radius=.38D+.12D*progress;
            double x=cx+Math.cos(angle)*radius,z=cz+Math.sin(angle)*radius,y=baseY+.12D+progress*(player.getBbHeight()*.82D);
            particles.add(new ArtifactFeedback.AuraParticle(i%3==0?NULL_HIGHLIGHT:NULL_DUST,
                    new net.minecraft.world.phys.Vec3(x,y,z),1,0,0,0,0));
        }
        particles.add(new ArtifactFeedback.AuraParticle(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL,
                new net.minecraft.world.phys.Vec3(cx,baseY+player.getBbHeight()*.54D,cz),7,.16D,.28D,.16D,.012D));
        ArtifactFeedback.nearbyWardPulse(player,particles);
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
