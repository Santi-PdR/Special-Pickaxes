package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.network.RelicNetwork;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="specialpickaxes",value=Dist.CLIENT)
public final class RelicKeys {
    private static final io.github.santipdr.specialpickaxes.artifact.PressLatch[] DOWN=java.util.stream.IntStream.range(0,RelicControl.Action.values().length).mapToObj(i->new io.github.santipdr.specialpickaxes.artifact.PressLatch()).toArray(io.github.santipdr.specialpickaxes.artifact.PressLatch[]::new);
    private static Object connection;private static long sequence;
    public static final KeyMapping MANUAL=new KeyMapping("key.specialpickaxes.manual",net.minecraftforge.client.settings.KeyConflictContext.IN_GAME,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_I,"key.categories.specialpickaxes");
    private static final int[] DEFAULTS={GLFW.GLFW_KEY_R,GLFW.GLFW_KEY_C,GLFW.GLFW_KEY_B,GLFW.GLFW_KEY_ENTER,GLFW.GLFW_KEY_V,GLFW.GLFW_KEY_K,GLFW.GLFW_KEY_X};
    public static final KeyMapping[] KEYS=new KeyMapping[RelicControl.Action.values().length];
    static {for(var a:RelicControl.Action.values())KEYS[a.ordinal()]=new KeyMapping("key.specialpickaxes."+a.name().toLowerCase(java.util.Locale.ROOT),net.minecraftforge.client.settings.KeyConflictContext.IN_GAME,com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM,DEFAULTS[a.ordinal()],"key.categories.specialpickaxes");}
    @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){
        if(e.phase!=TickEvent.Phase.END)return;var mc=Minecraft.getInstance();
        if(connection!=mc.getConnection()){
            connection=mc.getConnection();sequence=0;for(var a:RelicControl.Action.values())DOWN[a.ordinal()].update(KEYS[a.ordinal()].isDown());return;
        }
        if(MANUAL.consumeClick()&&mc.screen==null&&mc.player!=null){
            if(mc.player.getMainHandItem().getItem() instanceof ArtifactItem item)mc.setScreen(new ArtifactManualScreen(item.kind));
            else {var equipped=CuriosCompat.find(mc.player);if(equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem item)mc.setScreen(new ArtifactManualScreen(item.kind));}
        }
        for(var a:RelicControl.Action.values()){
            if(a==RelicControl.Action.CONFIRM||a==RelicControl.Action.PAUSE||a==RelicControl.Action.SELECT||a==RelicControl.Action.CANCEL)continue;
            var key=KEYS[a.ordinal()];for(int n=0;n<8&&key.consumeClick();n++){}
            boolean edge=DOWN[a.ordinal()].update(key.isDown());
            if(!edge||mc.screen!=null||mc.player==null)continue;
            var hand=mc.player.getMainHandItem();
            boolean validTool=hand.getItem() instanceof ArtifactItem item&&item.kind.playable();
            boolean identity=hand.hasTag()&&hand.getTag().hasUUID("controlIdentity");
            if(Boolean.getBoolean("specialpickaxes.clientSmoke"))
                com.mojang.logging.LogUtils.getLogger().info("CLIENT_INPUT_EDGE_{} validTool={} identity={}",a.name(),validTool,identity);
            if(validTool&&identity&&hand.getItem() instanceof ArtifactItem item)
                RelicNetwork.CHANNEL.sendToServer(new RelicNetwork.Intent(item.kind,a,++sequence,hand.getTag().getUUID("controlIdentity")));
        }
    }
    public static void chooseCopy(ArtifactKind source){
        var mc=Minecraft.getInstance();if(mc.player==null)return;var hand=mc.player.getMainHandItem();
        if(hand.getItem() instanceof ArtifactItem item&&item.kind==ArtifactKind.WORLDBREAKER&&hand.hasTag()&&hand.getTag().hasUUID("controlIdentity"))
            RelicNetwork.chooseCopy(source,++sequence,hand.getTag().getUUID("controlIdentity"));
    }
    public static net.minecraft.network.chat.Component name(RelicControl.Action action){return KEYS[action.ordinal()].getTranslatedKeyMessage();}
}
