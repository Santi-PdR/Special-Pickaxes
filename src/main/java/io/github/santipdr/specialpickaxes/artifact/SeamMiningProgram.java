package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Incremental, bounded contact-surface search followed by scheduled extraction. */
public final class SeamMiningProgram implements WorkProgram {
    private record Candidate(BlockPos pos,BlockState state) {}
    private static final int SCAN_LIMIT=512,MINING_LIMIT=256;
    private final BlockPos origin;
    private final BlockState material,boundary;
    private final boolean exposedToAir;
    private final ArrayDeque<BlockPos> frontier=new ArrayDeque<>();
    private final Set<BlockPos> seen=new HashSet<>();
    private final List<Candidate> candidates=new ArrayList<>();
    private int inspected,mineIndex;
    private boolean scanning=true;

    public SeamMiningProgram(BlockPos origin,BlockState material,BlockState boundary,boolean exposedToAir,Collection<BlockPos> seeds){
        this.origin=origin.immutable();this.material=material;this.boundary=boundary;this.exposedToAir=exposedToAir;
        for(var seed:seeds)if(seen.size()<SCAN_LIMIT&&seen.add(seed.immutable()))frontier.addLast(seed.immutable());
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
                    boolean touchesBoundary=false;
                    for(var direction:Direction.values()){
                        var adjacent=pos.relative(direction);if(!level.hasChunkAt(adjacent))continue;
                        var neighbor=level.getBlockState(adjacent);
                        if(exposedToAir?neighbor.isAir():neighbor==boundary){touchesBoundary=true;break;}
                    }
                    if(touchesBoundary&&candidates.size()<MINING_LIMIT)candidates.add(new Candidate(pos,material));
                    for(var direction:Direction.values()){
                        var adjacent=pos.relative(direction);
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
