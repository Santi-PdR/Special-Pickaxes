package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;

@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class FaultsongQuakeGameTests {
    @GameTest(template="empty",batch="faultsong_real_fan")
    public static void primaryReachesBroadSeismicFrontInsteadOfTinyCross(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.CHOIR);
        p.setPos(p.getX()+8192,h.getLevel().getMaxBuildHeight()-24,p.getZ()+8192);
        var at=SeismicReworkGameTests.target(h,p);
        var cells=ResonantFault.geometry(at,p.getDirection(),ResonantFault.Form.FAN);
        for(var cell:cells){h.getLevel().setBlock(cell,Blocks.AIR.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        var far=at.relative(p.getDirection(),20).relative(p.getDirection().getClockWise(),16);
        h.getLevel().setBlock(at,Blocks.STONE.defaultBlockState(),2);h.getLevel().setBlock(far,Blocks.TERRACOTTA.defaultBlockState(),2);
        try{
            h.assertTrue(ArtifactActions.primary(p,p.getMainHandItem(),ArtifactKind.CHOIR),"real primary starts authored seismic fan");
            for(int i=0;i<2000&&WorkQueue.busy(p);i++)WorkQueue.tick();
            h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(far).isAir(),"seismic front reaches forward20 and lateral16, not the old tiny cross");
        }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        h.succeed();
    }
    @GameTest(template="empty",batch="faultsong_quake_identity")
    public static void quakeDamagesAndLaunchesHostilesWithoutExcavatingOrHarmingAnimals(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.CHOIR);var level=h.getLevel();
        p.setPos(p.getX()+8192,level.getMaxBuildHeight()-24,p.getZ()+8192);
        var at=p.blockPosition().south(4);level.setBlock(at,Blocks.TERRACOTTA.defaultBlockState(),2);
        var zombie=EntityType.ZOMBIE.create(level);var cow=EntityType.COW.create(level);
        var remote=EntityType.ZOMBIE.create(level);
        try{
            zombie.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);zombie.setHealth(100);zombie.setPos(p.getX()+4,p.getY(),p.getZ());
            cow.setPos(p.getX()-4,p.getY(),p.getZ());float health=cow.getHealth();
            remote.setPos(p.getX()+30,p.getY(),p.getZ());
            level.addFreshEntity(zombie);level.addFreshEntity(cow);level.addFreshEntity(remote);
            h.assertTrue(ArtifactSkills.use(p,p.getMainHandItem(),ArtifactKind.CHOIR),"real alternate fires quake instead of larger mining signature");
            h.assertTrue(zombie.getHealth()<100&&zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&zombie.getDeltaMovement().y>0,"hostile damaged, staggered and launched");
            h.assertTrue(cow.getHealth()==health&&!cow.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&remote.getHealth()==remote.getMaxHealth(),"animals and out of range hostiles untouched");
            h.assertTrue(!WorkQueue.busy(p)&&level.getBlockState(at).is(Blocks.TERRACOTTA),"secondary never starts excavation or edits terrain");
        }finally{zombie.discard();cow.discard();remote.discard();WorkQueue.cancel(p);MiningObservations.forget(p);}
        h.succeed();
    }
}
