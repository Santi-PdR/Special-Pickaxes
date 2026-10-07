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
    private static int ticks;
    @SubscribeEvent public static void tick(TickEvent.ServerTickEvent e){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke")||e.phase!=TickEvent.Phase.END)return;
        var server=ServerLifecycleHooks.getCurrentServer();if(server==null)return;
        for(var p:server.getPlayerList().getPlayers()){
            if(!p.getTags().contains("artifact_gallery")||!java.nio.file.Files.exists(java.nio.file.Path.of("gallery-captured.flag")))continue;
            if(ticks>=240&&(ticks-240)/100<ArtifactKind.playableValues().length&&(ticks-240)%100==99){
                try{if(!java.nio.file.Files.readString(java.nio.file.Path.of("client-capture-ack.txt")).trim().equals(Integer.toString((ticks-240)/100)))return;}
                catch(java.io.IOException missing){return;}
            }
            ticks++;if(ticks<240)continue;
            int index=(ticks-240)/100,phase=(ticks-240)%100;
            if(index>=ArtifactKind.playableValues().length){
                if(index==ArtifactKind.playableValues().length&&phase==0)p.sendSystemMessage(Component.literal("ARTIFACT_UX_COMPLETE"));
                var data=p.getPersistentData();
                if(CompanionActions.active(p)&&!data.getBoolean("controlActive")){data.putBoolean("controlActive",true);p.sendSystemMessage(Component.literal("CONTROL_ACTIVE"));}
                if(data.getBoolean("controlActive")&&!CompanionActions.active(p)&&!data.getBoolean("controlCancelled")){data.putBoolean("controlCancelled",true);p.sendSystemMessage(Component.literal("CONTROL_CANCELLED"));}
                if(data.getBoolean("controlCancelled")&&!data.getBoolean("supremeSetup")){
                    data.putBoolean("supremeSetup",true);
                    for(int x=84;x<=116;x++)for(int z=84;z<=116;z++)for(int y=80;y<=92;y++)p.serverLevel().setBlock(new BlockPos(x,y,z),y==80?net.minecraft.world.level.block.Blocks.STONE.defaultBlockState():net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),2);
                    p.connection.teleport(100.5,81,115.5,180,6);
                    p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get()));
                    ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",6);ArtifactConfig.PER_PLAYER.set(4);
                    p.sendSystemMessage(Component.literal("SUPREME_READY"));
                }
                if(data.getBoolean("supremeSetup")&&WorkQueue.busy(p)&&!data.getBoolean("supremeRunning")){
                    data.putBoolean("supremeRunning",true);data.putLong("supremeCenter",ArtifactActions.target(p).orElse(p.blockPosition()).asLong());
                    p.sendSystemMessage(Component.literal("SUPREME_EXECUTING"));
                }
                if(data.getBoolean("supremeRunning")&&!WorkQueue.busy(p)&&!data.getBoolean("supremeDone")){
                    data.putBoolean("supremeDone",true);ArtifactConfig.PER_PLAYER.set(24);
                    var center=BlockPos.of(data.getLong("supremeCenter"));
                    if(!p.serverLevel().getBlockState(center).is(net.minecraft.world.level.block.Blocks.STONE)||!p.serverLevel().getBlockState(center.above(5)).is(net.minecraft.world.level.block.Blocks.STONE))throw new IllegalStateException("Supreme demo did not preserve its core and build its vault");
                    p.sendSystemMessage(Component.literal("SUPREME_COMPLETE"));
                }
                return;
            }
            var k=ArtifactKind.playableValues()[index];
            if(phase==0){
                CompanionActions.stop(p);ArtifactInteraction.clear(p);WorkQueue.cancel(p);DomainFields.stop(p);
                var stack=new ItemStack(SpecialPickaxes.PICKS.get(k).get());
                var list=new net.minecraft.nbt.ListTag();var ench=new net.minecraft.nbt.CompoundTag();ench.putString("id","minecraft:efficiency");ench.putInt("lvl",1000);list.add(ench);stack.getOrCreateTag().put("Enchantments",list);
                p.setItemInHand(InteractionHand.MAIN_HAND,stack);
                if(ArtifactInteraction.regional(k,ArtifactState.mode(p,k))){
                    ArtifactInteraction.use(p,stack,k,false);
                    ArtifactInteraction.left(p,new BlockPos(-5,65,6),false);ArtifactInteraction.left(p,new BlockPos(-3,67,8),false);
                    if(k==ArtifactKind.ATLAS||k==ArtifactKind.TESSELLATOR){ArtifactInteraction.left(p,new BlockPos(3,65,6),false);ArtifactInteraction.left(p,new BlockPos(5,67,8),false);ArtifactInteraction.left(p,new BlockPos(5,67,8),true);}
                }else if(k==ArtifactKind.MERIDIAN){
                    ArtifactState.anchor(p,k,"a",new BlockPos(-5,66,6));ArtifactState.anchor(p,k,"b",new BlockPos(5,66,6));
                }else if(k==ArtifactKind.INTERREGNUM||k==ArtifactKind.EVENTIDE||k==ArtifactKind.AEGIS)DomainFields.start(p,stack,k,new BlockPos(0,66,6),4);
                else if(k==ArtifactKind.LODESTAR)ArtifactState.anchor(p,k,"a",new BlockPos(-5,65,6));
                else DirectAbilities.preview(p,k);
                p.getPersistentData().putBoolean("uxFixture",true);
            }
            if(phase==40){
                p.sendSystemMessage(Component.literal("ARTIFACT_UX_READY_"+k.id));
                com.mojang.logging.LogUtils.getLogger().info("CLIENT_FIXTURE {} MODE={} EFFICIENCY={}",k.id,ArtifactInteraction.modeKey(k,ArtifactState.mode(p,k)),EnchantmentScaling.level(p.getMainHandItem(),net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY));
            }
        }
    }
}
