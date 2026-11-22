package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.*;
import java.util.*;

/** Real level ticks after mining, deliberately excluding client/network performance claims. */
@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class PersistentLootLoadGameTests {
    @GameTest(template="empty",batch="persistent_native_loot_load_r7",timeoutTicks=400)
    public static void nativeLootRemainsConservedThroughRealEntityTicks(GameTestHelper h){
        var probe=new Probe(h);MinecraftForge.EVENT_BUS.register(probe);
        try{probe.begin();}catch(RuntimeException|Error error){probe.close();throw error;}
    }
    public static final class Probe {
        private final GameTestHelper h;
        private final Set<ChunkPos> forced=new HashSet<>();
        private final List<Long> samples=new ArrayList<>();
        private ServerPlayer player;
        private BlockPos at;
        private int round,ticks,expected,initialEntities;
        private long start;
        private boolean measuring;
        Probe(GameTestHelper h){this.h=h;}
        @SubscribeEvent public void tick(TickEvent.LevelTickEvent event){
            if(!measuring||event.level!=h.getLevel())return;
            if(event.phase==TickEvent.Phase.START)start=System.nanoTime();
            else if(start!=0){long elapsed=System.nanoTime()-start;start=0;if(++ticks>20)samples.add(elapsed);}
        }
        private List<ItemEntity> drops(){
            // Only the dry interior, not items landing on the outside of its roof.
            return h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(at.getX()-9,at.getY()-5,at.getZ()-9,at.getX()+10,at.getY()+8,at.getZ()+10));
        }
        void begin(){
            measuring=false;samples.clear();ticks=0;start=0;
            boolean pouch=round==1||round==2; // ABBA order, not four identical warmup conditions.
            player=SeismicReworkGameTests.player(h,ArtifactKind.WORLDBREAKER);
            // Other fixtures leave falling blocks/drops around their +100 working height.
            // Keep this asynchronous scene above them, within normal world build limits.
            player.setPos(player.getX(),player.getY()+128,player.getZ());at=SeismicReworkGameTests.target(h,player);
            h.assertTrue(at.getY()+8<h.getLevel().getMaxBuildHeight(),"isolated chamber fits build height");
            var chunk=new ChunkPos(at);
            for(int x=chunk.x-1;x<=chunk.x+1;x++)for(int z=chunk.z-1;z<=chunk.z+1;z++){
                var pos=new ChunkPos(x,z);
                if(!h.getLevel().getForcedChunks().contains(pos.toLong())){h.getLevel().setChunkForced(x,z,true);forced.add(pos);}
            }
            // A sealed dry chamber prevents surrounding test-world fluids from carrying
            // loot out of the measured volume. Physics/merging inside remain native.
            for(var cell:BlockPos.betweenClosed(at.offset(-10,-6,-10),at.offset(10,8,10))){
                int x=cell.getX()-at.getX(),y=cell.getY()-at.getY(),z=cell.getZ()-at.getZ();
                boolean shell=Math.abs(x)==10||Math.abs(z)==10||y==-6||y==8;
                h.getLevel().setBlock(cell,(shell?Blocks.BEDROCK:Blocks.AIR).defaultBlockState(),2);
            }
            var aim=player.getLookAngle();var cells=SignatureExcavation.footprint(at,Direction.getNearest(aim.x,aim.y,aim.z),ArtifactKind.WORLDBREAKER,false);expected=cells.size();
            for(var cell:cells){h.getLevel().setBlock(cell,Blocks.STONE.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
            var tool=player.getMainHandItem();tool.getOrCreateTag().putBoolean("materialPouchEnabled",pouch);
            h.assertTrue(SignatureExcavation.start(player,tool,ArtifactKind.WORLDBREAKER,false),"persistent-loot scenario starts");
            SynchronousWorkDrain.drain(player,1000);
            h.assertTrue(!WorkQueue.busy(player)&&cells.stream().allMatch(c->h.getLevel().getBlockState(c).isAir()),"excavation completed before level-tick measurement");
            initialEntities=drops().size();measuring=true;h.runAfterDelay(70,this::finishRound);
        }
        private void finishRound(){
            measuring=false;boolean succeeded=false;
            try{
                boolean pouch=round==1||round==2;var drops=drops();
                int world=drops.stream().filter(d->d.getItem().is(Items.COBBLESTONE)).mapToInt(d->d.getItem().getCount()).sum();
                int stored=PickaxePouch.amount(player.getMainHandItem(),0);
                h.assertTrue(samples.size()>=40,"at least 40 real level-tick samples after 20 warmup ticks");
                com.mojang.logging.LogUtils.getLogger().info("PERSISTENT_LOOT_COUNTS round={} expected={} initial_entities={} remaining_entities={} world={} stored={}",round,expected,initialEntities,drops.size(),world,stored);
                h.assertTrue(world+stored==expected,"loot conserved after natural falling/merging ticks: round="+round+" expected="+expected+" world="+world+" stored="+stored);
                h.assertTrue(pouch?world==0&&drops.isEmpty()&&stored==expected:stored==0&&world==expected&&drops.stream().anyMatch(d->d.tickCount>=20),"items genuinely ticked, or capture prevented their spawn");
                Collections.sort(samples);long sum=samples.stream().mapToLong(Long::longValue).sum();
                com.mojang.logging.LogUtils.getLogger().info("PERSISTENT_LOOT_LOAD round={} pouch={} blocks={} initial_entities={} remaining_entities={} world_items={} stored_items={} samples={} level_total_ms={} level_p50_ms={} level_p95_ms={} level_max_ms={}",round,pouch,expected,initialEntities,drops.size(),world,stored,samples.size(),sum/1e6,samples.get(samples.size()/2)/1e6,samples.get((int)Math.ceil(samples.size()*.95)-1)/1e6,samples.get(samples.size()-1)/1e6);
                drops.forEach(ItemEntity::discard);WorkQueue.cancel(player);MiningObservations.forget(player);
                if(++round<4)begin();else{close();h.succeed();}succeeded=true;
            }finally{if(!succeeded)close();}
        }
        void close(){
            measuring=false;MinecraftForge.EVENT_BUS.unregister(this);
            if(player!=null){WorkQueue.cancel(player);MiningObservations.forget(player);}
            if(at!=null)drops().forEach(ItemEntity::discard);
            for(var chunk:forced)h.getLevel().setChunkForced(chunk.x,chunk.z,false);
            forced.clear();
        }
    }
}
