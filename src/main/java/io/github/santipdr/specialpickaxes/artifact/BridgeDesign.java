package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;

/** Worldloom architecture: vanilla stairs build an oriented ascent/descent; full blocks retain the flat bridge. */
public final class BridgeDesign {
    private BridgeDesign(){}
    public static boolean stairMaterial(BlockState state){
        var key=ForgeRegistries.BLOCKS.getKey(state.getBlock());
        return key!=null&&key.getNamespace().equals("minecraft")&&state.getBlock() instanceof StairBlock
                &&!state.hasBlockEntity()&&state.getFluidState().isEmpty();
    }
    /** Only a default, dry, straight, bottom stair with horizontal facing is accepted by the paid placer. */
    public static boolean allowedStair(BlockState state){
        return stairMaterial(state)&&state==state.getBlock().defaultBlockState().setValue(StairBlock.FACING,state.getValue(StairBlock.FACING));
    }
    public static int slope(float pitch,boolean stairs){return !stairs?0:Float.isFinite(pitch)&&pitch>35? -1:1;}
    public static BlockPos cell(BlockPos floor,Direction forward,int depth,int slope){
        if(depth<1||depth>64||Math.abs(slope)>1||forward.getAxis().isVertical())throw new IllegalArgumentException("bridge cell");
        return floor.relative(forward,depth).offset(0,slope*(depth-1),0);
    }
    /** A complete row is planned before enqueueing: an obstructed shoulder ends the road. */
    public static java.util.List<BlockPos> row(BlockPos floor,Direction forward,int depth,int slope,boolean wide){
        var center=cell(floor,forward,depth,slope);
        return wide?java.util.List.of(center,center.relative(forward.getCounterClockWise()),center.relative(forward.getClockWise())):java.util.List.of(center);
    }
    public static boolean clearance(net.minecraft.server.level.ServerLevel level,BlockPos at){
        return !level.isOutsideBuildHeight(at.above(2))&&level.hasChunkAt(at)&&level.hasChunkAt(at.above(2))
                &&level.getBlockState(at.above()).isAir()&&level.getBlockState(at.above(2)).isAir();
    }
}
