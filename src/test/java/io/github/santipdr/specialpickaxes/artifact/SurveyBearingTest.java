package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class SurveyBearingTest {
    private static final UUID OWNER=UUID.fromString("50fb5903-dd64-45af-9b15-75846d6fd58c");
    private static CompoundTag sample(BlockPos target){
        var tag=new CompoundTag();tag.putUUID(SurveyBearing.OWNER,OWNER);
        tag.putLong(SurveyBearing.POSITION,target.asLong());tag.putLong(SurveyBearing.UNTIL,1200);
        tag.putString(SurveyBearing.DIMENSION,"minecraft:overworld");return tag;
    }
    @Test void eightCardinalBearingsAndDirectVerticalTargetsAreStable(){
        int[][] offsets={{0,-8},{8,-8},{8,0},{8,8},{0,8},{-8,8},{-8,0},{-8,-8}};
        var observer=new Vec3(.5,64,.5);
        for(int i=0;i<offsets.length;i++){
            var tag=sample(new BlockPos(offsets[i][0],67,offsets[i][1]));
            var reading=SurveyBearing.read(tag,"minecraft:overworld",1000,observer,OWNER).orElseThrow();
            assertEquals(i,reading.direction());assertEquals(3,reading.vertical());assertEquals(200,reading.remaining());
        }
        var above=SurveyBearing.read(sample(new BlockPos(0,72,0)),"minecraft:overworld",1000,observer,OWNER).orElseThrow();
        assertEquals(8,above.direction());assertEquals(8,above.vertical());
    }
    @Test void movingObserverRecomputesDistanceAndBearingWithoutChangingTheDeposit(){
        var tag=sample(new BlockPos(10,64,0));
        var first=SurveyBearing.read(tag,"minecraft:overworld",1000,new Vec3(.5,64,.5),OWNER).orElseThrow();
        var second=SurveyBearing.read(tag,"minecraft:overworld",1010,new Vec3(20.5,65,.5),OWNER).orElseThrow();
        assertEquals(2,first.direction());assertEquals(6,second.direction());assertEquals(-1,second.vertical());
        assertEquals(first.target(),second.target());assertEquals(190,second.remaining());
    }
    @Test void expiredForgedFarFutureAndOverflowedDeadlinesAreHidden(){
        var tag=sample(BlockPos.ZERO);
        for(long until:new long[]{1000,999,1201,Long.MIN_VALUE,Long.MAX_VALUE}){
            tag.putLong(SurveyBearing.UNTIL,until);
            assertTrue(SurveyBearing.read(tag,"minecraft:overworld",1000,Vec3.ZERO,OWNER).isEmpty());
        }
        tag.putLong(SurveyBearing.UNTIL,Long.MAX_VALUE);
        assertTrue(SurveyBearing.read(tag,"minecraft:overworld",-1,Vec3.ZERO,OWNER).isEmpty());
    }
    @Test void foreignDimensionsOwnersMissingFieldsAndInvalidPositionsNeverRender(){
        var tag=sample(BlockPos.ZERO);
        assertTrue(SurveyBearing.read(tag,"minecraft:the_nether",1000,Vec3.ZERO,OWNER).isEmpty());
        assertTrue(SurveyBearing.read(tag,"minecraft:overworld",1000,Vec3.ZERO,UUID.randomUUID()).isEmpty());
        assertTrue(SurveyBearing.read(tag,"minecraft:overworld",1000,new Vec3(Double.NaN,0,0),OWNER).isEmpty());
        tag.putLong(SurveyBearing.POSITION,new BlockPos(10000,0,0).asLong());
        assertTrue(SurveyBearing.read(tag,"minecraft:overworld",1000,Vec3.ZERO,OWNER).isEmpty());
        tag.remove(SurveyBearing.POSITION);
        assertTrue(SurveyBearing.read(tag,"minecraft:overworld",1000,Vec3.ZERO,OWNER).isEmpty());
    }
}
