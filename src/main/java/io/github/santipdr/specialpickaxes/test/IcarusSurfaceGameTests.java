package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.*;

@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class IcarusSurfaceGameTests {
    /** Native collision movement of a FakePlayer complements, never replaces, the survival-client trial. */
    @GameTest(template="empty",batch="icarus_surface_depth")
    public static void ascentCrossesDeepOverburdenWithoutTheOldHeightOrTimeCaps(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.ICARUS);var level=h.getLevel();
        p.setPos(p.getX()+4096,level.getMinBuildHeight()+100,p.getZ()+4096);var base=p.blockPosition();
        for(var at:BlockPos.betweenClosed(base.offset(-2,-1,-2),base.offset(2,72,2))){
            boolean floor=at.getY()<base.getY();
            boolean roof=at.getY()>=base.getY()+4&&at.getY()<=base.getY()+60&&Math.abs(at.getX()-base.getX())<=1&&Math.abs(at.getZ()-base.getZ())<=1;
            level.setBlock(at,(floor?Blocks.BEDROCK:roof?Blocks.STONE:Blocks.AIR).defaultBlockState(),2);PlayerPlacedBlocks.get(level).unmark(at);
        }
        p.getMainHandItem().getOrCreateTag().putBoolean("materialPouchEnabled",true);
        long time=level.getGameTime();int ticks=0;double start=p.getY();
        try{
            h.assertTrue(IcarianFlight.start(p,p.getMainHandItem()),"deep surface ascent starts");
            for(;ticks<240&&IcarianFlight.active(p);ticks++){
                level.getServer().getWorldData().overworldData().setGameTime(time+ticks+1);
                IcarianFlight.tick();
                if(IcarianFlight.active(p))p.move(MoverType.SELF,p.getDeltaMovement());
            }
            h.assertTrue(!IcarianFlight.active(p)&&p.getY()>=start+63.98,"physical ascent reaches surface plus exit clearance, past old14/32 caps");
            h.assertTrue(ticks>32&&PickaxePouch.amount(p.getMainHandItem(),0)==57,"all57 native overburden blocks conserved during long ascent");
            h.assertTrue(level.getBlockState(base.above(60)).isAir()&&level.getBlockState(base.above(60).east()).is(Blocks.STONE),"continuous body path opened, lateral terrain retained");
            h.assertTrue(IcarianFlight.protectsFall(p),"landing guard survives completed thrust");
            IcarianFlight.stop(p);h.assertTrue(IcarianFlight.protectsFall(p),"cancel cannot strip landing safety");
            p.setOnGround(true);IcarianFlight.tick();h.assertTrue(!IcarianFlight.protectsFall(p),"landing consumes safety, not permanent flight/immunity");
            com.mojang.logging.LogUtils.getLogger().info("ICARUS_DEEP_COLLISION_OK rise={} ticks={} stored=57",p.getY()-start,ticks);
        }finally{IcarianFlight.forget(p);MiningObservations.forget(p);level.getServer().getWorldData().overworldData().setGameTime(time);}
        h.succeed();
    }
    @GameTest(template="empty",batch="icarus_authored_wings")
    public static void realPrimaryUsesAuthoredWingsInsteadOfThreeBlockCut(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.ICARUS);p.setPos(p.getX()+4096,h.getLevel().getMaxBuildHeight()-48,p.getZ()+4096);
        var at=SeismicReworkGameTests.target(h,p);var cells=IcarianWingcut.geometry(at,p.getDirection(),false);
        for(var cell:cells){h.getLevel().setBlock(cell,Blocks.AIR.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        var far=at.relative(p.getDirection(),12).relative(p.getDirection().getClockWise(),8).above(4);
        h.getLevel().setBlock(at,Blocks.STONE.defaultBlockState(),2);h.getLevel().setBlock(far,Blocks.STONE.defaultBlockState(),2);
        try{
            h.assertTrue(ArtifactActions.primary(p,p.getMainHandItem(),ArtifactKind.ICARUS),"real primary starts authored wing excavation");
            for(int i=0;i<1000&&WorkQueue.busy(p);i++)WorkQueue.tick();
            h.assertTrue(!WorkQueue.busy(p)&&h.getLevel().getBlockState(at).isAir()&&h.getLevel().getBlockState(far).isAir(),"wings reach laterally and forward beyond the rejected tiny cut");
        }finally{WorkQueue.cancel(p);MiningObservations.forget(p);IcarianFlight.forget(p);}
        h.succeed();
    }
}
