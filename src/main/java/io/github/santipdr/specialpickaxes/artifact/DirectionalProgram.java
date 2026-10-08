package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Bounded slice memory. Every slice is checked before excavation; any barrier terminates the entire route. */
public final class DirectionalProgram implements WorkProgram {
    public enum Shape { CARVE, FRACTURE, CLEAVE, CORE_DRILL, WORLD_SHATTER, ICARUS }
    private final BlockPos origin;private final Direction direction;private final Shape shape;private final int length;
    private int depth,index;private boolean digging,moving,finished;
    private List<BlockPos> slice;private BlockState[] expected;
    public DirectionalProgram(BlockPos origin,Direction direction,Shape shape){
        this.origin=origin.immutable();this.direction=shape==Shape.CORE_DRILL?Direction.DOWN:direction;this.shape=shape;
        length=switch(shape){case CARVE->64;case FRACTURE->48;case CLEAVE->9;case CORE_DRILL->192;case WORLD_SHATTER->96;case ICARUS->ArtifactConfig.BORE_LENGTH.get();};
        prepare();
    }
    private void prepare(){
        slice=section(origin,direction,shape,depth);
        expected=new BlockState[slice.size()];index=0;digging=false;moving=false;
    }
    /** Exact live geometry, also used by previews and deterministic shape regressions. */
    public static List<BlockPos> section(BlockPos origin,Direction direction,Shape shape,int depth){
        if(depth<0||depth>192)throw new IllegalArgumentException("slice depth");
        var slice=new ArrayList<BlockPos>();int w,h;
        switch(shape){
            case ICARUS->{w=3;h=2;}case CORE_DRILL->{w=2;h=2;}case CARVE->{w=3;h=3;}
            case FRACTURE->{w=Math.min(20,2+depth/2);h=3;}
            case CLEAVE->{w=18;h=8;}
            default->{w=Math.min(24,3+depth/3);h=Math.min(10,2+depth/10);}
        }
        for(int u=-w;u<=w;u++)for(int v=-h;v<=h;v++){
            if(shape==Shape.CARVE&&u*u+v*v>10)continue;
            if(shape==Shape.CLEAVE&&Math.abs(u)+Math.abs(v)*2>20)continue;
            if(shape==Shape.WORLD_SHATTER&&Math.abs(u)+Math.max(0,v)*2>w)continue;
            var c=origin.relative(direction,depth);
            var pos=switch(direction.getAxis()){case X->c.offset(0,v,u);case Y->c.offset(u,0,v);case Z->c.offset(u,v,0);};slice.add(pos);
        }
        return slice;
    }
    public int remaining(){return finished?0:(length-depth)*slice.size()*2;}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public boolean done(){return finished;}public void confirm(){}
    public boolean loaded(ServerPlayer p){return finished||p.serverLevel().hasChunkAt(moving?origin.relative(direction,depth):slice.get(index));}
    private void advance(){
        if(++index<slice.size())return;
        index=0;if(!digging){digging=true;return;}
        if(shape==Shape.ICARUS){moving=true;return;}nextSlice();
    }
    private void nextSlice(){if(++depth>=length){finished=true;return;}prepare();}
    public boolean backpressured(ServerPlayer p){return !finished&&!moving&&digging&&!expected[index].isAir()&&WorldSafety.dropPressure(p,slice.get(index));}
    public WorkStep next(ServerPlayer p){
        if(moving){var destination=origin.relative(direction,depth).below();nextSlice();return new WorkStep.Move(destination);}
        final int i=index;var at=slice.get(i);boolean mine=digging;
        if(!mine)expected[i]=p.serverLevel().getBlockState(at);var old=expected[i];advance();
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean stopOnFailure(){return true;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
                if(!WorldSafety.directionClear(actor,tool,kind,at)||actor.serverLevel().getBlockState(at)!=old)return false;
                return !mine||old.isAir()||WorldSafety.mine(actor,tool,kind,at,old);
            }
        };
    }
}
