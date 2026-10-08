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
            assertTrue(section.size()<=49*21);assertTrue(signatures.add(new java.util.HashSet<>(section)),"distinct "+shape);
        }
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
