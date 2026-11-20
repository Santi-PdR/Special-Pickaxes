package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import java.util.*;

/** Mines connected terrain from a close front face, then revisits cleared cells for settling blocks.
 * No pre-cut tunnel, ring or remote island: expansion occurs only after a successful native break.
 */
public final class ConnectedExcavation implements WorkProgram {
    private static final Set<UUID> STARTING=new HashSet<>();
    private final BlockPos seed;
    private final Vec3 origin,look;
    private final ArtifactKind kind;
    private final EquipmentLease<ItemStack> source;
    private final int reach,cap;
    private final Queue<BlockPos> frontier;
    private final ArrayDeque<BlockPos> cleared=new ArrayDeque<>();
    private final Set<BlockPos> seen=new HashSet<>();
    private int broken;
    private long lastVisual=Long.MIN_VALUE;
    private long now,settleUntil=Long.MIN_VALUE,hardDeadline;
    private boolean revoked;

    public ConnectedExcavation(ServerPlayer p,BlockPos seed,ArtifactKind kind,boolean alternate,EquipmentLease<ItemStack> source){
        this.seed=seed.immutable();this.kind=kind;this.source=source;origin=p.getEyePosition();look=p.getLookAngle().normalize();
        reach=reach(kind,alternate,source!=null);cap=Math.min(ArtifactConfig.JOB_LIMIT.get(),source!=null?2048:alternate?16384:12288);
        frontier=new PriorityQueue<>(Comparator.comparingDouble(this::priority).thenComparingLong(BlockPos::asLong));
        now=ArtifactState.now(p);hardDeadline=now+240;frontier.add(this.seed);seen.add(this.seed);
    }
    public static boolean handles(ArtifactKind kind){
        return switch(kind){case PALIMPSEST,CHOIR,EVENTIDE,WORLDLOOM,ICARUS,AXIOM,WORLDBREAKER,EXODIUM,HELLSPEC->true;default->false;};
    }
    public static int reach(ArtifactKind kind,boolean alternate,boolean equipped){
        if(equipped)return 9;
        return switch(kind){case WORLDBREAKER->alternate?40:36;case EXODIUM,HELLSPEC->alternate?28:24;
            case CHOIR,EVENTIDE->alternate?26:22;case CRUCIBLE->alternate?28:24;
            case AXIOM,WORLDLOOM->alternate?24:20;default->alternate?22:18;};
    }
    public static Optional<BlockPos> closeTarget(ServerPlayer p){
        var eye=p.getEyePosition();var hit=p.serverLevel().clip(new ClipContext(eye,ArtifactActions.loadedRayEnd(p.serverLevel(),eye,eye.add(p.getLookAngle().scale(6))),ClipContext.Block.OUTLINE,ClipContext.Fluid.NONE,p));
        return hit.getType()==HitResult.Type.BLOCK?Optional.of(hit.getBlockPos().immutable()):Optional.empty();
    }
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind kind,boolean alternate){
        if(!STARTING.add(p.getUUID()))return false;
        try{
            if(!p.isAlive()||p.isSpectator()||!p.mayBuild()||p.getMainHandItem()!=tool||tool.isEmpty()||WorkQueue.busy(p)
                    ||!(tool.getItem() instanceof ArtifactItem item)||ArtifactSkills.feedbackKind(tool,item.kind)!=kind)return false;
            var context=HeldUtilityContext.capture(p,tool);var target=closeTarget(p);if(target.isEmpty())return false;var at=target.get();
            if(!NaturalExcavationProgram.eligible(p.serverLevel().getBlockState(at))||PlayerPlacedBlocks.get(p.serverLevel()).contains(at))return false;
            if(!WorldSafety.allowed(p,kind,at)||!context.valid()||!p.serverLevel().hasChunkAt(at)
                    ||!NaturalExcavationProgram.eligible(p.serverLevel().getBlockState(at))||PlayerPlacedBlocks.get(p.serverLevel()).contains(at))return false;
            if(!WorkQueue.startRegion(p,tool,kind,new ConnectedExcavation(p,at,kind,alternate,null)))return false;
            ArtifactInteraction.clear(p);tool.getOrCreateTag().putString("artifactStatus","idle");return true;
        }finally{STARTING.remove(p.getUUID());}
    }
    private double priority(BlockPos at){
        var delta=Vec3.atCenterOf(at).subtract(Vec3.atCenterOf(seed));
        double depth=Math.max(0,delta.dot(look)),distance=delta.lengthSqr();
        // Only traversal order changes: the result follows connected terrain, not carved geometry.
        return switch(kind){
            case CHOIR -> depth*depth*4+distance*.1; // broad advancing seismic front
            case EXODIUM -> distance*.15-delta.y*12; // top-down stellar demolition
            case WORLDLOOM -> distance*.15+delta.y*12; // clear a bank from its base
            case AXIOM -> distance-depth*depth*.8; // deep connected void rupture
            case HELLSPEC -> distance*.5-Math.abs(delta.y)*3; // vertical infernal demolition
            default -> distance;
        };
    }
    private boolean active(){return !revoked&&(source==null||source.active()&&!source.expected().isEmpty());}
    private boolean within(BlockPos at){
        var delta=Vec3.atCenterOf(at).subtract(origin);
        return delta.dot(look)>=0&&at.distSqr(seed)<=reach*reach;
    }
    public int remaining(){return done()?0:Math.max(1,frontier.size());}
    public boolean awaiting(){return false;}public boolean executing(){return true;}public void confirm(){}
    public boolean loaded(ServerPlayer p){return true;}
    public boolean reportPartial(){return false;}
    public boolean done(){return !active()||now>=hardDeadline||broken>=cap||frontier.isEmpty()&&(cleared.isEmpty()||settleUntil!=Long.MIN_VALUE&&now>=settleUntil);}
    public int attemptsPerTick(ItemStack tool){return Math.max(256,EnchantmentScaling.budget(tool));}
    public WorkStep next(ServerPlayer p){
        now=ArtifactState.now(p);
        if(frontier.isEmpty()&&settleUntil==Long.MIN_VALUE)settleUntil=Math.min(hardDeadline,now+60);
        boolean revisit=frontier.isEmpty();
        var at=revisit?cleared.removeFirst():frontier.remove();
        if(revisit)cleared.addLast(at);
        return new WorkStep(){
            public BlockPos pos(){return at;}
            public boolean apply(ServerPlayer actor,ItemStack tool,ArtifactKind ignored){
                if(!active()||!within(at)||!actor.serverLevel().hasChunkAt(at)||actor.serverLevel().isOutsideBuildHeight(at))return false;
                var state=actor.serverLevel().getBlockState(at);
                if(state.isAir()&&!revisit){
                    cleared.addLast(at);
                    if(at.equals(seed))for(var direction:Direction.values()){
                        var next=at.relative(direction);if(within(next)&&seen.add(next))frontier.addLast(next);
                    }
                    return false;
                }
                if(!NaturalExcavationProgram.eligible(state)||PlayerPlacedBlocks.get(actor.serverLevel()).contains(at))return false;
                boolean mined=WorldSafety.mineNaturalLeased(actor,tool,kind,at,state,ConnectedExcavation.this::active);
                if(!active()){revoked=true;return mined;}
                if(mined){
                    broken++;
                    if(!revisit)cleared.addLast(at);
                    else settleUntil=Math.min(hardDeadline,now+60);
                    // Bound both queued positions and visited-state memory, not just successful breaks.
                    if(seen.size()<cap*4)for(var direction:Direction.values()){
                        var next=at.relative(direction);if(within(next)&&seen.size()<cap*4&&seen.add(next))frontier.addLast(next);
                    }
                }
                return mined;
            }
        };
    }
    public void reportProgress(ServerPlayer p){
        now=ArtifactState.now(p);
        if(now%5==0&&broken>0&&lastVisual!=now){lastVisual=now;AbilityVfx.excavation(p,kind,seed,false);}
    }
}
