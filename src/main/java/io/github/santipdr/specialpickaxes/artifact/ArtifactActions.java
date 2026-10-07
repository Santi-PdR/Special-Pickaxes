package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import java.util.*;

/** Artifact orchestration; mutation is exclusively delegated to scheduled, revalidated steps. */
public final class ArtifactActions {
    private ArtifactActions() {}
    private static final Set<UUID> ACTIVATING=new HashSet<>();
    public static Optional<BlockPos> target(ServerPlayer p) {
        var level=p.serverLevel();Vec3 from=p.getEyePosition(),to=from.add(p.getLookAngle().scale(32));
        BlockPos last=null;
        for(int i=0;i<=256;i++) {
            BlockPos pos=BlockPos.containing(from.lerp(to,i/256.0));
            if(pos.equals(last)) continue;last=pos;
            if(!level.hasChunkAt(pos) || level.isOutsideBuildHeight(pos)) return Optional.empty();
            var state=level.getBlockState(pos);
            if(state.getCollisionShape(level,pos).clip(from,to,pos)!=null) return Optional.of(pos.immutable());
        }
        return Optional.empty();
    }
    public static boolean use(ServerPlayer p,ItemStack tool,ArtifactKind kind,boolean secondary) {
        if(!ACTIVATING.add(p.getUUID()))return false;
        try{return activate(p,tool,kind,secondary);}finally{ACTIVATING.remove(p.getUUID());}
    }
    private static boolean activate(ServerPlayer p,ItemStack tool,ArtifactKind kind,boolean secondary){
        if(secondary && WorkQueue.busy(p)) { WorkQueue.cancel(p);ArtifactFeedback.message(p,"cancelled");return true; }
        if(!WorldSafety.allowed(p,kind,p.blockPosition()) || tool.isEmpty()) return false;
        var data=ArtifactState.of(p,kind);
        if(data.getLong("ready")>ArtifactState.now(p)){ArtifactFeedback.message(p,"cooldown");ArtifactFeedback.cue(p,"error");return false;}
        int cost=secondary?0:EnchantmentScaling.activationCost(tool,kind);
        if(!p.isCreative() && tool.getMaxDamage()-tool.getDamageValue()<=cost) return false;
        boolean done=secondary?secondary(p,kind):primary(p,tool,kind);
        if(!done) { ArtifactFeedback.message(p,"no_target");return false; }
        int cooldown=secondary?10:ArtifactConfig.COOLDOWN.get();
        data.putLong("ready",ArtifactState.now(p)+cooldown);p.getCooldowns().addCooldown(tool.getItem(),cooldown);
        tool.hurtAndBreak(cost,p,who -> who.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        ArtifactFeedback.sound(p,kind);return true;
    }
    private static boolean secondary(ServerPlayer p,ArtifactKind kind) {
        switch(kind) {
            case PALIMPSEST -> ArtifactState.rotate(p,kind);
            case MERIDIAN, ATLAS -> ArtifactState.clearAnchors(p,kind);
            case INTERREGNUM -> DomainFields.stop(p);
            default -> ArtifactState.rotate(p,kind);
        }
        ArtifactFeedback.message(p,"mode",ArtifactState.mode(p,kind)+1);return true;
    }
    public static int radius(ServerPlayer p,ArtifactKind kind) { return Math.min(ArtifactConfig.MAX_RADIUS.get(),8); }
    public static boolean primary(ServerPlayer p,ItemStack tool,ArtifactKind kind) {
        if(WorkQueue.busy(p)) return false;
        if(!kind.playable())return false;
        if(kind==ArtifactKind.WORLDBREAKER||kind==ArtifactKind.ICARUS)return MiningDesigns.drill(p,tool,kind);
        if(kind==ArtifactKind.CRUCIBLE&&ArtifactState.mode(p,kind)==1)return WorkQueue.start(p,tool,kind,rephase(p,p.blockPosition(),6));
        if(kind==ArtifactKind.PALIMPSEST) {
            var steps=new ArrayList<WorkStep>();
            var memories=ArtifactState.memories(p,kind);var aimed=target(p).orElse(p.blockPosition());
            // After mining, the ray can hit a distant wall beyond the remembered scar.
            var center=memories.stream().anyMatch(m->m.pos().distSqr(aimed)<=16*16)?aimed:p.blockPosition();
            for(var memory:memories)if(memory.pos().distSqr(center)<=16*16)steps.add(new WorkStep.Place(memory.pos(),memory.state()));
            return WorkQueue.start(p,tool,kind,steps);
        }

        var target=target(p);if(target.isEmpty() || !WorldSafety.allowed(p,kind,target.get())) return false;
        BlockPos center=target.get();
        int r=radius(p,kind);
        if(kind==ArtifactKind.INTERREGNUM) { if(!DomainFields.start(p,tool,kind,center,r))return false;ArtifactFeedback.ring(p,kind,center,r);return true; }


        List<WorkStep> steps=switch(kind) {
            case CHOIR -> echo(p,center);
            case CRUCIBLE -> rephase(p,center,r);
            case WORLDLOOM -> weave(p,center);
            case EVENTIDE -> quarry(p,kind,center,r);
            case AXIOM -> MiningDesigns.selective(p,center);
            default -> List.of();
        };
        boolean started=WorkQueue.start(p,tool,kind,steps);
        if(started) { if(kind!=ArtifactKind.AXIOM)ArtifactFeedback.preview(p,kind,steps);ArtifactFeedback.ring(p,kind,center,r);ArtifactFeedback.message(p,"queued",steps.size()); }
        return started;
    }
    public static List<WorkStep> echo(ServerPlayer p,BlockPos center) {
        var memories=ArtifactState.memories(p,ArtifactKind.CHOIR);var steps=new ArrayList<WorkStep>();
        if(memories.isEmpty()) return steps;
        BlockPos origin=memories.get(0).pos();
        for(var memory:memories) {
            int mode=ArtifactState.mode(p,ArtifactKind.CHOIR);BlockPos raw=memory.pos().subtract(origin);
            BlockPos offset=mode<4?Geometry.rotate(raw,mode):mode==4?new BlockPos(-raw.getX(),raw.getY(),raw.getZ()):new BlockPos(raw.getX(),raw.getY(),-raw.getZ());
            if(offset.distSqr(BlockPos.ZERO)<=32*32) steps.add(new WorkStep.Mine(center.offset(offset),memory.state()));
        }
        return steps;
    }
    public static List<WorkStep> quarry(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius) {
        var steps=new ArrayList<WorkStep>();
        for(BlockPos pos:Geometry.cube(center,radius,ArtifactConfig.JOB_LIMIT.get())) {
            if(kind==ArtifactKind.EVENTIDE && (Math.pow(pos.getX()-center.getX(),2)+4*Math.pow(pos.getY()-center.getY(),2)+Math.pow(pos.getZ()-center.getZ(),2))>radius*radius) continue;
            if(kind==ArtifactKind.AXIOM && ArtifactState.mode(p,kind)%2==0
                    && Math.floorMod(pos.getX()-center.getX(),4)==0 && Math.floorMod(pos.getZ()-center.getZ(),4)==0) continue;
            if(!WorldSafety.allowed(p,kind,pos)) continue;
            var state=p.serverLevel().getBlockState(pos);
            if(MiningDesigns.matrix(state)) steps.add(new WorkStep.Mine(pos,state));
        }
        if(kind==ArtifactKind.EVENTIDE)steps.sort(java.util.Comparator.comparingDouble(step->(ArtifactState.mode(p,kind)==0?-1:1)*step.pos().distSqr(center)));
        return steps;
    }
    public static BlockState geologyMaterial(ServerPlayer p){
        var item=p.getOffhandItem().getItem();return item==Items.DEEPSLATE?Blocks.DEEPSLATE.defaultBlockState():item==Items.BASALT?Blocks.BASALT.defaultBlockState():item==Items.OBSIDIAN?Blocks.OBSIDIAN.defaultBlockState():Blocks.STONE.defaultBlockState();
    }
    public static List<WorkStep> rephase(ServerPlayer p,BlockPos center,int radius) {
        BlockState next=geologyMaterial(p);
        var steps=new ArrayList<WorkStep>();
        for(BlockPos pos:Geometry.cube(center,radius,ArtifactConfig.JOB_LIMIT.get())) {
            if(pos.distSqr(center)>radius*radius||!WorldSafety.allowed(p,ArtifactKind.CRUCIBLE,pos)||WorldSafety.barrier(p,pos)) continue;
            var old=p.serverLevel().getBlockState(pos);
            if(MiningDesigns.matrix(old) && old!=next) steps.add(new WorkStep.Rephase(pos,old,next));
        }
        return steps;
    }
    public static List<WorkStep> weave(ServerPlayer p,BlockPos origin) {
        var held=p.getOffhandItem();if(!(held.getItem() instanceof BlockItem block)) return List.of();
        var state=block.getBlock().defaultBlockState();if(!WorldSafety.inert(state) || held.hasTag()) return List.of();
        var steps=new ArrayList<WorkStep>();var forward=p.getDirection();var side=forward.getClockWise();int mode=ArtifactState.mode(p,ArtifactKind.WORLDLOOM);
        if(mode==1){for(int z=0;z<48;z++)for(int x=-1;x<=1;x++)steps.add(new WorkStep.Place(origin.relative(forward,z).relative(side,x),state));}
        else if(mode==2){for(int y=1;y<=6;y++)for(int x=-4;x<=4;x++)steps.add(new WorkStep.Place(origin.relative(side,x).above(y),state));}
        else {for(int y=0;y<=4;y++)for(int z=0;z<=6;z++)for(int x=-3;x<=3;x++){
            if(y!=0&&y!=4&&z!=0&&z!=6&&Math.abs(x)!=3)continue;
            if(z==0&&x==0&&(y==1||y==2))continue;
            steps.add(new WorkStep.Place(origin.relative(forward,z).relative(side,x).above(y),state));
        }}return steps;
    }
    public static List<WorkStep> bore(ServerPlayer p) {
        var look=p.getLookAngle();var direction=Direction.getNearest(look.x,look.y,look.z);
        if(ArtifactState.mode(p,ArtifactKind.ICARUS)%2!=0) direction=direction.getOpposite();
        int length=ArtifactConfig.BORE_LENGTH.get();
        var steps=new ArrayList<WorkStep>();
        for(int i=1;i<=length;i++) {
            var feet=p.blockPosition().relative(direction,i);
            var slice=direction.getAxis()==Direction.Axis.Y?feet:feet.above();
            for(var pos:Geometry.section(slice,direction)) {
                if(!WorldSafety.allowed(p,ArtifactKind.ICARUS,pos)) continue;
                var state=p.serverLevel().getBlockState(pos);
                if(!state.isAir()) steps.add(new WorkStep.Mine(pos,state));
            }
            steps.add(new WorkStep.Move(feet));
        }
        return steps;
    }
    private static boolean link(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos center) {
        var first=ArtifactState.anchor(p,kind,"a");
        if(first.isEmpty()) {
            ArtifactState.of(p,kind).putInt("heading",p.getDirection().get2DDataValue());ArtifactState.anchor(p,kind,"a",center);ArtifactFeedback.burst(p,kind,center,8);ArtifactFeedback.message(p,"anchor_a");return true;
        }
        if(kind==ArtifactKind.MERIDIAN) {
            if(first.get().equals(center)) return false;
            ArtifactState.of(p,kind).putInt("rotation",Math.floorMod(p.getDirection().get2DDataValue()-ArtifactState.of(p,kind).getInt("heading"),4));ArtifactState.anchor(p,kind,"b",center);ArtifactFeedback.trace(p,kind,first.get(),center);ArtifactFeedback.message(p,"linked");return true;
        }
        var delta=center.subtract(first.get());int r=4;
        if(Math.abs(delta.getX())<=2*r && Math.abs(delta.getY())<=2*r && Math.abs(delta.getZ())<=2*r) return false;
        var steps=new ArrayList<WorkStep>();
        for(var pos:Geometry.cube(first.get(),r,ArtifactConfig.JOB_LIMIT.get())) {
            var other=pos.offset(delta);
            if(!WorldSafety.allowed(p,kind,pos) || !WorldSafety.allowed(p,kind,other)) continue;
            var a=p.serverLevel().getBlockState(pos);var b=p.serverLevel().getBlockState(other);
            if((WorldSafety.inert(a) || WorldSafety.vacant(a)) && (WorldSafety.inert(b) || WorldSafety.vacant(b)) && a!=b) steps.add(new WorkStep.Exchange(pos,other,a,b));
        }
        boolean result=WorkQueue.start(p,tool,kind,steps);
        if(result) { ArtifactFeedback.trace(p,kind,first.get(),center);ArtifactState.clearAnchors(p,kind); }
        return result;
    }
    public static void mined(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state) {
        if(WorkQueue.running() || !p.serverLevel().getBlockState(pos).isAir()) return;
        DomainFields.feed(p,pos);if(p.tickCount%3==0)ArtifactFeedback.burst(p,kind,pos,2);
        if(kind==ArtifactKind.PALIMPSEST || kind==ArtifactKind.CHOIR) ArtifactState.record(p,kind,pos,state);
        if(kind==ArtifactKind.MERIDIAN) {
            var a=ArtifactState.anchor(p,kind,"a");var b=ArtifactState.anchor(p,kind,"b");
            if(a.isPresent() && b.isPresent() && pos.distSqr(a.get())<=8*8) {
                var destination=b.get().offset(Geometry.rotate(pos.subtract(a.get()),ArtifactState.of(p,kind).getInt("rotation")));
                if(WorkQueue.append(p,tool,kind,new WorkStep.Mine(destination,state))) ArtifactFeedback.trace(p,kind,pos,destination);
            }
        }
    }
}
