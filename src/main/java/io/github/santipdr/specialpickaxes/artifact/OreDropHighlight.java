package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

/** Marks Iridium ore drops and emits a bounded, owner-only animated crystal cue. */
public final class OreDropHighlight {
    private static final String ORE_DROP="specialpickaxesIridiumOreDrop";
    private static final String OWNER="specialpickaxesIridiumOwner";
    private static final String NEXT_LODE_SENSE="specialpickaxesLodeSenseNextAt";
    private static final int MAX_SHARD_EMISSIONS_PER_TICK=32;
    private static final int PASSIVE_PULL_RADIUS=8;
    private static final int PASSIVE_PULL_TARGETS=16;
    private static final int PASSIVE_PULL_INTERVAL=10;
    private static final int MAX_PULL_TRAILS=4;
    private static long shardBudgetTick=Long.MIN_VALUE;
    private static int shardEmissionsThisTick;

    private OreDropHighlight(){}

    /** @return true only when this drop receives its first highlight for this wearer. */
    public static boolean mark(ItemEntity drop,ServerPlayer owner){
        if(drop.isRemoved()||drop.level()!=owner.level()||!(drop.level() instanceof ServerLevel level))return false;
        var data=drop.getPersistentData();
        boolean fresh=!data.getBoolean(ORE_DROP)||!data.hasUUID(OWNER)||!data.getUUID(OWNER).equals(owner.getUUID());
        drop.setGlowingTag(true);
        data.putBoolean(ORE_DROP,true);
        data.putUUID(OWNER,owner.getUUID());
        if(fresh){
            long now=level.getGameTime();
            if(now!=shardBudgetTick){shardBudgetTick=now;shardEmissionsThisTick=0;}
            if(shardEmissionsThisTick<MAX_SHARD_EMISSIONS_PER_TICK){
                shardEmissionsThisTick++;
                // The shard is private Curios feedback; only the item entity's glow is shared with nearby players.
                level.sendParticles(owner,io.github.santipdr.specialpickaxes.SpecialPickaxes.IRIDIUM_LODE_SHARD.get(),
                        false,drop.getX(),drop.getY()+drop.getBbHeight()*.5D,drop.getZ(),1,0,.012D,0,0);
            }
        }
        return fresh;
    }

    /** Curios' Lode Sense gathers only ore drops marked to this wearer, without scanning veins. */
    public static int drawNearbyDrops(ServerPlayer owner){
        var level=owner.serverLevel();long now=level.getGameTime();var data=owner.getPersistentData();
        if(data.getLong(NEXT_LODE_SENSE)>now)return 0;
        data.putLong(NEXT_LODE_SENSE,now+PASSIVE_PULL_INTERVAL);
        var center=owner.position().add(0,owner.getBbHeight()*.55D,0);
        var targets=EntitySelection.nearest(level,ItemEntity.class,owner.getBoundingBox().inflate(PASSIVE_PULL_RADIUS),
                item->ArtifactTechniques.isIridiumRecallTarget(owner,item,center,PASSIVE_PULL_RADIUS),
                center,PASSIVE_PULL_TARGETS,256);
        int moved=0,trails=0;
        for(var item:targets){
            Vec3 delta=center.subtract(item.position());double distanceSqr=delta.lengthSqr();
            if(distanceSqr<.64D)continue; // Leave items inside vanilla pickup reach alone.
            Vec3 pull=delta.normalize().scale(.28D);
            Vec3 motion=item.getDeltaMovement().scale(.55D).add(pull);
            if(motion.lengthSqr()>.36D)motion=motion.normalize().scale(.6D);
            item.setDeltaMovement(motion);item.hasImpulse=true;moved++;
            if(trails<MAX_PULL_TRAILS){emitLodeSenseTrail(owner,item.position(),center);trails++;}
        }
        return moved;
    }

    /** A wearer-only shard marks the start of a passive Lode Sense pull without broadcasting trails. */
    private static void emitLodeSenseTrail(ServerPlayer owner,Vec3 drop,Vec3 wearer){
        var level=owner.serverLevel();Vec3 point=drop.lerp(wearer,.42D);
        level.sendParticles(owner,io.github.santipdr.specialpickaxes.SpecialPickaxes.IRIDIUM_LODE_SHARD.get(),
                false,point.x,point.y,point.z,1,0,0,0,0);
    }
}
