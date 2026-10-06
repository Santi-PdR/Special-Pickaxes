package io.github.santipdr.specialpickaxes;

import com.mojang.serialization.Codec;
import io.github.santipdr.specialpickaxes.ability.*;
import io.github.santipdr.specialpickaxes.loot.SmeltingLootModifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mod(SpecialPickaxes.ID)
public final class SpecialPickaxes {
    public static final String ID = "specialpickaxes";
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ID);
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT =
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ID);
    public static final RegistryObject<MobEffect> OVERDRIVE = EFFECTS.register("overdrive", OverdriveEffect::new);
    public static final Map<String, RegistryObject<Item>> PICKS = new LinkedHashMap<>();
    static {
        add("overdrive", Tiers.DIAMOND, new SpeedAbility());
        add("excavator", Tiers.DIAMOND, new AreaMiningAbility());
        add("vein_miner", Tiers.DIAMOND, new VeinMiningAbility());
        add("inferno", Tiers.IRON, new AutoSmeltAbility());
        add("magnetic", Tiers.IRON, new MagnetAbility());
        add("scanner", Tiers.IRON, new OreScannerAbility());
        add("storm", Tiers.DIAMOND, new StormAbility());
        add("void", Tiers.NETHERITE, new VoidAnchorAbility());
        add("ender", Tiers.DIAMOND, new TeleportAbility());
        add("explosive", Tiers.DIAMOND, new ExplosiveMiningAbility());
        TABS.register("pickaxes", () -> CreativeModeTab.builder().title(Component.translatable("itemGroup.specialpickaxes"))
            .icon(() -> new ItemStack(PICKS.get("overdrive").get()))
            .displayItems((parameters, output) -> PICKS.values().forEach(item -> output.accept(item.get()))).build());
        LOOT.register("auto_smelt", () -> SmeltingLootModifier.CODEC);
    }
    private static void add(String id, Tier tier, PickaxeAbility... abilities) {
        var definition = new PickaxeDefinition(id, tier, 1, -2.8F, List.of(abilities));
        PICKS.put(id, ITEMS.register(id, () -> new SpecialPickaxeItem(definition)));
    }
    public SpecialPickaxes() {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus); EFFECTS.register(bus); TABS.register(bus); LOOT.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PickaxeConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(this::breakSpeed);
        MinecraftForge.EVENT_BUS.addListener(this::clonePlayer);
        MinecraftForge.EVENT_BUS.addListener(this::login);
    }
    private void breakSpeed(PlayerEvent.BreakSpeed event) {
        var player = event.getEntity();
        var stack = player.getMainHandItem();
        if (player.hasEffect(OVERDRIVE.get()) && stack.getItem() instanceof SpecialPickaxeItem pick
                && pick.hasAbility("overdrive") && stack.isCorrectToolForDrops(event.getState())
                && event.getNewSpeed() > 0 && !event.isCanceled())
            event.setNewSpeed(event.getNewSpeed() * PickaxeConfig.SPEED.get().floatValue());
    }
    private void clonePlayer(PlayerEvent.Clone event) {
        // No death/relog cooldown reset; anchors intentionally do not survive death.
        var old = event.getOriginal().getPersistentData();
        if (old.contains(AbilityRuntime.DATA)) {
            var copy = old.getCompound(AbilityRuntime.DATA).copy();
            copy.remove("anchor");
            event.getEntity().getPersistentData().put(AbilityRuntime.DATA, copy);
        }
    }
    private void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            var data = AbilityRuntime.data(player);
            PICKS.forEach((id, item) -> {
                long remaining = data.getLong(id + "_ready") - AbilityRuntime.now(player);
                if (remaining > 0) player.getCooldowns().addCooldown(item.get(), (int) Math.min(72000, remaining));
            });
        }
    }
    private static final class OverdriveEffect extends MobEffect {
        private OverdriveEffect() { super(MobEffectCategory.BENEFICIAL, 0xffc745); }
    }
}
