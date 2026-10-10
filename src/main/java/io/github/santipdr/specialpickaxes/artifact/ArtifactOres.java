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
    private static final ConcurrentMap<Block,String> ORE_FAMILIES = new ConcurrentHashMap<>();
    private static final String NO_ORE_FAMILY="";
    private static final String[] HOST_PREFIXES={"deepslate_","netherrack_","blackstone_","stone_","granite_","diorite_","andesite_","basalt_","end_stone_"};
    private static final Set<String> WORLDGEN_ORES=Set.of("terramity:brimslag","terramity:stellar_remnant","undead_revamp2:bostrokestone");
    private ArtifactOres() {}
    public static boolean isOre(BlockState state){
        return state.is(net.minecraftforge.common.Tags.Blocks.ORES)||state.is(BlockTags.COAL_ORES)||state.is(BlockTags.IRON_ORES)
            ||state.is(BlockTags.GOLD_ORES)||state.is(BlockTags.DIAMOND_ORES)||state.is(BlockTags.REDSTONE_ORES)
            ||state.is(BlockTags.LAPIS_ORES)||state.is(BlockTags.EMERALD_ORES)||state.is(BlockTags.COPPER_ORES)
            ||NONSTANDARD_ORES.computeIfAbsent(state.getBlock(), block -> {
                if(state.getTags().anyMatch(ArtifactOres::isOreTag))return true;
                var key=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
                return isOreBlock(key);
            });
    }

    /** Stable family fallback for conventional host-rock variants such as tin_ore/deepslate_tin_ore. */
    public static String registryOreFamily(Block block){
        String family=ORE_FAMILIES.get(block);
        if(family!=null)return family.isEmpty()?null:family;
        var key=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
        if(key==null)return null;
        if(!isOreBlock(key)){ORE_FAMILIES.putIfAbsent(block,NO_ORE_FAMILY);return null;}
        String path=stripHost(key.getPath());
        if(path.endsWith("_deepslate_ore"))path=path.substring(0,path.length()-"_deepslate_ore".length())+"_ore";
        if(path.startsWith("ores_"))path=canonicalPrefixedOre(path.substring("ores_".length()));
        else if(path.startsWith("ore_"))path=canonicalPrefixedOre(path.substring("ore_".length()));
        family=key.getNamespace()+":"+path;
        ORE_FAMILIES.putIfAbsent(block,family);
        return ORE_FAMILIES.get(block);
    }

    /** Terramity worldgen registers these ore-feature outputs without an ore suffix or ore tag. */
    private static boolean isOreBlock(net.minecraft.resources.ResourceLocation key){
        if(key==null)return false;
        if(isOreTagPath(key.getPath()))return true;
        return WORLDGEN_ORES.contains(key.toString());
    }

    /** Datapack reloads can change tag membership, so discard the bounded registry-block cache then. */
    public static void clearCache(){ NONSTANDARD_ORES.clear(); }

    private static String canonicalPrefixedOre(String material){
        String canonical=stripHost(material);
        if(canonical.endsWith("_deepslate"))canonical=canonical.substring(0,canonical.length()-"_deepslate".length());
        return canonical+"_ore";
    }

    private static String stripHost(String path){
        for(String host:HOST_PREFIXES)if(path.startsWith(host))return path.substring(host.length());
        return path;
    }

    static boolean isOreTagPath(String path) {
        if(path==null)return false;
        // Forge uses these tags for the host rock around ores, not for the ore blocks themselves.
        if(path.equals("ores_in_ground")||path.startsWith("ores_in_ground/")||path.startsWith("ores_in_ground_"))return false;
        boolean prefixed=path.startsWith("ore_")&&path.length()>4||path.startsWith("ores_")&&path.length()>5;
        if(prefixed&&(path.endsWith("_block")||path.endsWith("_blocks")))return false;
        return path.equals("ore")||path.equals("ores")||path.startsWith("ore/")||path.startsWith("ores/")
            ||prefixed||path.endsWith("_ore")||path.endsWith("_ores");
    }

    private static boolean isOreTag(TagKey<Block> tag) { return isOreTagPath(tag.location().getPath()); }

    /** Specific family tags bridge natural/deepslate and modded variants without joining every ore via forge:ores. */
    public static Set<TagKey<Block>> veinFamilies(BlockState state){
        return state.getTags().filter(tag->isVeinFamilyTag(tag.location().getPath()))
            .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean isVeinFamilyTag(String path) {
        // Forge's ores_in_ground tags classify host rock, not mineral families.
        if(path.equals("ores_in_ground")||path.startsWith("ores_in_ground/")||path.startsWith("ores_in_ground_"))return false;
        return path.startsWith("ores/")&&path.length()>6||path.startsWith("ore/")&&path.length()>4
            ||path.startsWith("ores_")&&path.length()>5||path.startsWith("ore_")&&path.length()>4
            ||path.endsWith("_ores")&&path.length()>5||path.endsWith("_ore")&&path.length()>4;
    }
}
