package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Five broad impact bowls, excavated top-down with native rock and ore loot. No explosions or entity damage. */
public final class ExodiumMeteorShower implements WorkProgram {
    private static final java.util.List<BlockPos> OFFSETS=offsets();
    public static final int VOLUME=OFFSETS.size();
    private final BlockPos center;
    private final ArtifactKind kind=ArtifactKind.EXODIUM;
    private int index,mined,visualTick=Integer.MIN_VALUE;
    public ExodiumMeteorShower(BlockPos center){this.center=center.immutable();}
    public static boolean contains(int x,int y,int z){
        if(y>0||y < -10)return false;
        // A deep central impact and four satellite bowls. Integer paraboloids;
        // each column opens from the surface downward, never a buried tunnel.
        return bowl(x,y,z,0,0,10,10)||bowl(x,y,z,12,0,7,6)||bowl(x,y,z,-12,0,7,6)
                ||bowl(x,y,z,0,12,7,6)||bowl(x,y,z,0,-12,7,6);
    }
    private static boolean bowl(int x,int y,int z,int cx,int cz,int radius,int depth){
        int dx=x-cx,dz=z-cz;
        return y>=-depth&&(dx*dx+dz*dz)*depth<=radius*radius*(depth+y);
    }
    private static java.util.List<BlockPos> offsets(){
        var out=new java.util.ArrayList<BlockPos>();
        for(int y=0;y>=-10;y--)for(int x=-19;x<=19;x++)for(int z=-19;z<=19;z++)
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
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.EXODIUM,at);
            return allowed&&context.valid()&&level.hasChunkAt(at)&&NaturalExcavationProgram.eligible(level.getBlockState(at))&&!PlayerPlacedBlocks.get(level).contains(at)
                    &&WorkQueue.startRegion(p,tool,ArtifactKind.EXODIUM,new ExodiumMeteorShower(at));
        }finally{STARTING.remove(p.getUUID());}
    }
    public static BlockPos cell(BlockPos center,int index){
        if(index<0||index>=VOLUME)throw new IllegalArgumentException("meteor shower cell");
        return center.offset(OFFSETS.get(index));
    }
    private int limit(){return Math.min(VOLUME,ArtifactConfig.JOB_LIMIT.get());}
    public void reportProgress(ServerPlayer p){
        int now=p.server.getTickCount();
        if(index>0&&(visualTick==Integer.MIN_VALUE||now-visualTick>=8)){
            visualTick=now;AbilityVfx.meteorImpact(p,cell(center,index-1));
        }
    }
    public int remaining(){return done()?0:VOLUME-index;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return index>=VOLUME||mined>=limit();}
    public void confirm(){}public boolean loaded(ServerPlayer player){return true;}
    public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer player){
        if(done())return false;var at=cell(center,index);var level=player.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at))return false;
        var state=level.getBlockState(at);
        return NaturalExcavationProgram.eligible(state)
                &&!PlayerPlacedBlocks.get(level).contains(at)
                &&WorldSafety.backpressuredMine(player,player.getMainHandItem(),kind,at,state);
    }
    public WorkStep next(ServerPlayer player){
        if(done())throw new IllegalStateException("meteor shower exhausted");
        var at=cell(center,index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();
                if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);
                if(!NaturalExcavationProgram.eligible(state))return false;
                boolean result=WorldSafety.mineNaturalQueued(actor,tool,kind,at,state);
                if(result)mined++;return result;
            }
        };
    }
}
