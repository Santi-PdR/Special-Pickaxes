package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Alternate skills are utility actions; timed status effects are handled by passives. */
public final class ArtifactSkills {
    private ArtifactSkills() {}

    public static boolean use(ServerPlayer player, ItemStack tool, ArtifactKind kind) {
        var data = ArtifactState.of(player, kind);
        long now = ArtifactState.now(player);
        if (data.getLong("alternateReady") > now) {
            ArtifactFeedback.message(player, "cooldown");
            return false;
        }
        if (!ArtifactTechniques.performHeldAlternate(player, tool, kind)) {
            ArtifactFeedback.message(player, "no_target");
            return false;
        }
        data.putLong("alternateReady", now + Math.max(100, ArtifactConfig.COOLDOWN.get() * 5));
        ArtifactFeedback.sound(player, kind, "alternate");
        ArtifactFeedback.burst(player, kind, player.blockPosition(), 10);
        ArtifactFeedback.message(player, "alternate_used");
        return true;
    }
}
