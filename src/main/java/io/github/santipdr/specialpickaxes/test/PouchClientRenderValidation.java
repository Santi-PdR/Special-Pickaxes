package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.nio.file.*;

/** Reads the actual client menu after rendering. Never sets client slot contents. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,value=Dist.CLIENT)
public final class PouchClientRenderValidation {
    private static Object lastScreen;
    private static ItemStack lastDiamond,lastGold;
    private static int stableFrames;
    @SubscribeEvent public static void rendered(ScreenEvent.Render.Post event){
        if(!Boolean.getBoolean("specialpickaxes.clientSmoke"))return;
        var request=Path.of("client-pouch-live-request.flag");var ack=Path.of("client-pouch-live-ack.flag");
        if(!Files.exists(request)||Files.exists(ack))return;
        if(!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                ||!screen.getTitle().getString().equals(Component.translatable("pouch.specialpickaxes.title").getString()))return;
        var menu=screen.getMenu();if(menu.slots.size()<27)return;
        var diamond=menu.getSlot(0).getItem();var gold=menu.getSlot(1).getItem();
        if(!diamond.is(Items.DIAMOND)||diamond.getCount()!=20||!diamond.getHoverName().getString().contains("20")
                ||!gold.is(Items.GOLD_INGOT)||gold.getCount()!=5||!gold.getHoverName().getString().contains("5")){
            stableFrames=0;return;
        }
        if(lastScreen!=screen||lastDiamond!=diamond||lastGold!=gold)stableFrames=0;
        lastScreen=screen;lastDiamond=diamond;lastGold=gold;
        if(++stableFrames<10)return;
        try{
            // Atomic acknowledgement prevents the server observing a partially written file.
            var pending=Path.of("client-pouch-live-ack.tmp");Files.writeString(pending,"20,5");
            Files.move(pending,ack,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
        }catch(java.io.IOException error){throw new IllegalStateException("Cannot acknowledge rendered live pouch",error);}
        com.mojang.logging.LogUtils.getLogger().info("POUCH_LIVE_RENDER_OK stable_frames={} diamond=20 gold=5",stableFrames);
    }
}
