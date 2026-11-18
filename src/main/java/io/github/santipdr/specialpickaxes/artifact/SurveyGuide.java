package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/** One temporary, server-approved deposit guide per casting tool. No passive ore scan or tick subscriber. */
public final class SurveyGuide {
    private static final java.util.Set<java.util.UUID> UPDATING=new java.util.HashSet<>();
    private SurveyGuide(){}
    public static void clear(ItemStack tool){
        if(!tool.hasTag())return;
        var tag=tool.getTag();
        tag.remove(SurveyBearing.POSITION);tag.remove(SurveyBearing.UNTIL);
        tag.remove(SurveyBearing.DIMENSION);tag.remove(SurveyBearing.ORE);tag.remove(SurveyBearing.OWNER);
    }
    private static boolean caster(ServerPlayer player,ItemStack tool){
        return player.isAlive()&&!player.isRemoved()&&!player.isSpectator()&&player.mayBuild()
                &&tool==player.getMainHandItem()&&!tool.isEmpty()&&tool.getItem() instanceof ArtifactItem item
                &&ArtifactSkills.feedbackKind(tool,item.kind)==ArtifactKind.PALIMPSEST;
    }
    private static boolean deposit(ServerPlayer player,BlockPos target,BlockState expected){
        var level=player.serverLevel();
        return !level.isOutsideBuildHeight(target)&&level.hasChunkAt(target)
                &&player.position().distanceToSqr(Vec3.atCenterOf(target))<=SurveyBearing.RANGE*SurveyBearing.RANGE
                &&level.getBlockState(target)==expected&&ArtifactOres.isOre(expected)
                &&!PlayerPlacedBlocks.get(level).contains(target);
    }
    public static boolean publish(ServerPlayer player,ItemStack tool,BlockPos target,BlockState expected){
        if(!UPDATING.add(player.getUUID()))return false;
        try{return publishOnce(player,tool,target,expected);}finally{UPDATING.remove(player.getUUID());}
    }
    private static boolean publishOnce(ServerPlayer player,ItemStack tool,BlockPos target,BlockState expected){
        clear(tool);
        if(!caster(player,tool)||!deposit(player,target,expected))return false;
        var context=HeldUtilityContext.capture(player,tool);
        boolean allowed=WorldSafety.allowed(player,ArtifactKind.PALIMPSEST,target);
        if(!allowed||!context.valid()||!caster(player,tool)||!deposit(player,target,expected))return false;
        var key=ForgeRegistries.BLOCKS.getKey(expected.getBlock());if(key==null)return false;
        var tag=tool.getOrCreateTag();tag.putLong(SurveyBearing.POSITION,target.asLong());
        tag.putLong(SurveyBearing.UNTIL,ArtifactState.now(player)+SurveyBearing.DURATION);
        tag.putString(SurveyBearing.DIMENSION,player.serverLevel().dimension().location().toString());
        tag.putString(SurveyBearing.ORE,key.toString());tag.putUUID(SurveyBearing.OWNER,player.getUUID());return true;
    }
    /** Revalidates only the stored deposit; NEVER searches for another one or renews its expiry. */
    public static void refresh(ServerPlayer player,ItemStack tool){
        if(!tool.hasTag()||!tool.getTag().contains(SurveyBearing.POSITION)||!UPDATING.add(player.getUUID()))return;
        try{refreshOnce(player,tool);}finally{UPDATING.remove(player.getUUID());}
    }
    private static void refreshOnce(ServerPlayer player,ItemStack tool){
        if(!tool.hasTag()||!tool.getTag().contains(SurveyBearing.POSITION))return;
        if(!caster(player,tool)){clear(tool);return;}
        var reading=SurveyBearing.read(tool.getTag(),player.serverLevel().dimension().location().toString(),ArtifactState.now(player),player.position(),player.getUUID());
        if(reading.isEmpty()){clear(tool);return;}
        var at=reading.get().target();var level=player.serverLevel();
        if(!level.hasChunkAt(at)){clear(tool);return;}
        var state=level.getBlockState(at);var key=ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if(key==null||!key.toString().equals(tool.getTag().getString(SurveyBearing.ORE))||!deposit(player,at,state)){clear(tool);return;}
        var context=HeldUtilityContext.capture(player,tool);
        boolean allowed=WorldSafety.allowed(player,ArtifactKind.PALIMPSEST,at);
        if(!allowed||!context.valid()||!caster(player,tool)||!deposit(player,at,state))clear(tool);
    }
}
