package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Claim callbacks must not redirect an already selected passive reaction to a changed actor. */
record PassiveReactionContext(ServerPlayer player,LivingEntity target,ServerLevel level,Vec3 origin,Vec3 targetOrigin) {
    static PassiveReactionContext capture(ServerPlayer player,LivingEntity target){
        return new PassiveReactionContext(player,target,player.serverLevel(),player.position(),target.position());
    }
    boolean valid(){
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.mayBuild()
                &&target.isAlive()&&!target.isRemoved()&&target!=player
                &&player.serverLevel()==level&&target.level()==level
                &&player.position().equals(origin)&&target.position().equals(targetOrigin)
                &&!target.isAlliedTo(player)&&!player.isAlliedTo(target)
                &&(!(target instanceof Player other)||player.canHarmPlayer(other));
    }
    boolean allowed(ArtifactKind kind){
        return valid()&&WorldSafety.allowed(player,kind,target.blockPosition())&&valid();
    }
}
