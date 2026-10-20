package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Marks only drops produced by Iridium's ore break and adds one small local sparkle burst. */
public final class OreDropHighlight {
    private static final String ORE_DROP="specialpickaxesIridiumOreDrop";
    private static final String OWNER="specialpickaxesIridiumOwner";
    private static final int MAX_SPARK_BURSTS_PER_TICK=32;
    private static final int PASSIVE_PULL_RADIUS=6;
    private static final int PASSIVE_PULL_TARGETS=12;
    private static final DustParticleOptions SPARK=new DustParticleOptions(new Vector3f(.32F,.95F,.82F),.72F);
    private static long sparkBudgetTick=Long.MIN_VALUE;
    private static int sparkBurstsThisTick;

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
            if(now!=sparkBudgetTick){sparkBudgetTick=now;sparkBurstsThisTick=0;}
            if(sparkBurstsThisTick<MAX_SPARK_BURSTS_PER_TICK){
                sparkBurstsThisTick++;
                // Sparkles are Curios feedback for the wearer; the entity glow itself remains visible to everyone.
                level.sendParticles(owner,SPARK,false,drop.getX(),drop.getY()+drop.getBbHeight()*.5D,drop.getZ(),5,.12D,.12D,.12D,.015D);
            }
        }
        return fresh;
    }

    /** Curios' Lode Sense gathers only ore drops marked to this wearer, without scanning veins. */
    public static int drawNearbyDrops(ServerPlayer owner){
        var level=owner.serverLevel();var center=owner.position().add(0,owner.getBbHeight()*.55D,0);
        var targets=EntitySelection.nearest(level,ItemEntity.class,owner.getBoundingBox().inflate(PASSIVE_PULL_RADIUS),
                item->ArtifactTechniques.isIridiumRecallTarget(owner,item,center,PASSIVE_PULL_RADIUS),
                center,PASSIVE_PULL_TARGETS,256);
        int moved=0;
        for(var item:targets){
            Vec3 delta=center.subtract(item.position());double distanceSqr=delta.lengthSqr();
            if(distanceSqr<.64D)continue; // Leave items inside vanilla pickup reach alone.
            Vec3 pull=delta.normalize().scale(.28D);
            Vec3 motion=item.getDeltaMovement().scale(.55D).add(pull);
            if(motion.lengthSqr()>.36D)motion=motion.normalize().scale(.6D);
            item.setDeltaMovement(motion);item.hasImpulse=true;moved++;
        }
        return moved;
    }
}
