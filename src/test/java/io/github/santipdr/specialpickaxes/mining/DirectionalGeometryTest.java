package io.github.santipdr.specialpickaxes.mining;
import io.github.santipdr.specialpickaxes.artifact.DirectionalProgram;
import static io.github.santipdr.specialpickaxes.artifact.DirectionalProgram.Shape.*;
import net.minecraft.core.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DirectionalGeometryTest {
    @Test void eachShapeHasDistinctCrossSectionAndNoDuplicates(){
        var signatures=new java.util.HashSet<java.util.Set<BlockPos>>();
        for(var shape:DirectionalProgram.Shape.values()){
            var section=DirectionalProgram.section(BlockPos.ZERO,Direction.SOUTH,shape,30);
            assertEquals(section.size(),new java.util.HashSet<>(section).size());
            assertTrue(section.size()<=49*21);
            var signature=new java.util.HashSet<>(section);
            // Worldloom's straight 5x5 bore and Worldbreaker's vertical core drill
            // share a cross-section while their direction and length define distinct jobs.
            assertTrue(signatures.add(signature)||shape==WORLDLOOM_BORE,"distinct "+shape);
        }
    }
    @Test void exodiumSinkIsAChamferedFiveByFiveShaft(){
        var sink=new java.util.HashSet<>(DirectionalProgram.section(BlockPos.ZERO,Direction.DOWN,DirectionalProgram.Shape.EXODIUM_SINK,0));
        var bore=new java.util.HashSet<>(DirectionalProgram.section(BlockPos.ZERO,Direction.SOUTH,DirectionalProgram.Shape.WORLDLOOM_BORE,0));
        var drill=new java.util.HashSet<>(DirectionalProgram.section(BlockPos.ZERO,Direction.DOWN,DirectionalProgram.Shape.CORE_DRILL,0));
        assertEquals(21,sink.size());
        assertEquals(25,bore.size());
        assertEquals(25,drill.size());
        assertTrue(sink.stream().allMatch(p->Math.abs(p.getX())<=2&&Math.abs(p.getZ())<=2));
        assertFalse(sink.contains(new BlockPos(-2,0,-2)));
        assertTrue(sink.contains(BlockPos.ZERO));
        assertTrue(bore.contains(new BlockPos(-2,2,0)));
        assertTrue(bore.contains(BlockPos.ZERO));
    }
    @Test void carveFollowsEveryLookAxisAndIsRounded(){
        for(var d:Direction.values()){
            var s=DirectionalProgram.section(BlockPos.ZERO,d,CARVE,8);
            assertEquals(37,s.size());
            assertTrue(s.stream().allMatch(p->d.getAxis().choose(p.getX(),p.getY(),p.getZ())==d.getAxisDirection().getStep()*8));
        }
    }
    @Test void supremeIsTerracedWedgeNotCuboidAndExceedsSmallPlans(){
        var cells=new java.util.HashSet<BlockPos>();
        for(int depth=0;depth<96;depth++)for(var p:DirectionalProgram.section(BlockPos.ZERO,Direction.SOUTH,WORLD_SHATTER,depth))assertTrue(cells.add(p));
        assertTrue(cells.size()>20000);assertTrue(cells.size()<49*21*96);
        assertFalse(cells.contains(new BlockPos(20,0,0)));assertTrue(cells.contains(new BlockPos(20,0,90)));
        assertTrue(DirectionalProgram.section(BlockPos.ZERO,Direction.SOUTH,WORLD_SHATTER,60).size()>DirectionalProgram.section(BlockPos.ZERO,Direction.SOUTH,WORLD_SHATTER,10).size());
    }
}
