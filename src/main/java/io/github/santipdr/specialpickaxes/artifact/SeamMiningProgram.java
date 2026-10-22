package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Incremental, bounded same-material geology search followed by scheduled extraction. */
public final class SeamMiningProgram implements WorkProgram {
    private record Candidate(BlockPos pos,BlockState state) {}
    private static final int SCAN_LIMIT=512,MINING_LIMIT=256;
    private static final List<BlockPos> NEIGHBOR_OFFSETS=createNeighborOffsets();
    private final BlockPos origin;
    private final BlockState material;
    private final ArrayDeque<BlockPos> frontier=new ArrayDeque<>();
    private final Set<BlockPos> seen=new HashSet<>();
    private final List<Candidate> candidates=new ArrayList<>();
    private int inspected,mineIndex;
    private boolean scanning=true;

    public SeamMiningProgram(BlockPos origin,BlockState material){
        this.origin=origin.immutable();this.material=material;
        frontier.add(this.origin);seen.add(this.origin);
    }

    /** A natural seam may touch along faces, edges or corners in the surrounding 3D stone. */
    private static List<BlockPos> createNeighborOffsets(){
        var offsets=new ArrayList<BlockPos>(26);
        for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)for(int z=-1;z<=1;z++)
            if(x!=0||y!=0||z!=0)offsets.add(new BlockPos(x,y,z));
        return List.copyOf(offsets);
    }

    @Override public int remaining(){return Math.max(0,(scanning?SCAN_LIMIT-inspected:0)+candidates.size()-mineIndex);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return !scanning&&mineIndex>=candidates.size();}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // Unloaded neighbors are skipped, never requested.
    @Override public boolean reportPartial(){return false;}
    @Override public boolean backpressured(ServerPlayer p){
        if(scanning||mineIndex>=candidates.size())return false;
        var candidate=candidates.get(mineIndex);var pos=candidate.pos();
        return !PlayerPlacedBlocks.get(p.serverLevel()).contains(pos)&&MiningDesigns.seamGeology(candidate.state())
                &&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER,pos,candidate.state());
    }

    @Override public WorkStep next(ServerPlayer p){
        if(scanning&&inspected<SCAN_LIMIT&&!frontier.isEmpty()){
            var pos=frontier.removeFirst();inspected++;
            return new WorkStep(){
                @Override public BlockPos pos(){return pos;}
                @Override public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
                    var level=actor.serverLevel();
                    if(!level.hasChunkAt(pos)||PlayerPlacedBlocks.get(level).contains(pos)||!MiningDesigns.seamGeology(material)
                            ||!WorldSafety.allowed(actor,ArtifactKind.SEAM_RIPPER,pos)||level.getBlockState(pos)!=material)return false;
                    if(candidates.size()<MINING_LIMIT)candidates.add(new Candidate(pos,material));
                    for(var offset:NEIGHBOR_OFFSETS){
                        var adjacent=pos.offset(offset.getX(),offset.getY(),offset.getZ());
                        if(seen.size()<SCAN_LIMIT&&level.hasChunkAt(adjacent)&&!PlayerPlacedBlocks.get(level).contains(adjacent)
                                &&level.getBlockState(adjacent)==material&&seen.add(adjacent.immutable()))frontier.addLast(adjacent.immutable());
                    }
                    return false;
                }
            };
        }
        if(scanning){
            scanning=false;
            candidates.sort(Comparator.comparingDouble(candidate->candidate.pos().distSqr(origin)));
            return empty(origin);
        }
        if(mineIndex>=candidates.size())return empty(origin);
        var candidate=candidates.get(mineIndex++);
        return new WorkStep(){
            @Override public BlockPos pos(){return candidate.pos();}
            @Override public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
                return !PlayerPlacedBlocks.get(actor.serverLevel()).contains(candidate.pos())
                        &&MiningDesigns.seamGeology(candidate.state())
                        &&WorldSafety.mineQueued(actor,tool,kind,candidate.pos(),candidate.state());
            }
        };
    }

    private static WorkStep empty(BlockPos pos){
        return new WorkStep(){
            @Override public BlockPos pos(){return pos;}
            @Override public boolean apply(ServerPlayer player,ItemStack tool,ArtifactKind kind){return false;}
        };
    }
}
