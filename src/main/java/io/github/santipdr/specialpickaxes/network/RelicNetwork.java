package io.github.santipdr.specialpickaxes.network;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.*;

/** One bounded intent packet; all authority stays on the server thread. */
public final class RelicNetwork {
    private static final String VERSION="9";
    public static final net.minecraftforge.network.simple.SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("specialpickaxes","controls"),()->VERSION,VERSION::equals,VERSION::equals);
    public record Intent(ArtifactKind kind,RelicControl.Action action,long sequence,java.util.UUID toolIdentity){}
    public record CopyChoice(ArtifactKind source,long sequence,java.util.UUID toolIdentity){}
    public record StasisBoundary(java.util.UUID owner,String dimension,long center,int radius,int halfHeight,boolean visible){}
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
            tool.getOrCreateTag().remove("copiedSkill");tool.getOrCreateTag().remove("copiedMode");
            ArtifactFeedback.message(p,"copy_native");return true;
        }
        if(!choice.source().playable())return false;
        tool.getOrCreateTag().putString("copiedSkill",choice.source().id);
        tool.getOrCreateTag().putInt("copiedMode",ArtifactState.mode(p,choice.source()));
        ArtifactFeedback.message(p,"copied_skill",net.minecraft.network.chat.Component.translatable("item.specialpickaxes."+choice.source().id));return true;
    }
    public static void chooseCopy(ArtifactKind source,long sequence,java.util.UUID identity){CHANNEL.sendToServer(new CopyChoice(source,sequence,identity));}
    public static void showStasisBoundary(ServerPlayer owner,BlockPos center,int radius,int halfHeight){
        sendStasisBoundary(owner,center,radius,halfHeight,true);
    }
    public static void clearStasisBoundary(ServerPlayer owner,BlockPos center,int radius,int halfHeight,net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension){
        sendStasisBoundary(owner,center,radius,halfHeight,dimension,false);
    }
    private static void sendStasisBoundary(ServerPlayer owner,BlockPos center,int radius,int halfHeight,boolean visible){
        sendStasisBoundary(owner,center,radius,halfHeight,owner.serverLevel().dimension(),visible);
    }
    private static void sendStasisBoundary(ServerPlayer owner,BlockPos center,int radius,int halfHeight,
                                           net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,boolean visible){
        double x=center.getX()+.5D,y=center.getY()+.5D,z=center.getZ()+.5D;
        CHANNEL.send(PacketDistributor.NEAR.with(()->new PacketDistributor.TargetPoint(x,y,z,48D,dimension)),
                new StasisBoundary(owner.getUUID(),dimension.location().toString(),center.asLong(),radius,halfHeight,visible));
    }
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
        CHANNEL.messageBuilder(StasisBoundary.class,2,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((m,b)->{b.writeUUID(m.owner());b.writeUtf(m.dimension(),128);b.writeLong(m.center());b.writeVarInt(m.radius());b.writeVarInt(m.halfHeight());b.writeBoolean(m.visible());})
            .decoder(b->new StasisBoundary(b.readUUID(),b.readUtf(128),b.readLong(),b.readVarInt(),b.readVarInt(),b.readBoolean()))
            .consumerMainThread((m,c)->{net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->io.github.santipdr.specialpickaxes.client.StasisBoundaryClient.receive(m));c.get().setPacketHandled(true);}).add();
    }
}
