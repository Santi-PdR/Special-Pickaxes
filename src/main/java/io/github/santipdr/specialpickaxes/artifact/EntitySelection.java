package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/** Select nearby targets without sorting every entity in a crowded ability area. */
final class EntitySelection {
    private EntitySelection() {}

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
