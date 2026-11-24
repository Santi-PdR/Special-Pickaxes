package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;

/** Actual vanilla slot-packet codec, not a TCP throughput/latency benchmark. */
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class PouchPacketGameTests {
    private record Encoded(ItemStack stack,int bytes){}
    private static Encoded roundTrip(GameTestHelper h,ClientboundContainerSetSlotPacket packet){
        var buffer=new FriendlyByteBuf(Unpooled.buffer());
        try{
            packet.write(buffer);int bytes=buffer.readableBytes();
            var decoded=new ClientboundContainerSetSlotPacket(buffer);
            h.assertTrue(buffer.readableBytes()==0&&decoded.getContainerId()==packet.getContainerId()
                    &&decoded.getStateId()==packet.getStateId()&&decoded.getSlot()==packet.getSlot(),"native slot packet framing round-trips exactly");
            h.assertTrue(ItemStack.matches(decoded.getItem(),packet.getItem()),"all shared item NBT survives actual packet codec");
            return new Encoded(decoded.getItem(),bytes);
        }finally{buffer.release();}
    }
    @GameTest(template="empty",batch="pouch_native_slot_codec_r7")
    public static void fullPouchSurvivesSlotCodecAndQueuedPacketIsIsolated(GameTestHelper h){
        var tool=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());
        int emptyBytes=roundTrip(h,new ClientboundContainerSetSlotPacket(1,7,0,tool)).bytes();
        var materials=net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
                .filter(item->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft"))
                .filter(item->{var stack=new ItemStack(item);return !stack.isEmpty()&&stack.isStackable()&&!stack.hasTag()&&!stack.serializeNBT().contains("ForgeCaps");})
                .limit(64).toList();
        h.assertTrue(materials.size()==64,"full codec fixture has 64 ordinary types");
        for(var item:materials)h.assertTrue(PickaxePouch.insert(tool,new ItemStack(item,4096)).isEmpty(),"fill codec fixture");
        var queued=new ClientboundContainerSetSlotPacket(1,7,0,tool);
        var removed=PickaxePouch.extract(tool,63,1);
        h.assertTrue(removed.is(materials.get(63))&&removed.getCount()==1,"change live bag after packet was created");
        var full=roundTrip(h,queued);var changed=roundTrip(h,new ClientboundContainerSetSlotPacket(1,8,0,tool));
        for(int slot=0;slot<64;slot++){
            h.assertTrue(PickaxePouch.preview(full.stack(),slot).is(materials.get(slot))&&PickaxePouch.amount(full.stack(),slot)==4096,"queued packet retains complete prior snapshot");
            h.assertTrue(PickaxePouch.preview(changed.stack(),slot).is(materials.get(slot))&&PickaxePouch.amount(changed.stack(),slot)==(slot==63?4095:4096),"changed packet carries exact new snapshot");
        }
        h.assertTrue(full.bytes()==changed.bytes(),"one amount update has fixed-width NBT cost, not one entry per stored unit");
        PickaxePouch.extract(changed.stack(),0,64);
        h.assertTrue(PickaxePouch.amount(tool,0)==4096&&PickaxePouch.amount(queued.getItem(),0)==4096,"decoded client copy cannot mutate live bag or queued packet");
        com.mojang.logging.LogUtils.getLogger().info("POUCH_SLOT_CODEC types=64 units_full=262144 units_changed=262143 empty_bytes={} full_bytes={} changed_bytes={}",emptyBytes,full.bytes(),changed.bytes());
        h.succeed();
    }
}
