package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Predicate;

/** Select nearby targets without materializing or sorting every entity in a crowded ability area. */
final class EntitySelection {
    private static final int MAX_INSPECTED = 1024;

    private EntitySelection() {}

    static <T extends Entity> List<T> nearest(ServerLevel level, Class<T> type, AABB bounds,
                                               Predicate<T> eligible, Vec3 center, int limit) {
        if (limit <= 0) return List.of();

        var farthestFirst = Comparator.<T>comparingDouble(entity -> entity.distanceToSqr(center)).reversed();
        var selected = new PriorityQueue<T>(limit, farthestFirst);
        int[] inspected = {0};
        level.getEntities().get(EntityTypeTest.forClass(type), bounds, candidate -> {
            if (++inspected[0] > MAX_INSPECTED) return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if (eligible.test(candidate)) {
                if (selected.size() < limit) selected.add(candidate);
                else if (candidate.distanceToSqr(center) < selected.peek().distanceToSqr(center)) {
                    selected.poll();
                    selected.add(candidate);
                }
            }
            return inspected[0] >= MAX_INSPECTED
                    ? net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT
                    : net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });

        var result = new ArrayList<T>(selected);
        result.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
        return result;
    }

    static <T extends Entity> List<T> nearest(List<T> candidates, Vec3 center, int limit) {
        if (limit <= 0) return List.of();
        if (candidates.size() <= limit) return candidates;

        var farthestFirst = Comparator.<T>comparingDouble(entity -> entity.distanceToSqr(center)).reversed();
        var selected = new PriorityQueue<T>(limit, farthestFirst);
        for (var candidate : candidates) {
            if (selected.size() < limit) selected.add(candidate);
            else if (candidate.distanceToSqr(center) < selected.peek().distanceToSqr(center)) {
                selected.poll();
                selected.add(candidate);
            }
        }

        var result = new ArrayList<T>(selected);
        result.sort(Comparator.comparingDouble(entity -> entity.distanceToSqr(center)));
        return result;
    }
}
