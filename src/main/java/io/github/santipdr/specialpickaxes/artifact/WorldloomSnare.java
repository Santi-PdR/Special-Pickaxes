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

/** Short-lived root bindings created by the Worldloom's held area skill. */
public final class WorldloomSnare {
    private static final net.minecraft.world.level.block.state.BlockState VINE_STATE=
            net.minecraft.world.level.block.Blocks.VINE.defaultBlockState();
    private static final net.minecraft.core.particles.BlockParticleOption VINE_PARTICLE=
            new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,VINE_STATE);
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
        BINDINGS.put(id,new Binding(target,owner,level,anchor,level.getGameTime()+80));
        return true;
    }

    public static void tick(){
        if(BINDINGS.isEmpty())return;
        var iterator=BINDINGS.entrySet().iterator();
        while(iterator.hasNext()){
            var binding=iterator.next().getValue();var target=binding.target;var owner=binding.owner;var level=binding.level;
            if(!target.isAlive()||target.isRemoved()||owner.isRemoved()||!owner.isAlive()||owner.serverLevel()!=level||target.level()!=level
                    ||target.isAlliedTo(owner)||target instanceof Player other&&!owner.canHarmPlayer(other)
                    ||level.getGameTime()>=binding.expires){iterator.remove();continue;}
            if(level.getGameTime()%5==0&&!WorldSafety.allowed(owner,ArtifactKind.WORLDLOOM,target.blockPosition())){iterator.remove();continue;}
            BlockPos at=BlockPos.containing(binding.anchor);
            if(level.hasChunkAt(at)){
                var box=target.getBoundingBox().move(binding.anchor.subtract(target.position()));
                if(level.noCollision(target,box))target.setPos(binding.anchor);
            }
            target.setDeltaMovement(Vec3.ZERO);target.hasImpulse=true;
            if(level.getGameTime()%10==0){
                // One small owner-only packet wraps the target's feet and shins in visible roots.
                level.sendParticles(owner,VINE_PARTICLE,false,binding.anchor.x,binding.anchor.y+.35,binding.anchor.z,8,.34,.34,.34,.015);
            }
        }
    }

    public static void forget(ServerPlayer owner){BINDINGS.values().removeIf(binding->binding.owner==owner);}
    public static void clear(){BINDINGS.clear();}
}
