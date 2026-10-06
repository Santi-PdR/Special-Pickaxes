package io.github.santipdr.specialpickaxes.mining;

import io.github.santipdr.specialpickaxes.artifact.Geometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeometryTest {
    @Test void cubeIsBoundedAndNearestFirst() {
        var positions=Geometry.cube(BlockPos.ZERO,8,512);
        assertEquals(512,positions.size());assertEquals(BlockPos.ZERO,positions.get(0));
        assertEquals(512,positions.stream().distinct().count());
        assertTrue(Geometry.cube(BlockPos.ZERO,13,100).isEmpty());
    }
    @Test void rotationsConserveOffsets() {
        var pos=new BlockPos(2,3,4);var copy=pos;
        for(int i=0;i<4;i++) copy=Geometry.rotate(copy,1);
        assertEquals(pos,copy);assertEquals(new BlockPos(-4,3,2),Geometry.rotate(pos,1));
    }
    @Test void sectionsHaveNineCellsOnCorrectPlane() {
        for(var direction:Direction.values()) {
            var positions=Geometry.section(BlockPos.ZERO,direction);assertEquals(9,positions.size());
            assertTrue(positions.stream().allMatch(p -> direction.getAxis().choose(p.getX(),p.getY(),p.getZ())==0));
        }
    }
}
