package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;

/** Small, always-on identities for every artifact. No attack-strength effects are used. */
public final class ArtifactPassives {
    private static final int PASSIVE_LEASE_TICKS=40;
    private ArtifactPassives() {}

    /** Remove an always-on Curios effect as soon as its source is unequipped. */
    public static void onCuriosUnequip(ServerPlayer player,ArtifactKind kind) {
        if(kind==ArtifactKind.EVENTIDE
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.EVENTIDE))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get());
        if(kind==ArtifactKind.ICARUS
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.ICARUS)){
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_MOMENTUM.get());
        }
        if(kind==ArtifactKind.HELLSPEC
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.HELLSPEC))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.SEARING_SHELL.get());
    }

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
        LivingEntity attacker=source.getEntity() instanceof LivingEntity living?living:null;
        if(attacker==null){
            Entity direct=source.getDirectEntity();
            if(!JegProjectileCompat.isProjectile(direct))return;
            Entity owner=JegProjectileCompat.owner(direct);
            if(owner instanceof LivingEntity living)attacker=living;
        }
        if(attacker==null||attacker==player||attacker.isAlliedTo(player))return;
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
            case PALIMPSEST -> {event.setAmount(event.getAmount()*.9F);player.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.DAWN_RESERVE.get(),80,0,true,false,true));}
            case CHOIR -> attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.FAULT_RESONANCE.get(),60,0,true,false,true));
            case EVENTIDE -> {event.setAmount(event.getAmount()*.9F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_TETHER.get(),70,0,true,false,true));}
            case CRUCIBLE -> attacker.setSecondsOnFire(3);
            case INTERREGNUM -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,0,true,false,true));}
            case WORLDLOOM -> player.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_RENEWAL.get(),60,0,true,false,true));
            case ICARUS -> {
                Vec3 away=attacker.position().subtract(player.position());
                if(away.lengthSqr()<.01)away=new Vec3(-player.getLookAngle().x,0,-player.getLookAngle().z);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(away.normalize().scale(.8).add(0,.25,0)));
                attacker.hasImpulse=true;
            }
            case AXIOM -> attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.NULL_MARK.get(),60,0,true,false,true));
            case WORLDBREAKER -> {event.setAmount(event.getAmount()*.8F);player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,60,1,true,false,true));}
            case EXODIUM -> {
                Vec3 away=attacker.position().subtract(player.position());
                if(away.lengthSqr()<.01)away=new Vec3(-player.getLookAngle().x,0,-player.getLookAngle().z);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(away.normalize().scale(.45).add(0,.65,0)));
                attacker.hasImpulse=true;
            }
            case IRIDIUM -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,80,0,true,false,true));
            case SEAM_RIPPER -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.SEAM_FRAY.get(),60,0,true,false,true));}
            default -> {return;}
        }
        ArtifactFeedback.burst(player,kind,player.blockPosition(),4);
    }

    public static void tick(ServerPlayer player, ArtifactKind kind) {
        grant(player, MobEffects.NIGHT_VISION, 0, PASSIVE_LEASE_TICKS);
        switch (kind) {
            case PALIMPSEST -> { grant(player, MobEffects.REGENERATION, 0, PASSIVE_LEASE_TICKS); grant(player, MobEffects.DIG_SPEED, 1, PASSIVE_LEASE_TICKS); }
            case CHOIR -> grant(player, MobEffects.DIG_SPEED, 0, PASSIVE_LEASE_TICKS);
            case EVENTIDE -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get(), 0, PASSIVE_LEASE_TICKS);
            case CRUCIBLE -> { if (player.isOnFire() || player.isInLava()) grant(player, MobEffects.FIRE_RESISTANCE, 0, PASSIVE_LEASE_TICKS); }
            case INTERREGNUM -> {
                grant(player, MobEffects.ABSORPTION, 0, PASSIVE_LEASE_TICKS);
                shareStasisWard(player);
            }
            case WORLDLOOM -> {
                grant(player, MobEffects.REGENERATION, 0, PASSIVE_LEASE_TICKS);
                sustain(player);
                shareHeartwood(player);
            }
            case ICARUS -> { if (!player.onGround()){
                grant(player, MobEffects.SLOW_FALLING, 0, PASSIVE_LEASE_TICKS);
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_MOMENTUM.get(), 0, PASSIVE_LEASE_TICKS);
            } }
            case AXIOM -> grant(player, MobEffects.DIG_SPEED, 0, PASSIVE_LEASE_TICKS);
            case WORLDBREAKER -> grant(player, MobEffects.DAMAGE_RESISTANCE, 0, PASSIVE_LEASE_TICKS);
            case EXODIUM -> grant(player, MobEffects.DIG_SPEED, 1, PASSIVE_LEASE_TICKS);
            case IRIDIUM -> grant(player, MobEffects.DIG_SPEED, 1, PASSIVE_LEASE_TICKS);
            case HELLSPEC -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.SEARING_SHELL.get(), 0, PASSIVE_LEASE_TICKS);
            case SEAM_RIPPER -> grant(player, MobEffects.DIG_SPEED, 1, PASSIVE_LEASE_TICKS);
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

    /** A Curios Stasis bearer shares a short absorption lease with at most eight nearby allies. */
    private static void shareStasisWard(ServerPlayer owner){
        var center=owner.position();double rangeSqr=36D;
        var allies=EntitySelection.nearest(owner.serverLevel(),ServerPlayer.class,owner.getBoundingBox().inflate(6),ally->
                ally!=owner&&ally.isAlive()&&ally.isAlliedTo(owner)&&ally.distanceToSqr(center)<=rangeSqr,
                center,8,256);
        for(var ally:allies)grant(ally,MobEffects.ABSORPTION,0,PASSIVE_LEASE_TICKS);
    }

    /** Worldloom lends its slow custom healing pulses to a small, nearby allied group. */
    private static void shareHeartwood(ServerPlayer owner){
        var center=owner.position();double rangeSqr=25D;
        var allies=EntitySelection.nearest(owner.serverLevel(),ServerPlayer.class,owner.getBoundingBox().inflate(5),ally->
                ally!=owner&&ally.isAlive()&&ally.isAlliedTo(owner)&&ally.distanceToSqr(center)<=rangeSqr,
                center,4,128);
        var renewal=io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        for(var ally:allies){
            var current=ally.getEffect(renewal);
            if(current==null||current.getDuration()<=20)
                ally.addEffect(new MobEffectInstance(renewal,40,0,true,false,true));
        }
    }
}
