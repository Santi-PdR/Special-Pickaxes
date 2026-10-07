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
            if(ticks>=240&&(ticks-240)/100<SCENES.size()&&(ticks-240)%100==99){
                try{if(!java.nio.file.Files.readString(java.nio.file.Path.of("client-capture-ack.txt")).trim().equals(Integer.toString((ticks-240)/100)))return;}
                catch(java.io.IOException missing){return;}
            }
            ticks++;if(ticks<240)continue;
            int index=(ticks-240)/100,phase=(ticks-240)%100;
            if(index>=SCENES.size()){
                if(index==SCENES.size()&&phase==0)p.sendSystemMessage(Component.literal("ARTIFACT_UX_COMPLETE"));
                var data=p.getPersistentData();
                if(WorkQueue.busy(p)&&!data.getBoolean("controlActive")){data.putBoolean("controlActive",true);p.sendSystemMessage(Component.literal("CONTROL_ACTIVE"));}
                if(data.getBoolean("controlActive")&&!WorkQueue.busy(p)&&!data.getBoolean("controlCancelled")){data.putBoolean("controlCancelled",true);p.sendSystemMessage(Component.literal("CONTROL_CANCELLED"));}
                if(data.getBoolean("controlCancelled")&&!data.getBoolean("supremeSetup")){
                    data.putBoolean("supremeSetup",true);
                    for(int x=76;x<=124;x++)for(int z=100;z<=151;z++)for(int y=75;y<=95;y++)p.serverLevel().setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                    p.serverLevel().setBlockAndUpdate(new BlockPos(100,82,150),net.minecraft.world.level.block.Blocks.BEDROCK.defaultBlockState());
                    for(int x=98;x<=102;x++)for(int z=94;z<=99;z++)p.serverLevel().setBlock(new BlockPos(x,80,z),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);
                    p.connection.teleport(100.5,81,96.5,0,0);
                    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get()));
                    p.getMainHandItem().enchant(net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY,1000);
                    ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",4);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("ready");
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
}
