package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Breadth-first vein mining with bounded, incremental discovery and extraction. */
public final class ConnectedVeinProgram implements WorkProgram {
    private static final int VEIN_RADIUS=12;
    private static final int VEIN_RADIUS_SQUARED=VEIN_RADIUS*VEIN_RADIUS;
    private final BlockPos origin;
    private final Block ore;
    private final Set<net.minecraft.tags.TagKey<Block>> families;
    private final String registryFamily;
    private final int limit;
    private final ArrayDeque<BlockPos> frontier=new ArrayDeque<>();
    private final Set<BlockPos> seen=new HashSet<>();
    private int accepted;

    public ConnectedVeinProgram(BlockPos origin,BlockState initial,int limit){
        this.origin=origin.immutable();this.ore=initial.getBlock();this.families=ArtifactOres.veinFamilies(initial);
        this.registryFamily=ArtifactOres.registryOreFamily(this.ore);this.limit=Math.max(1,limit);
        frontier.add(this.origin);seen.add(this.origin);
    }

    private boolean sameOreFamily(BlockState state){
        if(state.getBlock()==ore||!families.isEmpty()&&state.getTags().anyMatch(families::contains))return true;
        return registryFamily!=null&&registryFamily.equals(ArtifactOres.registryOreFamily(state.getBlock()));
    }

    @Override public int remaining(){return Math.max(0,limit-accepted);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return frontier.isEmpty()||accepted>=limit;}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // Skip unloaded neighbors instead of loading their chunks.

    @Override public boolean backpressured(ServerPlayer p){
        BlockPos pos=frontier.peekFirst();if(pos==null||!p.serverLevel().hasChunkAt(pos))return false;
        BlockState state=p.serverLevel().getBlockState(pos);
        return sameOreFamily(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.PALIMPSEST,pos,state);
    }

    @Override public WorkStep next(ServerPlayer p){
        BlockPos pos=frontier.removeFirst();
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){
                var level=player.serverLevel();
                if(!level.hasChunkAt(pos))return false;
                BlockState state=level.getBlockState(pos);
                if(!sameOreFamily(state)||!WorldSafety.allowed(player,kind,pos)||WorldSafety.barrier(player,pos))return false;
                if(!WorldSafety.mineQueued(player,tool,kind,pos,state))return false;
                accepted++;
                if(accepted<limit)for(int dx=-1;dx<=1;dx++)for(int dy=-1;dy<=1;dy++)for(int dz=-1;dz<=1;dz++){
                    if(dx==0&&dy==0&&dz==0)continue;
                    BlockPos next=pos.offset(dx,dy,dz).immutable();
                    if(next.distSqr(origin)<=VEIN_RADIUS_SQUARED&&seen.add(next)&&level.hasChunkAt(next)
                            &&sameOreFamily(level.getBlockState(next)))frontier.addLast(next);
                }
                return true;
            }
        };
    }
}
