package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Lazy cuboid program; at most one cell inspected per scheduler attempt. No chunk loading. */
public final class RegionWork implements WorkProgram {
    public final SelectionVolume source,target;
    public final SelectionVolume.Transform transform;
    public final ArtifactKind kind;
    public final int mode;
    private long cursor;
    private final BlockIdSnapshot expectedSource,expectedTarget;
    private boolean executing;
    public long eligible,excluded;
    private final BlockState material;
    public static long snapshotMemoryBytesFor(int size,int registeredStates,boolean hasTarget){
        return BlockIdSnapshot.memoryBytesFor(size,registeredStates)*(hasTarget?2L:1L);
    }
    public RegionWork(SelectionVolume a,SelectionVolume b,SelectionVolume.Transform t,ArtifactKind k,int mode,BlockState material){
        source=a;target=b;transform=t;kind=k;this.mode=mode;this.material=material;
        if(a.size()>ArtifactConfig.REGION_LIMIT.get() || a.size()<1)throw new IllegalArgumentException("volume limit");
        if(b!=null && (!t.compatible(a,b)||a.overlaps(b)))throw new IllegalArgumentException("incompatible/overlapping regions");
        int stateCount=Block.BLOCK_STATE_REGISTRY.size();
        expectedSource=new BlockIdSnapshot((int)a.size(),stateCount);
        expectedTarget=b==null?null:new BlockIdSnapshot((int)a.size(),stateCount);
    }
    public long total(){return source.size();}
    public long snapshotMemoryBytes(){return expectedSource.memoryBytes()+(expectedTarget==null?0:expectedTarget.memoryBytes());}
    public int remaining(){return (int)(total()-cursor);}
    public boolean awaiting(){return !executing && cursor==total();}
    public boolean executing(){return executing;}
    public boolean done(){return executing && cursor==total();}
    public void confirm(){if(awaiting()){executing=true;cursor=0;}}
    public boolean loaded(ServerPlayer p){if(cursor==total())return true;var pos=source.at(cursor);return p.serverLevel().hasChunkAt(pos)&&(target==null||p.serverLevel().hasChunkAt(transform.map(source,target,pos)));}
    public boolean backpressured(ServerPlayer p){
        if(!executing||target!=null||kind!=ArtifactKind.WORLDBREAKER||mode!=0&&mode!=1&&mode!=5||cursor>=total())return false;
        var pos=source.at(cursor);var level=p.serverLevel();if(!level.hasChunkAt(pos))return false;
        var state=level.getBlockState(pos);
        return eligible(p,state,pos)&&WorldSafety.harvestable(p,p.getMainHandItem(),pos)
                &&WorldSafety.allowed(p,kind,pos)&&WorldSafety.dropPressure(p,pos);
    }
    public WorkStep next(ServerPlayer p){
        int index=(int)cursor;BlockPos pos=source.at(cursor++);var level=p.serverLevel();var old=level.getBlockState(pos);
        BlockPos other=target==null?null:transform.map(source,target,pos);
        BlockState second=other==null?null:level.getBlockState(other);
        if(!executing){expectedSource.set(index,Block.getId(old));if(second!=null)expectedTarget.set(index,Block.getId(second));}
        if(executing&&(Block.getId(old)!=expectedSource.get(index)||second!=null&&Block.getId(second)!=expectedTarget.get(index)))return skip(pos);
        if(!executing)return new WorkStep(){
            public BlockPos pos(){return pos;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind k){boolean ok=WorldSafety.allowed(actor,k,pos)&&!WorldSafety.barrier(actor,pos)&&eligible(actor,old,pos)&&(other==null||WorldSafety.allowed(actor,k,other)&&(WorldSafety.inert(second)||WorldSafety.vacant(second)));if(ok&&kind==ArtifactKind.WORLDBREAKER&&(mode==0||mode==1||mode==5))ok=WorldSafety.harvestable(actor,tool,pos);
                if(ok&&other!=null)ok=kind==ArtifactKind.TESSELLATOR?WorldSafety.inert(old)&&WorldSafety.vacant(second):old!=second&&!(WorldSafety.vacant(old)&&WorldSafety.vacant(second));
                if(ok)eligible++;else excluded++;return ok;}
        };
        if(other!=null){
            if(kind==ArtifactKind.TESSELLATOR)return new WorkStep(){public BlockPos pos(){return other;}public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind k){return WorldSafety.allowed(actor,k,pos)&&WorldSafety.placePaid(actor,tool,k,other,old);}};
            return new WorkStep.Exchange(pos,other,old,second);
        }
        if(kind==ArtifactKind.KEYSTONE)return new WorkStep(){public BlockPos pos(){return pos;}public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind k){return arch(pos)&&WorldSafety.placePaid(actor,tool,k,pos,material);}};
        if(kind==ArtifactKind.CHRONICLE && mode==0)return new WorkStep(){
            public BlockPos pos(){return pos;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind k){return WorldSafety.allowed(actor,k,pos)&&ArtifactState.snapshot(actor,k,pos,old);}
        };
        if(kind==ArtifactKind.CHRONICLE || kind==ArtifactKind.PALIMPSEST || kind==ArtifactKind.WORLDBREAKER&&mode==4){
            // Indexed lookup assembled once at confirmation, never scan thousands of records per voxel.
            return new WorkStep.Place(pos,restoration.getOrDefault(pos,Blocks.AIR.defaultBlockState()));
        }
        if(kind==ArtifactKind.CRUCIBLE||kind==ArtifactKind.WORLDBREAKER&&mode==2)return new WorkStep.Rephase(pos,old,material);
        if(kind==ArtifactKind.WORLDBREAKER&&mode==1&&!carve(pos))return new WorkStep(){public BlockPos pos(){return pos;}public boolean apply(ServerPlayer a,ItemStack t,ArtifactKind k){return false;}};
        return new WorkStep.Mine(pos,old);
    }
    private static WorkStep skip(BlockPos pos){return new WorkStep(){public BlockPos pos(){return pos;}public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k){return false;}};}
    private final java.util.Map<BlockPos,BlockState> restoration=new java.util.HashMap<>();

    /** Compact exact state ids for region snapshots; retain int storage for unusually large registries. */
    private static final class BlockIdSnapshot {
        private final byte[] byteIds;
        private final char[] charIds;
        private final int[] intIds;

        BlockIdSnapshot(int size,int registeredStates){
            byteIds=registeredStates<=256?new byte[size]:null;
            charIds=registeredStates>256&&registeredStates<=Character.MAX_VALUE+1?new char[size]:null;
            intIds=registeredStates>Character.MAX_VALUE+1?new int[size]:null;
        }
        static long memoryBytesFor(int size,int registeredStates){return (long)size*(registeredStates<=256?Byte.BYTES:registeredStates<=Character.MAX_VALUE+1?Character.BYTES:Integer.BYTES);}
        long memoryBytes(){return byteIds!=null?byteIds.length:charIds!=null?(long)charIds.length*Character.BYTES:(long)intIds.length*Integer.BYTES;}
        void set(int index,int id){
            if(byteIds!=null)byteIds[index]=(byte)id;
            else if(charIds!=null)charIds[index]=(char)id;
            else intIds[index]=id;
        }
        int get(int index){
            if(byteIds!=null)return Byte.toUnsignedInt(byteIds[index]);
            if(charIds!=null)return charIds[index];
            return intIds[index];
        }
    }

    public void loadMemories(ServerPlayer p){for(var m:ArtifactState.memories(p,kind))if(source.contains(m.pos()))restoration.put(m.pos(),m.state());}
    private boolean eligible(ServerPlayer p,BlockState s,BlockPos pos){
        if(kind==ArtifactKind.KEYSTONE)return arch(pos)&&s.isAir();
        if(kind==ArtifactKind.CHRONICLE&&mode==1||kind==ArtifactKind.PALIMPSEST||kind==ArtifactKind.WORLDBREAKER&&mode==4)return restoration.containsKey(pos)&&s.isAir();
        if(target!=null)return WorldSafety.inert(s)||WorldSafety.vacant(s);
        if(kind==ArtifactKind.WORLDBREAKER&&(mode==0||mode==1))return ArtifactTools.effective(s)&&!s.hasBlockEntity()&&s.getFluidState().isEmpty()&&(mode==0||carve(pos));
        if(kind==ArtifactKind.WORLDBREAKER&&mode==5)return ArtifactTools.effective(s)&&!s.hasBlockEntity()&&s.getFluidState().isEmpty()&&s.getDestroySpeed(p.serverLevel(),pos)>=0;
        return kind==ArtifactKind.CRUCIBLE?MiningDesigns.crucibleGeology(s)&&!PlayerPlacedBlocks.get(p.serverLevel()).contains(pos):WorldSafety.inert(s);
    }
    /** Elliptical bore follows the selected Z axis; corners are preserved. */
    private boolean carve(BlockPos p){double x=2*(p.getX()-source.min().getX()+0.5)/source.width()-1,y=2*(p.getY()-source.min().getY()+0.5)/source.height()-1;return x*x+y*y<=1;}
    /** Parabolic vault, optional pier grid. Player defines span/rise/depth with corners. */
    private boolean arch(BlockPos p){double x=2*(p.getX()-source.min().getX()+0.5)/source.width()-1;int roof=(int)Math.round((source.height()-1)*(1-x*x));int y=p.getY()-source.min().getY();return Math.abs(y-roof)<=0 || (mode==1 && Math.floorMod(p.getZ()-source.min().getZ(),4)==0 && (p.getX()==source.min().getX()||p.getX()==source.max().getX()) && y<=roof);}
}
