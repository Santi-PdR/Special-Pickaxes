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
            if(ticks>=240&&(ticks-240)%100==99){
                try{if(!java.nio.file.Files.readString(java.nio.file.Path.of("client-capture-ack.txt")).trim().equals(Integer.toString((ticks-240)/100)))return;}
                catch(java.io.IOException missing){return;}
            }
            ticks++;if(ticks<240)continue;
            int index=(ticks-240)/100,phase=(ticks-240)%100;
            if(index>=ArtifactKind.values().length){if(index==ArtifactKind.values().length&&phase==0)p.sendSystemMessage(Component.literal("ARTIFACT_UX_COMPLETE"));return;}
            var k=ArtifactKind.values()[index];
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
