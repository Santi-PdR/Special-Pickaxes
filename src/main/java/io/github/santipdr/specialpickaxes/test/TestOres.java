package io.github.santipdr.specialpickaxes.test;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

/** Foreign registry blocks used to verify ore name and tag compatibility in GameTests. */
@Mod.EventBusSubscriber(modid="specialpickaxes",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class TestOres {
    public static Block NAMED_ORE;
    public static Block TAGGED_ORE;
    public static Block HOST_ROCK;
    public static Block GABBRO_ROCK;
    public static Block MUDSTONE_ROCK;

    private TestOres(){}

    @SubscribeEvent public static void register(RegisterEvent event){
        if(!Boolean.getBoolean("specialpickaxes.testOres"))return;
        event.register(ForgeRegistries.Keys.BLOCKS,helper->{
            NAMED_ORE=new Block(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE));
            TAGGED_ORE=new Block(BlockBehaviour.Properties.copy(Blocks.AMETHYST_BLOCK));
            HOST_ROCK=new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
            GABBRO_ROCK=new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
            MUDSTONE_ROCK=new Block(BlockBehaviour.Properties.copy(Blocks.STONE));
            helper.register(new ResourceLocation("pickaxe_compat_test","cobalt_ore"),NAMED_ORE);
            helper.register(new ResourceLocation("pickaxe_compat_test","raw_gem"),TAGGED_ORE);
            helper.register(new ResourceLocation("pickaxe_compat_test","limestone"),HOST_ROCK);
            helper.register(new ResourceLocation("pickaxe_compat_test","gabbro"),GABBRO_ROCK);
            helper.register(new ResourceLocation("pickaxe_compat_test","mudstone"),MUDSTONE_ROCK);
        });
    }
}
