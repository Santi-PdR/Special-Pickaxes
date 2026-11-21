package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Budgeted seismic fractures: broad horizontal fan, separated vertical faults, and a Curios cross. */
public final class ResonantFault implements WorkProgram {
    public enum Form { FAN, CASCADE, REPLICA }
    private record Streak(EquipmentLease<ItemStack> source,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,int at,int count){}
    private static final Map<UUID,Streak> STREAKS=new HashMap<>();
    private static final Set<UUID> STARTING=new HashSet<>();
    private final List<BlockPos> cells;private final BlockPos center;private final Direction forward;
    private final Form form;private final EquipmentLease<ItemStack> source;private final int limit;
    private int index,lastVisualIndex,lastVisualTick=Integer.MIN_VALUE;
    public ResonantFault(BlockPos center,Direction forward,Form form){this(center,forward,form,null);}
    ResonantFault(BlockPos center,Direction forward,Form form,EquipmentLease<ItemStack> source){
        this.center=center.immutable();this.forward=forward;this.form=form;this.source=source;
        cells=geometry(center,forward,form);limit=Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get());
    }
    public static List<BlockPos> geometry(BlockPos origin,Direction forward,Form form){
        if(forward.getAxis().isVertical())throw new IllegalArgumentException("horizontal fault orientation");
        var offsets=new ArrayList<BlockPos>();
        if(form==Form.FAN){
            for(int depth=0;depth<32;depth++)for(int side=-Math.min(24,2+depth);side<=Math.min(24,2+depth);side++)
                for(int y=-2;y<=2;y++)offsets.add(new BlockPos(side,y,depth));
        }else if(form==Form.CASCADE){
            for(int depth=0;depth<=18;depth+=6)for(int side=-20;side<=20;side++)for(int y=-12;y<=12;y++)offsets.add(new BlockPos(side,y,depth));
        }else{
            for(int n=-8;n<=8;n++)for(int y=-1;y<=1;y++){
                offsets.add(new BlockPos(n,y,0));if(n!=0)offsets.add(new BlockPos(0,y,n));
            }
        }
        // Advance one wave/plane at a time. Each starts at its center, not at the far edge.
        offsets.sort(Comparator.comparingInt((BlockPos p)->form==Form.REPLICA?0:p.getZ())
                .thenComparingDouble(p->p.distSqr(BlockPos.ZERO)).thenComparingLong(BlockPos::asLong));
        var right=forward.getClockWise();var result=new ArrayList<BlockPos>(offsets.size());
        for(var p:offsets)result.add(origin.relative(right,p.getX()).relative(forward,p.getZ()).above(p.getY()));
        return List.copyOf(result);
    }
    public static boolean start(ServerPlayer p,ItemStack tool,Form form){
        if(form==Form.REPLICA||!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()
                    ||!(tool.getItem() instanceof ArtifactItem item)||ArtifactSkills.feedbackKind(tool,item.kind)!=ArtifactKind.CHOIR)return false;
            var aimed=ArtifactActions.target(p);if(aimed.isEmpty())return false;
            var at=aimed.get();var level=p.serverLevel();var context=HeldUtilityContext.capture(p,tool);var heading=p.getDirection();
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.CHOIR,at);
            return allowed&&context.valid()&&level.hasChunkAt(at)&&NaturalExcavationProgram.eligible(level.getBlockState(at))
                    &&!PlayerPlacedBlocks.get(level).contains(at)&&WorkQueue.startRegion(p,tool,ArtifactKind.CHOIR,new ResonantFault(at,heading,form));
        }finally{STARTING.remove(p.getUUID());}
    }
    /** Called only after a manually mined natural block has become air. No queued-break recursion. */
    public static boolean observed(ServerPlayer p,ItemStack tool,BlockPos scar,EquipmentLease<ItemStack> source){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!source.active()||source.expected().isEmpty()||!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty())return false;
            int now=p.server.getTickCount();var old=STREAKS.get(p.getUUID());
            STREAKS.entrySet().removeIf(e->now-e.getValue().at>100||now-e.getValue().at<0);
            if(old==null&&STREAKS.size()>=256)return false;
            int count=old!=null&&old.source.active()&&old.source.expected()==source.expected()&&old.dimension==p.level().dimension()&&now-old.at>=0&&now-old.at<=100?old.count+1:1;
            if(count<6){STREAKS.put(p.getUUID(),new Streak(source,p.level().dimension(),now,count));return false;}
            STREAKS.remove(p.getUUID());var state=ArtifactState.of(p,ArtifactKind.CHOIR);long time=ArtifactState.now(p);
            if(WorkQueue.busy(p)||PersistedCooldown.active(time,state.getLong("curiosFaultReady"),160))return false;
            var context=HeldUtilityContext.capture(p,tool);var heading=p.getDirection();
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.CHOIR,scar);
            if(!allowed||!context.valid()||!source.active()||source.expected().isEmpty()||!p.serverLevel().hasChunkAt(scar)||!p.serverLevel().getBlockState(scar).isAir())return false;
            if(!WorkQueue.startRegion(p,tool,ArtifactKind.CHOIR,new ConnectedExcavation(p,scar,ArtifactKind.CHOIR,false,source)))return false;
            state.putLong("curiosFaultReady",time+160);return true;
        }finally{STARTING.remove(p.getUUID());}
    }
    public static void forget(ServerPlayer p){STREAKS.remove(p.getUUID());}
    public static void clear(){STREAKS.clear();}
    private boolean active(){return source==null||source.active()&&!source.expected().isEmpty();}
    public int remaining(){return active()?limit-index:0;}
    public boolean awaiting(){return false;}
    public boolean executing(){return true;}
    public boolean done(){return index>=limit||!active();}
    public void confirm(){}
    public boolean loaded(ServerPlayer p){return true;}
    public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=cells.get(index);var level=p.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
        var state=level.getBlockState(at);
        return NaturalExcavationProgram.eligible(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.CHOIR,at,state);
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("fault exhausted");var at=cells.get(index++);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();if(!active()||level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);if(!NaturalExcavationProgram.eligible(state))return false;
                return source==null?WorldSafety.mineNaturalQueued(actor,tool,ArtifactKind.CHOIR,at,state):
                        WorldSafety.mineNaturalLeased(actor,tool,ArtifactKind.CHOIR,at,state,ResonantFault.this::active);
            }
        };
    }
    public void reportProgress(ServerPlayer p){
        int tick=p.server.getTickCount();
        if(!active()||index==0||index<=lastVisualIndex||lastVisualTick!=Integer.MIN_VALUE&&tick-lastVisualTick<8)return;
        lastVisualIndex=index;lastVisualTick=tick;AbilityVfx.faultWave(p,center,forward,cells.get(index-1),form==Form.CASCADE);
    }
}
