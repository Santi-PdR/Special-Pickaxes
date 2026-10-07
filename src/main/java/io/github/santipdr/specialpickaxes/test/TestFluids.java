package io.github.santipdr.specialpickaxes.test;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fluids.*;
import net.minecraftforge.registries.*;
/** Headless test-only foreign fluid. This entire package is excluded from the release JAR. */
@Mod.EventBusSubscriber(modid="specialpickaxes",bus=Mod.EventBusSubscriber.Bus.MOD)
public final class TestFluids {
    public static final FluidType TYPE=new FluidType(FluidType.Properties.create()){};
    private static final ForgeFlowingFluid.Properties PROPERTIES=new ForgeFlowingFluid.Properties(()->TYPE,TestFluids::source,TestFluids::flowing).block(TestFluids::block);
    public static final ForgeFlowingFluid.Source SOURCE=new ForgeFlowingFluid.Source(PROPERTIES);
    public static final ForgeFlowingFluid.Flowing FLOWING=new ForgeFlowingFluid.Flowing(PROPERTIES);
    public static final LiquidBlock BLOCK=new LiquidBlock(TestFluids::source,BlockBehaviour.Properties.copy(Blocks.WATER));
    private static ForgeFlowingFluid.Source source(){return SOURCE;}
    private static ForgeFlowingFluid.Flowing flowing(){return FLOWING;}
    private static LiquidBlock block(){return BLOCK;}
    @SubscribeEvent public static void register(RegisterEvent event){
        if(!Boolean.getBoolean("specialpickaxes.testFluids"))return;
        event.register(ForgeRegistries.Keys.FLUID_TYPES,h->h.register(new ResourceLocation("mining_test","fluid_type"),TYPE));
        event.register(ForgeRegistries.Keys.FLUIDS,h->{h.register(new ResourceLocation("mining_test","source"),SOURCE);h.register(new ResourceLocation("mining_test","flowing"),FLOWING);});
        event.register(ForgeRegistries.Keys.BLOCKS,h->h.register(new ResourceLocation("mining_test","fluid"),BLOCK));
    }
}
