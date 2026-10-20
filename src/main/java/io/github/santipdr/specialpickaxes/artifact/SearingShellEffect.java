package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/** Hellspec's fireproof passive with a small ember tell while the wearer burns or swims in lava. */
public final class SearingShellEffect extends MobEffect {
    private static final String THERMAL_SCAN_TICK="specialpickaxesThermalMiningScanTick";
    private static final String THERMAL_SCAN_RESULT="specialpickaxesThermalMiningScanResult";
    private static final double THERMAL_MINING_MULTIPLIER=1.35D;
    public SearingShellEffect() {
        super(MobEffectCategory.BENEFICIAL,0xf35a39);
    }

    /** Near heat, Hellspec's Curios shell tempers ordinary mining into a short, strong passive bonus. */
    public static double miningSpeedMultiplier(ServerPlayer player){
        if(!player.hasEffect(io.github.santipdr.specialpickaxes.SpecialPickaxes.SEARING_SHELL.get())||!nearbyHeat(player))return 1D;
        return THERMAL_MINING_MULTIPLIER;
    }

    /** Checks a compact 5×3×5 area at most once per player per server tick. */
    public static boolean nearbyHeat(ServerPlayer player){
        var level=player.serverLevel();long now=level.getGameTime();var data=player.getPersistentData();
        if(data.contains(THERMAL_SCAN_TICK)&&data.getLong(THERMAL_SCAN_TICK)==now)return data.getBoolean(THERMAL_SCAN_RESULT);
        BlockPos origin=player.blockPosition();boolean hot=false;
        scan: for(BlockPos pos:BlockPos.betweenClosed(origin.offset(-2,-1,-2),origin.offset(2,1,2))){
            var state=level.getBlockState(pos);
            if(state.is(Blocks.FIRE)||state.is(Blocks.SOUL_FIRE)||state.is(Blocks.MAGMA_BLOCK)
                    ||state.is(Blocks.CAMPFIRE)||state.is(Blocks.SOUL_CAMPFIRE)||state.is(Blocks.LAVA_CAULDRON)
                    ||state.getFluidState().is(FluidTags.LAVA)) {hot=true;break scan;}
        }
        data.putLong(THERMAL_SCAN_TICK,now);data.putBoolean(THERMAL_SCAN_RESULT,hot);
        return hot;
    }

    public static double thermalMiningMultiplier(){return THERMAL_MINING_MULTIPLIER;}

    @Override
    public void applyEffectTick(LivingEntity entity,int amplifier) {
        if(entity.level().isClientSide)return;
        boolean inHeat=entity.isOnFire()||entity.isInLava();
        if(entity.isOnFire())entity.clearFire();
        if(inHeat&&entity.level() instanceof ServerLevel level){
            var halo=heatHaloParticles(entity,entity.tickCount);
            if(entity instanceof ServerPlayer wearer)ArtifactFeedback.nearbyWardPulse(wearer,halo);
            else for(var sample:halo){var at=sample.position();level.sendParticles(sample.particle(),at.x,at.y,at.z,
                    sample.count(),sample.dx(),sample.dy(),sample.dz(),sample.speed());}
        }
    }

    /** Six flames and three lava sparks retain the halo silhouette with a bounded local audience. */
    public static ArrayList<ArtifactFeedback.AuraParticle> heatHaloParticles(LivingEntity entity,int tick){
        double phase=tick*.16D,radius=Math.max(.34D,entity.getBbWidth()*.52D);
        double centerY=entity.getY()+entity.getBbHeight()*.52D;
        var particles=new ArrayList<ArtifactFeedback.AuraParticle>(9);
        for(int i=0;i<6;i++){
            double angle=phase+Math.PI*2D*i/6D;
            double x=entity.getX()+Math.cos(angle)*radius,z=entity.getZ()+Math.sin(angle)*radius;
            double y=centerY+Math.sin(angle*2D)*.1D;
            particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.FLAME,new Vec3(x,y,z),1,0,0,0,.001D));
            if((i&1)==0)particles.add(new ArtifactFeedback.AuraParticle(ParticleTypes.LAVA,new Vec3(x,y,z),1,.015D,.025D,.015D,.001D));
        }
        return particles;
    }

    @Override
    public boolean isDurationEffectTick(int duration,int amplifier) {
        return duration%10==0;
    }
}
