package io.github.santipdr.specialpickaxes.artifact;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/** Bounded, duplicate-free top-k collector. The comparator orders best candidates first. */
final class NearestCandidates<T> {
    private final int limit;
    private final Comparator<? super T> order;
    private final PriorityQueue<T> heap;
    private final Set<T> retained=new HashSet<>();

    NearestCandidates(int limit,Comparator<? super T> order){
        this.limit=Math.max(0,limit);
        this.order=order;
        heap=new PriorityQueue<>(Math.max(1,Math.min(this.limit,64)),order.reversed());
    }

    void offer(T candidate){
        if(limit==0||retained.contains(candidate))return;
        if(heap.size()==limit){
            if(order.compare(candidate,heap.peek())>=0)return;
            retained.remove(heap.poll());
        }
        heap.add(candidate);retained.add(candidate);
    }

    List<T> sorted(){
        var result=new ArrayList<T>(heap);
        result.sort(order);
        return List.copyOf(result);
    }
}
