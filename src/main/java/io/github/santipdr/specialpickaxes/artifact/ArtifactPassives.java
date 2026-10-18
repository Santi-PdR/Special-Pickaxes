package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;

/** Small, always-on identities for every artifact. No attack-strength effects are used. */
public final class ArtifactPassives {
    private static final int PASSIVE_LEASE_TICKS=40;
    private static final int ALLY_AURA_SCAN_TICKS=20;
    private static final String NIGHT_VISION_LEASE="specialpickaxesNightVisionLease";
    private static final String ABSORPTION_LEASE="specialpickaxesInterregnumAbsorptionLease";
    private ArtifactPassives() {}

    /** Remove an always-on Curios effect as soon as its source is unequipped. */
    public static void onCuriosUnequip(ServerPlayer player,ArtifactKind kind) {
        boolean heldArtifact=player.getMainHandItem().getItem() instanceof ArtifactItem;
        if(!heldArtifact&&CuriosCompat.find(player)==null)clearNightVisionLease(player);
        if(kind==ArtifactKind.EVENTIDE
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.EVENTIDE))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_BALANCE.get());
        if(kind==ArtifactKind.ICARUS
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.ICARUS)){
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_MOMENTUM.get());
        }
        if(kind==ArtifactKind.HELLSPEC
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.HELLSPEC))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.SEARING_SHELL.get());
        if(kind==ArtifactKind.CRUCIBLE
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.CRUCIBLE))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.CRUCIBLE_MANTLE.get());
        if(kind==ArtifactKind.WORLDBREAKER
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.WORLDBREAKER))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.WORLDBREAKER_AEGIS.get());
        if(isStrataFocusPick(kind)
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&isStrataFocusPick(held.kind)))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get());
        if(kind==ArtifactKind.INTERREGNUM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.INTERREGNUM))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.STASIS_WARD.get());
        if(kind==ArtifactKind.PALIMPSEST
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.PALIMPSEST))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.DAWN_MENDING.get());
        if(kind==ArtifactKind.AXIOM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.AXIOM))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.NULL_VEIL.get());
        if(kind==ArtifactKind.WORLDLOOM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.WORLDLOOM))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_RENEWAL.get());
        if(kind==ArtifactKind.EXODIUM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.EXODIUM))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get());
        if(kind==ArtifactKind.EXODIUM
                &&!(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.EXODIUM))
            removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_GUARD.get());
    }

    /** Curios-only defensive signatures. Each relic can answer one hostile hit every eight seconds. */
    public static void onCuriosDamage(ServerPlayer player,net.minecraftforge.event.entity.living.LivingHurtEvent event,ArtifactKind kind) {
        onCuriosDamage(player,event,kind,false);
    }

    /** Applies Exodium's shared ward even when the protected teammate has no Curios relic equipped. */
    public static boolean onSharedStarfallGuard(ServerPlayer player,net.minecraftforge.event.entity.living.LivingHurtEvent event){
        if(event.getAmount()<=0||!event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)
                ||!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_GUARD.get()))return false;
        event.setAmount(event.getAmount()*.75F);
        StarfallWardEffect.projectileImpact(player);
        return true;
    }

    public static void onCuriosDamage(ServerPlayer player,net.minecraftforge.event.entity.living.LivingHurtEvent event,ArtifactKind kind,boolean sharedGuardApplied) {
        if(event.getAmount()<=0)return;
        var source=event.getSource();
        boolean starfallGuarded=sharedGuardApplied||onSharedStarfallGuard(player,event);
        if(kind==ArtifactKind.EXODIUM&&player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get())
                &&source.is(net.minecraft.tags.DamageTypeTags.IS_PROJECTILE)){
            if(!starfallGuarded)event.setAmount(event.getAmount()*.75F);
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
            case CHOIR,EVENTIDE,CRUCIBLE,INTERREGNUM,WORLDLOOM,ICARUS,AXIOM,EXODIUM,HELLSPEC,SEAM_RIPPER -> WorldSafety.allowed(player,kind,attacker.blockPosition());
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
            case PALIMPSEST -> {event.setAmount(event.getAmount()*.9F);grantCombatEffect(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.DAWN_RESERVE.get(),0,80);}
            case CHOIR -> {
                event.setAmount(event.getAmount()*.85F);
                grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.FAULT_RESONANCE.get(),0,60);
            }
            case EVENTIDE -> {
                event.setAmount(event.getAmount()*.9F);
                if(retaliationAllowed){
                    grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_TETHER.get(),0,70);
                    Vec3 toward=player.position().subtract(attacker.position());double horizontalSqr=toward.x*toward.x+toward.z*toward.z;
                    if(horizontalSqr>.04D&&attacker.distanceToSqr(player)<=144D){
                        Vec3 tug=new Vec3(toward.x,0,toward.z).normalize().scale(.32D);
                        attacker.setDeltaMovement(attacker.getDeltaMovement().add(tug));attacker.hasImpulse=true;
                        ArtifactFeedback.gravityTether(player,attacker.position().add(0,.5D,0),player.getEyePosition());
                    }
                }
            }
            case CRUCIBLE -> grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.PHASE_SHEAR.get(),0,40);
            case HELLSPEC -> {
                event.setAmount(event.getAmount()*.8F);
                if(retaliationAllowed){
                    attacker.setSecondsOnFire(3);
                    ArtifactFeedback.burst(player,kind,attacker.blockPosition(),8);
                }
            }
            case INTERREGNUM -> {event.setAmount(event.getAmount()*.85F);if(retaliationAllowed)grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.GRAVITIC_TETHER.get(),1,40);}
            case WORLDLOOM -> {
                event.setAmount(event.getAmount()*.9F);
                grantCombatEffect(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_RENEWAL.get(),0,60);
                if(retaliationAllowed)grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.HEARTWOOD_GRASP.get(),0,60);
            }
            case ICARUS -> {
                Vec3 away=attacker.position().subtract(player.position());
                if(away.lengthSqr()<.01)away=new Vec3(-player.getLookAngle().x,0,-player.getLookAngle().z);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(away.normalize().scale(.8).add(0,.25,0)));
                attacker.hasImpulse=true;
            }
            case AXIOM -> grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.NULL_MARK.get(),0,60);
            case WORLDBREAKER -> {event.setAmount(event.getAmount()*.8F);grantCombatEffect(player,MobEffects.ABSORPTION,1,60);}
            case EXODIUM -> {
                Vec3 away=attacker.position().subtract(player.position());
                if(away.lengthSqr()<.01)away=new Vec3(-player.getLookAngle().x,0,-player.getLookAngle().z);
                attacker.setDeltaMovement(attacker.getDeltaMovement().add(away.normalize().scale(.45).add(0,.65,0)));
                attacker.hasImpulse=true;
            }
            case IRIDIUM -> {event.setAmount(event.getAmount()*.85F);grantCombatEffect(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.LODEGUARD.get(),0,80);}
            case SEAM_RIPPER -> {
                event.setAmount(event.getAmount()*.85F);
                if(retaliationAllowed){
                    grantCombatEffect(attacker,io.github.santipdr.specialpickaxes.SpecialPickaxes.SEAM_FRAY.get(),0,60);
                    SeamFrayEffect.impactPulse(attacker);
                }
            }
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
                grantAbsorption(player,PASSIVE_LEASE_TICKS);
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
                else removeLease(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.ICARIAN_MOMENTUM.get());
            }
            case AXIOM -> {
                grant(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(),0,PASSIVE_LEASE_TICKS);
                grant(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.NULL_VEIL.get(),0,PASSIVE_LEASE_TICKS);
            }
            case WORLDBREAKER -> {
                grant(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.WORLDBREAKER_AEGIS.get(),0,PASSIVE_LEASE_TICKS);
                shareWorldbreakerAegis(player);
            }
            case EXODIUM -> {
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS);
                grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_WARD.get(), 0, PASSIVE_LEASE_TICKS);
                shareStarfallGuard(player);
            }
            case IRIDIUM -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS);
            case HELLSPEC -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.SEARING_SHELL.get(), 0, PASSIVE_LEASE_TICKS);
            case SEAM_RIPPER -> grant(player, io.github.santipdr.specialpickaxes.SpecialPickaxes.STRATA_FOCUS.get(), 1, PASSIVE_LEASE_TICKS);
            default -> { }
        }
    }

    /** Successive Iridium ore breaks build up to three short mining-speed charges. */
    public static int lodeSurge(ServerPlayer player,BlockPos pos){
        var state=ArtifactState.of(player,ArtifactKind.IRIDIUM);long now=ArtifactState.now(player);
        int stacks=now<=state.getLong("lodeSurgeExpires")
                ?Math.min(3,state.getInt("lodeSurgeStacks")+1):1;
        state.putInt("lodeSurgeStacks",stacks);state.putLong("lodeSurgeExpires",now+80L);
        grantCombatEffect(player,io.github.santipdr.specialpickaxes.SpecialPickaxes.LODE_SURGE.get(),stacks-1,80);
        LodeSurgeEffect.oreBreakPulse(player,pos,stacks);
        return stacks;
    }

    private static boolean isStrataFocusPick(ArtifactKind kind){
        return switch(kind){case PALIMPSEST,CHOIR,AXIOM,EXODIUM,IRIDIUM,SEAM_RIPPER->true;default->false;};
    }

    private static void grant(ServerPlayer player, MobEffect effect, int amplifier, int duration) {
        var current=player.getEffect(effect);var data=player.getPersistentData();String key=passiveLeaseKey(effect);
        boolean owned=data.getBoolean(key);
        if(owned&&(current==null||!isPassiveLease(current))){data.remove(key);owned=false;}
        if(current==null){
            player.addEffect(new MobEffectInstance(effect,duration,amplifier,true,false,true));
            data.putBoolean(key,true);
        }else if(owned&&(current.getAmplifier()<amplifier||current.getAmplifier()==amplifier&&current.getDuration()<=duration)){
            player.addEffect(new MobEffectInstance(effect,duration,amplifier,true,false,true));
        }
    }

    /** Combat passives never replace an effect supplied by a server, potion or another relic. */
    public static void grantCombatEffect(LivingEntity target,MobEffect effect,int amplifier,int duration){
        var current=target.getEffect(effect);var data=target.getPersistentData();String key=combatLeaseKey(effect);
        boolean owned=data.getBoolean(key)||data.getBoolean(passiveLeaseKey(effect));
        if(effect==MobEffects.ABSORPTION&&data.getBoolean(ABSORPTION_LEASE)&&current!=null&&isAbsorptionLease(current))owned=true;
        if(owned&&(current==null||!isPassiveLease(current))){data.remove(key);owned=false;}
        if(current==null){
            target.addEffect(new MobEffectInstance(effect,duration,amplifier,true,false,true));
            data.putBoolean(key,true);
        }else if(owned&&(current.getAmplifier()<amplifier||current.getAmplifier()==amplifier&&current.getDuration()<=duration)){
            target.addEffect(new MobEffectInstance(effect,duration,amplifier,true,false,true));
            data.putBoolean(key,true);
        }
    }

    private static String combatLeaseKey(MobEffect effect){
        return "specialpickaxesCuriosCombatLease:"+net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect);
    }

    private static String passiveLeaseKey(MobEffect effect){
        return "specialpickaxesPassiveLease:"+net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect);
    }

    private static void removeLease(ServerPlayer player,MobEffect effect){
        var data=player.getPersistentData();String key=passiveLeaseKey(effect);
        if(!data.getBoolean(key))return;
        var current=player.getEffect(effect);
        if(current!=null&&isPassiveLease(current))player.removeEffect(effect);
        data.remove(key);
    }

    /** Keeps Curios absorption from replacing a potion supplied by the server or another mod. */
    private static void grantAbsorption(ServerPlayer player,int duration){
        var current=player.getEffect(MobEffects.ABSORPTION);var data=player.getPersistentData();
        boolean owned=data.getBoolean(ABSORPTION_LEASE);
        if(owned&&(current==null||!isAbsorptionLease(current))){data.remove(ABSORPTION_LEASE);owned=false;}
        if(current==null){
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,duration,0,true,false,true));
            data.putBoolean(ABSORPTION_LEASE,true);
        }else if(owned&&current.getAmplifier()==0&&current.getDuration()<=duration){
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,duration,0,true,false,true));
        }
    }

    private static boolean isAbsorptionLease(MobEffectInstance effect){
        return effect.getAmplifier()==0&&isPassiveLease(effect);
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
        return effect.getAmplifier()==0&&isPassiveLease(effect);
    }

    private static boolean isPassiveLease(MobEffectInstance effect){
        return effect.isAmbient()&&!effect.isVisible()&&effect.showIcon();
    }

    private static void sustain(ServerPlayer player){
        if(!player.getFoodData().needsFood())return;
        var state=ArtifactState.of(player,ArtifactKind.WORLDLOOM);
        long now=ArtifactState.now(player);
        if(state.getLong("rootSustainReady")>now)return;
        player.getFoodData().eat(2,.25F);
        state.putLong("rootSustainReady",now+100);
        HeartwoodRenewalEffect.foodPulse(player);
    }

    /** A Curios Stasis bearer shares a short absorption lease with at most eight nearby allies. */
    private static void shareStasisWard(ServerPlayer owner){
        if(!allyAuraScanDue(owner,ArtifactKind.INTERREGNUM,"stasisAuraScanAt"))return;
        var center=owner.position();double rangeSqr=36D;
        var allies=EntitySelection.nearest(owner.serverLevel(),ServerPlayer.class,owner.getBoundingBox().inflate(6),ally->
                ally!=owner&&ally.isAlive()&&ally.isAlliedTo(owner)&&ally.distanceToSqr(center)<=rangeSqr,
                center,8,256);
        for(var ally:allies){
            grantAbsorption(ally,PASSIVE_LEASE_TICKS);
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
            grant(ally,renewal,0,PASSIVE_LEASE_TICKS);
            sustain(ally);
        }
    }

    /** Worldbreaker lends its armor-and-anchor ward to a bounded three-player allied formation. */
    private static void shareWorldbreakerAegis(ServerPlayer owner){
        if(!allyAuraScanDue(owner,ArtifactKind.WORLDBREAKER,"worldbreakerAuraScanAt"))return;
        var center=owner.position();double rangeSqr=25D;
        var allies=EntitySelection.nearest(owner.serverLevel(),ServerPlayer.class,owner.getBoundingBox().inflate(5),ally->
                ally!=owner&&ally.isAlive()&&ally.isAlliedTo(owner)&&ally.distanceToSqr(center)<=rangeSqr,
                center,3,128);
        var aegis=io.github.santipdr.specialpickaxes.SpecialPickaxes.WORLDBREAKER_AEGIS.get();
        for(var ally:allies)grant(ally,aegis,0,PASSIVE_LEASE_TICKS);
    }

    /** Exodium lends a lighter projectile ward to at most three nearby teammates. */
    private static void shareStarfallGuard(ServerPlayer owner){
        if(!allyAuraScanDue(owner,ArtifactKind.EXODIUM,"starfallAuraScanAt"))return;
        var center=owner.position();double rangeSqr=36D;
        var allies=EntitySelection.nearest(owner.serverLevel(),ServerPlayer.class,owner.getBoundingBox().inflate(6),ally->
                ally!=owner&&ally.isAlive()&&ally.isAlliedTo(owner)&&ally.distanceToSqr(center)<=rangeSqr,
                center,3,128);
        var guard=io.github.santipdr.specialpickaxes.SpecialPickaxes.STARFALL_GUARD.get();
        for(var ally:allies)grant(ally,guard,0,PASSIVE_LEASE_TICKS);
    }

    private static boolean allyAuraScanDue(ServerPlayer owner,ArtifactKind kind,String key){
        var state=ArtifactState.of(owner,kind);long now=ArtifactState.now(owner);
        if(state.getLong(key)>now)return false;
        state.putLong(key,now+ALLY_AURA_SCAN_TICKS);
        return true;
    }
}
