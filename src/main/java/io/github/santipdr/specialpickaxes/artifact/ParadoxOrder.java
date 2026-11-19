package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;

/** Twelve causal traversal orders. Legacy material mode indices are now axis order and polarity. */
public final class ParadoxOrder {
    private static final int[][] AXES={{1,0,2},{1,2,0},{0,1,2},{0,2,1},{2,0,1},{2,1,0}};
    private ParadoxOrder(){}
    public static BlockPos at(SelectionVolume volume,long index,int mode){
        if(index<0||index>=volume.size())throw new IndexOutOfBoundsException();
        int normalized=Math.floorMod(mode,12);if(normalized>=6)index=volume.size()-1-index;
        long[] lengths={volume.width(),volume.height(),volume.depth()};int[] axes=AXES[normalized%6];int[] out=new int[3];
        for(int axis:axes){out[axis]=(int)(index%lengths[axis]);index/=lengths[axis];}
        return volume.min().offset(out[0],out[1],out[2]);
    }
}
