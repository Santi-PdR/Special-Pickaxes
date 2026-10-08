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
    public static boolean useAlternate(ServerPlayer p,ItemStack tool,ArtifactKind kind) {
        if(p.getMainHandItem()!=tool||tool.isEmpty()||!WorldSafety.allowed(p,kind,p.blockPosition()))return false;
        int cost=EnchantmentScaling.activationCost(tool,kind);
        if(!p.isCreative()&&tool.getMaxDamage()-tool.getDamageValue()<=cost)return false;
        if(!ArtifactSkills.use(p,tool,kind))return false;
        if(!p.isCreative())tool.hurtAndBreak(cost,p,who->who.broadcastBreakEvent(InteractionHand.MAIN_HAND));
        int cooldown=Math.max(100,ArtifactConfig.COOLDOWN.get()*5);p.getCooldowns().addCooldown(tool.getItem(),cooldown);
        return true;
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
    public static int radius(ServerPlayer p,ArtifactKind kind) { return Math.min(ArtifactConfig.MAX_RADIUS.get(),10); }
    public static boolean primary(ServerPlayer p,ItemStack tool,ArtifactKind kind) {
        if(WorkQueue.busy(p)) return false;
        if(!kind.playable())return false;
        if(kind==ArtifactKind.WORLDBREAKER&&tool.hasTag()&&tool.getTag().contains("copiedSkill")){
            try{var copied=ArtifactKind.byId(tool.getTag().getString("copiedSkill"));return copied!=ArtifactKind.WORLDBREAKER&&primary(p,tool,copied);}
            catch(IllegalArgumentException invalid){ArtifactFeedback.message(p,"copy_pick_first");return false;}
        }
        if(kind==ArtifactKind.WORLDBREAKER||kind==ArtifactKind.ICARUS||kind==ArtifactKind.EXODIUM||kind==ArtifactKind.CHOIR) {
            boolean started=MiningDesigns.drill(p,tool,kind);
            if(started&&(kind==ArtifactKind.ICARUS||ArtifactState.mode(p,kind)==3))p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING,40,0,false,false,true));
            return started;
        }
        if(kind==ArtifactKind.IRIDIUM) {
            var aimed=target(p);return aimed.isPresent()&&WorkQueue.startRegion(p,tool,kind,new OrefallProgram(aimed.get()));
        }
        if(kind==ArtifactKind.PALIMPSEST) {
            var aimed=target(p);if(aimed.isEmpty())return false;
            var origin=aimed.get();var initial=p.serverLevel().getBlockState(origin);
            return ArtifactOres.isOre(initial)&&WorldSafety.allowed(p,kind,origin)&&WorldSafety.harvestable(p,tool,origin)
                    &&WorkQueue.startRegion(p,tool,kind,new ConnectedVeinProgram(origin,initial,ArtifactConfig.PALIMPSEST_VEIN_LIMIT.get()));
        }

        var target=kind==ArtifactKind.INTERREGNUM&&ArtifactState.mode(p,kind)==1?Optional.of(p.blockPosition()):target(p);
        if(target.isEmpty() || !WorldSafety.allowed(p,kind,target.get())) return false;
        BlockPos center=target.get();
        int r=kind==ArtifactKind.WORLDLOOM?8:radius(p,kind);
        if(kind==ArtifactKind.INTERREGNUM) {
            int fieldRadius=ArtifactState.mode(p,kind)==1?2:8;
            if(!DomainFields.start(p,tool,kind,center,fieldRadius))return false;
            ArtifactFeedback.domain(p,kind,center,fieldRadius,ArtifactState.mode(p,kind)==1?2:fieldRadius/2);
            return true;
        }
        if(kind==ArtifactKind.EVENTIDE) {
            int fieldRadius=Math.min(r,8);
            if(!DomainFields.start(p,tool,kind,center,fieldRadius))return false;
            ArtifactFeedback.ring(p,kind,center,fieldRadius);ArtifactFeedback.message(p,"gravity_marked");return true;
        }
        if(kind==ArtifactKind.AXIOM){
            var program=new RadialMiningProgram(center,r,kind,ArtifactState.mode(p,kind),null);
            boolean started=WorkQueue.startRegion(p,tool,kind,program);
            if(started){ArtifactFeedback.ring(p,kind,center,r);ArtifactFeedback.message(p,"queued",program.remaining());}
            return started;
        }
        if(kind==ArtifactKind.WORLDLOOM||kind==ArtifactKind.CRUCIBLE){
            var program=new RadialMiningProgram(center,r,kind,ArtifactState.mode(p,kind),
                    kind==ArtifactKind.CRUCIBLE?geologyMaterial(p,ArtifactState.mode(p,kind)):null);
            boolean started=WorkQueue.startRegion(p,tool,kind,program);
            if(started){ArtifactFeedback.ring(p,kind,center,r);ArtifactFeedback.message(p,"queued",program.remaining());}
            return started;
        }
        if(kind==ArtifactKind.HELLSPEC) {
            var steps=MiningDesigns.hellforge(p,center);boolean started=WorkQueue.start(p,tool,kind,steps);
            if(started){ArtifactFeedback.preview(p,kind,steps);ArtifactFeedback.message(p,"queued",steps.size());}
            return started;
        }


        List<WorkStep> steps=List.of();
        boolean started=WorkQueue.start(p,tool,kind,steps);
        if(started) { if(kind==ArtifactKind.EVENTIDE&&!DomainFields.start(p,tool,kind,center,Math.min(r,8))){WorkQueue.cancel(p);return false;}if(kind!=ArtifactKind.AXIOM)ArtifactFeedback.preview(p,kind,steps);ArtifactFeedback.ring(p,kind,center,r);ArtifactFeedback.message(p,"queued",steps.size()); }
        return started;
    }
    public static List<WorkStep> echo(ServerPlayer p,BlockPos center) {
        var memories=ArtifactState.memories(p,ArtifactKind.CHOIR);var steps=new ArrayList<WorkStep>();
        if(memories.isEmpty()) return steps;
        BlockPos origin=memories.get(0).pos();
        int recordedHeading=Math.floorMod(ArtifactState.of(p,ArtifactKind.CHOIR).getInt("heading"),4);
        int turns=Math.floorMod(p.getDirection().get2DDataValue()-recordedHeading,4);
        for(var memory:memories) {
            BlockPos raw=memory.pos().subtract(origin);
            BlockPos offset=Geometry.rotate(raw,turns);
            if(offset.distSqr(BlockPos.ZERO)<=48*48) steps.add(new WorkStep.Mine(center.offset(offset),memory.state()));
        }
        return steps;
    }
    public static List<WorkStep> gravityPulse(ServerPlayer p,BlockPos broken,BlockState material,BlockPos fieldCenter,int mode) {
        var forward=p.getDirection();var side=forward.getClockWise();var candidates=new ArrayList<BlockPos>();
        for(int depth=1;depth<=2;depth++)for(int lateral=-1;lateral<=1;lateral++)for(int vertical=-1;vertical<=1;vertical++) {
            var pos=broken.relative(forward,depth).relative(side,lateral).above(vertical);
            if(!p.serverLevel().hasChunkAt(pos)||!DomainFields.contains(p,pos)||!WorldSafety.allowed(p,ArtifactKind.EVENTIDE,pos)||WorldSafety.barrier(p,pos))continue;
            var state=p.serverLevel().getBlockState(pos);if(state.getBlock()==material.getBlock())candidates.add(pos.immutable());
        }
        candidates.sort(Comparator.comparingDouble(pos->(mode==0?1:-1)*pos.distSqr(fieldCenter)));
        var steps=new ArrayList<WorkStep>(6);for(var pos:candidates){steps.add(new WorkStep.Mine(pos,p.serverLevel().getBlockState(pos)));if(steps.size()==6)break;}
        return steps;
    }
    public static BlockState geologyMaterial(ServerPlayer p,int mode){
        return switch(Math.floorMod(mode,ArtifactInteraction.modeCount(ArtifactKind.CRUCIBLE))) {
            case 1 -> Blocks.DEEPSLATE.defaultBlockState();case 2 -> Blocks.GRANITE.defaultBlockState();case 3 -> Blocks.DIORITE.defaultBlockState();
            case 4 -> Blocks.ANDESITE.defaultBlockState();case 5 -> Blocks.DIRT.defaultBlockState();case 6 -> Blocks.BASALT.defaultBlockState();case 7 -> Blocks.OBSIDIAN.defaultBlockState();
            case 8 -> Blocks.CALCITE.defaultBlockState();case 9 -> Blocks.TUFF.defaultBlockState();case 10 -> Blocks.DRIPSTONE_BLOCK.defaultBlockState();case 11 -> Blocks.GRAVEL.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
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
        DomainFields.feed(p,tool,pos,state);if(p.tickCount%3==0)ArtifactFeedback.burst(p,kind,pos,2);
        if(kind==ArtifactKind.MERIDIAN) {
            var a=ArtifactState.anchor(p,kind,"a");var b=ArtifactState.anchor(p,kind,"b");
            if(a.isPresent() && b.isPresent() && pos.distSqr(a.get())<=8*8) {
                var destination=b.get().offset(Geometry.rotate(pos.subtract(a.get()),ArtifactState.of(p,kind).getInt("rotation")));
                if(WorkQueue.append(p,tool,kind,new WorkStep.Mine(destination,state))) ArtifactFeedback.trace(p,kind,pos,destination);
            }
        }
    }
}
