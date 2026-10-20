package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

import java.util.ArrayList;

/** Worldloom's Curios counter heals in visible, deliberately spaced root pulses. */
public final class HeartwoodRenewalEffect extends MobEffect {
    private static final String NEXT_PULSE="specialpickaxesHeartwoodPulseAtWorldTime";
    private static final DustParticleOptions HEARTWOOD_DUST=new DustParticleOptions(new Vector3f(.32F,.88F,.38F),.92F);
    public HeartwoodRenewalEffect() {
        super(MobEffectCategory.BENEFICIAL,0x83d86e);
    }

    @Override
    public void applyEffectTick(LivingEntity entity,int amplifier) {
        if(entity.level().isClientSide)return;
        // Entity tickCount can reset when a player reconnects or respawns. World time
        // keeps the persisted cooldown stable across those entity lifecycles.
        long now=entity.level().getGameTime();
        var data=entity.getPersistentData();
        if(data.getLong(NEXT_PULSE)>now)return;
        if(entity.getHealth()>=entity.getMaxHealth())return;
        float before=entity.getHealth();
        entity.heal(1.0F+Math.max(0,amplifier)*.5F);
        // A blocked heal is not a Heartwood pulse: preserve both its cooldown and visuals.
        if(entity.getHealth()<=before)return;
        data.putLong(NEXT_PULSE,now+20L);
        if(entity.level() instanceof ServerLevel level)healBloom(level,entity);
    }

    /** Green mineral-like motes confirm a real heal without emitting idle particles at full health. */
    private static void healBloom(ServerLevel level,LivingEntity entity){
        double phase=entity.tickCount*.13D,x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.52D,z=entity.getZ();
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(6);
        for(int i=0;i<4;i++){
            double angle=phase+i*Math.PI*.5D;
            particles.add(new ArtifactFeedback.AuraParticle(HEARTWOOD_DUST,
                    new net.minecraft.world.phys.Vec3(x+Math.cos(angle)*.34D,y+(i%2)*.1D,z+Math.sin(angle)*.34D),1,0,.025D,0,0));
        }
        particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.COMPOSTER,new net.minecraft.world.phys.Vec3(x,y+.18D,z),2,.08D,.12D,.08D,.006D));
        ArtifactFeedback.entityAuraPulse(level,entity,particles);
    }

    /** Food restoration gets one quiet green pulse at its five-second cadence. */
    public static void foodPulse(ServerPlayer player){
        if(player.level().isClientSide||!(player.level() instanceof ServerLevel level))return;
        double phase=player.tickCount*.07D,x=player.getX(),y=player.getY()+player.getBbHeight()*.72D,z=player.getZ();
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(4);
        for(int i=0;i<3;i++){
            double angle=phase+i*Math.PI*2D/3D;
            particles.add(new ArtifactFeedback.AuraParticle(HEARTWOOD_DUST,
                    new net.minecraft.world.phys.Vec3(x+Math.cos(angle)*.24D,y,z+Math.sin(angle)*.24D),1,0,.06D,0,0));
        }
        particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.COMPOSTER,new net.minecraft.world.phys.Vec3(x,y+.08D,z),3,.1D,.1D,.1D,.008D));
        ArtifactFeedback.nearbyWardPulse(player,particles);
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%20==0;
    }
}
