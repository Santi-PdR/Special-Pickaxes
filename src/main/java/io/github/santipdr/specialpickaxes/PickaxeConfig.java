package io.github.santipdr.specialpickaxes;

import net.minecraftforge.common.ForgeConfigSpec;
import java.util.LinkedHashMap;
import java.util.Map;

/** Server configuration is synced by Forge; all limits have hard upper bounds. */
public final class PickaxeConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public record Timing(ForgeConfigSpec.IntValue cooldown, ForgeConfigSpec.IntValue duration,
                         ForgeConfigSpec.IntValue cost) {}
    public static final Map<String, Timing> TIMINGS = new LinkedHashMap<>();
    static {
        timing("overdrive", 600, 100, 8);
        timing("excavator", 10, 0, 0);
        timing("vein_miner", 40, 0, 0);
        timing("inferno", 0, 0, 0);
        timing("magnetic", 0, 0, 0);
        timing("scanner", 200, 0, 2);
        timing("storm", 160, 0, 8);
        timing("void", 300, 600, 12);
        timing("ender", 100, 0, 4);
        timing("explosive", 200, 0, 8);
    }
    public static final ForgeConfigSpec.DoubleValue SPEED = B.defineInRange("overdrive.multiplier", 10.0, 1.0, 10.0);
    public static final ForgeConfigSpec.IntValue AREA_RADIUS = B.defineInRange("excavator.radius", 1, 1, 2);
    public static final ForgeConfigSpec.IntValue VEIN_LIMIT = B.defineInRange("vein_miner.maxBlocks", 32, 1, 128);
    public static final ForgeConfigSpec.IntValue VEIN_RADIUS = B.defineInRange("vein_miner.radius", 8, 1, 16);
    public static final ForgeConfigSpec.DoubleValue HARDNESS = B.defineInRange("mining.maxRelativeHardness", 3.0, 1.0, 10.0);
    public static final ForgeConfigSpec.IntValue MAGNET_RADIUS = B.defineInRange("magnetic.radius", 5, 1, 12);
    public static final ForgeConfigSpec.DoubleValue MAGNET_SPEED = B.defineInRange("magnetic.speed", 0.2, 0.01, 0.5);
    public static final ForgeConfigSpec.IntValue MAGNET_LIMIT = B.defineInRange("magnetic.itemsPerTick", 24, 1, 64);
    public static final ForgeConfigSpec.IntValue SCAN_RADIUS = B.defineInRange("scanner.radius", 8, 1, 16);
    public static final ForgeConfigSpec.IntValue SCAN_LIMIT = B.defineInRange("scanner.maxResults", 16, 1, 64);
    public static final ForgeConfigSpec.IntValue STORM_RADIUS = B.defineInRange("storm.radius", 6, 1, 12);
    public static final ForgeConfigSpec.IntValue STORM_TARGETS = B.defineInRange("storm.targets", 3, 1, 8);
    public static final ForgeConfigSpec.DoubleValue STORM_DAMAGE = B.defineInRange("storm.damage", 6.0, 1.0, 20.0);
    public static final ForgeConfigSpec.IntValue STORM_SLOW = B.defineInRange("storm.slowTicks", 40, 0, 100);
    public static final ForgeConfigSpec.IntValue VOID_ARM_COOLDOWN = B.defineInRange("void.armCooldownTicks", 20, 1, 200);
    public static final ForgeConfigSpec.IntValue VOID_RANGE = B.defineInRange("void.range", 24, 1, 48);
    public static final ForgeConfigSpec.IntValue ENDER_RANGE = B.defineInRange("ender.range", 8, 1, 16);
    public static final ForgeConfigSpec.IntValue BLAST_RADIUS = B.defineInRange("explosive.radius", 2, 1, 3);
    public static final ForgeConfigSpec.IntValue BLAST_LIMIT = B.defineInRange("explosive.maxBlocks", 24, 1, 64);
    public static final ForgeConfigSpec.BooleanValue BLAST_ENABLED = B.define("explosive.enabled", true);
    public static final ForgeConfigSpec SPEC = B.build();
    private static void timing(String id, int cooldown, int duration, int cost) {
        TIMINGS.put(id, new Timing(B.defineInRange(id + ".cooldownTicks", cooldown, 0, 72000),
            B.defineInRange(id + ".durationTicks", duration, 0, 72000),
            B.defineInRange(id + ".durabilityCost", cost, 0, 1000)));
    }
    private PickaxeConfig() {}
}
