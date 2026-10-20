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
            ArtifactFeedback.message(player, "alternate_no_target");
            return false;
        }
        data.putLong("alternateReady", now + Math.max(100, ArtifactConfig.COOLDOWN.get() * 5));
        ArtifactKind feedbackKind=feedbackKind(tool,kind);
        ArtifactFeedback.sound(player, feedbackKind, "alternate");
        ArtifactFeedback.burst(player, feedbackKind, player.blockPosition(), 10);
        ArtifactFeedback.message(player, "alternate_used");
        return true;
    }

    /** Worldbreaker's copied skills use the source pick's visual and audio signature. */
    public static ArtifactKind feedbackKind(ItemStack tool,ArtifactKind kind){
        if(kind!=ArtifactKind.WORLDBREAKER||!tool.hasTag()||!tool.getTag().contains("copiedSkill"))return kind;
        try{
            var copied=ArtifactKind.byId(tool.getTag().getString("copiedSkill"));
            return copied==ArtifactKind.WORLDBREAKER?kind:copied;
        }catch(IllegalArgumentException invalid){return kind;}
    }
}
