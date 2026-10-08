package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Small, always-on identities for every artifact. No attack-strength effects are used. */
public final class ArtifactPassives {
    private ArtifactPassives() {}

    /** Curios-only Stasis reflex: a timed damage ward that briefly slows its aggressor. */
    public static void onCuriosDamage(ServerPlayer player,net.minecraftforge.event.entity.living.LivingHurtEvent event,ArtifactKind kind) {
        if(kind!=ArtifactKind.INTERREGNUM||event.getAmount()<=0)return;
        if(!(event.getSource().getEntity() instanceof LivingEntity attacker)||attacker==player||attacker.isAlliedTo(player))return;
        if(attacker instanceof Player other&&!player.canHarmPlayer(other))return;
        var state=ArtifactState.of(player,kind);long now=ArtifactState.now(player);
        if(state.getLong("curiosStasisReady")>now)return;
        state.putLong("curiosStasisReady",now+160);
        event.setAmount(event.getAmount()*0.85F);
        attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40,0,true,false,true));
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
