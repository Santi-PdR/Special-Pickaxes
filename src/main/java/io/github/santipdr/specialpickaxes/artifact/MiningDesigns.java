package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Mining-specific plans. Resource detection never sends ore positions or outlines to the client. */
public final class MiningDesigns {
    private MiningDesigns(){}
    public static boolean drill(ServerPlayer p,ItemStack tool,ArtifactKind k){
        var look=p.getLookAngle();var direction=Direction.getNearest(look.x,look.y,look.z);
        if(k==ArtifactKind.ICARUS&&ArtifactState.mode(p,k)==1)direction=direction.getOpposite();
        int mode=ArtifactState.mode(p,k);
        var shape=k==ArtifactKind.ICARUS?DirectionalProgram.Shape.ICARUS:DirectionalProgram.Shape.values()[mode];
        var origin=k==ArtifactKind.ICARUS?p.blockPosition().above().relative(direction):shape==DirectionalProgram.Shape.CORE_DRILL?p.blockPosition().below():ArtifactActions.target(p).orElse(p.blockPosition().relative(direction,2));
        return WorkQueue.startRegion(p,tool,k,new DirectionalProgram(origin,direction,shape));
    }
    public static List<WorkStep> selective(ServerPlayer p,BlockPos center){
        // Inspect an ellipsoid, retaining all ores and machines. Only matrix connected to a real ore is peeled.
        var candidates=new HashSet<BlockPos>();var distances=new HashMap<BlockPos,Integer>();var frontier=new ArrayDeque<BlockPos>();
        int r=Math.min(12,ArtifactConfig.MAX_RADIUS.get());
        for(int x=-r;x<=r;x++)for(int y=-r/2;y<=r/2;y++)for(int z=-r;z<=r;z++){
            if(x*x+4*y*y+z*z>r*r)continue;var at=center.offset(x,y,z);if(!WorldSafety.allowed(p,ArtifactKind.AXIOM,at)||WorldSafety.barrier(p,at))continue;
            var s=p.serverLevel().getBlockState(at);
            if(s.is(net.minecraftforge.common.Tags.Blocks.ORES)){frontier.add(at);distances.put(at,0);}
            else if(matrix(s))candidates.add(at);
        }
        var steps=new ArrayList<WorkStep>();int shellDepth=ArtifactConfig.AXIOM_DEPTH.get();
        while(!frontier.isEmpty()&&steps.size()<ArtifactConfig.JOB_LIMIT.get()){
            var at=frontier.removeFirst();int d=distances.get(at);if(d>=shellDepth)continue;
            for(var direction:Direction.values()){var next=at.relative(direction);if(candidates.remove(next)){distances.put(next,d+1);frontier.addLast(next);steps.add(new WorkStep.Mine(next,p.serverLevel().getBlockState(next)));}}
        }
        return steps;
    }
    public static boolean matrix(net.minecraft.world.level.block.state.BlockState s){return s.is(net.minecraftforge.common.Tags.Blocks.STONE)||s.is(Blocks.DEEPSLATE)||s.is(Blocks.NETHERRACK)||s.is(Blocks.END_STONE)||s.is(Blocks.TUFF)||s.is(Blocks.CALCITE)||s.is(Blocks.BASALT);}
}
