package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Alternate skills are utility actions; timed status effects are handled by passives. */
public final class ArtifactSkills {
    private static final java.util.Set<java.util.UUID> ACTIVE=new java.util.HashSet<>();
    private ArtifactSkills() {}

    public static boolean use(ServerPlayer player, ItemStack tool, ArtifactKind kind) {
        if(!kind.playable()||!player.isAlive()||player.isRemoved()||player.isSpectator()||!player.mayBuild()
                ||tool.isEmpty()||player.getMainHandItem()!=tool
                ||!(tool.getItem() instanceof ArtifactItem item)||item.kind!=kind||!ACTIVE.add(player.getUUID()))return false;
        try{return perform(player,tool,kind);}finally{ACTIVE.remove(player.getUUID());}
    }
    private static boolean perform(ServerPlayer player,ItemStack tool,ArtifactKind kind){
        var data = ArtifactState.of(player, kind);
        long now = ArtifactState.now(player);
        int interval=Math.max(100,ArtifactConfig.COOLDOWN.get()*5);
        if (PersistedCooldown.active(now,data.getLong("alternateReady"),interval)) {
            ArtifactFeedback.message(player, "cooldown");
            return false;
        }
        if (!ArtifactTechniques.performHeldAlternate(player, tool, kind)) {
            ArtifactFeedback.message(player, "alternate_no_target");
            return false;
        }
        data.putLong("alternateReady", now + interval);
        ArtifactKind feedbackKind=feedbackKind(tool,kind);
        ArtifactFeedback.sound(player, feedbackKind, "alternate");
        MiningSignature.emit(player,feedbackKind);
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
