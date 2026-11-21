package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Natural excavation only: a causal lattice, or echoes around four confirmed manual scars. */
public final class ParadoxLattice implements WorkProgram {
    private record Memory(EquipmentLease<ItemStack> source,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,int at,List<BlockPos> scars){}
    private static final Map<UUID,Memory> MEMORIES=new HashMap<>();
    private static final Set<UUID> STARTING=new HashSet<>();
    private final List<BlockPos> cells;private final EquipmentLease<ItemStack> source;private final int limit;
    private int index,visualTick=Integer.MIN_VALUE;
    public ParadoxLattice(BlockPos center){this(lattice(center),null);}
    ParadoxLattice(List<BlockPos> cells,EquipmentLease<ItemStack> source){this.cells=cells;this.source=source;limit=Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get());}
    public static List<BlockPos> lattice(BlockPos center){
        var offsets=new ArrayList<BlockPos>();offsets.add(BlockPos.ZERO);
        for(int x=-13;x<=13;x++)for(int y=-13;y<=13;y++)for(int z=-13;z<=13;z++)
            if((Math.abs(x)%3==1?1:0)+(Math.abs(y)%3==1?1:0)+(Math.abs(z)%3==1?1:0)>=2)offsets.add(new BlockPos(x,y,z));
        offsets.sort(Comparator.comparingDouble((BlockPos p)->p.distSqr(BlockPos.ZERO)).thenComparingLong(BlockPos::asLong));
        return offsets.stream().map(center::offset).toList();
    }
    public static List<BlockPos> echoes(List<BlockPos> scars){
        var unique=new LinkedHashSet<BlockPos>();
        for(var scar:scars)for(var cell:BlockPos.betweenClosed(scar.offset(-1,-1,-1),scar.offset(1,1,1)))unique.add(cell.immutable());
        return List.copyOf(unique);
    }
    public static boolean start(ServerPlayer p,ItemStack tool){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()
                    ||!(tool.getItem() instanceof ArtifactItem item)||ArtifactSkills.feedbackKind(tool,item.kind)!=ArtifactKind.CRUCIBLE)return false;
            var aimed=ArtifactActions.target(p);if(aimed.isEmpty())return false;var at=aimed.get();var level=p.serverLevel();var context=HeldUtilityContext.capture(p,tool);
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.CRUCIBLE,at);
            if(!allowed||!context.valid()||!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))
                    ||PlayerPlacedBlocks.get(level).contains(at))return false;
            if(!WorkQueue.startRegion(p,tool,ArtifactKind.CRUCIBLE,new ParadoxLattice(at)))return false;
            ArtifactInteraction.clear(p); // Alternate mining must end corner-selection's zero-speed lock.
            return true;
        }finally{STARTING.remove(p.getUUID());}
    }
    public static boolean observed(ServerPlayer p,ItemStack tool,BlockPos scar,EquipmentLease<ItemStack> source){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()||!source.active()||source.expected().isEmpty())return false;
            int now=p.server.getTickCount();MEMORIES.entrySet().removeIf(e->now-e.getValue().at>100||now-e.getValue().at<0);
            var old=MEMORIES.get(p.getUUID());if(old==null&&MEMORIES.size()>=256)return false;
            var scars=new ArrayList<BlockPos>();
            if(old!=null&&old.source.active()&&old.source.expected()==source.expected()&&old.dimension==p.level().dimension())scars.addAll(old.scars);
            scars.add(scar.immutable());
            if(scars.size()<4){MEMORIES.put(p.getUUID(),new Memory(source,p.level().dimension(),now,List.copyOf(scars)));return false;}
            MEMORIES.remove(p.getUUID());var state=ArtifactState.of(p,ArtifactKind.CRUCIBLE);long time=ArtifactState.now(p);
            if(WorkQueue.busy(p)||PersistedCooldown.active(time,state.getLong("curiosParadoxReady"),160))return false;
            var context=HeldUtilityContext.capture(p,tool);boolean allowed=WorldSafety.allowed(p,ArtifactKind.CRUCIBLE,scar);
            if(!allowed||!context.valid()||!source.active()||source.expected().isEmpty()||!p.serverLevel().hasChunkAt(scar)||!p.serverLevel().getBlockState(scar).isAir())return false;
            if(!WorkQueue.startRegion(p,tool,ArtifactKind.CRUCIBLE,new ConnectedExcavation(p,scar,ArtifactKind.CRUCIBLE,false,source)))return false;
            state.putLong("curiosParadoxReady",time+160);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
    public static void forget(ServerPlayer p){MEMORIES.remove(p.getUUID());}
    public static void clear(){MEMORIES.clear();}
    private boolean active(){return source==null||source.active()&&!source.expected().isEmpty();}
    public int remaining(){return active()?limit-index:0;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return index>=limit||!active();}
    public void confirm(){}public boolean loaded(ServerPlayer p){return true;}public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=cells.get(index);var level=p.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
        var state=level.getBlockState(at);return NaturalExcavationProgram.eligible(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,at,state);
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("causal lattice exhausted");var at=cells.get(index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();if(!active()||level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);if(!NaturalExcavationProgram.eligible(state))return false;
                return source==null?WorldSafety.mineNaturalQueued(actor,tool,ArtifactKind.CRUCIBLE,at,state):WorldSafety.mineNaturalLeased(actor,tool,ArtifactKind.CRUCIBLE,at,state,ParadoxLattice.this::active);
            }
        };
    }
    public void reportProgress(ServerPlayer p){int now=p.server.getTickCount();if(active()&&index>0&&(visualTick==Integer.MIN_VALUE||now-visualTick>=8)){visualTick=now;AbilityVfx.paradoxKnot(p,cells.get(index-1),true);}}
}
