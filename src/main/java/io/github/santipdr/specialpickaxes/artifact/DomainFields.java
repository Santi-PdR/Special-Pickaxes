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
        final ServerPlayer owner;final ItemStack tool;final ArtifactKind kind;final BlockPos center;final int radius;
        final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        final Map<UUID,Frozen> frozen=new HashMap<>();long expires;
        Field(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos center,int radius) {
            owner=p;dimension=p.level().dimension();this.tool=tool;this.kind=kind;this.center=center;this.radius=radius;expires=ArtifactState.now(p)+ArtifactConfig.FIELD_TIME.get();
        }
    }
    private static final Map<UUID,Field> FIELDS=new HashMap<>();
    private DomainFields() {}
    public static void start(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,int radius) {
        stop(p);FIELDS.put(p.getUUID(),new Field(p,tool,kind,pos.immutable(),radius));
    }
    private static void release(Field f) {
        f.frozen.values().forEach(v -> { if(v.entity.isAlive()) v.entity.setDeltaMovement(clamp(v.velocity.scale(2))); });
        f.frozen.clear();f.owner.removeEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.DOMINION.get());
    }
    private static Vec3 clamp(Vec3 v) { return v.lengthSqr()>9?v.normalize().scale(3):v; }
    public static void stop(ServerPlayer p) { var field=FIELDS.remove(p.getUUID());if(field!=null) release(field); }
    public static void clear() { FIELDS.values().forEach(DomainFields::release);FIELDS.clear(); }
    public static boolean active(ServerPlayer p){return FIELDS.containsKey(p.getUUID());}
    public static boolean contains(ServerPlayer p,BlockPos pos) {
        var f=FIELDS.get(p.getUUID());return f!=null && f.kind==ArtifactKind.INTERREGNUM && p.level().dimension()==f.dimension
            && p.getMainHandItem()==f.tool && ArtifactState.now(p)<=f.expires && pos.distSqr(f.center)<=f.radius*f.radius;
    }
    public static boolean frozen(Entity entity) {
        return entity!=null && FIELDS.values().stream().anyMatch(f -> f.frozen.containsKey(entity.getUUID()));
    }
    public static void feed(ServerPlayer p,BlockPos pos) {
        var field=FIELDS.get(p.getUUID());
        if(field!=null && contains(p,pos)) field.expires=Math.min(ArtifactState.now(p)+ArtifactConfig.FIELD_TIME.get(),field.expires+20);
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
            if(f.kind==ArtifactKind.INTERREGNUM && contains(p,p.blockPosition()))
                p.addEffect(new net.minecraft.world.effect.MobEffectInstance(io.github.santipdr.specialpickaxes.SpecialPickaxes.DOMINION.get(),12,0,false,true,true));
            var entities=level.getEntitiesOfClass(Entity.class,new AABB(f.center).inflate(f.radius),e ->
                e.isAlive() && (e instanceof Projectile || f.kind!=ArtifactKind.AEGIS && e instanceof Monster)
                && !e.isAlliedTo(p) && e.position().distanceToSqr(Vec3.atCenterOf(f.center))<=f.radius*f.radius);
            entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(Vec3.atCenterOf(f.center))));
            var current=new HashSet<UUID>();int processed=0;
            for(var entity:entities) {
                if(processed>=ArtifactConfig.FIELD_TARGETS.get()) break;
                if(entity instanceof Projectile projectile && projectile.getOwner()!=null
                        && (projectile.getOwner()==p || projectile.getOwner().isAlliedTo(p) || f.kind==ArtifactKind.AEGIS && projectile.getOwner() instanceof net.minecraft.world.entity.player.Player)) continue;
                if(!WorldSafety.allowed(p,f.kind,entity.blockPosition())) continue;
                processed++;current.add(entity.getUUID());
                if(f.kind==ArtifactKind.AEGIS){
                    var velocity=entity.getDeltaMovement();if(velocity.lengthSqr()<0.00001 || entity instanceof net.minecraft.world.entity.projectile.AbstractArrow && !level.noCollision(entity,entity.getBoundingBox()))continue;
                    var away=entity.position().subtract(Vec3.atCenterOf(f.center)).normalize();
                    if(ArtifactState.mode(p,f.kind)==1)away=new Vec3(-away.z,away.y*0.2,away.x).normalize();
                    entity.setDeltaMovement(away.scale(Math.min(3,Math.max(0.25,velocity.length()))));
                } else if(f.kind==ArtifactKind.INTERREGNUM) {
                    var original=f.frozen.computeIfAbsent(entity.getUUID(),id -> new Frozen(entity,entity.position(),entity.getDeltaMovement()));
                    var box=entity.getBoundingBox().move(original.position.subtract(entity.position()));
                    if(level.noCollision(entity,box)) { entity.setPos(original.position);entity.setDeltaMovement(Vec3.ZERO); }
                } else {
                    var delta=Vec3.atCenterOf(f.center).subtract(entity.position()).normalize();
                    double sign=ArtifactState.mode(p,f.kind)%2==0?1:-1;
                    entity.setDeltaMovement(clamp(entity.getDeltaMovement().scale(0.6).add(delta.scale(sign*ArtifactConfig.FIELD_FORCE.get()))));
                }
                entity.hasImpulse=true;
            }
            f.frozen.entrySet().removeIf(entry -> {
                if(current.contains(entry.getKey())) return false;
                if(entry.getValue().entity.isAlive()) entry.getValue().entity.setDeltaMovement(clamp(entry.getValue().velocity));
                return true;
            });
            if(p.tickCount%10==0){ArtifactFeedback.ring(p,f.kind,f.center,f.radius);RelicEffects.emit(p,f.kind,"sustain",Vec3.atCenterOf(f.center));}
        }
    }
}
