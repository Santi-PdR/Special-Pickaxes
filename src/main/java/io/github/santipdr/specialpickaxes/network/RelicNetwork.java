package io.github.santipdr.specialpickaxes.network;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;

/** One bounded intent packet; all authority stays on the server thread. */
public final class RelicNetwork {
    private static final String VERSION="4";
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("specialpickaxes","controls"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Intent(ArtifactKind kind,RelicControl.Action action){}
    public static void register(){
        CHANNEL.messageBuilder(Intent.class,0,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeEnum(m.kind());b.writeEnum(m.action());})
            .decoder(b->new Intent(b.readEnum(ArtifactKind.class),b.readEnum(RelicControl.Action.class)))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)RelicControl.execute(p,m.kind(),m.action());c.get().setPacketHandled(true);}).add();
    }
}
