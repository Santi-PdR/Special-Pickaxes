package io.github.santipdr.specialpickaxes.network;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;

/** One bounded intent packet; all authority stays on the server thread. */
public final class RelicNetwork {
    private static final String VERSION="5";
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("specialpickaxes","controls"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Intent(ArtifactKind kind,RelicControl.Action action,long sequence){}
    private static final java.util.Map<net.minecraft.server.level.ServerPlayer,Long> SEEN=new java.util.WeakHashMap<>();
    public static boolean accept(net.minecraft.server.level.ServerPlayer p,Intent intent){
        if(intent.sequence()<=0||intent.sequence()<=SEEN.getOrDefault(p,0L))return false;
        SEEN.put(p,intent.sequence());return RelicControl.execute(p,intent.kind(),intent.action());
    }
    public static void register(){
        CHANNEL.messageBuilder(Intent.class,0,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeEnum(m.kind());b.writeEnum(m.action());b.writeVarLong(m.sequence());})
            .decoder(b->new Intent(b.readEnum(ArtifactKind.class),b.readEnum(RelicControl.Action.class),b.readVarLong()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)accept(p,m);c.get().setPacketHandled(true);}).add();
    }
}
