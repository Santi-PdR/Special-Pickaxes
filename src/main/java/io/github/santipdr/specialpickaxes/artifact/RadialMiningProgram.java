package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Center-out, allocation-free scanner for bounded geology abilities. */
public final class RadialMiningProgram implements WorkProgram {
    private final BlockPos center;
    private final ArtifactKind kind;
    private final int radius, mode, scanLimit;
    private final BlockState target;
    private int shell=-1,x,y,z,scanned;
    private BlockPos pending;
    private boolean exhausted;

    public RadialMiningProgram(BlockPos center,int radius,ArtifactKind kind,int mode,BlockState target){
        this.center=center.immutable();this.radius=Math.max(0,Math.min(12,radius));this.kind=kind;this.mode=mode;this.target=target;
        this.scanLimit=Math.min(ArtifactConfig.JOB_LIMIT.get(),countCandidates());
    }

    private boolean inside(int dx,int dy,int dz){
        if(kind==ArtifactKind.WORLDLOOM||kind==ArtifactKind.CRUCIBLE)return dx*dx+dy*dy+dz*dz<=radius*radius;
        if(kind==ArtifactKind.HELLSPEC){
            int vertical=2*dy*dy;
            return dx*dx+dz*dz+vertical<=9
                    ||(dx-4)*(dx-4)+dz*dz+vertical<=9
                    ||(dx+4)*(dx+4)+dz*dz+vertical<=9
                    ||dx*dx+(dz-4)*(dz-4)+vertical<=9
                    ||dx*dx+(dz+4)*(dz+4)+vertical<=9;
        }
        if(kind==ArtifactKind.AXIOM){
            int distance=dx*dx+dy*dy+dz*dz,inner=Math.max(0,radius-2);
            return distance<=radius*radius&&distance>=inner*inner;
        }
        return true;
    }

    private int countCandidates(){
        int count=0,limit=Math.max(1,ArtifactConfig.JOB_LIMIT.get());
        for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++)for(int dz=-radius;dz<=radius;dz++)
            if(Math.max(Math.max(Math.abs(dx),Math.abs(dy)),Math.abs(dz))<=radius&&inside(dx,dy,dz)&&++count>=limit)return limit;
        return count;
    }

    private BlockPos peek(){
        if(pending!=null||exhausted)return pending;
        while(shell<=radius){
            if(x>shell){shell++;if(shell>radius){exhausted=true;return null;}x=-shell;y=-shell;z=-shell;}
            int dx=x,dy=y,dz=z;
            if(++z>shell){z=-shell;if(++y>shell){y=-shell;x++;}}
            if(Math.max(Math.max(Math.abs(dx),Math.abs(dy)),Math.abs(dz))!=shell||!inside(dx,dy,dz))continue;
            pending=center.offset(dx,dy,dz).immutable();return pending;
        }
        exhausted=true;return null;
    }

    @Override public int remaining(){return Math.max(0,scanLimit-scanned);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return scanned>=scanLimit||exhausted;}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // Unloaded positions are skipped without requesting chunks.
    @Override public boolean reportPartial(){return false;} // Empty, protected and non-geological cells are normal scan results.

    @Override public boolean backpressured(ServerPlayer p){
        if(kind==ArtifactKind.CRUCIBLE)return false;
        BlockPos pos=peek();if(pos==null||!p.serverLevel().hasChunkAt(pos))return false;
        var state=p.serverLevel().getBlockState(pos);
        if(PlayerPlacedBlocks.get(p.serverLevel()).contains(pos)||!mineableGeology(state))return false;
        return WorldSafety.backpressuredMine(p,p.getMainHandItem(),kind,pos,state);
    }

    @Override public WorkStep next(ServerPlayer p){
        BlockPos pos=peek();
        if(pos==null||scanned>=scanLimit){exhausted=true;return empty(center);}
        pending=null;scanned++;
        if(kind==ArtifactKind.CRUCIBLE)return transmuteAt(pos);
        return mineAt(pos);
    }

    private WorkStep mineAt(BlockPos pos){
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind ignored){
                if(!player.serverLevel().hasChunkAt(pos))return false;
                BlockState state=player.serverLevel().getBlockState(pos);
                if(PlayerPlacedBlocks.get(player.serverLevel()).contains(pos)||!mineableGeology(state))return false;
                return WorldSafety.mineQueued(player,tool,kind,pos,state);
            }
        };
    }

    /** Ordinary radial geology preserves every ore; Hellspec deliberately mines ores too. */
    private boolean mineableGeology(BlockState state){
        if(kind==ArtifactKind.HELLSPEC&&ArtifactOres.isOre(state))return true;
        return MiningDesigns.matrix(state)&&!ArtifactOres.isOre(state);
    }

    private WorkStep transmuteAt(BlockPos pos){
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind ignored){
                if(target==null||!player.serverLevel().hasChunkAt(pos))return false;
                BlockState old=player.serverLevel().getBlockState(pos);
                return old.getBlock()!=target.getBlock()&&WorldSafety.crucibleSource(player,pos,old)
                        &&WorldSafety.transmute(player,tool,kind,pos,old,target);
            }
        };
    }

    private static WorkStep empty(BlockPos at){
        return new WorkStep(){
            @Override public BlockPos pos(){return at;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){return false;}
        };
    }
}
