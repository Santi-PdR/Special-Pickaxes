package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Bounded held-tool alternate actions. Curios equipment grants passives only. */
public final class ArtifactTechniques {
    private ArtifactTechniques() {}
    private static final List<BlockPos> QUENCH_OFFSETS=createQuenchOffsets();

    private static List<BlockPos> createQuenchOffsets(){
        var offsets=new ArrayList<BlockPos>(123);
        for(int x=-3;x<=3;x++)for(int y=-3;y<=3;y++)for(int z=-3;z<=3;z++)
            if(x*x+y*y+z*z<=9)offsets.add(new BlockPos(x,y,z));
        offsets.sort(Comparator.comparingDouble(pos->pos.distSqr(BlockPos.ZERO)));
        return List.copyOf(offsets);
    }

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
        for (var drop : player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius),
                e -> e.isAlive() && (e.getOwner() == null || player.getUUID().equals(e.getOwner())))) {
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
        int pushed = 0;
        for (LivingEntity target : EntitySelection.nearest(player.serverLevel(), LivingEntity.class,
                player.getBoundingBox().inflate(radius), entity -> {
            if (!entity.isAlive() || entity == player || entity.isAlliedTo(player)
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
        boolean started=WorkQueue.startRegion(player,tool,ArtifactKind.ICARUS,new IcarianLiftProgram(player.blockPosition()));
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

    private static boolean nullwave(ServerPlayer player,int radius,int cap) {
        Vec3 center=player.position();double rangeSqr=(double)radius*radius;
        var targets=EntitySelection.nearest(player.serverLevel(),LivingEntity.class,player.getBoundingBox().inflate(radius),entity->{
            if(!entity.isAlive()||entity==player||!(entity instanceof Monster||entity instanceof Player)
                    ||entity.isAlliedTo(player)||entity.position().distanceToSqr(center)>rangeSqr
                    ||!WorldSafety.allowed(player,ArtifactKind.AXIOM,entity.blockPosition()))return false;
            return !(entity instanceof Player other)||player.canHarmPlayer(other);
        },center,cap);
        if(targets.isEmpty())return false;
        int repelled=0;
        for(var target:targets){
            Vec3 away=target.position().subtract(center);if(away.lengthSqr()<.01)away=new Vec3(0,0,1);
            target.setDeltaMovement(target.getDeltaMovement().add(away.normalize().scale(1.0).add(0,.35,0)));
            target.hasImpulse=true;
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.GLOWING,60,0,true,false,true));
            target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40,1,true,false,true));
            ArtifactFeedback.burst(player,ArtifactKind.AXIOM,target.blockPosition(),4);repelled++;
        }
        for(int i=0;i<16;i++){
            double angle=i*Math.PI/8,x=center.x+Math.cos(angle)*radius,z=center.z+Math.sin(angle)*radius;
            player.serverLevel().sendParticles(player,ParticleTypes.SCULK_SOUL,false,x,center.y+.15,z,1,0,0,0,0);
        }
        ArtifactFeedback.message(player,"nullwave",repelled);return true;
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

    private static boolean deflectProjectiles(ServerPlayer player, ArtifactKind kind, int radius, int cap) {
        int deflected = 0;
        for (Projectile projectile : EntitySelection.nearest(player.serverLevel(), Projectile.class,
                player.getBoundingBox().inflate(radius),
                entity -> entity.isAlive() && (entity.getOwner() == null || !entity.getOwner().isAlliedTo(player))
                        && WorldSafety.allowed(player, kind, entity.blockPosition()), player.position(), cap)) {
            Vec3 away = projectile.position().subtract(player.position()).normalize();
            projectile.setDeltaMovement(away.scale(Math.max(.8, projectile.getDeltaMovement().length())));
            projectile.hasImpulse = true;
            if (++deflected >= cap) break;
        }
        if(deflected>0)ArtifactFeedback.message(player, "projectiles_deflected", deflected);
        return deflected>0;
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

    /** Rends one visible hostile's foremost intact armor seam; no damage or potion effects. */
    private static boolean seamRend(ServerPlayer player) {
        Vec3 look=player.getLookAngle().normalize(),eye=player.getEyePosition();double range=8;
        AABB bounds=player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0,0.75,1.0);
        LivingEntity target=EntitySelection.nearest(player.serverLevel(),LivingEntity.class,bounds,entity->{
            if(!entity.isAlive()||entity==player||entity.isAlliedTo(player)
                    ||!WorldSafety.allowed(player,ArtifactKind.SEAM_RIPPER,entity.blockPosition()))return false;
            if(entity instanceof Player other&&!player.canHarmPlayer(other))return false;
            Vec3 delta=entity.getBoundingBox().getCenter().subtract(eye);
            return delta.lengthSqr()<=range*range&&delta.normalize().dot(look)>=.78&&player.hasLineOfSight(entity);
        },player.position(),1).stream().findFirst().orElse(null);
        if(target==null)return false;
        EquipmentSlot[] priority={EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.HEAD,EquipmentSlot.FEET};
        EquipmentSlot slot=null;ItemStack armor=ItemStack.EMPTY;
        for(var candidate:priority){var worn=target.getItemBySlot(candidate);if(!worn.isEmpty()&&worn.isDamageableItem()
                &&worn.getDamageValue()<worn.getMaxDamage()){slot=candidate;armor=worn;break;}}
        if(slot==null)return false;
        int before=armor.getDamageValue(),max=armor.getMaxDamage(),requested=Math.min(48,max-before);var damagedSlot=slot;
        armor.hurtAndBreak(requested,target,entity->entity.broadcastBreakEvent(damagedSlot));
        int applied=armor.isEmpty()?requested:Math.max(0,armor.getDamageValue()-before);
        if(applied<=0)return false;
        ArtifactFeedback.burst(player,ArtifactKind.SEAM_RIPPER,target.blockPosition(),10);
        return true;
    }

    private static boolean quenchLava(ServerPlayer player,ItemStack tool) {
        var level=player.serverLevel();Vec3 from=player.getEyePosition(),to=from.add(player.getLookAngle().normalize().scale(24));
        var end=ArtifactActions.loadedRayEnd(level,from,to);
        var hit=level.clip(new net.minecraft.world.level.ClipContext(from,end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.ANY,player));
        if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return false;
        BlockPos center=hit.getBlockPos();var focused=level.getFluidState(center);
        if(!focused.is(net.minecraft.tags.FluidTags.LAVA)||!focused.isSource())return false;

        var steps=new java.util.ArrayList<WorkStep>(32);
        // The cached order is center-out, so a source cap always favors nearby lava.
        for(BlockPos offset:QUENCH_OFFSETS){
            if(steps.size()>=32)break;
            BlockPos pos=center.offset(offset);if(!level.hasChunkAt(pos)||!WorldSafety.allowed(player,ArtifactKind.HELLSPEC,pos))continue;
            var state=level.getBlockState(pos);var fluid=level.getFluidState(pos);
            if(!fluid.is(net.minecraft.tags.FluidTags.LAVA)||!fluid.isSource()||state.hasBlockEntity())continue;
            steps.add(new WorkStep(){
                @Override public BlockPos pos(){return pos;}
                @Override public boolean apply(ServerPlayer actor,ItemStack held,ArtifactKind kind){
                    var current=level.getBlockState(pos);var currentFluid=level.getFluidState(pos);
                    if(!level.hasChunkAt(pos)||!currentFluid.is(net.minecraft.tags.FluidTags.LAVA)||!currentFluid.isSource()
                            ||current.hasBlockEntity()||!WorldSafety.allowed(actor,kind,pos)
                            ||net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(level,actor.gameMode.getGameModeForPlayer(),actor,pos)<0)return false;
                    var snapshot=BlockSnapshot.create(level.dimension(),level,pos);var obsidian=Blocks.OBSIDIAN.defaultBlockState();
                    if(!level.setBlock(pos,obsidian,3))return false;
                    if(ForgeEventFactory.onBlockPlace(actor,snapshot,Direction.UP)||level.getBlockState(pos)!=obsidian){
                        if(level.hasChunkAt(pos)&&level.getBlockState(pos)==obsidian&&level.getFluidState(pos).isEmpty())snapshot.restore(true,false);
                        return false;
                    }
                    level.blockUpdated(pos,Blocks.OBSIDIAN);ArtifactFeedback.burst(actor,ArtifactKind.HELLSPEC,pos,3);return true;
                }
            });
        }
        if(steps.isEmpty())return false;
        int sources=steps.size();if(!WorkQueue.start(player,tool,ArtifactKind.HELLSPEC,steps))return false;
        ArtifactFeedback.ring(player,ArtifactKind.HELLSPEC,center,3);
        ArtifactFeedback.message(player,"lava_quenched",sources);return true;
    }

    static boolean performHeldAlternate(ServerPlayer player, ItemStack tool, ArtifactKind kind) {
        return switch (kind) {
            case PALIMPSEST -> knockbackPulse(player, kind, 6, 1.0, 24);
            case CHOIR -> faultEcho(player, 6, 4);
            case EVENTIDE -> magnetDrops(player, 16, 64);
            case CRUCIBLE -> deflectProjectiles(player, kind, 10, 32);
            case INTERREGNUM -> ArtifactState.mode(player,kind)==1
                    ? arrestMotion(player,kind,player.blockPosition(),8,24)
                    : DomainFields.relocate(player,aimed(player));
            case WORLDLOOM -> rootSnare(player, 10);
            case ICARUS -> icarianLift(player,tool);
            case AXIOM -> nullwave(player,8,24);
            case WORLDBREAKER -> echo(player, tool, 2);
            case EXODIUM -> starfallSink(player,tool);
            case IRIDIUM -> recallIridiumDrops(player, 24, 96);
            case HELLSPEC -> quenchLava(player,tool);
            case SEAM_RIPPER -> seamRend(player);
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
