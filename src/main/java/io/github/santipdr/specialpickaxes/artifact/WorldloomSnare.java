package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Short-lived root bindings created by the Worldloom's held alternate skill. */
public final class WorldloomSnare {
    private record Binding(LivingEntity target,ServerPlayer owner,ServerLevel level,Vec3 anchor,long expires) {}
    private static final Map<UUID,Binding> BINDINGS=new HashMap<>();
    private WorldloomSnare() {}

    public static boolean bind(ServerPlayer owner,LivingEntity target){
        if(!target.isAlive()||target==owner||target.isAlliedTo(owner)||DomainFields.frozen(target)
                ||!WorldSafety.allowed(owner,ArtifactKind.WORLDLOOM,target.blockPosition()))return false;
        if(target instanceof Player other&&!owner.canHarmPlayer(other))return false;
        UUID id=target.getUUID();
        if(!BINDINGS.containsKey(id)&&BINDINGS.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
        var level=owner.serverLevel();var anchor=target.position();
        BINDINGS.put(id,new Binding(target,owner,level,anchor,level.getGameTime()+50));
        return true;
    }

    public static void tick(){
        if(BINDINGS.isEmpty())return;
        var iterator=BINDINGS.entrySet().iterator();
        while(iterator.hasNext()){
            var binding=iterator.next().getValue();var target=binding.target;var owner=binding.owner;var level=binding.level;
            if(!target.isAlive()||target.isRemoved()||owner.isRemoved()||!owner.isAlive()||target.level()!=level
                    ||level.getGameTime()>=binding.expires){iterator.remove();continue;}
            BlockPos at=BlockPos.containing(binding.anchor);
            if(level.hasChunkAt(at)){
                var box=target.getBoundingBox().move(binding.anchor.subtract(target.position()));
                if(level.noCollision(target,box))target.setPos(binding.anchor);
            }
            target.setDeltaMovement(Vec3.ZERO);target.hasImpulse=true;
            if(level.getGameTime()%5==0)level.sendParticles(owner,ParticleTypes.COMPOSTER,false,
                    binding.anchor.x,binding.anchor.y+target.getBbHeight()*.45,binding.anchor.z,3,.22,.35,.22,.01);
        }
    }

    public static void forget(ServerPlayer owner){BINDINGS.values().removeIf(binding->binding.owner==owner);}
    public static void clear(){BINDINGS.clear();}
}
