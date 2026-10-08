package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import java.util.*;

/** No player, item entity, AI/NBT flag or block-entity ticking is changed by these fields. */
public final class DomainFields {
    private record Frozen(Entity entity,Vec3 position,Vec3 velocity) {}
    private static final class Field {
        final ServerPlayer owner;final ItemStack tool;final ArtifactKind kind;BlockPos center;final int radius;
        final int mode;int minedSincePulse;long pulseReady;
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        final Map<UUID,Frozen> frozen=new HashMap<>();final Set<UUID> observed=new HashSet<>();long expires;
        Field(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos center,int radius) {
            owner=p;dimension=p.level().dimension();this.tool=tool;this.kind=kind;this.center=center;this.radius=radius;mode=ArtifactState.mode(p,kind);expires=ArtifactState.now(p)+ArtifactConfig.FIELD_TIME.get();
        }
    }
    private static final Map<UUID,Field> FIELDS=new HashMap<>();
    /** Membership counts keep combat-event lookups constant-time when fields overlap. */
    private static final Map<UUID,Integer> FROZEN_ENTITIES=new HashMap<>();
    private DomainFields() {}
    public static boolean start(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,int radius) {
        if((kind!=ArtifactKind.INTERREGNUM&&kind!=ArtifactKind.EVENTIDE)||!FIELDS.containsKey(p.getUUID())&&FIELDS.size()>=ArtifactConfig.ACTIVE_JOBS.get())return false;
        stop(p);FIELDS.put(p.getUUID(),new Field(p,tool,kind,pos.immutable(),radius));return true;
    }
    public static boolean relocate(ServerPlayer p,BlockPos destination){
        var field=FIELDS.get(p.getUUID());
        if(field==null||field.kind!=ArtifactKind.INTERREGNUM||field.mode!=0
                ||!WorldSafety.allowed(p,ArtifactKind.INTERREGNUM,destination))return false;
        var previous=field.center;field.center=destination.immutable();
        ArtifactFeedback.trace(p,field.kind,previous,field.center);
        ArtifactFeedback.domain(p,field.kind,field.center,field.radius,Math.max(2,field.radius/2));
        ArtifactFeedback.message(p,"domain_relocated");
        return true;
    }
    private static void release(Field f) {
        f.frozen.forEach((id,v) -> {
            if(v.entity.isAlive()) v.entity.setDeltaMovement(clamp(v.velocity));
            removeFrozenMembership(id);
        });
        f.frozen.clear();f.owner.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.DOMINION.get());
    }
    private static void removeFrozenMembership(UUID id) {
        FROZEN_ENTITIES.computeIfPresent(id,(ignored,count)->count<=1?null:count-1);
    }
    private static Vec3 clamp(Vec3 v) { return v.lengthSqr()>9?v.normalize().scale(3):v; }
    private static void holdFrozen(net.minecraft.server.level.ServerLevel level,Field field) {
        for(var frozen:field.frozen.values())if(frozen.entity.isAlive()){
            var box=frozen.entity.getBoundingBox().move(frozen.position.subtract(frozen.entity.position()));
            if(level.noCollision(frozen.entity,box))frozen.entity.setPos(frozen.position);
            frozen.entity.setDeltaMovement(Vec3.ZERO);frozen.entity.hasImpulse=true;
        }
    }
    public static void stop(ServerPlayer p) { var field=FIELDS.remove(p.getUUID());if(field!=null) release(field); }
    public static void clear() { FIELDS.values().forEach(DomainFields::release);FIELDS.clear();FROZEN_ENTITIES.clear(); }
    public static boolean active(ServerPlayer p){return FIELDS.containsKey(p.getUUID());}
    public static boolean contains(ServerPlayer p,BlockPos pos) {
        var f=FIELDS.get(p.getUUID());if(f==null||p.level().dimension()!=f.dimension||p.getMainHandItem()!=f.tool||ArtifactState.now(p)>f.expires)return false;
        if(f.kind==ArtifactKind.INTERREGNUM&&f.mode==1){var owner=p.blockPosition();return Math.abs(pos.getX()-owner.getX())<=2&&Math.abs(pos.getY()-owner.getY())<=2&&Math.abs(pos.getZ()-owner.getZ())<=2;}
        if(f.kind==ArtifactKind.INTERREGNUM)return Math.abs(pos.getX()-f.center.getX())<=f.radius&&Math.abs(pos.getZ()-f.center.getZ())<=f.radius&&Math.abs(pos.getY()-f.center.getY())<=Math.max(2,f.radius/2);
        return f.kind==ArtifactKind.EVENTIDE&&pos.distSqr(f.center)<=f.radius*f.radius;
    }
    public static boolean frozen(Entity entity) {
        return entity!=null && FROZEN_ENTITIES.containsKey(entity.getUUID());
    }
    public static void feed(ServerPlayer p,ItemStack tool,BlockPos pos,net.minecraft.world.level.block.state.BlockState minedState) {
        var field=FIELDS.get(p.getUUID());
        if(field==null||!contains(p,pos))return;
        field.expires=Math.min(ArtifactState.now(p)+ArtifactConfig.FIELD_TIME.get(),field.expires+20);
        if(field.kind==ArtifactKind.EVENTIDE&&p.getMainHandItem()==tool&&minedState.is(net.minecraftforge.common.Tags.Blocks.STONE)
                &&++field.minedSincePulse>=4&&ArtifactState.now(p)>=field.pulseReady) {
            field.minedSincePulse=0;field.pulseReady=ArtifactState.now(p)+10;
            var pulse=ArtifactActions.gravityPulse(p,pos,minedState,field.center,field.mode);
            int added=0;for(var step:pulse)if(added<6&&WorkQueue.append(p,tool,ArtifactKind.EVENTIDE,step))added++;
            if(added>0)ArtifactFeedback.burst(p,ArtifactKind.EVENTIDE,pos,6);
        }
    }
    public static void tick() {
        var iterator=FIELDS.values().iterator();
        while(iterator.hasNext()) {
            var f=iterator.next();var p=f.owner;var level=p.serverLevel();
            if(!p.isAlive() || p.isRemoved() || p.getMainHandItem()!=f.tool || f.tool.isEmpty()
                    || p.level().dimension()!=f.dimension || ArtifactState.now(p)>f.expires || !WorldSafety.allowed(p,f.kind,f.center)) {
                if(p.isAlive()&&!p.isRemoved()&&p.getMainHandItem()==f.tool&&p.level().dimension()==f.dimension&&ArtifactState.now(p)>f.expires){ArtifactFeedback.message(p,"released");ArtifactFeedback.cue(p,"complete");}
                release(f);iterator.remove();continue;
            }
            if(f.kind==ArtifactKind.INTERREGNUM && contains(p,p.blockPosition())) {
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.DOMINION.get(),12,0,false,true,true));
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,40,0,false,true,true));
                if(f.mode==0){
                    p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,12,4,false,true,true));
                    p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,40,3,false,true,true));
                    p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE,12,1,false,true,true));
                    if(p.tickCount%20==0)p.getFoodData().eat(1,0.2F);
                } else if(p.tickCount%100==0)p.getFoodData().eat(1,0.2F);
            }
            if(f.kind==ArtifactKind.INTERREGNUM&&f.mode==0&&p.tickCount%10==0){
                var bounds=new AABB(f.center).inflate(f.radius+1,Math.max(2,f.radius/2)+1,f.radius+1);int supported=0;
                for(var ally:level.getEntitiesOfClass(ServerPlayer.class,bounds,q->q!=p&&q.isAlive()&&q.isAlliedTo(p)&&contains(p,q.blockPosition()))){
                    ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.DOMINION.get(),40,0,true,false,true));
                    ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,80,0,true,false,true));
                    ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED,40,2,true,false,true));
                    ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,40,1,true,false,true));
                    if(ally.tickCount%100==0)ally.getFoodData().eat(1,0.2F);
                    if(++supported>=16)break;
                }
            }
            if(f.kind==ArtifactKind.EVENTIDE) {
                if(contains(p,p.blockPosition()))
                    p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED,12,2,false,true,true));
                if(p.tickCount%20==0){ArtifactFeedback.ring(p,f.kind,f.center,f.radius);RelicEffects.emit(p,f.kind,"sustain",Vec3.atCenterOf(f.center));}
                continue; // Eventide shapes mining only; it never moves or damages entities.
            }
            if(f.mode==1){if(p.tickCount%20==0)ArtifactFeedback.domain(p,f.kind,p.blockPosition(),2,2);continue;}
            if(p.tickCount%20==0)ArtifactFeedback.domain(p,f.kind,f.center,f.radius,Math.max(2,f.radius/2));
            // Keep already frozen entities pinned every tick, but scan for new targets every other tick.
            if((p.tickCount&1)!=0){holdFrozen(level,f);continue;}
            var centerPosition=Vec3.atCenterOf(f.center);
            double radiusSqr=(double)f.radius*f.radius;
            var entities=level.getEntitiesOfClass(Entity.class,new AABB(f.center).inflate(f.radius),e ->
                e.isAlive() && (e instanceof Projectile || f.kind!=ArtifactKind.AEGIS && e instanceof Monster)
                && !e.isAlliedTo(p) && (f.kind==ArtifactKind.INTERREGNUM?contains(p,e.blockPosition()):e.position().distanceToSqr(centerPosition)<=radiusSqr));
            int targetLimit=ArtifactConfig.FIELD_TARGETS.get();
            if(entities.size()>targetLimit)entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(centerPosition)));
            var current=f.observed;current.clear();int processed=0;
            for(var entity:entities) {
                if(processed>=targetLimit) break;
                if(entity instanceof Projectile projectile && projectile.getOwner()!=null
                        && (projectile.getOwner()==p || projectile.getOwner().isAlliedTo(p) || f.kind==ArtifactKind.AEGIS && projectile.getOwner() instanceof net.minecraft.world.entity.player.Player)) continue;
                if(!WorldSafety.allowed(p,f.kind,entity.blockPosition())) continue;
                processed++;current.add(entity.getUUID());
                if(f.kind==ArtifactKind.AEGIS){
                    var velocity=entity.getDeltaMovement();if(velocity.lengthSqr()<0.00001 || entity instanceof net.minecraft.world.entity.projectile.AbstractArrow && !level.noCollision(entity,entity.getBoundingBox()))continue;
                    var away=entity.position().subtract(centerPosition).normalize();
                    if(ArtifactState.mode(p,f.kind)==1)away=new Vec3(-away.z,away.y*0.2,away.x).normalize();
                    entity.setDeltaMovement(away.scale(Math.min(3,Math.max(0.25,velocity.length()))));
                } else if(f.kind==ArtifactKind.INTERREGNUM) {
                    var original=f.frozen.get(entity.getUUID());
                    if(original==null) {
                        original=new Frozen(entity,entity.position(),entity.getDeltaMovement());
                        f.frozen.put(entity.getUUID(),original);
                        FROZEN_ENTITIES.merge(entity.getUUID(),1,Integer::sum);
                    }
                    var box=entity.getBoundingBox().move(original.position.subtract(entity.position()));
                    if(level.noCollision(entity,box)) { entity.setPos(original.position);entity.setDeltaMovement(Vec3.ZERO); }
                } else {
                    var delta=centerPosition.subtract(entity.position()).normalize();
                    double sign=ArtifactState.mode(p,f.kind)%2==0?1:-1;
                    entity.setDeltaMovement(clamp(entity.getDeltaMovement().scale(0.6).add(delta.scale(sign*ArtifactConfig.FIELD_FORCE.get()))));
                }
                entity.hasImpulse=true;
            }
            f.frozen.entrySet().removeIf(entry -> {
                if(current.contains(entry.getKey())) return false;
                if(entry.getValue().entity.isAlive()) entry.getValue().entity.setDeltaMovement(clamp(entry.getValue().velocity));
                removeFrozenMembership(entry.getKey());
                return true;
            });
            if(p.tickCount%10==0){ArtifactFeedback.ring(p,f.kind,f.center,f.radius);RelicEffects.emit(p,f.kind,"sustain",centerPosition);}
        }
    }
}
