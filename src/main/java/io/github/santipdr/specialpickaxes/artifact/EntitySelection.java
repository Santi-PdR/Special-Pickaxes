package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/** Select nearby targets without materializing or sorting every entity in a crowded ability area. */
final class EntitySelection {
    static final int DEFAULT_INSPECTION_LIMIT = 1024;

    private EntitySelection() {}

    /** Hostiles include custom mobs that have selected the wearer or an ally as their live target. */
    static boolean isHostileCandidate(LivingEntity entity,ServerPlayer wearer){
        if(entity instanceof Player)return entity!=wearer;
        if(entity instanceof Enemy)return true;
        if(entity instanceof Mob mob){
            LivingEntity target=mob.getTarget();
            return target!=null&&target.isAlive()&&target.level()==wearer.level()
                    &&(target==wearer||target.isAlliedTo(wearer));
        }
        return false;
    }

    static <T extends Entity> List<T> nearest(ServerLevel level, Class<T> type, AABB bounds,
                                               Predicate<T> eligible, Vec3 center, int limit) {
        return nearest(level,type,bounds,eligible,center,limit,DEFAULT_INSPECTION_LIMIT);
    }

    static <T extends Entity> List<T> nearest(ServerLevel level, Class<T> type, AABB bounds,
                                               Predicate<T> eligible, Vec3 center, int limit,int inspectionLimit) {
        if (limit <= 0 || inspectionLimit <= 0) return List.of();

        var order=Comparator.<T>comparingDouble(entity->entity.distanceToSqr(center)).thenComparingInt(Entity::getId);
        var selected=new NearestCandidates<T>(limit,order);
        int[] inspected = {0};
        level.getEntities().get(EntityTypeTest.forClass(type), bounds, candidate -> {
            if (++inspected[0] > inspectionLimit) return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if (eligible.test(candidate)) selected.offer(candidate);
            return inspected[0] >= inspectionLimit
                    ? net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT
                    : net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });

        return selected.sorted();
    }

    static <T extends Entity> List<T> nearest(List<T> candidates, Vec3 center, int limit) {
        if (limit <= 0) return List.of();
        var order=Comparator.<T>comparingDouble(entity->entity.distanceToSqr(center)).thenComparingInt(Entity::getId);
        var selected=new NearestCandidates<T>(limit,order);
        for(var candidate:candidates)selected.offer(candidate);
        return selected.sorted();
    }
}
