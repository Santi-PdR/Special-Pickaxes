package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import java.util.*;

@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class SignatureExcavationGameTests {
    @GameTest(template="empty",batch="signature_geometry_r7") public static void everySignatureHasUniqueBoundedForwardFootprint(GameTestHelper h){
        for(var direction:Direction.values())for(boolean alternate:new boolean[]{false,true}){
            var unique=new HashSet<Set<BlockPos>>();
            for(var kind:ArtifactKind.playableValues()){
                if(!SignatureExcavation.handles(kind))continue;
                var seed=new BlockPos(5,100,5);var cells=SignatureExcavation.footprint(seed,direction,kind,alternate);
                h.assertTrue(cells.get(0).equals(seed)&&cells.size()<=(alternate?384:256),"seed first and fixed volume cap "+kind.id);
                h.assertTrue(new HashSet<>(cells).size()==cells.size(),"no duplicate inspections "+kind.id);
                for(var at:cells){var delta=at.subtract(seed);h.assertTrue(delta.getX()*direction.getStepX()+delta.getY()*direction.getStepY()+delta.getZ()*direction.getStepZ()>=0,"all cells on target face or forward "+kind.id);}
                h.assertTrue(unique.add(new HashSet<>(cells)),"distinct footprint for each held signature "+kind.id);
            }
        }h.succeed();
    }
    @GameTest(template="empty",batch="signature_settling_r7") public static void signatureHarvestsLateSettlingMaterialInsideItsOwnClearedCell(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.EVENTIDE);var at=SeismicReworkGameTests.target(h,p);
        h.assertTrue(SignatureExcavation.start(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,false),"bounded fan starts");
        for(int i=0;i<32;i++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir(),"initial target actually cleared");
        h.runAfterDelay(3,()->{
            // Model the settled block state, not an item entity or an unbounded new scan.
            h.getLevel().setBlock(at,net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState(),2);
            h.getLevel().setBlock(at.below(),net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState(),2);
        });
        h.runAfterDelay(12,()->{
            h.assertTrue(h.getLevel().getBlockState(at).isAir(),"settled gravel in cleared cell is harvested");
            SeismicReworkGameTests.finish(h,p);
        });
    }

    @GameTest(template="empty",batch="signature_runtime_r7") public static void everyReworkedPrimaryUsesItsBoundedSignatureAndNativeLoot(GameTestHelper h){
        for(var kind:ArtifactKind.playableValues()){
            if(!SignatureExcavation.handles(kind))continue;
            var p=SeismicReworkGameTests.player(h,kind);var at=SeismicReworkGameTests.target(h,p);var tool=p.getMainHandItem();
            var look=p.getLookAngle();var direction=Direction.getNearest(look.x,look.y,look.z);
            var cells=SignatureExcavation.footprint(at,direction,kind,false);
            for(var cell:cells){h.getLevel().setBlock(cell,net.minecraft.world.level.block.Blocks.TERRACOTTA.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
            var outside=at.relative(direction.getOpposite(),2).above(4);h.getLevel().setBlock(outside,net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState(),2);
            h.assertTrue(ArtifactActions.primary(p,tool,kind),"real primary accepts signature "+kind.id);
            for(int tick=0;tick<80;tick++)WorkQueue.tick();
            h.assertTrue(cells.stream().allMatch(cell->h.getLevel().getBlockState(cell).isAir()),"primary clears its own footprint "+kind.id);
            h.assertTrue(h.getLevel().getBlockState(outside).is(net.minecraft.world.level.block.Blocks.DIAMOND_ORE),"primary does not jump outside its footprint "+kind.id);
            WorkQueue.cancel(p);MiningObservations.forget(p);
        }h.succeed();
    }

    @GameTest(template="empty",batch="signature_paradox_controls_r7") public static void realParadoxControlStartsWedgeWithoutSelectionOrSpeedLock(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.CRUCIBLE);var at=SeismicReworkGameTests.target(h,p);var tool=p.getMainHandItem();
        h.assertTrue(!RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT)&&!ArtifactInteraction.selecting(p),"retired selection control cannot arm a hidden region");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).remove("inputTick");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE)&&WorkQueue.busy(p),"real primary starts wedge immediately");WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).isAir()&&!ArtifactInteraction.selecting(p)&&tool.getDestroySpeed(net.minecraft.world.level.block.Blocks.STONE.defaultBlockState())>=72,"wedge mines without regional speed lock");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CANCEL)&&!WorkQueue.busy(p),"cancel remains available immediately");SeismicReworkGameTests.finish(h,p);
    }

    @GameTest(template="empty",batch="signature_falling_physics_r7",timeoutTicks=100) public static void signatureHarvestsRealGravelFallingAfterActivation(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.EVENTIDE);var at=SeismicReworkGameTests.target(h,p);var level=h.getLevel();
        for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)for(int y=-1;y<=7;y++){
            var cell=at.offset(x,y,z);PlayerPlacedBlocks.get(level).unmark(cell);
            level.setBlock(cell,x==0&&z==0&&y>=0&&y<7?net.minecraft.world.level.block.Blocks.AIR.defaultBlockState():net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState(),2);
        }
        level.setBlock(at,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
        h.assertTrue(SignatureExcavation.start(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,false),"bounded signature starts in sealed falling fixture");
        for(int i=0;i<32;i++)WorkQueue.tick();
        h.assertTrue(level.getBlockState(at).isAir()&&WorkQueue.busy(p),"cleared cell is watched before gravel exists");
        h.runAtTickTime(5,()->{
            var gravel=net.minecraft.world.level.block.Blocks.GRAVEL.defaultBlockState();level.setBlock(at.above(4),gravel,2);
            net.minecraft.world.entity.item.FallingBlockEntity.fall(level,at.above(4),gravel);
        });
        h.runAtTickTime(65,()->{
            h.assertTrue(level.getBlockState(at).isAir(),"real falling gravel has landed and been harvested");
            var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(6));
            h.assertTrue(drops.stream().anyMatch(d->d.getItem().is(net.minecraft.world.item.Items.GRAVEL)||d.getItem().is(net.minecraft.world.item.Items.FLINT)),"native gravel or flint loot exists after landing");
            h.assertTrue(level.getEntitiesOfClass(net.minecraft.world.entity.item.FallingBlockEntity.class,new net.minecraft.world.phys.AABB(at).inflate(6)).isEmpty(),"test does not mistake still-airborne gravel for harvested material");
            SeismicReworkGameTests.finish(h,p);
        });
    }

}
