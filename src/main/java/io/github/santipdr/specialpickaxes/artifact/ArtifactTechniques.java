package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Bounded held-tool alternate actions. Curios equipment grants passives only. */
public final class ArtifactTechniques {
    public static final int IRIDIUM_RECALL_RADIUS=48;
    private static final int IRIDIUM_RECALL_INSPECTION_LIMIT=4096;
    private static final int SEAM_FACE_RADIUS=7;
    private static final int SEAM_FACE_CELLS=(SEAM_FACE_RADIUS*2+1)*(SEAM_FACE_RADIUS*2+1);
    private ArtifactTechniques() {}

    private static boolean echo(ServerPlayer player, ItemStack tool, int skill) {
        if (!tool.hasTag() || !tool.getTag().contains("copiedSkill")) {
            ArtifactFeedback.message(player, "copy_pick_first");
            return false;
        }
        ArtifactKind source;
        try { source = ArtifactKind.byId(tool.getTag().getString("copiedSkill")); }
        catch (IllegalArgumentException invalid) { return false; }
        return source != ArtifactKind.WORLDBREAKER && performHeldAlternate(player, tool, source);
    }

    private static BlockPos aimed(ServerPlayer player) {
        return ArtifactActions.target(player).orElseGet(() -> player.blockPosition().relative(player.getDirection(), 6)).immutable();
    }

    private static boolean magnetDrops(ServerPlayer player,ItemStack tool,int radius,int cap) {
        var context=HeldUtilityContext.capture(player,tool);
        Vec3 center = player.getEyePosition();
        int moved = 0,visualized=0;
        var recalledDrops=new java.util.ArrayList<ItemEntity>();
        var level=player.serverLevel();
        var drops=EntitySelection.nearest(level,ItemEntity.class,player.getBoundingBox().inflate(radius),
                e->e.isAlive()&&DropOwnership.allows(player,e)
                        &&e.position().distanceToSqr(center)<=(double)radius*radius,center,cap);
        for (var drop : drops) {
            var checkedPosition=drop.position();var checkedStack=drop.getItem();
            boolean allowed=WorldSafety.allowed(player,ArtifactKind.EVENTIDE,drop.blockPosition());
            if(!context.valid())break;
            if(!allowed||!drop.isAlive()||drop.level()!=level||!drop.position().equals(checkedPosition)
                    ||drop.getItem()!=checkedStack||drop.getItem().isEmpty()||!DropOwnership.allows(player,drop)
                    ||drop.position().distanceToSqr(center)>(double)radius*radius)continue;
            Vec3 delta = center.subtract(drop.position());
            if (delta.lengthSqr() < .25) continue;
            if(visualized<12){ArtifactFeedback.gravityTether(player,drop.position(),center);visualized++;}
            drop.setDeltaMovement(UtilityPullMotion.velocity(delta,Vec3.ZERO,0,.65,.65));
            drop.hasImpulse = true;recalledDrops.add(drop);
            if (++moved >= cap) break;
        }
        if(moved>0){DropRecall.start(player,tool,ArtifactKind.EVENTIDE,recalledDrops,radius);ArtifactFeedback.message(player, "drops_gathered", moved);}
        return moved>0;
    }



    /** Query predicates stay side-effect free; permissions are checked immediately before each mutation. */







    private static boolean markSquare(ServerPlayer player, BlockPos center, int radius) {
        var level = player.serverLevel();
        var color = new DustParticleOptions(new Vector3f(.55F, .78F, 1F), 1.35F);
        for (int offset = -radius; offset <= radius; offset += 2) {
            particle(level, player, color, center.offset(offset, 0, -radius));
            particle(level, player, color, center.offset(offset, 0, radius));
            particle(level, player, color, center.offset(-radius, 0, offset));
            particle(level, player, color, center.offset(radius, 0, offset));
        }
        ArtifactFeedback.message(player, "stasis_boundary");
        return true;
    }

