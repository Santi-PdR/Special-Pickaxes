package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Destructive twilight horizons. No entity attraction, domain, ore search or player movement. */
public final class EventideRupture implements WorkProgram {
    public enum Form { ECLIPSE, CRESCENT, HORIZON, AFTERGLOW }
    private static final Set<UUID> STARTING=new HashSet<>();
    private final List<BlockPos> cells;private final BlockPos center;private final Direction forward;
    private final Form form;private final EquipmentLease<ItemStack> source;private final int limit;
    private int index,lastVisualIndex,lastVisualTick=Integer.MIN_VALUE;
    public EventideRupture(BlockPos center,Direction forward,Form form){this(center,forward,form,null);}
    EventideRupture(BlockPos center,Direction forward,Form form,EquipmentLease<ItemStack> source){
        this.center=center.immutable();this.forward=forward;this.form=form;this.source=source;
        cells=geometry(center,forward,form);limit=Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get());
    }
    public static List<BlockPos> geometry(BlockPos center,Direction forward,Form form){
        if(forward.getAxis().isVertical())throw new IllegalArgumentException("horizontal horizon orientation");
        int radius=form==Form.HORIZON?24:form==Form.AFTERGLOW?6:18;
        int inner=form==Form.HORIZON?15:form==Form.AFTERGLOW?2:0;
        int half=form==Form.HORIZON?2:form==Form.AFTERGLOW?1:3;
        var offsets=new ArrayList<BlockPos>();
        for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++)if(x*x+z*z>=inner*inner&&x*x+z*z<=radius*radius)
            for(int y=-half;y<=half;y++)offsets.add(new BlockPos(x,y,z));
        offsets.sort(Comparator.comparingInt((BlockPos p)->p.getX()*p.getX()+p.getZ()*p.getZ())
                .thenComparingInt(p->Math.abs(p.getY())).thenComparingLong(BlockPos::asLong));
        var right=forward.getClockWise();var cells=new ArrayList<BlockPos>(offsets.size());
        for(var p:offsets)cells.add(form==Form.CRESCENT?center.relative(right,p.getX()).above(p.getZ()).relative(forward,p.getY()):center.offset(p));
        return List.copyOf(cells);
    }
    private boolean active(){return source==null||source.active()&&!source.expected().isEmpty();}
    public static boolean start(ServerPlayer p,ItemStack tool,Form form){
        if(form==Form.AFTERGLOW||!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()
                    ||!(tool.getItem() instanceof ArtifactItem item)||ArtifactSkills.feedbackKind(tool,item.kind)!=ArtifactKind.EVENTIDE)return false;
            var aimed=ArtifactActions.target(p);if(aimed.isEmpty())return false;
            var at=aimed.get();var level=p.serverLevel();var context=HeldUtilityContext.capture(p,tool);var heading=p.getDirection();
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.EVENTIDE,at);
            return allowed&&context.valid()&&level.hasChunkAt(at)&&NaturalExcavationProgram.eligible(level.getBlockState(at))
                    &&!PlayerPlacedBlocks.get(level).contains(at)&&WorkQueue.startRegion(p,tool,ArtifactKind.EVENTIDE,new EventideRupture(at,heading,form));
        }finally{STARTING.remove(p.getUUID());}
    }
    /** One confirmed manual ore break can unleash a small annulus; no passive ore detection. */
    public static boolean observed(ServerPlayer p,ItemStack tool,BlockPos scar,EquipmentLease<ItemStack> source){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()
                    ||!source.active()||source.expected().isEmpty()||WorkQueue.busy(p))return false;
            var state=ArtifactState.of(p,ArtifactKind.EVENTIDE);long now=ArtifactState.now(p);
            if(PersistedCooldown.active(now,state.getLong("curiosAfterglowReady"),160))return false;
            var context=HeldUtilityContext.capture(p,tool);var heading=p.getDirection();boolean allowed=WorldSafety.allowed(p,ArtifactKind.EVENTIDE,scar);
            if(!allowed||!context.valid()||!source.active()||source.expected().isEmpty()||!p.serverLevel().hasChunkAt(scar)||!p.serverLevel().getBlockState(scar).isAir())return false;
            if(!WorkQueue.startRegion(p,tool,ArtifactKind.EVENTIDE,new EventideRupture(scar,heading,Form.AFTERGLOW,source)))return false;
            state.putLong("curiosAfterglowReady",now+160);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
    public int remaining(){return active()?limit-index:0;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return index>=limit||!active();}
    public void confirm(){}public boolean loaded(ServerPlayer p){return true;}public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=cells.get(index);var level=p.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
        var state=level.getBlockState(at);
        return NaturalExcavationProgram.eligible(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,at,state);
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("twilight horizon exhausted");var at=cells.get(index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();if(!active()||level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);if(!NaturalExcavationProgram.eligible(state))return false;
                return source==null?WorldSafety.mineNaturalQueued(actor,tool,ArtifactKind.EVENTIDE,at,state):
                        WorldSafety.mineNaturalLeased(actor,tool,ArtifactKind.EVENTIDE,at,state,EventideRupture.this::active);
            }
        };
    }
    public void reportProgress(ServerPlayer p){
        int tick=p.server.getTickCount();if(!active()||index==0||index<=lastVisualIndex||lastVisualTick!=Integer.MIN_VALUE&&tick-lastVisualTick<8)return;
        lastVisualIndex=index;lastVisualTick=tick;var delta=cells.get(index-1).subtract(center);
        double radius=form==Form.CRESCENT?Math.sqrt(delta.getX()*delta.getX()+delta.getZ()*delta.getZ()+delta.getY()*delta.getY()):Math.sqrt(delta.getX()*delta.getX()+delta.getZ()*delta.getZ());
        AbilityVfx.twilightHorizon(p,center,forward,radius,form==Form.CRESCENT,form==Form.HORIZON||form==Form.AFTERGLOW);
    }
}
