package io.github.santipdr.specialpickaxes.artifact;

import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NearestCandidatesTest {
    @Test void underCapacityResultsStillSortAndDoNotAliasInput(){
        var input=new java.util.ArrayList<>(List.of(7,1,4));
        var selected=new NearestCandidates<Integer>(10,Comparator.naturalOrder());
        input.forEach(selected::offer);
        assertEquals(List.of(1,4,7),selected.sorted());
        assertEquals(List.of(7,1,4),input);
        assertThrows(UnsupportedOperationException.class,()->selected.sorted().add(2));
    }
    @Test void duplicatesDoNotConsumeTheLimit(){
        var selected=new NearestCandidates<Integer>(3,Comparator.naturalOrder());
        List.of(1,1,1,9,4,2,2).forEach(selected::offer);
        assertEquals(List.of(1,2,4),selected.sorted());
    }
    @Test void zeroAndNegativeLimitsStayEmpty(){
        for(int limit:new int[]{0,-1,Integer.MIN_VALUE}){
            var selected=new NearestCandidates<Integer>(limit,Comparator.naturalOrder());
            selected.offer(1);assertTrue(selected.sorted().isEmpty());
        }
    }
    @Test void largestLimitDoesNotAllocateAnEnormousHeap(){
        var selected=new NearestCandidates<Integer>(Integer.MAX_VALUE,Comparator.naturalOrder());
        selected.offer(2);selected.offer(1);
        assertEquals(List.of(1,2),selected.sorted());
    }
    @Test void randomizedCollectorMatchesSortedDistinctReference(){
        var random=new Random(9317L);
        for(int limit:new int[]{1,3,16,64}){
            var selected=new NearestCandidates<Integer>(limit,Comparator.naturalOrder());
            var input=IntStream.range(0,1000).map(i->random.nextInt(200)).boxed().toList();
            input.forEach(selected::offer);
            assertEquals(input.stream().distinct().sorted().limit(limit).toList(),selected.sorted());
        }
    }
    @Test void secondaryOrderMakesEqualDistanceSelectionStable(){
        record Candidate(int distance,int id){}
        var order=Comparator.comparingInt(Candidate::distance).thenComparingInt(Candidate::id);
        var selected=new NearestCandidates<Candidate>(2,order);
        selected.offer(new Candidate(5,9));selected.offer(new Candidate(5,3));selected.offer(new Candidate(5,1));
        assertEquals(List.of(new Candidate(5,1),new Candidate(5,3)),selected.sorted());
    }
    @Test void snapshotsRemainUnchangedWhenCollectorContinues(){
        var selected=new NearestCandidates<Integer>(2,Comparator.naturalOrder());
        selected.offer(4);var snapshot=selected.sorted();selected.offer(1);
        assertEquals(List.of(4),snapshot);assertEquals(List.of(1,4),selected.sorted());
    }
}
