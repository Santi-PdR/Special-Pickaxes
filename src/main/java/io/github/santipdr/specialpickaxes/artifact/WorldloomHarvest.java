package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Harvest exposed natural ore, never reveal or convert buried deposits. */
public final class WorldloomHarvest implements WorkProgram {
    private static final List<BlockPos> OFFSETS=offsets();
    private static final Set<UUID> STARTING=new HashSet<>();
    private final BlockPos center;private final int limit;private int index,mined,visualTick=Integer.MIN_VALUE;
    public WorldloomHarvest(BlockPos center){this.center=center.immutable();limit=ArtifactConfig.JOB_LIMIT.get();}
    private static List<BlockPos> offsets(){
        var cells=new ArrayList<BlockPos>();
        for(int x=-12;x<=12;x++)for(int y=-12;y<=12;y++)for(int z=-12;z<=12;z++)if(x*x+z*z<=144)cells.add(new BlockPos(x,y,z));
        cells.sort(Comparator.comparingDouble((BlockPos p)->p.distSqr(BlockPos.ZERO)).thenComparingLong(BlockPos::asLong));return List.copyOf(cells);
    }
    public static List<BlockPos> scanOffsets(){return OFFSETS;}
    public static boolean exposed(ServerPlayer p,BlockPos at){
        var level=p.serverLevel();
        for(var direction:Direction.values()){
            var neighbor=at.relative(direction);
            if(!level.isOutsideBuildHeight(neighbor)&&level.hasChunkAt(neighbor)&&level.getBlockState(neighbor).isAir())return true;
        }return false;
    }
    public static boolean start(ServerPlayer p,ItemStack tool){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||tool.isEmpty()||p.getMainHandItem()!=tool)return false;
            var aim=ArtifactActions.target(p);if(aim.isEmpty())return false;var at=aim.get();var context=HeldUtilityContext.capture(p,tool);
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.WORLDLOOM,at);
            return allowed&&context.valid()&&WorkQueue.startRegion(p,tool,ArtifactKind.WORLDLOOM,new WorldloomHarvest(at));
        }finally{STARTING.remove(p.getUUID());}
    }
    private boolean eligible(ServerPlayer p,BlockPos at){
        var level=p.serverLevel();return !level.isOutsideBuildHeight(at)&&level.hasChunkAt(at)
                &&ArtifactOres.isOre(level.getBlockState(at))&&!PlayerPlacedBlocks.get(level).contains(at)&&exposed(p,at);
    }
    public int remaining(){return done()?0:OFFSETS.size()-index;}
    public boolean done(){return index>=OFFSETS.size()||mined>=limit;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}
    public void confirm(){}public boolean loaded(ServerPlayer p){return true;}public boolean reportPartial(){return false;}
    public int attemptsPerTick(ItemStack tool){return Math.max(1,ArtifactConfig.ORE_SCAN_BUDGET.get());}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=center.offset(OFFSETS.get(index));
        return eligible(p,at)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,at,p.serverLevel().getBlockState(at));
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("harvest exhausted");var at=center.offset(OFFSETS.get(index++));
        return new WorkStep(){public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                if(!eligible(actor,at))return false;var state=actor.serverLevel().getBlockState(at);
                // Exposure can change during external permission callbacks, too.
                boolean result=WorldSafety.mineNaturalLeased(actor,tool,ArtifactKind.WORLDLOOM,at,state,()->exposed(actor,at));
                if(result){mined++;int now=actor.server.getTickCount();if(visualTick==Integer.MIN_VALUE||now-visualTick>=8){visualTick=now;AbilityVfx.worldloomStrands(actor,at);}}
                return result;
            }};
    }
}
