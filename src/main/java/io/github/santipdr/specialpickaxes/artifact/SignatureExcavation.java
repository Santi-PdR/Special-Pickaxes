package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Bounded, source-specific held cuts. Every inspected cell shares the existing queue budget. */
public final class SignatureExcavation implements WorkProgram {
    private final List<BlockPos> cells;
    private int index;
    private SignatureExcavation(List<BlockPos> cells){this.cells=cells;}
    public static boolean handles(ArtifactKind kind){return switch(kind){
        case PALIMPSEST,CHOIR,EVENTIDE,CRUCIBLE,INTERREGNUM,WORLDLOOM,ICARUS,AXIOM,EXODIUM,HELLSPEC,WORLDBREAKER->true;
        default->false;
    };}
    public static List<BlockPos> footprint(BlockPos seed,Direction facing,ArtifactKind kind,boolean alternate){
        var normal=facing;var side=normal.getAxis()==Direction.Axis.Y?Direction.EAST:normal.getClockWise();
        var up=normal.getAxis()==Direction.Axis.Y?Direction.SOUTH:Direction.UP;
        int width=switch(kind){case PALIMPSEST->4;case EVENTIDE,WORLDLOOM->5;case CHOIR->3;default->2;};
        int height=switch(kind){case HELLSPEC->4;case EXODIUM->5;case PALIMPSEST->3;default->2;};
        int depth=switch(kind){case AXIOM->8;case INTERREGNUM->12;case CRUCIBLE->4;case HELLSPEC->3;case WORLDBREAKER->4;default->2;};
        if(alternate){width++;depth++;}
        var result=new ArrayList<BlockPos>();result.add(seed.immutable());
        for(int d=0;d<depth;d++)for(int y=-height;y<=height;y++)for(int x=-width;x<=width;x++){
            boolean include=switch(kind){
                case PALIMPSEST->d==0; // Broad single surface.
                case CHOIR->(x==0||y==0)&&d<2; // Orthogonal fracture, never a remote ring.
                case EVENTIDE->Math.abs(x)+Math.abs(y)<=width-d; // Tapered fan.
                case CRUCIBLE->Math.abs(x)<=d+1&&Math.abs(y)<=d+1; // Expanding causal wedge.
                case INTERREGNUM->x==0&&y==0; // Surgical single line, no invisible field.
                case ICARUS->y==0&&(d==0||Math.abs(x)>=1); // Horizontal wing clearance.
                case WORLDLOOM->y<=0&&d<2; // Downward terrace.
                case AXIOM->Math.abs(x)+Math.abs(y)<=1; // Narrow deep core.
                case EXODIUM->y>=0&&Math.abs(x)<=2; // Roof dismantling.
                case HELLSPEC->Math.abs(x)<=1; // Tall compact hard-rock cut.
                case WORLDBREAKER->x*x+y*y+d*d<=16; // Compact breaching mass.
                default->false;
            };
            if(include&&(d!=0||x!=0||y!=0))result.add(seed.relative(normal,d).relative(side,x).relative(up,y));
        }
        Comparator<BlockPos> order=switch(kind){
            case EXODIUM->Comparator.comparingInt((BlockPos p)->p.getY()).reversed().thenComparingDouble(p->p.distSqr(seed));
            case WORLDLOOM->Comparator.comparingInt((BlockPos p)->p.getY()).thenComparingDouble(p->p.distSqr(seed));
            default->Comparator.comparingDouble(p->p.distSqr(seed));
        };
        result.subList(1,result.size()).sort(order);
        return List.copyOf(result.subList(0,Math.min(alternate?384:256,result.size())));
    }
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind kind,boolean alternate){
        if(!handles(kind)||WorkQueue.busy(p)||p.getMainHandItem()!=tool||tool.isEmpty())return false;
        var context=HeldUtilityContext.capture(p,tool);var aim=p.getLookAngle();var target=ConnectedExcavation.closeTarget(p);if(target.isEmpty())return false;var at=target.get();
        if(!WorldSafety.harvestable(p,tool,at)||!NaturalExcavationProgram.eligible(p.serverLevel().getBlockState(at))||PlayerPlacedBlocks.get(p.serverLevel()).contains(at))return false;
        if(!WorldSafety.allowed(p,kind,at)||!context.valid()||!p.getLookAngle().equals(aim)||!WorldSafety.harvestable(p,tool,at)
                ||!NaturalExcavationProgram.eligible(p.serverLevel().getBlockState(at))||PlayerPlacedBlocks.get(p.serverLevel()).contains(at))return false;
        if(!WorkQueue.startRegion(p,tool,kind,new SignatureExcavation(footprint(at,Direction.getNearest(aim.x,aim.y,aim.z),kind,alternate))))return false;
        ArtifactInteraction.clear(p);tool.getOrCreateTag().putString("artifactStatus","idle");return true;
    }
    public int remaining(){return cells.size()-index;}
    public boolean done(){return index>=cells.size();}
    public boolean awaiting(){return false;}
    public boolean executing(){return true;}
    public void confirm(){}
    public boolean loaded(ServerPlayer p){return true;}
    public boolean reportPartial(){return false;}
    public int attemptsPerTick(ItemStack tool){return 16;}
    public WorkStep next(ServerPlayer p){var at=cells.get(index++);return new WorkStep(){
        public BlockPos pos(){return at;}
        public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
            if(!actor.serverLevel().hasChunkAt(at))return false;
            var state=actor.serverLevel().getBlockState(at);
            return NaturalExcavationProgram.eligible(state)&&WorldSafety.mineNaturalQueued(actor,tool,kind,at,state);
        }
    };}
}
