package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** The lift must not pull a miner back after they leave its one-block shaft or teleport away. */
public final class IcarianShaft {
    private IcarianShaft(){}
    public static boolean contains(BlockPos base,int rise,Vec3 feet){
        return rise>=0&&Double.isFinite(feet.x)&&Double.isFinite(feet.y)&&Double.isFinite(feet.z)
                &&feet.x>=base.getX()&&feet.x<base.getX()+1D
                &&feet.z>=base.getZ()&&feet.z<base.getZ()+1D
                // Tolerate ordinary jumping, partial-block footing and movement packet interpolation.
                &&Math.abs(feet.y-((double)base.getY()+rise))<=2.5D;
    }
}
