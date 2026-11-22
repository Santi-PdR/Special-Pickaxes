package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.*;
import java.util.*;

/** Controlled scheduler microbenchmark, not a full server-tick or client FPS benchmark. */
@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class MiningLoadGameTests {
    @GameTest(template="empty",batch="paired_scheduler_load_r7")
    public static void eightConcurrentSignaturesConserveLootWithinGlobalBudget(GameTestHelper h){
        // Alternate ordering to expose (not eliminate) JVM warmup/order effects.
        for(int round=0;round<4;round++)run(h,round,(round&1)!=0);
        h.succeed();
    }
    private static void run(GameTestHelper h,int round,boolean pouch){
        var players=new ArrayList<ServerPlayer>();
        var origins=new ArrayList<net.minecraft.core.BlockPos>();
        var footprints=new ArrayList<List<net.minecraft.core.BlockPos>>();
        var durations=new ArrayList<Long>();int blocks=0,maxAttempts=0,turns=0;
        long time=h.getLevel().getGameTime();
        try{
            for(int i=0;i<8;i++){
                var p=SeismicReworkGameTests.player(h,ArtifactKind.WORLDBREAKER);players.add(p);
                p.setPos(p.getX()+i*32,p.getY(),p.getZ());
                var at=SeismicReworkGameTests.target(h,p);origins.add(at);
                var aim=p.getLookAngle();var cells=SignatureExcavation.footprint(at,Direction.getNearest(aim.x,aim.y,aim.z),ArtifactKind.WORLDBREAKER,false);
                footprints.add(cells);blocks+=cells.size();
                for(var cell:cells){h.getLevel().setBlock(cell,Blocks.STONE.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
                p.getMainHandItem().getOrCreateTag().putBoolean("materialPouchEnabled",pouch);
                h.assertTrue(SignatureExcavation.start(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,false),"concurrent signature starts "+i);
            }
            for(;turns<160&&players.stream().anyMatch(WorkQueue::busy);turns++){
                h.getLevel().getServer().getWorldData().overworldData().setGameTime(time+turns+1);
                long start=System.nanoTime();WorkQueue.tick();durations.add(System.nanoTime()-start);
                maxAttempts=Math.max(maxAttempts,WorkQueue.lastAttempts());
                h.assertTrue(WorkQueue.lastAttempts()<=ArtifactConfig.GLOBAL.get(),"global attempt budget respected under load");
                for(var p:players)h.assertTrue(!WorkQueue.status(p).equals("paused"),"no density-based pause");
            }
            int stored=0,world=0,entities=0;
            for(int i=0;i<players.size();i++){
                var p=players.get(i);
                h.assertTrue(!WorkQueue.busy(p)&&footprints.get(i).stream().allMatch(c->h.getLevel().getBlockState(c).isAir()),"each job finishes full footprint "+i);
                stored+=PickaxePouch.amount(p.getMainHandItem(),0);
                var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(origins.get(i)).inflate(12));
                entities+=drops.size();world+=drops.stream().filter(d->d.getItem().is(Items.COBBLESTONE)).mapToInt(d->d.getItem().getCount()).sum();
            }
            h.assertTrue(world+stored==blocks,"exact aggregate native loot conservation");
            h.assertTrue(pouch?world==0&&entities==0:stored==0&&world==blocks,"storage location matches capture preference");
            Collections.sort(durations);long sum=durations.stream().mapToLong(Long::longValue).sum();
            com.mojang.logging.LogUtils.getLogger().info("SCHEDULER_LOAD round={} pouch={} jobs=8 blocks={} ticks={} max_attempts={} world_items={} stored_items={} entities={} scheduler_total_ms={} scheduler_p50_ms={} scheduler_p95_ms={} scheduler_max_ms={}",round,pouch,blocks,turns,maxAttempts,world,stored,entities,sum/1e6,durations.get(durations.size()/2)/1e6,durations.get((int)Math.ceil(durations.size()*.95)-1)/1e6,durations.get(durations.size()-1)/1e6);
        }finally{
            for(var p:players){WorkQueue.cancel(p);MiningObservations.forget(p);}
            for(var at:origins)for(var drop:h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(12)))drop.discard();
            h.getLevel().getServer().getWorldData().overworldData().setGameTime(time);
        }
    }
}
