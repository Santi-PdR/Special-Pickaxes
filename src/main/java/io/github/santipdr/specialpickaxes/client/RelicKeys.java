package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.network.RelicNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="specialpickaxes",value=Dist.CLIENT)
public final class RelicKeys {
    private static final io.github.santipdr.specialpickaxes.artifact.PressLatch[] DOWN=java.util.stream.IntStream.range(0,RelicControl.Action.values().length).mapToObj(i->new io.github.santipdr.specialpickaxes.artifact.PressLatch()).toArray(io.github.santipdr.specialpickaxes.artifact.PressLatch[]::new);
    private static Object connection;private static long sequence;
    private static final int[] DEFAULTS={GLFW.GLFW_KEY_R,GLFW.GLFW_KEY_G,GLFW.GLFW_KEY_C,GLFW.GLFW_KEY_B,GLFW.GLFW_KEY_ENTER,GLFW.GLFW_KEY_V,GLFW.GLFW_KEY_K};
    public static final KeyMapping[] KEYS=new KeyMapping[RelicControl.Action.values().length];
    static {for(var a:RelicControl.Action.values())KEYS[a.ordinal()]=new KeyMapping("key.specialpickaxes."+a.name().toLowerCase(java.util.Locale.ROOT),net.minecraftforge.client.settings.KeyConflictContext.IN_GAME,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,DEFAULTS[a.ordinal()],"key.categories.specialpickaxes");}
    @Mod.EventBusSubscriber(modid="specialpickaxes",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterKeyMappingsEvent e){for(var key:KEYS)e.register(key);}
    }
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(connection!=mc.getConnection()){
            connection=mc.getConnection();sequence=0;for(var a:RelicControl.Action.values())DOWN[a.ordinal()].update(KEYS[a.ordinal()].isDown());return;
        }
        for(var a:RelicControl.Action.values()){
            var key=KEYS[a.ordinal()];for(int n=0;n<8&&key.consumeClick();n++){}
            boolean edge=DOWN[a.ordinal()].update(key.isDown());
            if(edge&&mc.screen==null&&mc.player!=null&&mc.player.getMainHandItem().getItem() instanceof ArtifactItem item&&item.kind.playable())
                RelicNetwork.CHANNEL.sendToServer(new RelicNetwork.Intent(item.kind,a,++sequence));
        }
    }
    public static net.minecraft.network.chat.Component name(RelicControl.Action action){return KEYS[action.ordinal()].getTranslatedKeyMessage();}
}
