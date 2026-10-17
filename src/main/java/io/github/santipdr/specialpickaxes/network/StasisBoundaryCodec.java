package io.github.santipdr.specialpickaxes.network;

import net.minecraft.network.FriendlyByteBuf;

/** Small codec for the nearby client-only Stasis boundary renderer. */
public final class StasisBoundaryCodec {
    private static final int MAX_DIMENSION_LENGTH=128;
    private StasisBoundaryCodec(){}

    public static void encode(StasisBoundaryMessage message,FriendlyByteBuf buffer){
        buffer.writeUUID(message.owner());
        buffer.writeUtf(message.dimension(),MAX_DIMENSION_LENGTH);
        buffer.writeLong(message.center());
        buffer.writeVarInt(message.radius());
        buffer.writeVarInt(message.halfHeight());
        buffer.writeBoolean(message.visible());
    }

    public static StasisBoundaryMessage decode(FriendlyByteBuf buffer){
        return new StasisBoundaryMessage(buffer.readUUID(),buffer.readUtf(MAX_DIMENSION_LENGTH),buffer.readLong(),
                buffer.readVarInt(),buffer.readVarInt(),buffer.readBoolean());
    }
}
