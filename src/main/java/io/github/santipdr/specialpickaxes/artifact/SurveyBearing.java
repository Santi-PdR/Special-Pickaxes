package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;

/** Pure display decoder. Coordinates are NEVER accepted back as mining authority. */
public final class SurveyBearing {
    public static final int DURATION=200,RANGE=48;
    public static final String POSITION="surveyGuidePos",UNTIL="surveyGuideUntil",DIMENSION="surveyGuideDimension",ORE="surveyGuideOre",OWNER="surveyGuideOwner";
    public record Reading(BlockPos target,int direction,int distance,int vertical,int remaining){}
    private SurveyBearing(){}
    public static Optional<Reading> read(CompoundTag tag,String dimension,long now,Vec3 observer,java.util.UUID viewer){
        if(!tag.hasUUID(OWNER)||!tag.getUUID(OWNER).equals(viewer)||!tag.contains(POSITION,Tag.TAG_LONG)||!tag.contains(UNTIL,Tag.TAG_LONG)
                ||!dimension.equals(tag.getString(DIMENSION)))return Optional.empty();
        long until=tag.getLong(UNTIL),remaining=until-now;
        if(until<=now||remaining<=0||remaining>DURATION)return Optional.empty();
        var target=BlockPos.of(tag.getLong(POSITION));var delta=Vec3.atCenterOf(target).subtract(observer);
        double squared=delta.lengthSqr();
        if(!Double.isFinite(squared)||squared>RANGE*RANGE)return Optional.empty();
        double horizontal=delta.x*delta.x+delta.z*delta.z;
        int direction=horizontal<.01?8:Math.floorMod((int)Math.round(Math.atan2(delta.x,-delta.z)/(Math.PI/4)),8);
        return Optional.of(new Reading(target,direction,(int)Math.ceil(Math.sqrt(squared)),
                (int)Math.round(target.getY()-Math.floor(observer.y)),(int)remaining));
    }
}
