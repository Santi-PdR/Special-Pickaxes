package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;

/** Inclusive integer cuboid. Long arithmetic before multiplication; no world reads. */
public record SelectionVolume(BlockPos min,BlockPos max) {
    public SelectionVolume {
        var a=min;var b=max;
        min=new BlockPos(Math.min(a.getX(),b.getX()),Math.min(a.getY(),b.getY()),Math.min(a.getZ(),b.getZ()));
        max=new BlockPos(Math.max(a.getX(),b.getX()),Math.max(a.getY(),b.getY()),Math.max(a.getZ(),b.getZ()));
    }
    public long width(){return (long)max.getX()-min.getX()+1;}
    public long height(){return (long)max.getY()-min.getY()+1;}
    public long depth(){return (long)max.getZ()-min.getZ()+1;}
    public long size(){try{return Math.multiplyExact(Math.multiplyExact(width(),height()),depth());}catch(ArithmeticException e){return Long.MAX_VALUE;}}
    public boolean contains(BlockPos p){return p.getX()>=min.getX()&&p.getX()<=max.getX()&&p.getY()>=min.getY()&&p.getY()<=max.getY()&&p.getZ()>=min.getZ()&&p.getZ()<=max.getZ();}
    public boolean overlaps(SelectionVolume b){return min.getX()<=b.max.getX()&&max.getX()>=b.min.getX()&&min.getY()<=b.max.getY()&&max.getY()>=b.min.getY()&&min.getZ()<=b.max.getZ()&&max.getZ()>=b.min.getZ();}
    /** Chunk-friendly traversal: Y, then X, then Z. */
    public BlockPos at(long index){if(index<0||index>=size())throw new IndexOutOfBoundsException();return min.offset((int)(index/height()%width()),(int)(index%height()),(int)(index/(height()*width())));}
    public String dimensions(){return width()+" × "+height()+" × "+depth();}
    public enum Transform {
        IDENTITY,MIRROR_X,MIRROR_Z,ROTATE_90,ROTATE_180,ROTATE_270;
        public boolean compatible(SelectionVolume a,SelectionVolume b){boolean turn=this==ROTATE_90||this==ROTATE_270;return a.height()==b.height()&&(turn?a.width()==b.depth()&&a.depth()==b.width():a.width()==b.width()&&a.depth()==b.depth());}
        public BlockPos map(SelectionVolume a,SelectionVolume b,BlockPos p){
            if(!compatible(a,b)||!a.contains(p))throw new IllegalArgumentException("incompatible volume");
            int x=p.getX()-a.min.getX(),y=p.getY()-a.min.getY(),z=p.getZ()-a.min.getZ(),w=(int)a.width(),d=(int)a.depth();
            return b.min.offset(switch(this){case MIRROR_X,ROTATE_180->w-1-x;case ROTATE_90->d-1-z;case ROTATE_270->z;default->x;},y,
                switch(this){case MIRROR_Z,ROTATE_180->d-1-z;case ROTATE_90->x;case ROTATE_270->w-1-x;default->z;});
        }
    }
}
