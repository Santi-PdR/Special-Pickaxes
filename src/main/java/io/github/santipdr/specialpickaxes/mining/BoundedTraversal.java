package io.github.santipdr.specialpickaxes.mining;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/** Iterative BFS. Rejected nodes are barriers. Both results and visited nodes are bounded. */
public final class BoundedTraversal {
    private BoundedTraversal() {}
    public static <T> List<T> walk(T seed, int limit, int visitBudget,
                                  Function<T, Iterable<T>> neighbours, Predicate<T> accept) {
        if (limit < 1 || visitBudget < 1) return List.of();
        ArrayDeque<T> queue = new ArrayDeque<>();
        Set<T> seen = new HashSet<>();
        List<T> result = new ArrayList<>();
        queue.add(seed);
        seen.add(seed);
        while (!queue.isEmpty() && result.size() < limit) {
            T node = queue.removeFirst();
            if (!accept.test(node)) continue;
            result.add(node);
            for (T next : neighbours.apply(node)) {
                if (seen.size() >= visitBudget) break;
                if (seen.add(next)) queue.addLast(next);
            }
        }
        return List.copyOf(result);
    }
}
