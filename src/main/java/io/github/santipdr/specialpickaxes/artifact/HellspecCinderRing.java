package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Curios annular stone fracture after confirmed manual natural mining; preserves all ore. */
public final class HellspecCinderRing implements WorkProgram {
    private static final Set<UUID> STARTING=new HashSet<>();
    private final List<BlockPos> cells;
    private final EquipmentLease<ItemStack> source;
    private final int limit;
    private int index,visualTick=Integer.MIN_VALUE;
    HellspecCinderRing(BlockPos scar,EquipmentLease<ItemStack> source){
        cells=cells(scar);this.source=source;limit=Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get());
    }
    public static List<BlockPos> cells(BlockPos scar){
        var cells=new ArrayList<BlockPos>();
        for(int y=-1;y<=1;y++)for(int x=-5;x<=5;x++)for(int z=-5;z<=5;z++)
            if(x*x+z*z>=9&&x*x+z*z<=25)cells.add(scar.offset(x,y,z));
        cells.sort(Comparator.comparingDouble((BlockPos p)->p.distSqr(scar)).thenComparingLong(BlockPos::asLong));
        return List.copyOf(cells);
    }
    private static boolean rock(net.minecraft.world.level.block.state.BlockState state){return MiningDesigns.seamGeology(state)&&!ArtifactOres.isOre(state);}

    private boolean active(){return source.active()&&!source.expected().isEmpty();}
    public static boolean observed(ServerPlayer p,ItemStack tool,BlockPos scar,EquipmentLease<ItemStack> source){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()
                    ||!source.active()||source.expected().isEmpty()||WorkQueue.busy(p))return false;
            var state=ArtifactState.of(p,ArtifactKind.HELLSPEC);long now=ArtifactState.now(p);
            if(PersistedCooldown.active(now,state.getLong("curiosCinderReady"),160))return false;
            var context=HeldUtilityContext.capture(p,tool);boolean allowed=WorldSafety.allowed(p,ArtifactKind.HELLSPEC,scar);
            if(!allowed||!context.valid()||!source.active()||source.expected().isEmpty()||!p.serverLevel().hasChunkAt(scar)||!p.serverLevel().getBlockState(scar).isAir())return false;
            if(!WorkQueue.startRegion(p,tool,ArtifactKind.HELLSPEC,new HellspecCinderRing(scar,source)))return false;
            state.putLong("curiosCinderReady",now+160);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
    public int remaining(){return active()?limit-index:0;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return index>=limit||!active();}
    public void confirm(){}public boolean loaded(ServerPlayer p){return true;}public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=cells.get(index);var level=p.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
        var state=level.getBlockState(at);
        return rock(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.HELLSPEC,at,state);
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("cinder ring exhausted");var at=cells.get(index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();if(!active()||level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);if(!rock(state))return false;
                return WorldSafety.mineNaturalLeased(actor,tool,ArtifactKind.HELLSPEC,at,state,HellspecCinderRing.this::active);
            }
        };
    }
    public void reportProgress(ServerPlayer p){
        int now=p.server.getTickCount();if(active()&&index>0&&(visualTick==Integer.MIN_VALUE||now-visualTick>=8)){
            visualTick=now;AbilityVfx.infernalRift(p,cells.get(index-1));
        }
    }
}
