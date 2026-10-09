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

    private static boolean knockbackTarget(ServerPlayer player, ArtifactKind kind, double range, double strength) {
        Vec3 look = player.getLookAngle().normalize(), eye = player.getEyePosition();
        AABB bounds = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.25, 1.0, 1.25);
        LivingEntity target = player.serverLevel().getEntitiesOfClass(LivingEntity.class, bounds, entity -> {
            if (!entity.isAlive() || entity == player || entity.isAlliedTo(player)
                    || !WorldSafety.allowed(player, kind, entity.blockPosition())) return false;
            if (entity instanceof Player other && !player.canHarmPlayer(other)) return false;
            Vec3 delta = entity.getBoundingBox().getCenter().subtract(eye);
            return delta.lengthSqr() <= range * range && delta.normalize().dot(look) >= .72;
        }).stream().min(java.util.Comparator.comparingDouble(entity -> entity.distanceToSqr(player))).orElse(null);
        if (target == null) return false;
        target.setDeltaMovement(target.getDeltaMovement().add(look.scale(strength).add(0, kind==ArtifactKind.ICARUS?.48:.18, 0)));
        target.hasImpulse = true;
        double fx=target.getX(),fy=target.getY()+target.getBbHeight()/2,fz=target.getZ();
        if(kind==ArtifactKind.ICARUS){
            var feather=new DustParticleOptions(new Vector3f(1F,.86F,.58F),1.25F);
            player.serverLevel().sendParticles(player,feather,false,fx,fy,fz,18,.38,.65,.38,.035);
            player.serverLevel().sendParticles(player,ParticleTypes.CLOUD,false,fx,fy,fz,7,.28,.25,.28,.08);
        }else player.serverLevel().sendParticles(player, ParticleTypes.SWEEP_ATTACK, false, fx, fy, fz, 8, .2, .2, .2, .04);
        return true;
    }

    private static boolean knockbackPulse(ServerPlayer player, ArtifactKind kind, int radius, double strength, int cap) {
        Vec3 center = player.position();
        int pushed = 0;
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), entity -> {
            if (!entity.isAlive() || entity == player || entity.isAlliedTo(player)
                    || !WorldSafety.allowed(player, kind, entity.blockPosition())) return false;
            return !(entity instanceof Player other) || player.canHarmPlayer(other);
        })) {
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

    private static boolean lift(ServerPlayer player) {
        player.setDeltaMovement(player.getDeltaMovement().add(0, 1.05, 0));
        player.hasImpulse = true;
        player.serverLevel().sendParticles(player, ParticleTypes.END_ROD, false, player.getX(), player.getY(), player.getZ(), 16, .45, .2, .45, .08);
        return true;
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

    private static boolean pullHostiles(ServerPlayer player, ArtifactKind kind, BlockPos center, int radius, int cap) {
        Vec3 point = Vec3.atCenterOf(center);
        int pulled = 0;
        for (var mob : player.serverLevel().getEntitiesOfClass(Monster.class, new AABB(center).inflate(radius),
                m -> m.isAlive() && !m.isAlliedTo(player) && WorldSafety.allowed(player, kind, m.blockPosition()))) {
            Vec3 delta = point.subtract(mob.position());
            if (delta.lengthSqr() > .01) mob.setDeltaMovement(mob.getDeltaMovement().add(delta.normalize().scale(.6)));
            mob.hasImpulse = true;
            if (++pulled >= cap) break;
        }
        if(pulled>0){
            player.serverLevel().sendParticles(player, ParticleTypes.PORTAL, false, point.x, point.y, point.z, 24, .8, .8, .8, .08);
            ArtifactFeedback.message(player, "hollow_pulse", pulled);
        }
        return pulled>0;
    }

    /** Ground-borne echo arrests nearby grounded threats; allies and protected PvP targets are excluded. */
    private static boolean faultEcho(ServerPlayer player,int radius,int cap){
        var level=player.serverLevel();var center=player.position();double rangeSqr=(double)radius*radius;
        var candidates=level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),entity->{
            if(!entity.isAlive()||entity==player||!(entity instanceof Monster||entity instanceof Player)
                    ||!entity.onGround()||entity.isAlliedTo(player)
                    ||entity.position().distanceToSqr(center)>rangeSqr||!WorldSafety.allowed(player,ArtifactKind.CHOIR,entity.blockPosition()))return false;
            return !(entity instanceof Player other)||player.canHarmPlayer(other);
        });
        var targets=EntitySelection.nearest(candidates,center,cap);
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
        for (Projectile projectile : player.serverLevel().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(radius),
                entity -> entity.isAlive() && (entity.getOwner() == null || !entity.getOwner().isAlliedTo(player))
                        && WorldSafety.allowed(player, kind, entity.blockPosition()))) {
            Vec3 away = projectile.position().subtract(player.position()).normalize();
            projectile.setDeltaMovement(away.scale(Math.max(.8, projectile.getDeltaMovement().length())));
            projectile.hasImpulse = true;
            if (++deflected >= cap) break;
        }
        if(deflected>0)ArtifactFeedback.message(player, "projectiles_deflected", deflected);
        return deflected>0;
    }

    private static boolean rootSnare(ServerPlayer player, double range) {
        LivingEntity target = targetInLook(player, ArtifactKind.WORLDLOOM, range);
        if (target == null) return false;
        if(!WorldloomSnare.bind(player,target))return false;
        ArtifactFeedback.message(player,"root_snared",target.getDisplayName());return true;
    }

    private static LivingEntity targetInLook(ServerPlayer player, ArtifactKind kind, double range) {
        Vec3 look = player.getLookAngle().normalize(), eye = player.getEyePosition();
        AABB bounds = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.25, 1.0, 1.25);
        return player.serverLevel().getEntitiesOfClass(LivingEntity.class, bounds, entity -> {
            if (!entity.isAlive() || entity == player || entity.isAlliedTo(player)
                    || !WorldSafety.allowed(player, kind, entity.blockPosition())) return false;
            if (entity instanceof Player other && !player.canHarmPlayer(other)) return false;
            Vec3 delta = entity.getBoundingBox().getCenter().subtract(eye);
            return delta.lengthSqr() <= range * range && delta.normalize().dot(look) >= .72;
        }).stream().min(java.util.Comparator.comparingDouble(entity -> entity.distanceToSqr(player))).orElse(null);
    }

    private static boolean blinkBehindTarget(ServerPlayer player, double range) {
        LivingEntity target = targetInLook(player, ArtifactKind.EXODIUM, range);
        if (target == null || player.isPassenger() || player.isSleeping()
                || !WorldSafety.allowed(player,ArtifactKind.EXODIUM,target.blockPosition())) return false;
        Vec3 destination = target.position().subtract(target.getLookAngle().normalize().scale(1.5));
        BlockPos at = BlockPos.containing(destination);
        if (!WorldSafety.allowed(player,ArtifactKind.EXODIUM,at)||!WorldSafety.freeBody(player,destination)) return false;
        player.connection.teleport(destination.x, destination.y, destination.z, player.getYRot(), player.getXRot());
        return true;
    }

    private static boolean recallIridiumDrops(ServerPlayer player, int radius, int cap) {
        Vec3 center = player.getEyePosition();
        int recalled = 0;
        for (ItemEntity drop : player.serverLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius),
                item -> item.isAlive() && item.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop")
                        && item.getPersistentData().hasUUID("specialpickaxesIridiumOwner")
                        && player.getUUID().equals(item.getPersistentData().getUUID("specialpickaxesIridiumOwner")))) {
            Vec3 delta = center.subtract(drop.position());
            drop.setGlowingTag(true);
            if (delta.lengthSqr() < .25) continue;
            drop.setDeltaMovement(drop.getDeltaMovement().scale(.2).add(delta.normalize().scale(.85)));
            drop.hasImpulse = true;
            if (++recalled >= cap) break;
        }
        if(recalled>0)ArtifactFeedback.message(player,"ore_drops_recalled",recalled);
        return recalled>0;
    }

    /** Rends one visible hostile's foremost intact armor seam; no damage or potion effects. */
    private static boolean seamRend(ServerPlayer player) {
        Vec3 look=player.getLookAngle().normalize(),eye=player.getEyePosition();double range=8;
        AABB bounds=player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0,0.75,1.0);
        LivingEntity target=player.serverLevel().getEntitiesOfClass(LivingEntity.class,bounds,entity->{
            if(!entity.isAlive()||entity==player||entity.isAlliedTo(player)
                    ||!WorldSafety.allowed(player,ArtifactKind.SEAM_RIPPER,entity.blockPosition()))return false;
            if(entity instanceof Player other&&!player.canHarmPlayer(other))return false;
            Vec3 delta=entity.getBoundingBox().getCenter().subtract(eye);
            return delta.lengthSqr()<=range*range&&delta.normalize().dot(look)>=.78&&player.hasLineOfSight(entity);
        }).stream().min(java.util.Comparator.comparingDouble(entity->entity.distanceToSqr(player))).orElse(null);
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
        var level=player.serverLevel();Vec3 from=player.getEyePosition(),look=player.getLookAngle().normalize();
        var steps=new java.util.ArrayList<WorkStep>(24);BlockPos previous=null;
        for(int sample=1;sample<=96&&steps.size()<24;sample++){
            Vec3 point=from.add(look.scale(sample*.25));BlockPos pos=BlockPos.containing(point);
            if(pos.equals(previous))continue;previous=pos;
            if(!level.hasChunkAt(pos))break;
            var state=level.getBlockState(pos);var fluid=level.getFluidState(pos);
            if(fluid.is(net.minecraft.tags.FluidTags.LAVA)){
                if(!WorldSafety.allowed(player,ArtifactKind.HELLSPEC,pos))break;
                if(fluid.isSource()&&!state.hasBlockEntity())steps.add(new WorkStep(){
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
                continue;
            }
            if(!fluid.isEmpty()||!state.getCollisionShape(level,pos).isEmpty())break;
        }
        if(steps.isEmpty())return false;
        int sources=steps.size();if(!WorkQueue.start(player,tool,ArtifactKind.HELLSPEC,steps))return false;
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
            case ICARUS -> knockbackTarget(player, kind, 7, 1.35);
            case AXIOM -> pullHostiles(player, kind, aimed(player), 9, 24);
            case WORLDBREAKER -> echo(player, tool, 2);
            case EXODIUM -> starfold(player);
            case IRIDIUM -> recallIridiumDrops(player, 24, 96);
            case HELLSPEC -> quenchLava(player,tool);
            case SEAM_RIPPER -> seamRend(player);
            default -> false;
        };
    }

    private static boolean starfold(ServerPlayer player){
        if(!blinkBehindTarget(player,16))return false;
        ArtifactFeedback.message(player,"exodium_blink");
        return true;
    }

}
