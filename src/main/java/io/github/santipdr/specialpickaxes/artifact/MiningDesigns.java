package io.github.santipdr.specialpickaxes.artifact;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import java.util.*;

/** Mining-specific plans. Resource detection never sends ore positions or outlines to the client. */
public final class MiningDesigns {
    private static final java.util.concurrent.ConcurrentMap<net.minecraft.world.level.block.Block,Boolean> MODDED_GEOLOGY=new java.util.concurrent.ConcurrentHashMap<>();
    private static final Set<String> WORLDGEN_GEOLOGY=Set.of("terramity:igneostone","undead_revamp2:chiseleddripstoneblock","undead_revamp2:chiseled_dripstonepillar","undead_revamp2:chiseleddripstonepillarbottom","undead_revamp2:chiseleddripstonepillartop");
    private static final Set<String> GEOLOGY_PARTS=Set.of("stone","rock","rocks","basalt","granite","diorite","andesite","limestone","marble","slate","shale","tuff","calcite","dripstone","speleothem","stalactite","stalagmite","deepslate","blackstone","netherrack","endstone","obsidian","sandstone","cobble","cobblestone","gravel","dirt","clay",
            "gabbro","gneiss","quartzite","schist","phyllite","argillite","hornfels","siltstone","mudstone","claystone","conglomerate","breccia","pumice","rhyolite","dacite","travertine","dolomite","chalk","soapstone","gypsum","mud","soil");
    private static final Set<net.minecraft.world.level.block.Block> VANILLA_GEOLOGY=Set.of(
            Blocks.STONE,Blocks.COBBLESTONE,Blocks.MOSSY_COBBLESTONE,Blocks.STONE_BRICKS,Blocks.MOSSY_STONE_BRICKS,Blocks.CRACKED_STONE_BRICKS,Blocks.CHISELED_STONE_BRICKS,
            Blocks.GRANITE,Blocks.POLISHED_GRANITE,Blocks.DIORITE,Blocks.POLISHED_DIORITE,Blocks.ANDESITE,Blocks.POLISHED_ANDESITE,
            Blocks.DEEPSLATE,Blocks.COBBLED_DEEPSLATE,Blocks.POLISHED_DEEPSLATE,Blocks.DEEPSLATE_BRICKS,Blocks.CRACKED_DEEPSLATE_BRICKS,Blocks.DEEPSLATE_TILES,Blocks.CRACKED_DEEPSLATE_TILES,Blocks.CHISELED_DEEPSLATE,
            Blocks.BLACKSTONE,Blocks.POLISHED_BLACKSTONE,Blocks.POLISHED_BLACKSTONE_BRICKS,Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS,Blocks.CHISELED_POLISHED_BLACKSTONE,
            Blocks.TUFF,Blocks.CALCITE,Blocks.BASALT,Blocks.SMOOTH_BASALT,Blocks.OBSIDIAN,Blocks.NETHERRACK,Blocks.END_STONE,Blocks.DRIPSTONE_BLOCK,Blocks.POINTED_DRIPSTONE,
            Blocks.DIRT,Blocks.COARSE_DIRT,Blocks.ROOTED_DIRT,Blocks.GRASS_BLOCK,Blocks.PODZOL,Blocks.GRAVEL,Blocks.SAND,Blocks.RED_SAND,Blocks.CLAY,
            Blocks.SANDSTONE,Blocks.CHISELED_SANDSTONE,Blocks.CUT_SANDSTONE,Blocks.SMOOTH_SANDSTONE,
            Blocks.RED_SANDSTONE,Blocks.CHISELED_RED_SANDSTONE,Blocks.CUT_RED_SANDSTONE,Blocks.SMOOTH_RED_SANDSTONE);
    private MiningDesigns(){}
    public static boolean drill(ServerPlayer p,ItemStack tool,ArtifactKind k){
        var look=p.getLookAngle();var direction=Direction.getNearest(look.x,look.y,look.z);
        int mode=ArtifactState.mode(p,k);
        var shape=k==ArtifactKind.CHOIR?DirectionalProgram.Shape.RESONANT_TUNNEL:k==ArtifactKind.ICARUS?(mode==1?DirectionalProgram.Shape.ICARUS_WIDE:DirectionalProgram.Shape.ICARUS):k==ArtifactKind.EXODIUM?DirectionalProgram.Shape.EXODIUM_LANCE:DirectionalProgram.Shape.values()[mode];
        var origin=k==ArtifactKind.CHOIR||k==ArtifactKind.ICARUS||k==ArtifactKind.EXODIUM?p.blockPosition().above().relative(direction):shape==DirectionalProgram.Shape.CORE_DRILL?p.blockPosition().below():ArtifactActions.target(p).orElse(p.blockPosition().relative(direction,2));
        return WorkQueue.startRegion(p,tool,k,new DirectionalProgram(origin,direction,shape,k));
    }
    public static boolean worldloomBore(ServerPlayer p,ItemStack tool,BlockPos origin){
        var level=p.serverLevel();var state=level.getBlockState(origin);
        if(!MiningDesigns.matrix(state)||ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(level).contains(origin)
                ||!WorldSafety.allowed(p,ArtifactKind.WORLDLOOM,origin)||!WorldSafety.harvestable(p,tool,origin))return false;
        Direction direction=Direction.getNearest(p.getLookAngle().x,p.getLookAngle().y,p.getLookAngle().z);
        return WorkQueue.startRegion(p,tool,ArtifactKind.WORLDLOOM,
                new DirectionalProgram(origin,direction,DirectionalProgram.Shape.WORLDLOOM_BORE,ArtifactKind.WORLDLOOM));
    }
    public static boolean orefall(ServerPlayer p,ItemStack tool,BlockPos center){
        int radius=8,halfHeight=4;var candidates=new ArrayList<BlockPos>();var level=p.serverLevel();
        for(int x=-radius;x<=radius;x++)for(int y=-halfHeight;y<=halfHeight;y++)for(int z=-radius;z<=radius;z++){
            if(x*x+z*z+4*y*y>radius*radius)continue;var pos=center.offset(x,y,z);
            if(level.hasChunkAt(pos)&&ArtifactOres.isOre(level.getBlockState(pos))
                    &&WorldSafety.allowed(p,ArtifactKind.IRIDIUM,pos)&&WorldSafety.harvestable(p,tool,pos))candidates.add(pos.immutable());
        }
        candidates.sort(java.util.Comparator.comparingDouble(pos->pos.distSqr(center)));
        var steps=new ArrayList<WorkStep>(Math.min(candidates.size(),128));
        for(int i=0;i<candidates.size()&&i<128;i++)steps.add(new WorkStep.Mine(candidates.get(i),level.getBlockState(candidates.get(i))));
        return WorkQueue.start(p,tool,ArtifactKind.IRIDIUM,steps);
    }
    public static boolean matrix(net.minecraft.world.level.block.state.BlockState s){return s.is(net.minecraftforge.common.Tags.Blocks.STONE)||s.is(net.minecraft.tags.BlockTags.DIRT)||s.is(net.minecraftforge.common.Tags.Blocks.GRAVEL)||VANILLA_GEOLOGY.contains(s.getBlock())||isModdedGeology(s);}
    public static boolean seamGeology(net.minecraft.world.level.block.state.BlockState s){return matrix(s)&&!ArtifactOres.isOre(s)&&!s.hasBlockEntity()&&s.getFluidState().isEmpty();}
    public static boolean crucibleGeology(net.minecraft.world.level.block.state.BlockState s){
        if(s.hasBlockEntity()||!s.getFluidState().isEmpty()||ArtifactOres.isOre(s))return false;
        if(s.is(net.minecraftforge.common.Tags.Blocks.STONE)||s.is(net.minecraft.tags.BlockTags.DIRT)||s.is(net.minecraftforge.common.Tags.Blocks.GRAVEL))return true;
        return VANILLA_GEOLOGY.contains(s.getBlock())||isModdedGeology(s);
    }
    private static boolean isModdedGeology(net.minecraft.world.level.block.state.BlockState state){
        return MODDED_GEOLOGY.computeIfAbsent(state.getBlock(),block->{
            var key=net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(block);
            if(key==null)return false;
            if(WORLDGEN_GEOLOGY.contains(key.toString()))return true;
            if(key.getNamespace().equals("minecraft"))return false;
            var defaultState=block.defaultBlockState();
            if(defaultState.hasBlockEntity()||!defaultState.getFluidState().isEmpty()||ArtifactOres.isOre(defaultState))return false;
            String path=key.getPath().toLowerCase(java.util.Locale.ROOT);
            for(String part:path.split("_"))if(GEOLOGY_PARTS.contains(part))return true;
            return false;
        });
    }
    /** Tag membership can change during datapack reloads, so clear geology decisions with ore caches. */
    public static void clearCache(){MODDED_GEOLOGY.clear();}
}
