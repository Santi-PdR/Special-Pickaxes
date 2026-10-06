package io.github.santipdr.specialpickaxes;

import io.github.santipdr.specialpickaxes.ability.PickaxeAbility;
import net.minecraft.world.item.Tier;
import java.util.List;

public record PickaxeDefinition(String id, Tier tier, int attackDamage, float attackSpeed,
                                List<PickaxeAbility> abilities) {
    public PickaxeDefinition { abilities = List.copyOf(abilities); }
}