    private static void particle(net.minecraft.server.level.ServerLevel level, ServerPlayer player, DustParticleOptions color, BlockPos pos) {
        if (level.hasChunkAt(pos)) AbilityVfx.retired(level,player, color, false, pos.getX() + .5, pos.getY() + .08, pos.getZ() + .5, 3, .08, .03, .08, 0);
    }

    private static boolean dash(ServerPlayer player, int distance) {
        Vec3 start = player.position(), direction = player.getLookAngle().normalize(), last = start;
        var box = player.getBoundingBox();
        for (int step = 1; step <= distance; step++) {
            Vec3 next = start.add(direction.scale(step));
            BlockPos at = BlockPos.containing(next);
            if (!player.serverLevel().hasChunkAt(at) || !player.serverLevel().getWorldBorder().isWithinBounds(at)
                    || !player.serverLevel().noCollision(player, box.move(next.subtract(start)))) break;
            last = next;
        }
        if (last.distanceToSqr(start) < 4) return false;
        player.connection.teleport(last.x, last.y, last.z, player.getYRot(), player.getXRot());
        return true;
    }

    private static boolean blink(ServerPlayer player, int distance) {
        return dash(player, distance);
    }

    private static boolean recallIridiumDrops(ServerPlayer player,ItemStack tool,int radius,int cap) {
        var context=HeldUtilityContext.capture(player,tool);
        Vec3 center = player.getEyePosition();
        // Keep only the nearest capped drops while inspecting a bounded number of entities.
        // This avoids allocating and sorting thousands of candidates in dense farms.
        var targets=EntitySelection.nearest(player.serverLevel(),ItemEntity.class,
                player.getBoundingBox().inflate(radius),
                item->isIridiumRecallTarget(player,item,center,radius),center,cap,IRIDIUM_RECALL_INSPECTION_LIMIT);
        int recalled=0,visualized=0;
        var recalledDrops=new java.util.ArrayList<ItemEntity>();
        for(var item:targets){
            if(recalled>=cap)break;
            var checkedPosition=item.position();var checkedStack=item.getItem();
            boolean allowed=WorldSafety.allowed(player,ArtifactKind.IRIDIUM,item.blockPosition());
            if(!context.valid())break;
            if(!allowed||!item.position().equals(checkedPosition)||item.getItem()!=checkedStack
                    ||!isIridiumRecallTarget(player,item,center,radius))continue;
            Vec3 delta = center.subtract(item.position());
            item.setGlowingTag(true);
            if(delta.lengthSqr()>=.25){
                item.setDeltaMovement(UtilityPullMotion.velocity(delta,item.getDeltaMovement(),.2,.85,1.05));
                item.hasImpulse=true;
            }
            if(visualized<24){ArtifactFeedback.burst(player,ArtifactKind.IRIDIUM,item.blockPosition(),4);visualized++;}
            recalledDrops.add(item);recalled++;
        }
        if(recalled>0){DropRecall.start(player,tool,ArtifactKind.IRIDIUM,recalledDrops,radius);ArtifactFeedback.message(player,"ore_drops_recalled",recalled);}
        return recalled>0;
    }

    /** Shared eligibility rule for the spatial query and its radius/ownership regression tests. */
    public static boolean isIridiumRecallTarget(ServerPlayer player,ItemEntity item,Vec3 center,double radius){
        var data=item.getPersistentData();
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&item.isAlive()&&!item.getItem().isEmpty()&&item.level()==player.level()
                &&Double.isFinite(radius)&&radius>=0D
                &&DropOwnership.allows(player,item)
                &&data.getBoolean("specialpickaxesIridiumOreDrop")
                &&data.hasUUID("specialpickaxesIridiumOwner")
                &&player.getUUID().equals(data.getUUID("specialpickaxesIridiumOwner"))
                &&item.position().distanceToSqr(center)<=radius*radius;
    }

    /** A clear, timed projectile ward follows its owner and catches fast shots every tick. */


