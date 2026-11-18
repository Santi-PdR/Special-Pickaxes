package io.github.santipdr.specialpickaxes.artifact;

import java.util.List;

/** Immutable mode keys: hot-path count/key queries never allocate arrays. */
final class ArtifactModeCatalog {
    private ArtifactModeCatalog(){}
    private static final List<String> VEIN=List.of("vein");
    private static final List<String> FORWARD=List.of("forward");
    private static final List<String> GRAVITY=List.of("gravity_in","gravity_out");
    private static final List<String> MATERIAL=List.of("stone","deepslate","granite","diorite","andesite","dirt","basalt","obsidian","calcite","tuff","dripstone","gravel");
    private static final List<String> STASIS=List.of("domain","aura");
    private static final List<String> QUARRY=List.of("quarry");
    private static final List<String> ICARIAN=List.of("forward","wide");
    private static final List<String> HOLLOW=List.of("hollow_pulse");
    private static final List<String> STAR=List.of("starfall");
    private static final List<String> ORE=List.of("orefall");
    private static final List<String> HELL=List.of("hellforge");
    private static final List<String> CONNECTED=List.of("connected");
    private static final List<String> WORLD=List.of("carve","fracture","cleave","core_drill","world_shatter","region_break");
    private static final List<String> RETIRED=List.of("retired");
    static List<String> keys(ArtifactKind kind){return switch(kind){
        case PALIMPSEST -> VEIN; case CHOIR -> FORWARD; case EVENTIDE -> GRAVITY;
        case CRUCIBLE -> MATERIAL; case INTERREGNUM -> STASIS; case WORLDLOOM -> QUARRY;
        case ICARUS -> ICARIAN; case AXIOM -> HOLLOW; case EXODIUM -> STAR;
        case IRIDIUM -> ORE; case HELLSPEC -> HELL; case SEAM_RIPPER -> CONNECTED;
        case WORLDBREAKER -> WORLD; default -> RETIRED;
    };}
}
