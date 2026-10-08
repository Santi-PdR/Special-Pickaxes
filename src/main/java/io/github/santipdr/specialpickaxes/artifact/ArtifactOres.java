package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.stream.Collectors;

/** Vanilla/common ore tags, modded ore tags and conventional registry names. */
public final class ArtifactOres {
    private static final ConcurrentMap<Block,Boolean> NONSTANDARD_ORES = new ConcurrentHashMap<>();
    private ArtifactOres() {}
    public static boolean isOre(BlockState state){
        return state.is(net.minecraftforge.common.Tags.Blocks.ORES)||state.is(BlockTags.COAL_ORES)||state.is(BlockTags.IRON_ORES)
            ||state.is(BlockTags.GOLD_ORES)||state.is(BlockTags.DIAMOND_ORES)||state.is(BlockTags.REDSTONE_ORES)
            ||state.is(BlockTags.LAPIS_ORES)||state.is(BlockTags.EMERALD_ORES)||state.is(BlockTags.COPPER_ORES)
            ||NONSTANDARD_ORES.computeIfAbsent(state.getBlock(), block -> {
                if(state.getTags().anyMatch(ArtifactOres::isOreTag))return true;
                var key=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
                return key!=null&&isOreTagPath(key.getPath());
            });
    }

    /** Stable family fallback for conventional host-rock variants such as tin_ore/deepslate_tin_ore. */
    public static String registryOreFamily(Block block){
        var key=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        if(key==null||!isOreTagPath(key.getPath()))return null;
        String path=key.getPath();
        for(String host:new String[]{"deepslate_","netherrack_","blackstone_","stone_","granite_","diorite_","andesite_","basalt_","end_stone_"})
            if(path.startsWith(host)){path=path.substring(host.length());break;}
        if(path.endsWith("_deepslate_ore"))path=path.substring(0,path.length()-"_deepslate_ore".length())+"_ore";
        return key.getNamespace()+":"+path;
    }

    /** Datapack reloads can change tag membership, so discard the bounded registry-block cache then. */
    public static void clearCache(){ NONSTANDARD_ORES.clear(); }

    static boolean isOreTagPath(String path) {
        return path.equals("ore")||path.equals("ores")||path.startsWith("ore/")||path.startsWith("ores/")
            ||path.endsWith("_ore")||path.endsWith("_ores");
    }

    private static boolean isOreTag(TagKey<Block> tag) { return isOreTagPath(tag.location().getPath()); }

    /** Specific family tags bridge natural/deepslate and modded variants without joining every ore via forge:ores. */
    public static Set<TagKey<Block>> veinFamilies(BlockState state){
        return state.getTags().filter(tag->isVeinFamilyTag(tag.location().getPath()))
            .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isVeinFamilyTag(String path) {
        return path.startsWith("ores/")&&path.length()>6||path.startsWith("ore/")&&path.length()>4
            ||path.endsWith("_ores")&&path.length()>5||path.endsWith("_ore")&&path.length()>4;
    }
}
