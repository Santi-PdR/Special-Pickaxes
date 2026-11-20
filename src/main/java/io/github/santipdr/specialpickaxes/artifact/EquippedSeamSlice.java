package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;
/** Small equipped face cut, independent of preserved Strata held abilities. */
final class EquippedSeamSlice implements WorkProgram {
    private final List<BlockPos> cells;private final EquipmentLease<ItemStack> source;private int index;
    EquippedSeamSlice(BlockPos center,Direction normal,EquipmentLease<ItemStack> source){
        this.source=source;var out=new ArrayList<BlockPos>();
        for(int a=-3;a<=3;a++)for(int b=-3;b<=3;b++)out.add(switch(normal.getAxis()){case X->center.offset(0,a,b);case Y->center.offset(a,0,b);case Z->center.offset(a,b,0);});
        out.sort(Comparator.comparingDouble(p->p.distSqr(center)));cells=List.copyOf(out);
    }
    private boolean active(){return source.active()&&!source.expected().isEmpty();}
    public int remaining(){return done()?0:Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get())-index;}
    public boolean done(){return !active()||index>=Math.min(cells.size(),ArtifactConfig.JOB_LIMIT.get());}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public void confirm(){}public boolean loaded(ServerPlayer p){return true;}
    private boolean eligible(ServerPlayer p,BlockPos at){return p.serverLevel().hasChunkAt(at)&&!p.serverLevel().isOutsideBuildHeight(at)&&NaturalExcavationProgram.eligible(p.serverLevel().getBlockState(at))&&!PlayerPlacedBlocks.get(p.serverLevel()).contains(at);}
    public boolean backpressured(ServerPlayer p){if(done())return false;var at=cells.get(index);return eligible(p,at)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER,at,p.serverLevel().getBlockState(at));}
    public WorkStep next(ServerPlayer p){if(done())throw new IllegalStateException("equipped seam exhausted");var at=cells.get(index++);return new WorkStep(){public BlockPos pos(){return at;}public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){return active()&&eligible(actor,at)&&WorldSafety.mineNaturalLeased(actor,tool,ArtifactKind.SEAM_RIPPER,at,actor.serverLevel().getBlockState(at),EquippedSeamSlice.this::active);}};}
}
