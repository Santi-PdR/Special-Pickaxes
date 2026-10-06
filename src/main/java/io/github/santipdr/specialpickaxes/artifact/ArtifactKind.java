package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public enum ArtifactKind {
    PALIMPSEST("palimpsest", 0xffd783, SoundEvents.AMETHYST_BLOCK_CHIME),
    CHOIR("fault_choir", 0x46ede0, SoundEvents.AMETHYST_CLUSTER_BREAK),
    EVENTIDE("eventide", 0xa67aff, SoundEvents.RESPAWN_ANCHOR_CHARGE),
    MERIDIAN("meridian", 0x68b9ff, SoundEvents.ENDERMAN_TELEPORT),
    CRUCIBLE("paradox_crucible", 0xff8648, SoundEvents.FIRECHARGE_USE),
    INTERREGNUM("interregnum", 0x8ae5ff, SoundEvents.BEACON_ACTIVATE),
    WORLDLOOM("worldloom", 0x64ffa9, SoundEvents.STONE_PLACE),
    ICARUS("icarus", 0xffc057, SoundEvents.PISTON_EXTEND),
    AXIOM("hollow_axiom", 0xd9ffbd, SoundEvents.SCULK_CATALYST_BLOOM),
    ATLAS("bifold_atlas", 0xff7b99, SoundEvents.ENCHANTMENT_TABLE_USE),
    WORLDBREAKER("worldbreaker",0xf1bf65,SoundEvents.END_PORTAL_SPAWN),
    CHRONICLE("chronicle",0xb6a1ed,SoundEvents.AMETHYST_BLOCK_RESONATE),
    KEYSTONE("keystone",0x8fdac2,SoundEvents.ANVIL_PLACE),
    TESSELLATOR("tessellator",0xe49abf,SoundEvents.NOTE_BLOCK_PLING.value());
    public final String id;
    public final int color;
    public final SoundEvent sound;
    ArtifactKind(String id, int color, SoundEvent sound) { this.id=id; this.color=color; this.sound=sound; }
    public static ArtifactKind byId(String id) {
        for (var kind : values()) if (kind.id.equals(id)) return kind;
        throw new IllegalArgumentException("Unknown artifact: " + id);
    }
}
