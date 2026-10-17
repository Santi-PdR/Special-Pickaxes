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
    private static final int ALLY_AURA_SCAN_TICKS=20;
    private static final String NIGHT_VISION_LEASE="specialpickaxesNightVisionLease";
    private ArtifactPassives() {}

    /** Remove an always-on Curios effect as soon as its source is unequipped. */
    public static void onCuriosUnequip(ServerPlayer player,ArtifactKind kind) {
        boolean heldArtifact=player.getMainHandItem().getItem() instanceof ArtifactItem;
        if(!heldArtifact&&CuriosCompat.find(player)==null)clearNightVisionLease(player);
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
        if(kind==ArtifactKind.CRUCIBLE
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.CRUCIBLE))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.CRUCIBLE_MANTLE.get());
        if(kind==ArtifactKind.WORLDBREAKER
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.WORLDBREAKER))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.WORLDBREAKER_AEGIS.get());
        if(isStrataFocusPick(kind)
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&isStrataFocusPick(held.kind)))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get());
        if(kind==ArtifactKind.INTERREGNUM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.INTERREGNUM))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_WARD.get());
        if(kind==ArtifactKind.PALIMPSEST
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.PALIMPSEST))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.DAWN_MENDING.get());
        if(kind==ArtifactKind.WORLDLOOM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.WORLDLOOM))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_RENEWAL.get());
        if(kind==ArtifactKind.EXODIUM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.EXODIUM))
            player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get());
    }

    /** Curios-only defensive signatures. Each relic can answer one hostile hit every eight seconds. */
    public static void onCuriosDamage(ServerPlayer player,net.minecraftforge.event.entity.living.LivingHurtEvent event,ArtifactKind kind) {
        if(event.getAmount()<=0)return;
        var source=event.getSource();
        if(kind==ArtifactKind.EXODIUM&&player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get())
                &&source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)){
            event.setAmount(event.getAmount()*.75F);
            StarfallWardEffect.projectileImpact(player);
        }
        // The Searing Shell already cancels fire and lava at LivingAttackEvent;
        // its Curios counter is reserved for hostile non-fire hits.
        if(kind==ArtifactKind.HELLSPEC&&source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE))return;
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
            case CHOIR,EVENTIDE,CRUCIBLE,INTERREGNUM,ICARUS,AXIOM,EXODIUM,HELLSPEC,SEAM_RIPPER -> WorldSafety.allowed(player,kind,attacker.blockPosition());
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
            case CRUCIBLE -> attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.PHASE_SHEAR.get(),40,0,true,false,true));
            case HELLSPEC -> {
                event.setAmount(event.getAmount()*.8F);
                if(retaliationAllowed){
                    attacker.setSecondsOnFire(3);
                    ArtifactFeedback.burst(player,kind,attacker.blockPosition(),8);
                }
            }
            case INTERREGNUM -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_TETHER.get(),40,1,true,false,true));}
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
            case IRIDIUM -> {event.setAmount(event.getAmount()*.85F);player.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.LODEGUARD.get(),80,0,true,false,true));}
            case SEAM_RIPPER -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)attacker.addEffect(new MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.SEAM_FRAY.get(),60,0,true,false,true));}
            default -> {return;}
        }
        ArtifactFeedback.burst(player,kind,player.blockPosition(),4);
    }

    public static void tick(ServerPlayer player, ArtifactKind kind) {
        grantNightVision(player);
        switch (kind) {
            case PALIMPSEST -> { grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.DAWN_MENDING.get(), 0, PASSIVE_LEASE_TICKS); grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS); }
            case CHOIR -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 0, PASSIVE_LEASE_TICKS);
            case EVENTIDE -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get(), 0, PASSIVE_LEASE_TICKS);
            case CRUCIBLE -> { if (player.isOnFire() || player.isInLava()) grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.CRUCIBLE_MANTLE.get(), 0, PASSIVE_LEASE_TICKS); }
            case INTERREGNUM -> {
                grant(player, MobEffects.ABSORPTION, 0, PASSIVE_LEASE_TICKS);
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_WARD.get(), 0, PASSIVE_LEASE_TICKS);
                shareStasisWard(player);
            }
            case WORLDLOOM -> {
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_RENEWAL.get(), 0, PASSIVE_LEASE_TICKS);
                sustain(player);
                shareHeartwood(player);
            }
            case ICARUS -> {
                if(!player.onGround())grant(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_MOMENTUM.get(),0,PASSIVE_LEASE_TICKS);
                else player.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_MOMENTUM.get());
            }
            case AXIOM -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 0, PASSIVE_LEASE_TICKS);
            case WORLDBREAKER -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.WORLDBREAKER_AEGIS.get(), 0, PASSIVE_LEASE_TICKS);
            case EXODIUM -> {
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS);
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get(), 0, PASSIVE_LEASE_TICKS);
            }
            case IRIDIUM -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS);
            case HELLSPEC -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.SEARING_SHELL.get(), 0, PASSIVE_LEASE_TICKS);
            case SEAM_RIPPER -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS);
            default -> { }
        }
    }

    private static boolean isStrataFocusPick(ArtifactKind kind){
        return switch(kind){case PALIMPSEST,CHOIR,AXIOM,EXODIUM,IRIDIUM,SEAM_RIPPER->true;default->false;};
    }

    private static void grant(ServerPlayer player, MobEffect effect, int amplifier, int duration) {
        var current = player.getEffect(effect);
        if (current == null || current.getAmplifier() < amplifier || current.getAmplifier() == amplifier && current.getDuration() <= 40)
            player.addEffect(new MobEffectInstance(effect, duration, amplifier, true, false, true));
    }

    /** Keeps the shared passive from replacing or removing a server/mod-owned Night Vision effect. */
    private static void grantNightVision(ServerPlayer player){
        var current=player.getEffect(MobEffects.NIGHT_VISION);var data=player.getPersistentData();
        boolean owned=data.getBoolean(NIGHT_VISION_LEASE);
        if(owned&&(current==null||current.getDuration()>PASSIVE_LEASE_TICKS||!isNightVisionLease(current))){
            data.remove(NIGHT_VISION_LEASE);owned=false;
        }
        if(current==null){
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,PASSIVE_LEASE_TICKS,0,true,false,true));
            data.putBoolean(NIGHT_VISION_LEASE,true);
        }else if(owned&&current.getDuration()<=PASSIVE_LEASE_TICKS){
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,PASSIVE_LEASE_TICKS,0,true,false,true));
        }
    }

    private static void clearNightVisionLease(ServerPlayer player){
        var data=player.getPersistentData();var current=player.getEffect(MobEffects.NIGHT_VISION);
        if(data.getBoolean(NIGHT_VISION_LEASE)&&current!=null&&current.getDuration()<=PASSIVE_LEASE_TICKS&&isNightVisionLease(current))
            player.removeEffect(MobEffects.NIGHT_VISION);
        data.remove(NIGHT_VISION_LEASE);
    }

    private static boolean isNightVisionLease(MobEffectInstance effect){
        return effect.getAmplifier()==0&&effect.isAmbient()&&!effect.isVisible()&&effect.showIcon();
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
        if(!allyAuraScanDue(owner,ArtifactKind.INTERREGNUM,"stasisAuraScanAt"))return;
        var center=owner.position();double rangeSqr=36D;
        var allies=EntitySelection.nearest(owner.serverLevel(),ServerPlayer.class,owner.getBoundingBox().inflate(6),ally->
                ally!=owner&&ally.isAlive()&&ally.isAlliedTo(owner)&&ally.distanceToSqr(center)<=rangeSqr,
                center,8,256);
        for(var ally:allies){
            grant(ally,MobEffects.ABSORPTION,0,PASSIVE_LEASE_TICKS);
            grant(ally,io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_WARD.get(),0,PASSIVE_LEASE_TICKS);
        }
    }

    /** Worldloom lends its slow custom healing pulses to a small, nearby allied group. */
    private static void shareHeartwood(ServerPlayer owner){
        if(!allyAuraScanDue(owner,ArtifactKind.WORLDLOOM,"heartwoodAuraScanAt"))return;
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

    private static boolean allyAuraScanDue(ServerPlayer owner,ArtifactKind kind,String key){
        var state=ArtifactState.of(owner,kind);long now=ArtifactState.now(owner);
        if(state.getLong(key)>now)return false;
        state.putLong(key,now+ALLY_AURA_SCAN_TICKS);
        return true;
    }
}
