package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/** Deterministic alternate excavation geometry; no world access or per-cast cell list. */
public final class ExcavationVolume {
    public enum Shape {
        VAULT(2832), CHIMNEY(5808);
        public final int volume;
        Shape(int volume){this.volume=volume;}
    }
    private ExcavationVolume(){}

    public static BlockPos cell(BlockPos origin,Direction forward,Shape shape,int index){
        if(index<0||index>=shape.volume)throw new IllegalArgumentException("excavation cell");
        if(forward.getAxis()==Direction.Axis.Y)throw new IllegalArgumentException("horizontal gallery direction required");
        // Visit the aimed block immediately, preserving a bijection with the linear plan.
        int originIndex=shape==Shape.VAULT?4:60;
        int linear=index==0?originIndex:index==originIndex?0:index;
        if(shape==Shape.CHIMNEY)return origin.offset(linear%11-5,linear/121,(linear/11)%11-5);
        int depth=linear/59,slot=linear%59;
        int side=slot<54?slot%9-4:slot-54-2;
        int height=slot<54?slot/9:6;
        return origin.relative(forward,depth).relative(forward.getClockWise(),side).above(height);
    }
}
