package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.common.util.BlockSnapshot;
import org.joml.Vector3f;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Bounded held-tool alternate actions. Curios equipment grants passives only. */
public final class ArtifactTechniques {
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

    private static boolean magnetDrops(ServerPlayer player, int radius, int cap) {
        Vec3 center = player.getEyePosition();
        int moved = 0;
        var drops=EntitySelection.nearest(player.serverLevel(),ItemEntity.class,player.getBoundingBox().inflate(radius),
                e->e.isAlive()&&(e.getOwner()==null||player.getUUID().equals(e.getOwner()))
                        &&e.position().distanceToSqr(center)<=(double)radius*radius,center,cap);
        for (var drop : drops) {
            Vec3 delta = center.subtract(drop.position());
            if (delta.lengthSqr() < .25) continue;
            drop.setDeltaMovement(delta.normalize().scale(.65));
            drop.hasImpulse = true;
            if (++moved >= cap) break;
        }
        if(moved>0)ArtifactFeedback.message(player, "drops_gathered", moved);
        return moved>0;
    }

    private static boolean eatFromInventory(ServerPlayer player) {
        if (!player.getFoodData().needsFood()) return false;
        int bestSlot = -1, bestNutrition = 0;
        float bestSaturation = 0;
        net.minecraft.world.food.FoodProperties bestFood = null;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            var stack = player.getInventory().getItem(slot);
            if (stack.isEmpty()) continue;
            var food = stack.getFoodProperties(player);
            if (food == null) continue;
            if (food.getNutrition() > bestNutrition || food.getNutrition() == bestNutrition && food.getSaturationModifier() > bestSaturation) {
                bestSlot = slot;
                bestNutrition = food.getNutrition();
                bestSaturation = food.getSaturationModifier();
                bestFood = food;
            }
        }
        if (bestSlot < 0 || bestFood == null) return false;
        var stack = player.getInventory().getItem(bestSlot);
        player.getFoodData().eat(bestFood.getNutrition(), bestFood.getSaturationModifier());
        if (!player.isCreative()) stack.shrink(1);
        ArtifactFeedback.message(player, "food_used");
        return true;
    }

    private static boolean knockbackPulse(ServerPlayer player, ArtifactKind kind, int radius, double strength, int cap) {
        Vec3 center = player.position();
        double rangeSqr=(double)radius*radius;
        int pushed = 0;
        for (LivingEntity target : EntitySelection.nearest(player.serverLevel(), LivingEntity.class,
                player.getBoundingBox().inflate(radius), entity -> {
            if (!entity.isAlive() || entity == player || entity.isAlliedTo(player)
                    ||entity.position().distanceToSqr(center)>rangeSqr
                    || !WorldSafety.allowed(player, kind, entity.blockPosition())) return false;
            return !(entity instanceof Player other) || player.canHarmPlayer(other);
        }, center, cap)) {
            Vec3 away = target.position().subtract(center);
            if (away.lengthSqr() < .01) continue;
            target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(strength).add(0, .25, 0)));
            target.hasImpulse = true;
            if (++pushed >= cap) break;
        }
        if(pushed>0)ArtifactFeedback.message(player, "targets_pushed", pushed);
        return pushed>0;
    }

    private static boolean rotateCrucible(ServerPlayer player) {
        ArtifactState.rotate(player, ArtifactKind.CRUCIBLE);
        int mode = ArtifactState.mode(player, ArtifactKind.CRUCIBLE);
        ArtifactFeedback.message(player, "named_mode", net.minecraft.network.chat.Component.translatable(
                "mode.specialpickaxes." + ArtifactInteraction.modeKey(ArtifactKind.CRUCIBLE, mode)));
        return true;
    }

    private static boolean arrestMotion(ServerPlayer player, ArtifactKind kind, BlockPos center, int radius, int cap) {
        int stopped=DomainFields.stasisPulse(player,kind,center,radius,cap,40);
        if(stopped>0)ArtifactFeedback.message(player, "motion_arrested", stopped);
        if(stopped>0)ArtifactFeedback.burst(player,kind,center,8);
        return stopped>0;
    }

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
        if (level.hasChunkAt(pos)) level.sendParticles(player, color, false, pos.getX() + .5, pos.getY() + .08, pos.getZ() + .5, 3, .08, .03, .08, 0);
    }

    private static boolean icarianLift(ServerPlayer player,ItemStack tool) {
        if(player.isPassenger()||player.isSleeping())return false;
        BlockPos base=player.blockPosition();
        int availableRise=player.serverLevel().getMaxBuildHeight()-base.getY()-2;
        boolean started=WorkQueue.startRegion(player,tool,ArtifactKind.ICARUS,new IcarianLiftProgram(base,availableRise));
        if(started){
            ArtifactFeedback.message(player,"icarian_lift_started");
            player.serverLevel().sendParticles(player,ParticleTypes.END_ROD,false,player.getX(),player.getY()+0.2,player.getZ(),12,.25,.2,.25,.04);
        }
        return started;
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

    /** Null Ward collapses nearby hostile shots into a player-only sculk pulse. */
    private static boolean nullWard(ServerPlayer player,int radius,int cap) {
        Vec3 center=player.position();double rangeSqr=(double)radius*radius;int collapsed=0;
        var shots=EntitySelection.nearest(player.serverLevel(),Projectile.class,player.getBoundingBox().inflate(radius),projectile->{
            var owner=projectile.getOwner();
            return projectile.isAlive()&&(owner==null||!owner.isAlliedTo(player))
                    &&projectile.position().distanceToSqr(center)<=rangeSqr
                    &&WorldSafety.allowed(player,ArtifactKind.AXIOM,projectile.blockPosition());
        },center,cap);
        for(var projectile:shots){
            var point=projectile.position();
            player.serverLevel().sendParticles(player,ParticleTypes.SCULK_SOUL,false,point.x,point.y,point.z,8,.12,.12,.12,.015);
            projectile.discard();
            collapsed++;
        }
        if(collapsed==0)return false;
        ArtifactFeedback.nullWard(player,radius);
        ArtifactFeedback.message(player,"nullward",collapsed);
        return true;
    }

    /** Ground-borne echo arrests nearby grounded threats; allies and protected PvP targets are excluded. */
    private static boolean faultEcho(ServerPlayer player,int radius,int cap){
        var level=player.serverLevel();var center=player.position();double rangeSqr=(double)radius*radius;
        var targets=EntitySelection.nearest(level,LivingEntity.class,player.getBoundingBox().inflate(radius),entity->{
            if(!entity.isAlive()||entity==player||!(entity instanceof Monster||entity instanceof Player)
                    ||!entity.onGround()||entity.isAlliedTo(player)
                    ||entity.position().distanceToSqr(center)>rangeSqr||!WorldSafety.allowed(player,ArtifactKind.CHOIR,entity.blockPosition()))return false;
            return !(entity instanceof Player other)||player.canHarmPlayer(other);
        },center,cap);
        if(targets.isEmpty())return false;
        int stopped=0;
        for(var target:targets){
            if(stopped>=cap)break;
            target.setDeltaMovement(0,Math.min(0,target.getDeltaMovement().y),0);target.hasImpulse=true;
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40,2,true,false,true));
            ArtifactFeedback.burst(player,ArtifactKind.CHOIR,target.blockPosition(),3);stopped++;
        }
        for(int i=0;i<12;i++){
            double angle=i*Math.PI/6,x=center.x+Math.cos(angle)*radius,z=center.z+Math.sin(angle)*radius;
            level.sendParticles(player,ParticleTypes.ELECTRIC_SPARK,false,x,player.getY()+.08,z,1,0,0,0,0);
        }
        ArtifactFeedback.message(player,"fault_echo",stopped);return true;
    }

    private static boolean rootSnare(ServerPlayer player,double range) {
        Vec3 center=player.position();double rangeSqr=range*range;
        var targets=EntitySelection.nearest(player.serverLevel(),LivingEntity.class,player.getBoundingBox().inflate(range),entity->{
            if(!entity.isAlive()||entity==player||!(entity instanceof Monster||entity instanceof Player)
                    ||entity.isAlliedTo(player)||entity.position().distanceToSqr(center)>rangeSqr
                    ||!WorldSafety.allowed(player,ArtifactKind.WORLDLOOM,entity.blockPosition()))return false;
            return !(entity instanceof Player other)||player.canHarmPlayer(other);
        },center,16);
        int bound=0;for(var target:targets)if(WorldloomSnare.bind(player,target))bound++;
        if(bound>0)ArtifactFeedback.message(player,"root_snared",bound);
        return bound>0;
    }

    private static boolean recallIridiumDrops(ServerPlayer player, int radius, int cap) {
        Vec3 center = player.getEyePosition();
        int[] recalled = {0}, inspected = {0};
        // Stop without materializing the full ItemEntity list around a dense farm.
        player.serverLevel().getEntities().get(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
                player.getBoundingBox().inflate(radius), item -> {
            if(++inspected[0]>4096)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if(!item.isAlive()||!item.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop")
                    ||!item.getPersistentData().hasUUID("specialpickaxesIridiumOwner")
                    ||!player.getUUID().equals(item.getPersistentData().getUUID("specialpickaxesIridiumOwner")))
                return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
            Vec3 delta = center.subtract(item.position());
            item.setGlowingTag(true);
            if(delta.lengthSqr()<.25)return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
            item.setDeltaMovement(item.getDeltaMovement().scale(.2).add(delta.normalize().scale(.85)));
            item.hasImpulse=true;
            if(++recalled[0]>=cap)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            return inspected[0]>=4096?net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT
                    :net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });
        if(recalled[0]>0)ArtifactFeedback.message(player,"ore_drops_recalled",recalled[0]);
        return recalled[0]>0;
    }

    /** A clear, timed projectile ward follows its owner and catches fast shots every tick. */
    private static boolean crucibleWard(ServerPlayer player,ItemStack tool) {
        int radius=10,duration=160;
        if(!DomainFields.start(player,tool,ArtifactKind.CRUCIBLE,player.blockPosition(),radius,duration))return false;
        ArtifactFeedback.ring(player,ArtifactKind.CRUCIBLE,player.blockPosition(),radius);
        ArtifactFeedback.message(player,"crucible_ward_started");
        return true;
    }

    private static boolean seamFace(ServerPlayer player,ItemStack tool) {
        var target=ArtifactActions.target(player);if(target.isEmpty())return false;
        var level=player.serverLevel();var origin=target.get();var state=level.getBlockState(origin);
        if(!MiningDesigns.seamGeology(state)||PlayerPlacedBlocks.get(level).contains(origin)
                ||!WorldSafety.allowed(player,ArtifactKind.SEAM_RIPPER,origin))return false;
        var hit=level.clip(new net.minecraft.world.level.ClipContext(player.getEyePosition(),
                player.getEyePosition().add(player.getLookAngle().normalize().scale(24)),
                net.minecraft.world.level.ClipContext.Block.OUTLINE,net.minecraft.world.level.ClipContext.Fluid.NONE,player));
        if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return false;
        Direction face=hit.getDirection();
        Direction u=face.getAxis()==Direction.Axis.Y?Direction.EAST:Direction.UP;
        Direction v=face.getAxis()==Direction.Axis.Y?Direction.SOUTH:face.getAxis()==Direction.Axis.X?Direction.SOUTH:Direction.EAST;
        var steps=new ArrayList<WorkStep>(25);
        for(int a=-2;a<=2;a++)for(int b=-2;b<=2;b++){
            var pos=origin.relative(u,a).relative(v,b);if(!level.hasChunkAt(pos))continue;
            var block=level.getBlockState(pos);
            if(MiningDesigns.seamGeology(block)&&!PlayerPlacedBlocks.get(level).contains(pos))steps.add(new WorkStep.Mine(pos,block));
        }
        if(steps.isEmpty())return false;
        steps.sort(Comparator.comparingDouble(step->step.pos().distSqr(origin)));
        if(!WorkQueue.start(player,tool,ArtifactKind.SEAM_RIPPER,steps))return false;
        ArtifactFeedback.preview(player,ArtifactKind.SEAM_RIPPER,steps);
        ArtifactFeedback.message(player,"seam_face_started",steps.size());
        return true;
    }

    private static boolean magmaEruption(ServerPlayer player,ItemStack tool) {
        var level=player.serverLevel();Vec3 from=player.getEyePosition(),to=from.add(player.getLookAngle().normalize().scale(24));
        var end=ArtifactActions.loadedRayEnd(level,from,to);
        var hit=level.clip(new net.minecraft.world.level.ClipContext(from,end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.ANY,player));
        if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return false;
        BlockPos center=hit.getBlockPos();var focused=level.getFluidState(center);var old=level.getBlockState(center);
        if(!focused.is(net.minecraft.tags.FluidTags.LAVA)||!focused.isSource()
                ||old.hasBlockEntity()||PlayerPlacedBlocks.get(level).contains(center)
                ||!WorldSafety.allowed(player,ArtifactKind.HELLSPEC,center)
                ||net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(level,player.gameMode.getGameModeForPlayer(),player,center)<0)return false;
        var snapshot=BlockSnapshot.create(level.dimension(),level,center);var obsidian=Blocks.OBSIDIAN.defaultBlockState();
        if(!level.setBlock(center,obsidian,3))return false;
        if(ForgeEventFactory.onBlockPlace(player,snapshot,Direction.UP)||level.getBlockState(center)!=obsidian){
            if(level.hasChunkAt(center)&&level.getBlockState(center)==obsidian&&level.getFluidState(center).isEmpty())snapshot.restore(true,false);
            return false;
        }
        level.blockUpdated(center,Blocks.OBSIDIAN);
        var bounds=new AABB(center).inflate(8,4,8);int affected=0;
        var targets=EntitySelection.nearest(level,LivingEntity.class,bounds,e->e.isAlive()&&e!=player
                &&!e.isAlliedTo(player)&&e.position().distanceToSqr(Vec3.atCenterOf(center))<=64
                &&WorldSafety.allowed(player,ArtifactKind.HELLSPEC,e.blockPosition())
                &&(!(e instanceof Player other)||player.canHarmPlayer(other)),Vec3.atCenterOf(center),12);
        for(var target:targets){
            Vec3 away=target.position().subtract(Vec3.atCenterOf(center)).normalize();
            if(away.lengthSqr()<.01)away=player.getLookAngle().normalize();
            target.setDeltaMovement(target.getDeltaMovement().scale(.35).add(away.scale(1.65)).add(0,.45,0));target.hasImpulse=true;
            target.setSecondsOnFire(4);affected++;
        }
        ArtifactFeedback.burst(player,ArtifactKind.HELLSPEC,center,20);ArtifactFeedback.ring(player,ArtifactKind.HELLSPEC,center,5);
        ArtifactFeedback.message(player,"magma_erupted",affected);return true;
    }

    static boolean performHeldAlternate(ServerPlayer player, ItemStack tool, ArtifactKind kind) {
        return switch (kind) {
            case PALIMPSEST -> knockbackPulse(player, kind, 6, 1.0, 24);
            case CHOIR -> faultEcho(player, 6, 4);
            case EVENTIDE -> magnetDrops(player, 16, 64);
            case CRUCIBLE -> crucibleWard(player,tool);
            case INTERREGNUM -> ArtifactState.mode(player,kind)==1
                    ? arrestMotion(player,kind,player.blockPosition(),8,24)
                    : DomainFields.relocate(player,aimed(player));
            case WORLDLOOM -> rootSnare(player, 10);
            case ICARUS -> icarianLift(player,tool);
            case AXIOM -> nullWard(player,8,24);
            case WORLDBREAKER -> echo(player, tool, 2);
            case EXODIUM -> starfallSink(player,tool);
            case IRIDIUM -> recallIridiumDrops(player, 24, 96);
            case HELLSPEC -> magmaEruption(player,tool);
            case SEAM_RIPPER -> seamFace(player,tool);
            default -> false;
        };
    }

    private static boolean starfallSink(ServerPlayer player,ItemStack tool){
        var aimed=ArtifactActions.target(player);if(aimed.isEmpty())return false;
        BlockPos center=aimed.get();var level=player.serverLevel();var state=level.getBlockState(center);
        if(!WorldSafety.allowed(player,ArtifactKind.EXODIUM,center)
                ||PlayerPlacedBlocks.get(level).contains(center)
                ||!MiningDesigns.matrix(state)&&!ArtifactOres.isOre(state)
                ||!WorldSafety.harvestable(player,tool,center))return false;
        var program=new DirectionalProgram(center,Direction.DOWN,DirectionalProgram.Shape.EXODIUM_SINK,ArtifactKind.EXODIUM);
        if(!WorkQueue.startRegion(player,tool,ArtifactKind.EXODIUM,program))return false;
        ArtifactFeedback.ring(player,ArtifactKind.EXODIUM,center,2);
        ArtifactFeedback.message(player,"starfall_sink_started");
        return true;
    }

}
