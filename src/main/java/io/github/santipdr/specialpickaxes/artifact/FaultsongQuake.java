package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import java.util.HashSet;
import java.util.UUID;

/** Held seismic crowd control. No excavation, explosion, terrain edits or player targets. */
public final class FaultsongQuake {
    private static final HashSet<UUID> STARTING=new HashSet<>();
    private FaultsongQuake(){}
    public static boolean start(ServerPlayer player,ItemStack tool){
        if(!STARTING.add(player.getUUID()))return false;
        try{
            var context=HeldUtilityContext.capture(player,tool);
            if(!context.valid()||!(tool.getItem() instanceof ArtifactItem item)
                    ||ArtifactSkills.feedbackKind(tool,item.kind)!=ArtifactKind.CHOIR)return false;
            if(!WorldSafety.allowed(player,ArtifactKind.CHOIR,player.blockPosition())||!context.valid())return false;
            var level=player.serverLevel();var origin=player.position();
            // Broadphase scan and result count are both bounded; never loads remote chunks.
            var targets=EntitySelection.nearest(level,Mob.class,player.getBoundingBox().inflate(24),
                    mob->mob.isAlive()&&!mob.isAlliedTo(player)&&EntitySelection.isHostileCandidate(mob,player)
                            &&mob.distanceToSqr(origin)<=24*24,origin,32);
            for(var mob:targets){
                if(!context.valid())return false;
                if(!mob.isAlive()||mob.level()!=level||mob.isAlliedTo(player)||mob.distanceToSqr(origin)>24*24)continue;
                if(!WorldSafety.allowed(player,ArtifactKind.CHOIR,mob.blockPosition())||!context.valid())continue;
                if(!mob.isAlive()||mob.level()!=level||mob.isAlliedTo(player)||mob.distanceToSqr(origin)>24*24)continue;
                if(!mob.hurt(player.damageSources().playerAttack(player),24))continue;
                // Damage listeners may disarm/move the caster or teleport/remove the target.
                if(!context.valid())return false;
                if(!mob.isAlive()||mob.level()!=level||mob.distanceToSqr(origin)>24*24)continue;
                mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,4,false,true,true),player);
                if(!context.valid())return false;
                if(mob.isAlive()&&mob.level()==level&&mob.distanceToSqr(origin)<=24*24){
                    var away=mob.position().subtract(origin).multiply(1,0,1).normalize();
                    mob.push(away.x*.9,.65,away.z*.9);mob.hasImpulse=true;
                }
            }
            if(!context.valid())return false;
            AbilityVfx.seismicQuake(player);
            level.playSound(null,player.blockPosition(),SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,1.5F,.55F);
            return true;
        }finally{STARTING.remove(player.getUUID());}
    }
}
