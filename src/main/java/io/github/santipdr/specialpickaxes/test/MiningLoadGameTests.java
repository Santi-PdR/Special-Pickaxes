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
    /** Saturates the default shared limit; correctness/fairness, not a timing benchmark. */
    @GameTest(template="empty",batch="saturated_mixed_scheduler_r7")
    public static void sixteenMixedSignaturesRotateUnderSaturatedGlobalBudget(GameTestHelper h){
        final int count=16,budget=ArtifactConfig.GLOBAL.get();
        h.assertTrue(budget>=16&&budget<count*16,"fixture must genuinely saturate shared allowance");
        var kinds=Arrays.stream(ArtifactKind.playableValues()).filter(SignatureExcavation::handles).toList();
        var players=new ArrayList<ServerPlayer>();var origins=new ArrayList<net.minecraft.core.BlockPos>();
        var footprints=new ArrayList<List<net.minecraft.core.BlockPos>>();
        int[] previous=new int[count],lastService=new int[count];
        int bound=(int)Math.ceil(count*16D/budget)+1,maxWait=0,maxAttempts=0,saturatedTurns=0,total=0,turn=0;
        long time=h.getLevel().getGameTime();
        try{
            for(int i=0;i<count;i++){
                var kind=kinds.get(i%kinds.size());var p=SeismicReworkGameTests.player(h,kind);players.add(p);
                // Separate this wide fixture from neighboring GameTest structures/drops.
                p.setPos(p.getX()+i*32,h.getLevel().getMaxBuildHeight()-48,p.getZ()+4096);
                var at=SeismicReworkGameTests.target(h,p);origins.add(at);var aim=p.getLookAngle();
                var cells=SignatureExcavation.footprint(at,Direction.getNearest(aim.x,aim.y,aim.z),kind,(i&1)!=0);
                footprints.add(cells);total+=cells.size();
                for(var cell:cells){h.getLevel().setBlock(cell,Blocks.STONE.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
                p.getMainHandItem().getOrCreateTag().putBoolean("materialPouchEnabled",true);
            }
            // Establish all baselines before any of the sixteen jobs can mine.
            // Do not hide contamination by filtering item IDs/UUIDs in the final check.
            for(int i=0;i<count;i++)h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(origins.get(i)).inflate(12)).isEmpty(),"isolated fixture starts without item entities "+i);
            for(int i=0;i<count;i++){
                var p=players.get(i);var kind=kinds.get(i%kinds.size());
                h.assertTrue(SignatureExcavation.start(p,p.getMainHandItem(),kind,(i&1)!=0),"mixed signature starts "+i);
            }
            for(turn=1;turn<=160&&players.stream().anyMatch(WorkQueue::busy);turn++){
                h.getLevel().getServer().getWorldData().overworldData().setGameTime(time+turn);
                WorkQueue.tick();int attempts=WorkQueue.lastAttempts();maxAttempts=Math.max(maxAttempts,attempts);
                if(attempts==budget)saturatedTurns++;
                h.assertTrue(attempts<=budget,"mixed work never exceeds shared budget");
                for(int i=0;i<count;i++){
                    var p=players.get(i);if(!WorkQueue.busy(p))continue;
                    int gap=turn-lastService[i];maxWait=Math.max(maxWait,gap);
                    h.assertTrue(gap<=bound,"active job receives service within bounded rotation: job="+i+" gap="+gap);
                    h.assertTrue(!WorkQueue.status(p).equals("paused"),"no loot-density pause while saturated");
                    int completed=WorkQueue.completed(p);
                    if(completed>previous[i])lastService[i]=turn;
                    previous[i]=completed;
                }
            }
            int stored=0;
            for(int i=0;i<count;i++){
                var p=players.get(i);int amount=PickaxePouch.amount(p.getMainHandItem(),0);stored+=amount;
                h.assertTrue(!WorkQueue.busy(p)&&footprints.get(i).stream().allMatch(c->h.getLevel().getBlockState(c).isAir()),"each mixed job finishes its own footprint "+i);
                h.assertTrue(amount==footprints.get(i).size(),"per-player native loot conserved, not only aggregate "+i);
                h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(origins.get(i)).inflate(12)).isEmpty(),"capture keeps item entities out of world "+i);
            }
            h.assertTrue(stored==total&&saturatedTurns>0&&maxAttempts==budget,"fixture really saturated and conserved all blocks");
            com.mojang.logging.LogUtils.getLogger().info("SATURATED_MIXED_LOAD jobs={} blocks={} stored={} ticks={} saturated_turns={} max_attempts={} max_service_gap={} service_bound={}",count,total,stored,turn-1,saturatedTurns,maxAttempts,maxWait,bound);
        }finally{
            for(var p:players){WorkQueue.cancel(p);MiningObservations.forget(p);io.github.santipdr.specialpickaxes.network.RelicNetwork.forget(p);}
            for(var at:origins)for(var drop:h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at).inflate(12)))drop.discard();
            h.getLevel().getServer().getWorldData().overworldData().setGameTime(time);
        }
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
