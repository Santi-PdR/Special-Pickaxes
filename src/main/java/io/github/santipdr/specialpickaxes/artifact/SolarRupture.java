package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

/** Dawn's expanding sunburst. Geometry only, never an ore search or coordinate detector. */
public final class SolarRupture implements WorkProgram {
    public enum Form { RAYS, NOVA }
    private static final List<BlockPos> RAYS=offsets(Form.RAYS),NOVA=offsets(Form.NOVA);
    private static final Set<UUID> STARTING=new HashSet<>();
    private final BlockPos center;private final Form form;private final List<BlockPos> offsets;
    private final int limit;private int index,lastVisualIndex,lastVisualTick=Integer.MIN_VALUE;
    public SolarRupture(BlockPos center,Form form){this.center=center.immutable();this.form=form;offsets=form==Form.RAYS?RAYS:NOVA;limit=Math.min(offsets.size(),ArtifactConfig.JOB_LIMIT.get());}
    /** Unique, nearest-first cells make the fracture expand outwards instead of truncating one side. */
    public static List<BlockPos> offsets(Form form){
        int r=form==Form.RAYS?24:12;var cells=new ArrayList<BlockPos>();
        for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
            if(form==Form.RAYS&&!(Math.abs(x)<=1||Math.abs(z)<=1||Math.abs(Math.abs(x)-Math.abs(z))<=1))continue;
            int h=form==Form.RAYS?2:r;
            for(int y=-h;y<=h;y++)if(form==Form.RAYS||x*x+y*y+z*z<=r*r)cells.add(new BlockPos(x,y,z));
        }
        cells.sort(Comparator.comparingDouble((BlockPos p)->p.distSqr(BlockPos.ZERO)).thenComparingLong(BlockPos::asLong));
        return List.copyOf(cells);
    }
    public static boolean start(ServerPlayer p,ItemStack tool,Form form){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isRemoved()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()
                    ||!(tool.getItem() instanceof ArtifactItem item)||ArtifactSkills.feedbackKind(tool,item.kind)!=ArtifactKind.PALIMPSEST)return false;
            var aimed=ArtifactActions.target(p);if(aimed.isEmpty())return false;
            var at=aimed.get();var level=p.serverLevel();var context=HeldUtilityContext.capture(p,tool);
            if(!level.hasChunkAt(at)||!NaturalExcavationProgram.eligible(level.getBlockState(at))||PlayerPlacedBlocks.get(level).contains(at))return false;
            boolean allowed=WorldSafety.allowed(p,ArtifactKind.PALIMPSEST,at);
            return allowed&&context.valid()&&level.hasChunkAt(at)&&NaturalExcavationProgram.eligible(level.getBlockState(at))
                    &&!PlayerPlacedBlocks.get(level).contains(at)&&WorkQueue.startRegion(p,tool,ArtifactKind.PALIMPSEST,new SolarRupture(at,form));
        }finally{STARTING.remove(p.getUUID());}
    }
    /** Remove retired 5.x guide data without reading any ore or publishing locations. */
    public static void clearLegacyGuide(ItemStack tool){
        if(!tool.hasTag())return;
        for(String key:List.of("surveyGuidePos","surveyGuideUntil","surveyGuideDimension","surveyGuideOre","surveyGuideOwner"))tool.getTag().remove(key);
    }
    public int remaining(){return limit-index;}
    public boolean awaiting(){return false;}
    public boolean executing(){return true;}
    public boolean done(){return index>=limit;}
    public void confirm(){}
    public boolean loaded(ServerPlayer p){return true;}
    public boolean reportPartial(){return false;}
    public boolean backpressured(ServerPlayer p){
        if(done())return false;var at=center.offset(offsets.get(index));var level=p.serverLevel();
        if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
        var state=level.getBlockState(at);
        return NaturalExcavationProgram.eligible(state)&&WorldSafety.backpressuredMine(p,p.getMainHandItem(),ArtifactKind.PALIMPSEST,at,state);
    }
    public WorkStep next(ServerPlayer p){
        if(done())throw new IllegalStateException("solar rupture exhausted");var at=center.offset(offsets.get(index++));
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                var level=actor.serverLevel();if(level.isOutsideBuildHeight(at)||!level.hasChunkAt(at)||PlayerPlacedBlocks.get(level).contains(at))return false;
                var state=level.getBlockState(at);
                return NaturalExcavationProgram.eligible(state)&&WorldSafety.mineNaturalQueued(actor,tool,ArtifactKind.PALIMPSEST,at,state);
            }
        };
    }
    public void reportProgress(ServerPlayer p){
        int tick=p.server.getTickCount();
        if(index==0||index<=lastVisualIndex||lastVisualTick!=Integer.MIN_VALUE&&tick-lastVisualTick<8)return;
        lastVisualIndex=index;lastVisualTick=tick;
        AbilityVfx.solarWave(p,center,Math.sqrt(offsets.get(index-1).distSqr(BlockPos.ZERO)),form==Form.NOVA);
    }
}
