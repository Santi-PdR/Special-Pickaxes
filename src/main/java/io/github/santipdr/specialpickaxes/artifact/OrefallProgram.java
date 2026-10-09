package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Incremental Iridium prospecting and extraction. The scan and harvest both consume scheduler budget. */
public final class OrefallProgram implements WorkProgram {
    private record Ore(BlockPos pos,BlockState state) {}
    private static final int RADIUS=14,HALF_HEIGHT=7,MAX_ORES=192;
    private static final int SCAN_SIZE=countScan();
    private final BlockPos center;
    private final List<Ore> ores=new ArrayList<>();
    private int scanX=-RADIUS,scanY=-HALF_HEIGHT,scanZ=-RADIUS,scanned,oreIndex;private boolean sorted;
    public OrefallProgram(BlockPos center){
        this.center=center.immutable();
    }
    private static int countScan(){
        int count=0;
        for(int x=-RADIUS;x<=RADIUS;x++)for(int y=-HALF_HEIGHT;y<=HALF_HEIGHT;y++)for(int z=-RADIUS;z<=RADIUS;z++)
            if(x*x+z*z+4*y*y<=RADIUS*RADIUS)count++;
        return count;
    }
    private BlockPos nextScanPos(){
        while(scanY<=HALF_HEIGHT){
            int x=scanX,y=scanY,z=scanZ;
            if(++scanZ>RADIUS){scanZ=-RADIUS;if(++scanX>RADIUS){scanX=-RADIUS;scanY++;}}
            if(x*x+z*z+4*y*y<=RADIUS*RADIUS){scanned++;return center.offset(x,y,z).immutable();}
        }
        return null;
    }
    @Override public int remaining(){return Math.max(0,SCAN_SIZE-scanned)+Math.max(0,ores.size()-oreIndex);}
    @Override public boolean awaiting(){return false;}
    @Override public boolean executing(){return true;}
    @Override public boolean done(){return scanned>=SCAN_SIZE&&sorted&&oreIndex>=ores.size();}
    @Override public void confirm(){}
    @Override public boolean loaded(ServerPlayer p){return true;} // unloaded chunks are skipped, never requested
    @Override public int attemptsPerTick(ItemStack tool){
        return scanned<SCAN_SIZE?Math.max(ArtifactConfig.ORE_SCAN_BUDGET.get(),EnchantmentScaling.budget(tool)):EnchantmentScaling.budget(tool);
    }
    @Override public boolean backpressured(ServerPlayer p){return sorted&&oreIndex<ores.size()&&WorldSafety.dropPressure(p,ores.get(oreIndex).pos());}
    @Override public WorkStep next(ServerPlayer p){
        if(scanned<SCAN_SIZE){
            var pos=nextScanPos();
            return new WorkStep(){
                public BlockPos pos(){return pos;}
                public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind kind){
                    var level=actor.serverLevel();if(!level.hasChunkAt(pos))return false;
                    var state=level.getBlockState(pos);
                    if(!ArtifactOres.isOre(state)||!WorldSafety.harvestable(actor,tool,pos)
                            ||!WorldSafety.allowed(actor,ArtifactKind.IRIDIUM,pos))return false;
                    if(ores.size()<MAX_ORES)ores.add(new Ore(pos,state));
                    return false;
                }
            };
        }
        if(!sorted){ores.sort(Comparator.comparingDouble(ore->ore.pos().distSqr(center)));sorted=true;}
        if(oreIndex>=ores.size())return new WorkStep(){public BlockPos pos(){return center;}public boolean apply(ServerPlayer p,ItemStack t,ArtifactKind k){return false;}};
        var ore=ores.get(oreIndex++);return new WorkStep.Mine(ore.pos(),ore.state());
    }
}
