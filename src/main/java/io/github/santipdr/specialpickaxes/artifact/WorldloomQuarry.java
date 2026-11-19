package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Top-down terraced quarry. Natural rock only; ore deposits remain exposed in place. */
public final class WorldloomQuarry implements WorkProgram {
    private static final java.util.List<BlockPos> OFFSETS=offsets();
    public static final int VOLUME=OFFSETS.size();
    private final BlockPos center;
    private final ArtifactKind kind=ArtifactKind.WORLDLOOM;
    private int index,visualTick=Integer.MIN_VALUE;
    public WorldloomQuarry(BlockPos center){this.center=center.immutable();}
    public static boolean contains(int x,int y,int z){
        int depth=-y;if(depth<0||depth>=12)return false;
        int radius=12-depth/3;return x*x+z*z<=radius*radius;
    }
    private static java.util.List<BlockPos> offsets(){
        var out=new java.util.ArrayList<BlockPos>();
        for(int y=0;y>-12;y--)for(int x=-12;x<=12;x++)for(int z=-12;z<=12;z++)
            if(contains(x,y,z))out.add(new BlockPos(x,y,z));
        out.sort(java.util.Comparator.comparingInt((BlockPos p)->-p.getY())
                .thenComparingDouble(p->p.distSqr(BlockPos.ZERO)).thenComparingLong(BlockPos::asLong));
        return java.util.List.copyOf(out);
    }
    private static final java.util.Set<java.util.UUID> STARTING=new java.util.HashSet<>();
    public static boolean start(ServerPlayer p,ItemStack tool){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||tool.isEmpty()||p.getMainHandItem()!=tool)return false;
            var aim=ArtifactActions.target(p);if(aim.isEmpty())return false;var at=aim.get();var context=HeldUtilityContext.capture(p,tool);var level=p.serverLevel();
            if(!level.hasChunkAt(at)||!MiningDesigns.seamGeology(level.getBlockState(at))||ArtifactOres.isOre(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.WORLDLOOM,at);
            return allowed&&context.valid()&&level.hasChunkAt(at)&&MiningDesigns.seamGeology(level.getBlockState(at))
                    &&!ArtifactOres.isOre(level.getBlockState(at))&&!PlayerPlacedBlocks.get(level).contains(at)
                    &&WorkQueue.startRegion(p,tool,ArtifactKind.WORLDLOOM,new WorldloomQuarry(at));
        }finally{STARTING.remove(p.getUUID());}
    }
    public static BlockPos cell(BlockPos center,int index){
        if(index<0||index>=VOLUME)throw new IllegalArgumentException("quarry cell");
        return center.offset(OFFSETS.get(index));
    }
    private int limit(){return Math.min(VOLUME,ArtifactConfig.JOB_LIMIT.get());}
    public void reportProgress(ServerPlayer p){
        int now=p.server.getTickCount();
        if(index>0&&(visualTick==Integer.MIN_VALUE||now-visualTick>=8)){
            visualTick=now;AbilityVfx.worldloomStrands(p,cell(center,index-1));
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
        return (MiningDesigns.seamGeology(state)&&!ArtifactOres.isOre(state))
                &&!PlayerPlacedBlocks.get(level).contains(at)
                &&WorldSafety.backpressuredMine(player,player.getMainHandItem(),kind,at,state);
    }
    public WorkStep next(ServerPlayer player){
        if(done())throw new IllegalStateException("quarry exhausted");
        var at=cell(center,index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();
                if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);
                if(!MiningDesigns.seamGeology(state)||ArtifactOres.isOre(state))return false;
                return WorldSafety.mineNaturalQueued(actor,tool,kind,at,state);
            }
        };
    }
}
