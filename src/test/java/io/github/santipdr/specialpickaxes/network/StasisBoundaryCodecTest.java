package io.github.santipdr.specialpickaxes.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StasisBoundaryCodecTest {
    @Test void visibleBoundaryRoundTripsItsDimensionAndExactVolume(){
        var message=new StasisBoundaryMessage(UUID.fromString("b74a2277-b7bc-4d99-93e8-6e21dc1555b4"),
                "minecraft:the_nether",new net.minecraft.core.BlockPos(-129,72,511).asLong(),8,4,true);
        var buffer=new FriendlyByteBuf(Unpooled.buffer());
        StasisBoundaryCodec.encode(message,buffer);
        assertEquals(message,StasisBoundaryCodec.decode(buffer));
        buffer.release();
    }

    @Test void clearMessageRoundTripsWithoutLosingRemovalFlag(){
        var message=new StasisBoundaryMessage(UUID.fromString("5c90d6c1-4092-40cc-86e1-e36e44d93b56"),
                "minecraft:overworld",0L,2,2,false);
        var buffer=new FriendlyByteBuf(Unpooled.buffer());
        StasisBoundaryCodec.encode(message,buffer);
        var decoded=StasisBoundaryCodec.decode(buffer);
        assertEquals(message,decoded);
        assertFalse(decoded.visible());
        buffer.release();
    }
}
