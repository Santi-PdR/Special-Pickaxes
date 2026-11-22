package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

/** Disposable dev-client fixture. This package is excluded from the release JAR. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID)
public final class ClientValidation {
    private record Scene(ArtifactKind kind,int mode){}
    private static final java.util.List<Scene> SCENES=new java.util.ArrayList<>();
    static {for(var k:ArtifactKind.playableValues())for(int m=0;m<ArtifactInteraction.modeCount(k);m++)SCENES.add(new Scene(k,m));}
    private static int ticks,modeAckWait;
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke")||e.phase!=TickEvent.Phase.END)return;
        var server=ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        for(var p:server.getPlayerList().getPlayers()){
            if(!p.getTags().contains("artifact_gallery")||!java.nio.file.Files.exists(java.nio.file.Path.of("gallery-captured.flag")))continue;
            // Observe input even while waiting for screenshot acknowledgement. Otherwise a
            // slow screenshot helper can make activation and acknowledgement wait on each other.
            if(ticks>=240&&(ticks-240)/100<SCENES.size()&&(ticks-240)%100>=40){
                var current=SCENES.get((ticks-240)/100);var state=p.getPersistentData();
                String sceneId=current.kind().id+"-"+ArtifactInteraction.modeKey(current.kind(),current.mode());
                var aimRequest=java.nio.file.Path.of("client-aim-scene.txt");
                if(!state.getBoolean("sceneAimed")&&java.nio.file.Files.exists(aimRequest)){
                    try{
                        if(java.nio.file.Files.readString(aimRequest).trim().equals(sceneId)){
                            // Releasing the mouse after a real GUI can change camera yaw under Xvfb.
                            // Restore fixture pose once; still require the real keyboard/network activation.
                            p.connection.teleport(.5,65,14.5,180,current.kind()==ArtifactKind.PALIMPSEST?-25:-14);
                            p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                            state.putBoolean("sceneAimed",true);java.nio.file.Files.deleteIfExists(aimRequest);
                            p.sendSystemMessage(Component.literal("CLIENT_FIXTURE_AIMED_"+sceneId));
                        }
                    }catch(java.io.IOException error){throw new IllegalStateException("Cannot prepare client fixture aim",error);}
                }
                if(!state.getBoolean("sceneExecuted")&&ArtifactState.of(p,current.kind()).getLong("ready")>state.getLong("sceneReadyBefore")){
                    if(current.kind()==ArtifactKind.WORLDLOOM){
                        if(!state.getBoolean("worldloomObserver")){
                            for(int x=19;x<=21;x++)for(int z=31;z<=33;z++)p.serverLevel().setBlock(new BlockPos(x,81,z),net.minecraft.world.level.block.Blocks.POLISHED_ANDESITE.defaultBlockState(),2);
                            p.connection.teleport(20.5,82,32.5,138,35);state.putBoolean("worldloomObserver",true);
                        }
                        // Leave native loot in the world: the client must finish without
                        // simulated collection or automatically resuming a loot pause.
                        if(WorkQueue.status(p).equals("paused")&&WorkQueue.pauseReason(p).equals("drops"))
                            throw new IllegalStateException("Worldloom paused for loot in the real client");
                        if(WorkQueue.busy(p))continue;
                        if(!p.serverLevel().getBlockState(new BlockPos(0,67,10)).isAir())
                            throw new IllegalStateException("Worldloom failed to clear the close frontal bank");
                        var loot=p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                                new net.minecraft.world.phys.AABB(new BlockPos(0,62,10)).inflate(48));
                        int items=loot.stream().mapToInt(drop->drop.getItem().getCount()).sum();
                        if(items<1||loot.stream().anyMatch(net.minecraft.world.entity.item.ItemEntity::isCurrentlyGlowing))
                            throw new IllegalStateException("Worldloom survival loot missing or unexpectedly glowing: "+items);
                        p.sendSystemMessage(Component.literal("WORLDLOOM_NATIVE_LOOT_OK items="+items+" entities="+loot.size()));
                        p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                    }
                    if(current.kind()==ArtifactKind.INTERREGNUM&&DomainFields.active(p))
                        throw new IllegalStateException("Reworked Interregnum must not start an invisible domain");
                    state.putBoolean("sceneExecuted",true);p.sendSystemMessage(Component.literal("SCENE_ACTIVATED_"+current.kind().id+"-"+ArtifactInteraction.modeKey(current.kind(),current.mode())));
                }
            }
            if(ticks>=240&&(ticks-240)/100<SCENES.size()&&(ticks-240)%100==99){
                try{if(!java.nio.file.Files.readString(java.nio.file.Path.of("client-capture-ack.txt")).trim().equals(Integer.toString((ticks-240)/100)))return;}
                catch(java.io.IOException missing){return;}
                int next=(ticks-240)/100+1;
                // The filesystem screenshot ACK may beat the C-key network packet.
                // Observe real server state before advancing; never synthesize a mode.
                if(next<SCENES.size()&&SCENES.get(next).mode()>0){
                    var expected=SCENES.get(next);
                    if(ArtifactState.mode(p,expected.kind())!=expected.mode()){
                        if(++modeAckWait>100)throw new IllegalStateException("Real C key mode acknowledgement timed out: "+expected.kind()+" expected "+expected.mode());
                        return;
                    }
                }
                modeAckWait=0;
            }
            if(ticks==239+SCENES.size()*100){WorkQueue.cancel(p);DomainFields.stop(p);ArtifactInteraction.clear(p);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("ready");}
            ticks++;if(ticks<240)continue;
            int index=(ticks-240)/100,phase=(ticks-240)%100;
            if(index>=SCENES.size()){
                var data=p.getPersistentData();
                if(!data.getBoolean("flightVerified")){
                    verifyFlight(p);return;
                }
                if(!EquippedClientValidation.tick(p))return;
                if(!PouchClientValidation.tick(p))return;
                if(!data.getBoolean("controlFixtureReady")){
                    if(!data.getBoolean("controlFixtureStarted")){
                        data.putBoolean("controlFixtureStarted",true);
                        beginSupremeSetup(p);
                    }
                    if(advanceSupremeSetup(p)){
                        data.putBoolean("controlFixtureReady",true);
                        p.sendSystemMessage(Component.literal("ARTIFACT_UX_COMPLETE"));
                    }
                    return;
                }
                if(WorkQueue.busy(p)&&!data.getBoolean("controlActive")){data.putBoolean("controlActive",true);WorkQueue.togglePause(p);p.sendSystemMessage(Component.literal("CONTROL_ACTIVE"));}
                if(data.getBoolean("controlActive")&&!WorkQueue.busy(p)&&!data.getBoolean("controlCancelled")){data.putBoolean("controlCancelled",true);p.sendSystemMessage(Component.literal("CONTROL_CANCELLED"));}
                if(data.getBoolean("controlCancelled")&&!data.getBoolean("supremeSetup")){
                    data.putBoolean("supremeSetup",true);
                    beginSupremeSetup(p);
                }
                if(data.getBoolean("supremeSetup")&&!data.getBoolean("supremeSetupComplete")&&advanceSupremeSetup(p)){
                    data.putBoolean("supremeSetupComplete",true);
                    p.sendSystemMessage(Component.literal("SUPREME_READY"));
                }
                if(data.getBoolean("supremeSetupComplete")&&WorkQueue.busy(p)&&!data.getBoolean("supremeRunning")){
                    data.putBoolean("supremeRunning",true);data.putLong("supremeCenter",ArtifactActions.target(p).orElse(p.blockPosition()).asLong());
                    p.sendSystemMessage(Component.literal("SUPREME_EXECUTING"));
                }
                if(data.getBoolean("supremeRunning")&&!WorkQueue.busy(p)&&!data.getBoolean("supremeDone")){
                    data.putBoolean("supremeDone",true);// Higher CI throughput is configured before the long World Shatter work queue starts.
                    var center=BlockPos.of(data.getLong("supremeCenter"));
                    var centerState=p.serverLevel().getBlockState(new BlockPos(100,82,100));
                    var connectedState=p.serverLevel().getBlockState(new BlockPos(100,82,102));
                    var outsideState=p.serverLevel().getBlockState(new BlockPos(112,82,149));
                    var bedrockState=p.serverLevel().getBlockState(new BlockPos(100,82,150));
                    var barrierState=p.serverLevel().getBlockState(new BlockPos(100,82,151));
                    String geometry="center="+center+" states="+centerState+","+connectedState+","+outsideState+","+bedrockState+","+barrierState;
                    if(!centerState.isAir()||!connectedState.isAir()||!outsideState.is(net.minecraft.world.level.block.Blocks.STONE)
                        ||!bedrockState.is(net.minecraft.world.level.block.Blocks.BEDROCK)||!barrierState.is(net.minecraft.world.level.block.Blocks.STONE)){
                        p.sendSystemMessage(Component.literal("SUPREME_GEOMETRY_FAILED "+geometry));
                        throw new IllegalStateException("Supreme connected front or barrier failed: "+geometry);
                    }
                    var expected=SignatureExcavation.footprint(new BlockPos(100,82,100),net.minecraft.core.Direction.SOUTH,ArtifactKind.WORLDBREAKER,false);
                    int removed=0;for(int x=94;x<=106;x++)for(int z=100;z<112;z++)for(int y=76;y<=88;y++)if(p.serverLevel().getBlockState(new BlockPos(x,y,z)).isAir())removed++;
                    if(removed!=expected.size()||removed>256||expected.stream().anyMatch(at->!p.serverLevel().getBlockState(at).isAir()))
                        throw new IllegalStateException("Supreme bounded signature mismatch: removed="+removed+" expected="+expected.size());
                    p.sendSystemMessage(Component.literal("SUPREME_REMOVED_BLOCKS_"+removed));
                    p.sendSystemMessage(Component.literal("SUPREME_COMPLETE"));
                }
                return;
            }
            var scene=SCENES.get(index);var k=scene.kind();
            if(phase==0){
                ArtifactInteraction.clear(p);WorkQueue.cancel(p);DomainFields.stop(p);
                if(scene.mode()==0){
                    var stack=new ItemStack(SpecialPickaxes.PICKS.get(k).get());stack.getOrCreateTag().putUUID("controlIdentity",java.util.UUID.randomUUID());
                    var list=new net.minecraft.nbt.ListTag();var ench=new net.minecraft.nbt.CompoundTag();ench.putString("id","minecraft:efficiency");ench.putInt("lvl",1000);list.add(ench);stack.getOrCreateTag().put("Enchantments",list);
                    p.setItemInHand(InteractionHand.MAIN_HAND,stack);ArtifactState.of(p,k).putInt("mode",0);
                } else if(ArtifactState.mode(p,k)!=scene.mode())throw new IllegalStateException("Real C key did not change mode: "+k+" expected "+scene.mode());
                // CORE DRILL legitimately excavates under the actor. Restore the next
                // scene's standing platform instead of inheriting that open shaft.
                for(int x=-9;x<=9;x++)for(int z=-1;z<=17;z++)p.serverLevel().setBlock(new BlockPos(x,64,z),net.minecraft.world.level.block.Blocks.POLISHED_ANDESITE.defaultBlockState(),2);
                p.connection.teleport(0.5,65,14.5,180,k==ArtifactKind.PALIMPSEST?-25:-14);
                p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;
                for(int x=-6;x<=6;x++)for(int y=65;y<=72;y++)for(int z=4;z<=12;z++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
                for(int x=-5;x<=-3;x++)for(int y=65;y<=67;y++)for(int z=6;z<=8;z++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                var rock=new BlockPos(0,67,10);p.serverLevel().setBlockAndUpdate(rock,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                p.serverLevel().setBlockAndUpdate(rock.above(),net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState());
                if(k==ArtifactKind.WORLDLOOM){
                    // Disposable bank: use survival so native loot stays in the world throughout the real job.
                    p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                    for(int x=-13;x<=13;x++)for(int y=56;y<=67;y++)for(int z=-13;z<=13;z++)if(x*x+z*z<=169){
                        var cell=new BlockPos(x,y,10+z);PlayerPlacedBlocks.get(p.serverLevel()).unmark(cell);
                        p.serverLevel().setBlock(cell,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                    }
                    for(int x=-1;x<=1;x++)for(int y=65;y<=72;y++)for(int z=11;z<=16;z++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
                    for(int x:new int[]{-5,5})p.serverLevel().setBlock(new BlockPos(x,62,10),net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState(),2);
                    p.getPersistentData().putBoolean("worldloomObserver",false);
                }
                p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(k==ArtifactKind.CRUCIBLE?net.minecraft.world.item.Items.BASALT:net.minecraft.world.item.Items.STONE,64));
                if(k==ArtifactKind.PALIMPSEST||k==ArtifactKind.CHOIR){
                    p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                    if(!p.gameMode.destroyBlock(rock))throw new IllegalStateException("Memory fixture mining failed");MiningObservations.flush();
                    p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                    if(k==ArtifactKind.CHOIR)p.serverLevel().setBlockAndUpdate(rock,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                }
                p.getPersistentData().putLong("sceneReadyBefore",ArtifactState.of(p,k).getLong("ready"));p.getPersistentData().putBoolean("sceneExecuted",false);p.getPersistentData().putBoolean("sceneAimed",false);
                if(!SignatureExcavation.handles(k)&&ArtifactInteraction.regional(k,scene.mode())){
                    ArtifactInteraction.use(p,p.getMainHandItem(),k,false);
                    RelicControl.corner(p,new BlockPos(-5,65,6));RelicControl.corner(p,new BlockPos(-3,67,8));
                }
                p.getPersistentData().putBoolean("uxFixture",true);
            }
            if(phase==40){
                p.sendSystemMessage(Component.literal("ARTIFACT_UX_READY_"+k.id+"-"+ArtifactInteraction.modeKey(k,scene.mode())));
                com.mojang.logging.LogUtils.getLogger().info("CLIENT_FIXTURE {} MODE={} EFFICIENCY={}",k.id,ArtifactInteraction.modeKey(k,ArtifactState.mode(p,k)),EnchantmentScaling.level(p.getMainHandItem(),net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY));
            }
        }
    }
    /** Real survival-client velocity trial after the gallery; only fixture setup teleports. */
    private static void verifyFlight(net.minecraft.server.level.ServerPlayer p){
        var data=p.getPersistentData();long now=ArtifactState.now(p);
        if(!data.getBoolean("flightFixtureReady")){
            WorkQueue.cancel(p);DomainFields.stop(p);IcarianFlight.stop(p);
            for(int x=-2;x<=2;x++)for(int z=12;z<=16;z++){
                p.serverLevel().setBlock(new BlockPos(x,99,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                for(int y=100;y<=130;y++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
            }
            // Two natural ceilings: verify the real survival client opens its own path,
            // rather than demonstrating flight through an already empty shaft.
            for(int roofY:new int[]{104,108})for(int x=-1;x<=1;x++)for(int z=13;z<=15;z++){
                var roof=new BlockPos(x,roofY,z);PlayerPlacedBlocks.get(p.serverLevel()).unmark(roof);
                p.serverLevel().setBlock(roof,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
            }
            p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            p.connection.teleport(.5,100,14.5,180,45);p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.ICARUS).get()));
            ArtifactState.of(p,ArtifactKind.ICARUS).remove("alternateReady");ArtifactState.of(p,ArtifactKind.ICARUS).remove("inputTick");
            data.putBoolean("flightFixtureReady",true);data.putLong("flightFixtureAt",now);data.putDouble("flightLastY",100);
            p.sendSystemMessage(Component.literal("ICARUS_FLIGHT_READY"));return;
        }
        if(now-data.getLong("flightFixtureAt")>240)throw new IllegalStateException("Real Icarus flight input or motion timed out");
        if(!data.contains("flightStartedAt")){
            if(!IcarianFlight.active(p))return;
            data.putLong("flightStartedAt",now);
        }
        double y=p.getY(),step=Math.abs(y-data.getDouble("flightLastY"));data.putDouble("flightLastY",y);
        data.putDouble("flightMaxStep",Math.max(step,data.getDouble("flightMaxStep")));
        data.putDouble("flightRise",Math.max(y-100,data.getDouble("flightRise")));
        if(!data.getBoolean("flightObserved")&&y>101&&IcarianFlight.active(p)){
            data.putBoolean("flightObserved",true);p.sendSystemMessage(Component.literal("ICARUS_FLIGHT_OBSERVED"));
        }
        if(IcarianFlight.active(p))return;
        if(!data.getBoolean("flightObserved")||data.getDouble("flightRise")<3||data.getDouble("flightMaxStep")>2.5
                ||!p.hasEffect(SpecialPickaxes.ICARIAN_FEATHERFALL.get()))
            throw new IllegalStateException("Real ascent failed: rise="+data.getDouble("flightRise")+" step="+data.getDouble("flightMaxStep"));
        for(int roofY:new int[]{104,108}){
            if(!p.serverLevel().getBlockState(new BlockPos(0,roofY,14)).isAir()
                    ||!p.serverLevel().getBlockState(new BlockPos(1,roofY,14)).is(net.minecraft.world.level.block.Blocks.STONE))
                throw new IllegalStateException("Real ascent failed to excavate only its body path through ceiling "+roofY);
        }
        var roofLoot=p.serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(new BlockPos(0,106,14)).inflate(16));
        if(data.getDouble("flightRise")<8||roofLoot.stream().noneMatch(drop->drop.getItem().is(net.minecraft.world.item.Items.COBBLESTONE)))
            throw new IllegalStateException("Real ascent did not cross both ceilings with native stone loot");
        p.sendSystemMessage(Component.literal("ICARUS_CEILINGS_OK"));
        p.sendSystemMessage(Component.literal("ICARUS_FLIGHT_OK rise="+data.getDouble("flightRise")+" maxStep="+data.getDouble("flightMaxStep")));
        data.putBoolean("flightVerified",true);p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
    }
        private static void beginSupremeSetup(net.minecraft.server.level.ServerPlayer p){
        ArtifactConfig.PER_PLAYER.set(16);ArtifactConfig.GLOBAL.set(256);
        p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,12000,0,false,false,false));
        // Keep each server tick bounded: synchronous chunk-spanning fixture fills can stall
        // the integrated server long enough to starve the client gallery and its acknowledgements.
        for(int x=97;x<=103;x++)for(int z=90;z<=99;z++)for(int y=78;y<=84;y++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
        for(int x=98;x<=102;x++)for(int z=94;z<=99;z++)p.serverLevel().setBlock(new BlockPos(x,80,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
        p.serverLevel().setBlockAndUpdate(new BlockPos(112,82,149),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        p.serverLevel().setBlockAndUpdate(new BlockPos(100,82,150),net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState());
        p.serverLevel().setBlockAndUpdate(new BlockPos(100,82,151),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        p.connection.teleport(100.5,81,96.5,0,0);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get()));
        p.getMainHandItem().enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY,1000);
        p.getMainHandItem().getOrCreateTag().putUUID("controlIdentity",java.util.UUID.randomUUID());
        ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",4);
        ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("ready");
        p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;
        p.getMainHandItem().getOrCreateTag().putInt("artifactMode",4);
        p.getPersistentData().putInt("supremeFillCursor",0);
    }
    private static boolean advanceSupremeSetup(net.minecraft.server.level.ServerPlayer p){
        final int xSize=13,zSize=12,ySize=13,total=xSize*zSize*ySize;
        int cursor=p.getPersistentData().getInt("supremeFillCursor");
        int end=Math.min(total,cursor+256);
        for(int i=cursor;i<end;i++){
            int x=94+i/(zSize*ySize);
            int z=100+(i/ySize)%zSize;
            int y=76+i%ySize;
            p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
        }
        p.getPersistentData().putInt("supremeFillCursor",end);
        return end==total;
    }
}
