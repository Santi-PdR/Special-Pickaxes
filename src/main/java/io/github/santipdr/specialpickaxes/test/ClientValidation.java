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
    private static int ticks;
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke")||e.phase!=TickEvent.Phase.END)return;
        var server=ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        for(var p:server.getPlayerList().getPlayers()){
            if(!p.getTags().contains("artifact_gallery")||!java.nio.file.Files.exists(java.nio.file.Path.of("gallery-captured.flag")))continue;
            // Observe input even while waiting for screenshot acknowledgement. Otherwise a
            // slow screenshot helper can make activation and acknowledgement wait on each other.
            if(ticks>=240&&(ticks-240)/100<SCENES.size()&&(ticks-240)%100>=40){
                var current=SCENES.get((ticks-240)/100);var state=p.getPersistentData();
                if(!state.getBoolean("sceneExecuted")&&ArtifactState.of(p,current.kind()).getLong("ready")>state.getLong("sceneReadyBefore")){
                    state.putBoolean("sceneExecuted",true);p.sendSystemMessage(Component.literal("SCENE_ACTIVATED_"+current.kind().id+"-"+ArtifactInteraction.modeKey(current.kind(),current.mode())));
                }
            }
            if(ticks>=240&&(ticks-240)/100<SCENES.size()&&(ticks-240)%100==99){
                try{if(!java.nio.file.Files.readString(java.nio.file.Path.of("client-capture-ack.txt")).trim().equals(Integer.toString((ticks-240)/100)))return;}
                catch(java.io.IOException missing){return;}
            }
            if(ticks==239+SCENES.size()*100){WorkQueue.cancel(p);DomainFields.stop(p);ArtifactInteraction.clear(p);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("ready");setupSupreme(p);}
            ticks++;if(ticks<240)continue;
            int index=(ticks-240)/100,phase=(ticks-240)%100;
            if(index>=SCENES.size()){
                if(index==SCENES.size()&&phase==0)p.sendSystemMessage(Component.literal("ARTIFACT_UX_COMPLETE"));
                var data=p.getPersistentData();
                if(WorkQueue.busy(p)&&!data.getBoolean("controlActive")){data.putBoolean("controlActive",true);WorkQueue.togglePause(p);p.sendSystemMessage(Component.literal("CONTROL_ACTIVE"));}
                if(data.getBoolean("controlActive")&&!WorkQueue.busy(p)&&!data.getBoolean("controlCancelled")){data.putBoolean("controlCancelled",true);p.sendSystemMessage(Component.literal("CONTROL_CANCELLED"));}
                if(data.getBoolean("controlCancelled")&&!data.getBoolean("supremeSetup")){
                    data.putBoolean("supremeSetup",true);
                    setupSupreme(p);
                    p.sendSystemMessage(Component.literal("SUPREME_READY"));
                }
                if(data.getBoolean("supremeSetup")&&WorkQueue.busy(p)&&!data.getBoolean("supremeRunning")){
                    data.putBoolean("supremeRunning",true);data.putLong("supremeCenter",ArtifactActions.target(p).orElse(p.blockPosition()).asLong());
                    p.sendSystemMessage(Component.literal("SUPREME_EXECUTING"));
                }
                if(data.getBoolean("supremeRunning")&&!WorkQueue.busy(p)&&!data.getBoolean("supremeDone")){
                    data.putBoolean("supremeDone",true);ArtifactConfig.PER_PLAYER.set(24);
                    if(!p.serverLevel().getBlockState(new BlockPos(100,82,100)).isAir()
                        ||!p.serverLevel().getBlockState(new BlockPos(112,82,140)).isAir()
                        ||!p.serverLevel().getBlockState(new BlockPos(112,82,100)).is(net.minecraft.world.level.block.Blocks.STONE)
                        ||!p.serverLevel().getBlockState(new BlockPos(100,82,150)).is(net.minecraft.world.level.block.Blocks.BEDROCK)
                        ||!p.serverLevel().getBlockState(new BlockPos(100,82,151)).is(net.minecraft.world.level.block.Blocks.STONE))throw new IllegalStateException("Supreme wedge geometry or barrier failed");
                    p.sendSystemMessage(Component.literal("SUPREME_COMPLETE"));
                }
                return;
            }
            var scene=SCENES.get(index);var k=scene.kind();
            if(phase==0){
                ArtifactInteraction.clear(p);WorkQueue.cancel(p);DomainFields.stop(p);
                if(scene.mode()==0){
                    var stack=new ItemStack(SpecialPickaxes.PICKS.get(k).get());
                    var list=new net.minecraft.nbt.ListTag();var ench=new net.minecraft.nbt.CompoundTag();ench.putString("id","minecraft:efficiency");ench.putInt("lvl",1000);list.add(ench);stack.getOrCreateTag().put("Enchantments",list);
                    p.setItemInHand(InteractionHand.MAIN_HAND,stack);ArtifactState.of(p,k).putInt("mode",0);
                } else if(ArtifactState.mode(p,k)!=scene.mode())throw new IllegalStateException("Real C key did not change mode: "+k+" expected "+scene.mode());
                // CORE DRILL legitimately excavates under the actor. Restore the next
                // scene's standing platform instead of inheriting that open shaft.
                for(int x=-9;x<=9;x++)for(int z=-1;z<=17;z++)p.serverLevel().setBlock(new BlockPos(x,64,z),net.minecraft.world.level.block.Blocks.POLISHED_ANDESITE.defaultBlockState(),2);
                p.connection.teleport(0.5,65,14.5,180,-14);
                p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;
                for(int x=-6;x<=6;x++)for(int y=65;y<=72;y++)for(int z=4;z<=12;z++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
                for(int x=-5;x<=-3;x++)for(int y=65;y<=67;y++)for(int z=6;z<=8;z++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                var rock=new BlockPos(0,67,10);p.serverLevel().setBlockAndUpdate(rock,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                p.serverLevel().setBlockAndUpdate(rock.above(),net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState());
                p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(k==ArtifactKind.CRUCIBLE?net.minecraft.world.item.Items.BASALT:net.minecraft.world.item.Items.STONE,64));
                if(k==ArtifactKind.PALIMPSEST||k==ArtifactKind.CHOIR){
                    p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                    if(!p.gameMode.destroyBlock(rock))throw new IllegalStateException("Memory fixture mining failed");MiningObservations.flush();
                    p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
                    if(k==ArtifactKind.CHOIR)p.serverLevel().setBlockAndUpdate(rock,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                }
                p.getPersistentData().putLong("sceneReadyBefore",ArtifactState.of(p,k).getLong("ready"));p.getPersistentData().putBoolean("sceneExecuted",false);
                if(ArtifactInteraction.regional(k,scene.mode())){
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
    private static void setupSupreme(net.minecraft.server.level.ServerPlayer p){
        // Clear the approach as well as the workpiece: the disposable server world has
        // natural terrain, which must not put the actor inside a tree or mountain.
        for(int x=76;x<=124;x++)for(int z=90;z<=99;z++)for(int y=75;y<=95;y++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
                    for(int x=76;x<=124;x++)for(int z=100;z<=151;z++)for(int y=75;y<=95;y++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                    p.serverLevel().setBlockAndUpdate(new BlockPos(100,82,150),net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState());
                    for(int x=98;x<=102;x++)for(int z=94;z<=99;z++)p.serverLevel().setBlock(new BlockPos(x,80,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                    p.connection.teleport(100.5,81,96.5,0,0);
                    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get()));
                    p.getMainHandItem().enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY,1000);
                    ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",4);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("ready");
        p.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);p.fallDistance=0;
        p.getMainHandItem().getOrCreateTag().putInt("artifactMode",4);
    }

}
