package io.github.santipdr.specialpickaxes.artifact;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ArtifactConfig {
    private static final ForgeConfigSpec.Builder B = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.IntValue PER_PLAYER = B.defineInRange("work.perPlayerPerTick",24,1,64);
    public static final ForgeConfigSpec.IntValue GLOBAL = B.defineInRange("work.globalPerTick",192,1,512);
    public static final ForgeConfigSpec.IntValue JOB_LIMIT = B.defineInRange("work.maxSteps",4096,32,8192);
    public static final ForgeConfigSpec.IntValue JOB_TTL = B.defineInRange("work.timeoutTicks",2400,100,12000);
    public static final ForgeConfigSpec.IntValue RANGE = B.defineInRange("work.playerTether",64,8,96);
    public static final ForgeConfigSpec.IntValue COOLDOWN = B.defineInRange("abilities.cooldownTicks",40,0,12000);
    public static final ForgeConfigSpec.IntValue COST = B.defineInRange("abilities.activationDurability",4,0,100);
    public static final ForgeConfigSpec.IntValue MEMORY = B.defineInRange("memory.maxRecords",256,8,512);
    public static final ForgeConfigSpec.IntValue MEMORY_TTL = B.defineInRange("memory.lifetimeTicks",24000,200,72000);
    public static final ForgeConfigSpec.IntValue FIELD_TIME = B.defineInRange("domain.durationTicks",400,20,2400);
    public static final ForgeConfigSpec.IntValue FIELD_TARGETS = B.defineInRange("domain.maxEntities",24,1,64);
    public static final ForgeConfigSpec.DoubleValue FIELD_FORCE = B.defineInRange("domain.force",0.25,0.01,0.5);
    public static final ForgeConfigSpec.IntValue MAX_RADIUS = B.defineInRange("geometry.maxRadius",8,3,12);
    public static final ForgeConfigSpec.IntValue BORE_LENGTH = B.defineInRange("geometry.boreLength",24,4,48);
    public static final ForgeConfigSpec SPEC = B.build();
    private ArtifactConfig() {}
}
