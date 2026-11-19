package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Temporal hourglass: broad polar chambers joined by a narrow waist; natural excavation only. */
public final class StasisHourglassProgram implements WorkProgram {
    private static final java.util.List<BlockPos> OFFSETS=offsets();
    public static final int VOLUME=OFFSETS.size();
    private final BlockPos center;
    private final ArtifactKind kind=ArtifactKind.INTERREGNUM;
    private final boolean harvestOres=true;
    private int index,visualTick=Integer.MIN_VALUE;
    public StasisHourglassProgram(BlockPos center){this.center=center.immutable();}
    public static boolean contains(int x,int y,int z){
        if(Math.abs(y)>12)return false;
        int radius=3+Math.abs(y)*3/4;
        return x*x+z*z<=radius*radius;
    }
    private static java.util.List<BlockPos> offsets(){
        var out=new java.util.ArrayList<BlockPos>();
        for(int y=-12;y<=12;y++)for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)
            if(contains(x,y,z))out.add(new BlockPos(x,y,z));
        out.sort(java.util.Comparator.comparingInt((BlockPos p)->Math.abs(p.getY()))
                .thenComparingDouble(p->p.distSqr(BlockPos.ZERO)).thenComparingLong(BlockPos::asLong));
        return java.util.List.copyOf(out);
    }
    public static BlockPos cell(BlockPos center,int index){
        if(index<0||index>=VOLUME)throw new IllegalArgumentException("hourglass cell");
        return center.offset(OFFSETS.get(index));
    }
    private int limit(){return Math.min(VOLUME,ArtifactConfig.JOB_LIMIT.get());}
    public void reportProgress(ServerPlayer p){
        int now=p.server.getTickCount();
        if(index>0&&(visualTick==Integer.MIN_VALUE||now-visualTick>=8)){
            visualTick=now;AbilityVfx.stasisHourglass(p,cell(center,index-1));
        }
    }
    public int remaining(){return limit()-index;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return index>=limit();}
    public void confirm(){}public boolean loaded(ServerPlayer player){return true;}
    public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer player){
        if(done())return false;var at=cell(center,index);var level=player.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at))return false;
        var state=level.getBlockState(at);
        return (MiningDesigns.matrix(state)||harvestOres&&ArtifactOres.isOre(state))
                &&!PlayerPlacedBlocks.get(level).contains(at)
                &&WorldSafety.backpressuredMine(player,player.getMainHandItem(),kind,at,state);
    }
    public WorkStep next(ServerPlayer player){
        if(done())throw new IllegalStateException("rift exhausted");
        var at=cell(center,index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();
                if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);
                if(!MiningDesigns.matrix(state)&&!(harvestOres&&ArtifactOres.isOre(state)))return false;
                return WorldSafety.mineNaturalQueued(actor,tool,kind,at,state);
            }
        };
    }
}