    private static boolean seamFace(ServerPlayer player,ItemStack tool) {
        var hit=ArtifactActions.targetHit(player);if(hit.isEmpty())return false;
        var context=HeldUtilityContext.capture(player,tool);var level=player.serverLevel();var origin=hit.get().getBlockPos();var state=level.getBlockState(origin);
        if(!MiningDesigns.seamGeology(state)||PlayerPlacedBlocks.get(level).contains(origin)
                ||!WorldSafety.allowed(player,ArtifactKind.SEAM_RIPPER,origin)
                ||!context.valid()
                ||!level.hasChunkAt(origin)||level.getBlockState(origin)!=state||PlayerPlacedBlocks.get(level).contains(origin))return false;
        Direction face=hit.get().getDirection();
        Direction u=face.getAxis()==Direction.Axis.Y?Direction.EAST:Direction.UP;
        Direction v=face.getAxis()==Direction.Axis.Y?Direction.SOUTH:face.getAxis()==Direction.Axis.X?Direction.SOUTH:Direction.EAST;
        var steps=new ArrayList<WorkStep>(SEAM_FACE_CELLS);
        for(int a=-SEAM_FACE_RADIUS;a<=SEAM_FACE_RADIUS;a++)for(int b=-SEAM_FACE_RADIUS;b<=SEAM_FACE_RADIUS;b++){
            var pos=origin.relative(u,a).relative(v,b);if(!level.hasChunkAt(pos))continue;
            var block=level.getBlockState(pos);
            if(MiningDesigns.seamGeology(block)&&!PlayerPlacedBlocks.get(level).contains(pos))steps.add(new WorkStep.Mine(pos,block,true));
        }
        if(steps.isEmpty())return false;
        steps.sort(Comparator.comparingDouble(step->step.pos().distSqr(origin)));
        if(!WorkQueue.start(player,tool,ArtifactKind.SEAM_RIPPER,steps))return false;
        ArtifactFeedback.preview(player,ArtifactKind.SEAM_RIPPER,steps);
        ArtifactFeedback.message(player,"seam_face_started",steps.size());
        return true;
    }



    static boolean performHeldAlternate(ServerPlayer player, ItemStack tool, ArtifactKind kind) {
        return switch (kind) {
            case CHOIR,CRUCIBLE,INTERREGNUM,WORLDLOOM,AXIOM,HELLSPEC -> MiningAlternates.use(player,tool,kind);
            case PALIMPSEST -> SolarRupture.start(player,tool,SolarRupture.Form.NOVA);
            case EVENTIDE -> magnetDrops(player,tool,48,256);
            case ICARUS -> IcarianFlight.start(player,tool);
            case WORLDBREAKER -> tool.hasTag()&&tool.getTag().contains("copiedSkill")?echo(player,tool,2):
                    WorkQueue.startRegion(player,tool,kind,new TemporalRiftProgram(aimed(player),kind,true));
            case EXODIUM -> starfallSink(player,tool);
            case IRIDIUM -> recallIridiumDrops(player,tool,IRIDIUM_RECALL_RADIUS,96);
            case SEAM_RIPPER -> seamFace(player,tool);
            default -> false;
        };
    }

    private static boolean starfallSink(ServerPlayer player,ItemStack tool){
        var aimed=ArtifactActions.target(player);if(aimed.isEmpty())return false;
        var context=HeldUtilityContext.capture(player,tool);
        BlockPos center=aimed.get();var level=player.serverLevel();var state=level.getBlockState(center);
        if(!WorldSafety.allowed(player,ArtifactKind.EXODIUM,center)||!context.valid()
                ||!level.hasChunkAt(center)||level.getBlockState(center)!=state
                ||PlayerPlacedBlocks.get(level).contains(center)
                ||!MiningDesigns.matrix(state)&&!ArtifactOres.isOre(state)
                ||!WorldSafety.harvestable(player,tool,center))return false;
        var program=new DirectionalProgram(center,Direction.DOWN,DirectionalProgram.Shape.EXODIUM_SINK,ArtifactKind.EXODIUM);
        if(!WorkQueue.startRegion(player,tool,ArtifactKind.EXODIUM,program))return false;
        ArtifactFeedback.starfallSink(player,center);
        ArtifactFeedback.message(player,"starfall_sink_started");
        return true;
    }

}
