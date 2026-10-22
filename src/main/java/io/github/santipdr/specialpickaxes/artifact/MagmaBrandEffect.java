package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Hellspec's eruption and Cinder Retort leave a visible brand with custom magma damage. */
public final class MagmaBrandEffect extends MobEffect {
    private static final String CASTER="specialpickaxesMagmaBrandCaster";
    public MagmaBrandEffect(){super(MobEffectCategory.HARMFUL,0xff5428);}

    /** Keeps the owner so each later damage pulse can recheck PvP, teams, range and protected regions. */
    public static boolean applyFrom(ServerPlayer caster,LivingEntity target,int duration,int amplifier){
        if(!canDamage(caster,target))return false;
        if(!ArtifactPassives.grantCombatEffect(target,io.github.santipdr.specialpickaxes.SpecialPickaxes.MAGMA_BRAND.get(),amplifier,duration))return false;
        target.getPersistentData().putUUID(CASTER,caster.getUUID());
        return true;
    }

    private static boolean canDamage(ServerPlayer caster,LivingEntity target){
        if(caster==null||target==null||!caster.isAlive()||caster.isRemoved()||target==caster||!target.isAlive()
                ||caster.serverLevel()!=target.level()||caster.isAlliedTo(target))return false;
        if(target instanceof Player other&&!caster.canHarmPlayer(other))return false;
        return WorldSafety.allowed(caster,ArtifactKind.HELLSPEC,target.blockPosition());
    }

    @Override public void applyEffectTick(LivingEntity entity,int amplifier){
        if(entity.level().isClientSide||!(entity.level() instanceof ServerLevel level))return;
        var data=entity.getPersistentData();
        if(!data.hasUUID(CASTER))return; // An effect supplied outside the relic system never becomes owned damage.
        var id=data.getUUID(CASTER);
        net.minecraft.world.entity.Entity owner=level.getEntity(id);
        ServerPlayer caster=owner instanceof ServerPlayer player?player:level.getServer().getPlayerList().getPlayer(id);
        if(!canDamage(caster,entity)){
            entity.removeEffect(this);
            ArtifactPassives.clearCombatEffectLease(entity,this);
            data.remove(CASTER);
            return;
        }
        // Keep the custom pulse distinct from vanilla fire ticks so already-burning targets still feel the mark.
        entity.invulnerableTime=0;
        if(!entity.hurt(level.damageSources().magic(),1.5F+.5F*Math.max(0,amplifier)))return;
        entity.setSecondsOnFire(2);
        double x=entity.getX(),y=entity.getY()+entity.getBbHeight()*.52D,z=entity.getZ();
        level.sendParticles(ParticleTypes.FLAME,x,y,z,4,.12D,.2D,.12D,.012D);
        level.sendParticles(ParticleTypes.LAVA,x,y-.18D,z,1,.08D,.08D,.08D,.005D);
    }

    @Override public boolean isDurationEffectTick(int duration,int amplifier){return duration%20==0;}
}
