package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Breadth-first vein mining with bounded, incremental discovery and extraction. */
public final class ConnectedVeinProgram implements WorkProgram {
    private final BlockPos origin;
    private final Block ore;
    private final int limit;
    private final ArrayDeque<BlockPos> frontier=new ArrayDeque<>();
    private final Set<BlockPos> seen=new HashSet<>();
    private int accepted;

    public ConnectedVeinProgram(BlockPos origin,BlockState initial,int limit){
        this.origin=origin.immutable();this.ore=initial.getBlock();this.limit=Math.max(1,limit);
        frontier.add(this.origin);seen.add(this.origin);
    }

    @Override public int remaining(){return Math.max(0,limit-accepted);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return frontier.isEmpty()||accepted>=limit;}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // Skip unloaded neighbors instead of loading their chunks.

    @Override public boolean backpressured(ServerPlayer p){
        BlockPos pos=frontier.peekFirst();if(pos==null||!p.serverLevel().hasChunkAt(pos)||!WorldSafety.dropPressure(p,pos))return false;
        BlockState state=p.serverLevel().getBlockState(pos);
        return state.getBlock()==ore&&WorldSafety.allowed(p,ArtifactKind.PALIMPSEST,pos)
                &&!WorldSafety.barrier(p,pos)&&WorldSafety.harvestable(p,p.getMainHandItem(),pos);
    }

    @Override public WorkStep next(ServerPlayer p){
        BlockPos pos=frontier.removeFirst();
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){
                var level=player.serverLevel();
                if(!level.hasChunkAt(pos))return false;
                BlockState state=level.getBlockState(pos);
                if(state.getBlock()!=ore||!WorldSafety.allowed(player,kind,pos)||WorldSafety.barrier(player,pos))return false;
                accepted++;
                if(accepted<limit)for(Direction direction:Direction.values()){
                    BlockPos next=pos.relative(direction);
                    if(next.distSqr(origin)<=64&&seen.add(next.immutable())&&level.hasChunkAt(next)
                            &&level.getBlockState(next).getBlock()==ore)frontier.addLast(next.immutable());
                }
                return WorldSafety.mine(player,tool,kind,pos,state);
            }
        };
    }
}
