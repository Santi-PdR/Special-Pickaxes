package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/** Small, always-on identities for every artifact. No attack-strength effects are used. */
public final class ArtifactPassives {
    private ArtifactPassives() {}

    /** Curios-only defensive signatures. Each relic can answer one hostile hit every eight seconds. */
    public static void onCuriosDamage(ServerPlayer player,net.minecraftforge.event.entity.living.LivingHurtEvent event,ArtifactKind kind) {
        if(event.getAmount()<=0)return;
        var source=event.getSource();
        if(kind==ArtifactKind.HELLSPEC) {
            if(!source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE))return;
            var state=ArtifactState.of(player,kind);long now=ArtifactState.now(player);
            if(state.getLong("curiosPassiveReady")<=now){
                state.putLong("curiosPassiveReady",now+80);event.setAmount(event.getAmount()*.35F);
                ArtifactFeedback.burst(player,kind,player.blockPosition(),4);
            }
            return;
        }
        if(!(source.getEntity() instanceof LivingEntity attacker)||attacker==player||attacker.isAlliedTo(player))return;
        if(attacker instanceof Player other&&!player.canHarmPlayer(other))return;
        boolean retaliationAllowed=switch(kind){
            case CHOIR,EVENTIDE,CRUCIBLE,INTERREGNUM,ICARUS,AXIOM,EXODIUM,SEAM_RIPPER -> WorldSafety.allowed(player,kind,attacker.blockPosition());
            default -> false;
        };
        if(!retaliationAllowed&&switch(kind){
            case CHOIR,CRUCIBLE,ICARUS,AXIOM,EXODIUM -> true;
            default -> false;
        })return;
        var state=ArtifactState.of(player,kind);long now=ArtifactState.now(player);
        if(state.getLong("curiosPassiveReady")>now)return;
        state.putLong("curiosPassiveReady",now+160);
        switch(kind) {
            case PALIMPSEST -> {event.setAmount(event.getAmount()*.9F);player.getFoodData().eat(1,.1F);}
            case CHOIR -> attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1,true,false,true));
            case EVENTIDE -> {event.setAmount(event.getAmount()*.9F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0,true,false,true));}
            case CRUCIBLE -> attacker.setSecondsOnFire(3);
            case INTERREGNUM -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0,true,false,true));}
            case WORLDLOOM -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION,60,1,true,false,true));
            case ICARUS -> {
                Vec3 away=attacker.position().subtract(player.position());
                if(away.lengthSqr()<.01)away=new Vec3(-player.getLookAngle().x,0,-player.getLookAngle().z);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(away.normalize().scale(.8).add(0,.25,0)));
                attacker.hasImpulse=true;
            }
            case AXIOM -> {attacker.addEffect(new MobEffectInstance(MobEffects.GLOWING,60,0,true,false,true));attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,0,true,false,true));}
            case WORLDBREAKER -> {event.setAmount(event.getAmount()*.8F);player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,60,1,true,false,true));}
            case EXODIUM -> {
                Vec3 away=attacker.position().subtract(player.position());
                if(away.lengthSqr()<.01)away=new Vec3(-player.getLookAngle().x,0,-player.getLookAngle().z);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(away.normalize().scale(.45).add(0,.65,0)));
                attacker.hasImpulse=true;
            }
            case IRIDIUM -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,80,0,true,false,true));
            case SEAM_RIPPER -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0,true,false,true));}
            default -> {return;}
        }
        ArtifactFeedback.burst(player,kind,player.blockPosition(),4);
    }

    public static void tick(ServerPlayer player, ArtifactKind kind) {
        grant(player, MobEffects.NIGHT_VISION, 0, 240);
        switch (kind) {
            case PALIMPSEST -> { grant(player, MobEffects.REGENERATION, 0, 80); grant(player, MobEffects.DIG_SPEED, 1, 80); }
            case CHOIR -> grant(player, MobEffects.DIG_SPEED, 0, 80);
            case EVENTIDE -> grant(player, MobEffects.SLOW_FALLING, 0, 80);
            case CRUCIBLE -> { if (player.isOnFire() || player.isInLava()) grant(player, MobEffects.FIRE_RESISTANCE, 0, 80); }
            case INTERREGNUM -> grant(player, MobEffects.ABSORPTION, 0, 80);
            case WORLDLOOM -> {
                grant(player, MobEffects.REGENERATION, 0, 80);
                sustain(player);
            }
            case ICARUS -> { if (!player.onGround()) grant(player, MobEffects.SLOW_FALLING, 0, 80); }
            case AXIOM -> grant(player, MobEffects.DIG_SPEED, 0, 80);
            case WORLDBREAKER -> grant(player, MobEffects.DAMAGE_RESISTANCE, 0, 80);
            case EXODIUM -> grant(player, MobEffects.DIG_SPEED, 1, 80);
            case IRIDIUM -> grant(player, MobEffects.DIG_SPEED, 1, 80);
            case HELLSPEC -> grant(player, MobEffects.FIRE_RESISTANCE, 0, 80);
            case SEAM_RIPPER -> grant(player, MobEffects.DIG_SPEED, 1, 80);
            default -> { }
        }
    }

    private static void grant(ServerPlayer player, MobEffect effect, int amplifier, int duration) {
        var current = player.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier || current.getAmplifier() == amplifier && current.getDuration() <= 40)
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
    }

    private static void sustain(ServerPlayer player){
        if(!player.getFoodData().needsFood())return;
        var state=ArtifactState.of(player,ArtifactKind.WORLDLOOM);
        long now=ArtifactState.now(player);
        if(state.getLong("rootSustainReady")>now)return;
        player.getFoodData().eat(2,.25F);
        state.putLong("rootSustainReady",now+100);
    }
}
