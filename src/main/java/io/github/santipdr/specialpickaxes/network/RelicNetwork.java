package io.github.santipdr.specialpickaxes.network;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;

/** One bounded intent packet; all authority stays on the server thread. */
public final class RelicNetwork {
    private static final String VERSION="8";
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("specialpickaxes","controls"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Intent(ArtifactKind kind,RelicControl.Action action,long sequence,java.util.UUID toolIdentity){}
    public record CopyChoice(ArtifactKind source,long sequence,java.util.UUID toolIdentity){}
    private static final java.util.Map<java.util.UUID,Long> SEEN=new java.util.HashMap<>();
    public static boolean accept(net.minecraft.server.level.ServerPlayer p,Intent intent){
        if(intent.sequence()<=0||intent.sequence()<=SEEN.getOrDefault(p.getUUID(),0L))return false;
        SEEN.put(p.getUUID(),intent.sequence());
        var tool=p.getMainHandItem();if(!tool.hasTag()||!tool.getTag().hasUUID("controlIdentity")||!tool.getTag().getUUID("controlIdentity").equals(intent.toolIdentity()))return false;
        return RelicControl.execute(p,intent.kind(),intent.action());
    }
    public static boolean acceptCopy(net.minecraft.server.level.ServerPlayer p,CopyChoice choice){
        if(choice.sequence()<=0||choice.sequence()<=SEEN.getOrDefault(p.getUUID(),0L))return false;
        SEEN.put(p.getUUID(),choice.sequence());var tool=p.getMainHandItem();
        if(!(tool.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.WORLDBREAKER)return false;
        if(!tool.hasTag()||!tool.getTag().hasUUID("controlIdentity")||!tool.getTag().getUUID("controlIdentity").equals(choice.toolIdentity()))return false;
        if(WorkQueue.busy(p)||ArtifactInteraction.selecting(p)){ArtifactFeedback.message(p,"copy_busy");return false;}
        if(choice.source()==ArtifactKind.WORLDBREAKER){
            tool.getOrCreateTag().remove("copiedSkill");
            ArtifactFeedback.message(p,"copy_native");return true;
        }
        if(!choice.source().playable())return false;
        tool.getOrCreateTag().putString("copiedSkill",choice.source().id);
        ArtifactFeedback.message(p,"copied_skill",net.minecraft.network.chat.Component.translatable("item.specialpickaxes."+choice.source().id));return true;
    }
    public static void chooseCopy(ArtifactKind source,long sequence,java.util.UUID identity){CHANNEL.sendToServer(new CopyChoice(source,sequence,identity));}
    public static void forget(net.minecraft.server.level.ServerPlayer p){SEEN.remove(p.getUUID());}
    public static void clear(){SEEN.clear();}
    public static void register(){
        CHANNEL.messageBuilder(Intent.class,0,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeEnum(m.kind());b.writeEnum(m.action());b.writeVarLong(m.sequence());b.writeUUID(m.toolIdentity());})
            .decoder(b->new Intent(b.readEnum(ArtifactKind.class),b.readEnum(RelicControl.Action.class),b.readVarLong(),b.readUUID()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)accept(p,m);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(CopyChoice.class,1,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeEnum(m.source());b.writeVarLong(m.sequence());b.writeUUID(m.toolIdentity());})
            .decoder(b->new CopyChoice(b.readEnum(ArtifactKind.class),b.readVarLong(),b.readUUID()))
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)acceptCopy(p,m);c.get().setPacketHandled(true);}).add();
    }
}
