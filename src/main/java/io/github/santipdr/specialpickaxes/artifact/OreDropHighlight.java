package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Vector3f;

/** Marks only drops produced by Iridium's ore break and adds one small local sparkle burst. */
public final class OreDropHighlight {
    private static final String ORE_DROP="specialpickaxesIridiumOreDrop";
    private static final String OWNER="specialpickaxesIridiumOwner";
    private static final int MAX_SPARK_BURSTS_PER_TICK=32;
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
}
