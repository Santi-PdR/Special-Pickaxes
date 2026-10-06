package io.github.santipdr.specialpickaxes.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.santipdr.specialpickaxes.SpecialPickaxeItem;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public final class SmeltingLootModifier extends LootModifier {
    public static final Codec<SmeltingLootModifier> CODEC = RecordCodecBuilder.create(instance ->
        codecStart(instance).apply(instance, SmeltingLootModifier::new));
    public SmeltingLootModifier(LootItemCondition[] conditions) { super(conditions); }
    @Override public Codec<? extends IGlobalLootModifier> codec() { return CODEC; }
    @Override protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
        if (!context.hasParam(LootContextParams.BLOCK_STATE)) return loot;
        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        if (tool == null || !(tool.getItem() instanceof SpecialPickaxeItem pick) || !pick.hasAbility("inferno")
                || EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) return loot;
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>();
        for (ItemStack drop : loot) {
            var input = new SimpleContainer(drop.copyWithCount(1));
            var recipe = context.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, context.getLevel());
            ItemStack output = recipe.map(r -> r.assemble(input, context.getLevel().registryAccess())).orElse(ItemStack.EMPTY);
            if (output.isEmpty()) { result.add(drop); continue; }
            long total = (long) output.getCount() * drop.getCount();
            // Malformed mod recipes must not allocate an unbounded list; leave their input intact.
            if (total > 4096) { result.add(drop); continue; }
            while (total > 0) {
                int count = (int) Math.min(total, output.getMaxStackSize());
                result.add(output.copyWithCount(count));
                total -= count;
            }
        }
        return result;
    }
}
