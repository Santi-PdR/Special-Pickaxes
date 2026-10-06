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
        if(secondary && WorkQueue.busy(p)) { WorkQueue.cancel(p);ArtifactFeedback.message(p,"cancelled");return true; }
        if(!WorldSafety.allowed(p,kind,p.blockPosition()) || tool.isEmpty()) return false;
        var data=ArtifactState.of(p,kind);
        if(data.getLong("ready")>ArtifactState.now(p)) return false;
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
        if(DirectAbilities.handles(kind))return DirectAbilities.activate(p,tool,kind);
        if(kind==ArtifactKind.PALIMPSEST) {
            var steps=new ArrayList<WorkStep>();
            var center=target(p).orElse(p.blockPosition());
            for(var memory:ArtifactState.memories(p,kind))if(memory.pos().distSqr(center)<=16*16)steps.add(new WorkStep.Place(memory.pos(),memory.state()));
            return WorkQueue.start(p,tool,kind,steps);
        }
        if(kind==ArtifactKind.ICARUS) return WorkQueue.start(p,tool,kind,bore(p));
        var target=target(p);if(target.isEmpty() || !WorldSafety.allowed(p,kind,target.get())) return false;
        BlockPos center=target.get();
        int r=radius(p,kind);
        if(kind==ArtifactKind.INTERREGNUM) { DomainFields.start(p,tool,kind,center,r);ArtifactFeedback.ring(p,kind,center,r);return true; }
        if(kind==ArtifactKind.MERIDIAN || kind==ArtifactKind.ATLAS) return link(p,tool,kind,center);
        if(kind==ArtifactKind.EVENTIDE) DomainFields.start(p,tool,kind,center,r);
        List<WorkStep> steps=switch(kind) {
            case CHOIR -> echo(p,center);
            case CRUCIBLE -> rephase(p,center,r);
            case WORLDLOOM -> weave(p,center.above());
            case EVENTIDE, AXIOM -> quarry(p,kind,center,r);
            default -> List.of();
        };
        boolean started=WorkQueue.start(p,tool,kind,steps);
        if(started) { ArtifactFeedback.preview(p,kind,steps);ArtifactFeedback.ring(p,kind,center,r);ArtifactFeedback.message(p,"queued",steps.size()); }
        return started || kind==ArtifactKind.EVENTIDE;
    }
    public static List<WorkStep> echo(ServerPlayer p,BlockPos center) {
        var memories=ArtifactState.memories(p,ArtifactKind.CHOIR);var steps=new ArrayList<WorkStep>();
        if(memories.isEmpty()) return steps;
        BlockPos origin=memories.get(0).pos();
        for(var memory:memories) {
            BlockPos offset=Geometry.rotate(memory.pos().subtract(origin),ArtifactState.mode(p,ArtifactKind.CHOIR));
            if(offset.distSqr(BlockPos.ZERO)<=32*32) steps.add(new WorkStep.Mine(center.offset(offset),memory.state()));
        }
        return steps;
    }
    public static List<WorkStep> quarry(ServerPlayer p,ArtifactKind kind,BlockPos center,int radius) {
        var steps=new ArrayList<WorkStep>();
        for(BlockPos pos:Geometry.cube(center,radius,ArtifactConfig.JOB_LIMIT.get())) {
            if(kind==ArtifactKind.EVENTIDE && pos.distSqr(center)>radius*radius) continue;
            if(kind==ArtifactKind.AXIOM && ArtifactState.mode(p,kind)%2==0
                    && Math.floorMod(pos.getX()-center.getX(),4)==0 && Math.floorMod(pos.getZ()-center.getZ(),4)==0) continue;
            if(!WorldSafety.allowed(p,kind,pos)) continue;
            var state=p.serverLevel().getBlockState(pos);
            if(WorldSafety.inert(state)) steps.add(new WorkStep.Mine(pos,state));
        }
        return steps;
    }
    public static List<WorkStep> rephase(ServerPlayer p,BlockPos center,int radius) {
        BlockState next=switch(ArtifactState.mode(p,ArtifactKind.CRUCIBLE)) {
            case 1 -> Blocks.DEEPSLATE.defaultBlockState();case 2 -> Blocks.BASALT.defaultBlockState();
            case 3 -> Blocks.OBSIDIAN.defaultBlockState();default -> Blocks.STONE.defaultBlockState();
        };
        var steps=new ArrayList<WorkStep>();
        for(BlockPos pos:Geometry.cube(center,radius,ArtifactConfig.JOB_LIMIT.get())) {
            if(!WorldSafety.allowed(p,ArtifactKind.CRUCIBLE,pos)) continue;
            var old=p.serverLevel().getBlockState(pos);
            if(WorldSafety.inert(old) && old!=next) steps.add(new WorkStep.Rephase(pos,old,next));
        }
        return steps;
    }
    public static List<WorkStep> weave(ServerPlayer p,BlockPos origin) {
        var held=p.getOffhandItem();if(!(held.getItem() instanceof BlockItem block)) return List.of();
        var state=block.getBlock().defaultBlockState();if(!WorldSafety.inert(state) || held.hasTag()) return List.of();
        var steps=new ArrayList<WorkStep>();int r=Math.min(6,radius(p,ArtifactKind.WORLDLOOM));
        if(ArtifactState.mode(p,ArtifactKind.WORLDLOOM)%2==0) {
            for(int x=-r;x<=r;x++) for(int y=0;y<=r;y++) for(int z=-r;z<=r;z++) {
                if(Math.abs(x)!=r && Math.abs(z)!=r && y!=0 && y!=r) continue;
                // Open doorway, not an inescapable enclosure.
                if(x==0 && z==-r && (y==1 || y==2)) continue;
                steps.add(new WorkStep.Place(origin.offset(x,y,z),state));
            }
        } else {
            var forward=p.getDirection();var side=forward.getClockWise();
            int length=48;
            for(int i=0;i<length;i++) for(int w=-1;w<=1;w++) steps.add(new WorkStep.Place(origin.relative(forward,i).relative(side,w),state));
        }
        return steps;
    }
    public static List<WorkStep> bore(ServerPlayer p) {
        var look=p.getLookAngle();var direction=Direction.getNearest(look.x,look.y,look.z);
        if(ArtifactState.mode(p,ArtifactKind.ICARUS)%2!=0) direction=direction.getOpposite();
        int length=Math.min(ArtifactConfig.BORE_LENGTH.get(),ArtifactConfig.BORE_LENGTH.get());
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
        DomainFields.feed(p,pos);CompanionActions.mined(p,kind,pos);if(p.tickCount%3==0)ArtifactFeedback.burst(p,kind,pos,2);
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
