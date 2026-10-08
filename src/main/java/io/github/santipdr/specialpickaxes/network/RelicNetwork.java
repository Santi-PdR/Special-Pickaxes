package io.github.santipdr.specialpickaxes.network;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.*;

/** One bounded intent packet; all authority stays on the server thread. */
public final class RelicNetwork {
    private static final String VERSION="7";
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("specialpickaxes","controls"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Intent(ArtifactKind kind,RelicControl.Action action,long sequence,java.util.UUID toolIdentity,int curioSlot){}
    public record CopyChoice(ArtifactKind source,long sequence,java.util.UUID toolIdentity,int curioSlot){}
    private static final java.util.Map<java.util.UUID,Long> SEEN=new java.util.HashMap<>();
    public static boolean accept(net.minecraft.server.level.ServerPlayer p,Intent intent){
        if(intent.sequence()<=0||intent.sequence()<=SEEN.getOrDefault(p.getUUID(),0L))return false;
        SEEN.put(p.getUUID(),intent.sequence());
        if(intent.action()==RelicControl.Action.CURIO_ONE||intent.action()==RelicControl.Action.CURIO_TWO){
            if(intent.curioSlot()<0)return false;
            var equipped=CuriosCompat.find(p);if(equipped==null||equipped.slot()!=intent.curioSlot()||!(equipped.stack().getItem() instanceof ArtifactItem item)||item.kind!=intent.kind())return false;
            return CurioAbilities.execute(p,equipped.stack(),item.kind,intent.action());
        }
        if(intent.curioSlot()!=-1)return false;
        var tool=p.getMainHandItem();if(!tool.hasTag()||!tool.getTag().hasUUID("controlIdentity")||!tool.getTag().getUUID("controlIdentity").equals(intent.toolIdentity()))return false;
        return RelicControl.execute(p,intent.kind(),intent.action());
    }
    public static boolean acceptCopy(net.minecraft.server.level.ServerPlayer p,CopyChoice choice){
        if(choice.sequence()<=0||choice.sequence()<=SEEN.getOrDefault(p.getUUID(),0L))return false;
        SEEN.put(p.getUUID(),choice.sequence());var tool=p.getMainHandItem();
        if(choice.curioSlot()>=0){var equipped=CuriosCompat.find(p);if(equipped==null||equipped.slot()!=choice.curioSlot())return false;tool=equipped.stack();}
        if(!(tool.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.WORLDBREAKER)return false;
        if(choice.curioSlot()<0&&(!tool.hasTag()||!tool.getTag().hasUUID("controlIdentity")||!tool.getTag().getUUID("controlIdentity").equals(choice.toolIdentity())))return false;
        if(!choice.source().playable()||choice.source()==ArtifactKind.WORLDBREAKER)return false;
        tool.getOrCreateTag().putString("copiedSkill",choice.source().id);
        ArtifactFeedback.message(p,"copied_skill",net.minecraft.network.chat.Component.translatable("item.specialpickaxes."+choice.source().id));return true;
    }
    public static void chooseCopy(ArtifactKind source,long sequence,java.util.UUID identity,int curioSlot){CHANNEL.sendToServer(new CopyChoice(source,sequence,identity,curioSlot));}
    public static void forget(net.minecraft.server.level.ServerPlayer p){SEEN.remove(p.getUUID());}
    public static void clear(){SEEN.clear();}
    public static void register(){
        CHANNEL.messageBuilder(Intent.class,0,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeEnum(m.kind());b.writeEnum(m.action());b.writeVarLong(m.sequence());b.writeBoolean(m.curioSlot()>=0);if(m.curioSlot()>=0)b.writeVarInt(m.curioSlot());else b.writeUUID(m.toolIdentity());})
            .decoder(b->{var kind=b.readEnum(ArtifactKind.class);var action=b.readEnum(RelicControl.Action.class);long sequence=b.readVarLong();boolean curio=b.readBoolean();return new Intent(kind,action,sequence,curio?null:b.readUUID(),curio?b.readVarInt():-1);})
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)accept(p,m);c.get().setPacketHandled(true);}).add();
        CHANNEL.messageBuilder(CopyChoice.class,1,NetworkDirection.PLAY_TO_SERVER)
            .encoder((m,b)->{b.writeEnum(m.source());b.writeVarLong(m.sequence());b.writeBoolean(m.curioSlot()>=0);if(m.curioSlot()>=0)b.writeVarInt(m.curioSlot());else b.writeUUID(m.toolIdentity());})
            .decoder(b->{var source=b.readEnum(ArtifactKind.class);long sequence=b.readVarLong();boolean curio=b.readBoolean();return new CopyChoice(source,sequence,curio?null:b.readUUID(),curio?b.readVarInt():-1);})
            .consumerMainThread((m,c)->{var p=c.get().getSender();if(p!=null)acceptCopy(p,m);c.get().setPacketHandled(true);}).add();
    }
}
