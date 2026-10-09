package io.github.santipdr.specialpickaxes.artifact;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ArtifactConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue PER_PLAYER = B.defineInRange("work.perPlayerPerTick",8,1,16);
    public static final ForgeConfigSpec.IntValue ORE_SCAN_BUDGET = B.defineInRange("work.oreScanPerPlayerPerTick",32,1,128);
    public static final ForgeConfigSpec.IntValue GLOBAL = B.defineInRange("work.globalPerTick",96,1,256);
    public static final ForgeConfigSpec.IntValue JOB_LIMIT = B.defineInRange("work.maxSteps",4096,32,8192);
    public static final ForgeConfigSpec.IntValue JOB_TTL = B.defineInRange("work.timeoutTicks",36000,100,144000);
    public static final ForgeConfigSpec.IntValue RANGE = B.defineInRange("work.playerTether",512,8,2048);
    public static final ForgeConfigSpec.IntValue COOLDOWN = B.defineInRange("abilities.cooldownTicks",20,0,12000);
    public static final ForgeConfigSpec.IntValue COST = B.defineInRange("abilities.activationDurability",2,0,100);
    public static final ForgeConfigSpec.IntValue MEMORY = B.defineInRange("memory.maxRecords",4096,8,16384);
    public static final ForgeConfigSpec.IntValue MEMORY_TTL = B.defineInRange("memory.lifetimeTicks",1728000,200,12096000);
    public static final ForgeConfigSpec.IntValue FIELD_TIME = B.defineInRange("domain.durationTicks",600,20,2400);
    public static final ForgeConfigSpec.IntValue FIELD_TARGETS = B.defineInRange("domain.maxEntities",24,1,64);
    public static final ForgeConfigSpec.IntValue PALIMPSEST_VEIN_LIMIT = B.defineInRange("palimpsest.maxVeinBlocks",256,16,1024);
    public static final ForgeConfigSpec.DoubleValue FIELD_FORCE = B.defineInRange("domain.force",0.4,0.01,0.5);
    public static final ForgeConfigSpec.IntValue MAX_RADIUS = B.defineInRange("geometry.maxRadius",10,3,12);
    public static final ForgeConfigSpec.IntValue BORE_LENGTH = B.defineInRange("geometry.boreLength",40,4,48);
    public static final ForgeConfigSpec.IntValue SOUND_RADIUS = B.defineInRange("audio.nearbyRadius",5,1,8);
    public static final ForgeConfigSpec.IntValue SOUND_COOLDOWN = B.defineInRange("audio.cooldownTicks",20,1,40);
    public static final ForgeConfigSpec.IntValue REGION_LIMIT=B.defineInRange("selection.maxVolume",262144,64,1048576);
    public static final ForgeConfigSpec.IntValue REGION_MEMORY_MIB=B.defineInRange("selection.maxSnapshotMemoryMiB",128,8,1024);
    public static final ForgeConfigSpec.IntValue ACTIVE_JOBS=B.defineInRange("work.maxConcurrentJobs",32,1,128);
    public static final ForgeConfigSpec.IntValue ENCHANT_BUDGET=B.defineInRange("work.enchantedPlayerBudget",128,24,512);
    public static final ForgeConfigSpec SPEC = B.build();
    private ArtifactConfig() {}
}
