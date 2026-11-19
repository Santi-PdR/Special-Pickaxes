package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

/** One synchronous aura handoff must not follow an owner or recipient moved by effect callbacks. */
record AuraShareContext(ServerPlayer owner,ServerLevel level,Vec3 origin,double rangeSqr) {
    static AuraShareContext capture(ServerPlayer owner,double rangeSqr){
        return new AuraShareContext(owner,owner.serverLevel(),owner.position(),rangeSqr);
    }
    boolean ownerValid(){
        return WorldbreakerCuriosPassives.valid(owner)&&owner.isAlive()&&!owner.isRemoved()&&!owner.isSpectator()
                &&owner.serverLevel()==level&&owner.position().equals(origin);
    }
    boolean allows(ServerPlayer ally){
        return ownerValid()&&ally!=owner&&ally.isAlive()&&!ally.isRemoved()&&!ally.isSpectator()
                &&ally.serverLevel()==level&&ally.isAlliedTo(owner)&&owner.isAlliedTo(ally)
                &&ally.distanceToSqr(origin)<=rangeSqr;
    }
    Recipient recipient(ServerPlayer ally){return new Recipient(this,ally,ally.position());}
    record Recipient(AuraShareContext context,ServerPlayer ally,Vec3 origin) {
        boolean valid(){return context.allows(ally)&&ally.position().equals(origin);}
    }
}
