package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

/** Forge 47 invokes Item.mineBlock BEFORE removeBlock. Confirm the resulting scar at end-of-tick. */
public final class MiningObservations {
    private static final int DROP_QUERY_LIMIT=256;
    private record Observation(ServerPlayer player,ItemStack tool,ArtifactKind kind,BlockPos pos,
                               BlockState state,ResourceKey<Level> dimension,int time,Map<UUID,ItemStack> dropsBefore,
                               boolean dropsBeforeComplete,boolean solarDrops,boolean applyHeldMiningEffects,EquipmentLease<ItemStack> curioSource) {}
    private record DropSnapshot(Map<UUID,ItemStack> stacks,boolean complete) {}
    private static final ArrayDeque<Observation> PENDING=new ArrayDeque<>();
    private MiningObservations() {}
    public static boolean capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state) {
        return capture(p,tool,kind,pos,state,true);
    }
    public static boolean capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state,boolean applyHeldMiningEffects) {
        return capture(p,tool,kind,pos,state,applyHeldMiningEffects,null);
    }
    private static boolean capture(ServerPlayer p,ItemStack tool,ArtifactKind kind,BlockPos pos,BlockState state,
                                   boolean applyHeldMiningEffects,EquipmentLease<ItemStack> curioSource) {
        if(!p.isAlive()||p.isRemoved()||p.isSpectator()||tool.isEmpty()||p.getMainHandItem()!=tool
                ||curioSource!=null&&(!curioSource.active()||curioSource.expected().isEmpty()))return false;
        boolean trackOreDrops=kind==ArtifactKind.IRIDIUM&&ArtifactOres.isOre(state);
        boolean solarDrops=kind==ArtifactKind.PALIMPSEST||curioSource==null&&ArtifactSkills.feedbackKind(tool,kind)==ArtifactKind.PALIMPSEST;
        // Queued Iridium breaks never chain held-mining effects.
        boolean queuedIridiumOre=WorkQueue.running()&&trackOreDrops;
        if((WorkQueue.running()&&!queuedIridiumOre) || PENDING.size()>=1024) return false;
        boolean tagDropsAtSpawn=trackOreDrops&&WorldSafety.capturesIridiumDrops();
        DropSnapshot snapshot=(trackOreDrops||solarDrops)&&!tagDropsAtSpawn?dropSnapshot(p,pos):new DropSnapshot(Map.of(),!tagDropsAtSpawn);
        PENDING.addLast(new Observation(p,tool,kind,pos.immutable(),state,p.level().dimension(),p.server.getTickCount(),snapshot.stacks(),snapshot.complete(),solarDrops,
                applyHeldMiningEffects&&!queuedIridiumOre,curioSource));
        return true;
    }
    /** Only manual natural mining can feed the equipped seismic streak. */
    public static boolean captureCuriosChoir(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.CHOIR
                ||!NaturalExcavationProgram.eligible(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.CHOIR,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosEventide(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.EVENTIDE
                ||!ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosWorldloom(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.WORLDLOOM
                ||!ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosAxiom(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.AXIOM
                ||!ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.AXIOM,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosExodium(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.EXODIUM
                ||!ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.EXODIUM,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosHellspec(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.HELLSPEC
                ||!NaturalExcavationProgram.eligible(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.HELLSPEC,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosStasis(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.INTERREGNUM
                ||!ArtifactOres.isOre(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,pos,state,false,new EquipmentLease<>(curio,current));
    }
    public static boolean captureCuriosParadox(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.CRUCIBLE
                ||!NaturalExcavationProgram.eligible(state)||PlayerPlacedBlocks.get(p.serverLevel()).contains(pos))return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,pos,state,false,new EquipmentLease<>(curio,current));
    }
    /** Dawn in Curios lights only newly produced loot; it never searches for ore. */
    public static boolean captureCuriosDawn(ServerPlayer p,ItemStack curio,BlockPos pos,BlockState state,java.util.function.Supplier<ItemStack> current){
        if(curio.isEmpty()||!(curio.getItem() instanceof ArtifactItem item)||item.kind!=ArtifactKind.PALIMPSEST)return false;
        if(p.getMainHandItem().getItem() instanceof ArtifactItem held&&ArtifactSkills.feedbackKind(p.getMainHandItem(),held.kind)==ArtifactKind.PALIMPSEST)return false;
        return capture(p,p.getMainHandItem(),ArtifactKind.PALIMPSEST,pos,state,false,new EquipmentLease<>(curio,current));
    }
    /** Snapshot a vanilla break when Iridium is in Curios and another tool is held. */
    public static boolean captureCuriosIridium(ServerPlayer player,ItemStack curioPick,BlockPos pos,BlockState state) {
        var equipped=CuriosCompat.findFresh(player);
        return equipped!=null&&equipped.stack()==curioPick&&captureCuriosIridium(player,equipped,pos,state);
    }
    /** A discovered source keeps its original slot; only that slot is read during confirmation. */
    public static boolean captureCuriosIridium(ServerPlayer player,CuriosCompat.Equipped equipped,BlockPos pos,BlockState state){
        return equipped!=null&&captureCuriosIridium(player,equipped.stack(),pos,state,
                ()->CuriosCompat.pickInSlotFresh(player,equipped.slot()));
    }
    /** Inventory adapters supply a live slot lookup, not a snapshot captured before the break. */
    public static boolean captureCuriosIridium(ServerPlayer player,ItemStack curioPick,BlockPos pos,BlockState state,
                                               java.util.function.Supplier<ItemStack> currentPick) {
        if(curioPick.isEmpty()||!(curioPick.getItem() instanceof ArtifactItem equipped)||equipped.kind!=ArtifactKind.IRIDIUM
                ||!ArtifactOres.isOre(state))return false;
        if(player.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.IRIDIUM)return false;
        return capture(player,player.getMainHandItem(),ArtifactKind.IRIDIUM,pos,state,false,new EquipmentLease<>(curioPick,currentPick));
    }
    public static void flush() {
        int budget=512;
        while(budget-->0 && !PENDING.isEmpty()) {
            var o=PENDING.removeFirst();var p=o.player;
            if(!valid(o))continue;
            if(p.serverLevel().getBlockState(o.pos).isAir()) {
                if(o.kind==ArtifactKind.IRIDIUM&&ArtifactOres.isOre(o.state)){
                    var level=p.serverLevel();var origin=p.position();
                    ArtifactPassives.lodeSurgeObserved(p,o.pos,o.curioSource!=null,()->valid(o));
                    // Adding a mob effect invokes external Forge callbacks too.
                    if(!valid(o)||p.serverLevel()!=level||!p.position().equals(origin))continue;
                    if(o.dropsBeforeComplete)highlightNewOreDrops(p,o);
                }
                if(o.solarDrops&&o.dropsBeforeComplete&&valid(o))highlightNewSolarDrops(p,o);
                if(o.curioSource!=null&&o.kind==ArtifactKind.CHOIR&&valid(o))ResonantFault.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.EVENTIDE&&valid(o))EventideRupture.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.CRUCIBLE&&valid(o))ParadoxLattice.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.INTERREGNUM&&valid(o))StasisTimeSlice.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.WORLDLOOM&&valid(o))WorldloomRoots.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.AXIOM&&valid(o))AxiomAftershock.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.EXODIUM&&valid(o))ExodiumFallingStar.observed(p,o.tool,o.pos,o.curioSource);
                if(o.curioSource!=null&&o.kind==ArtifactKind.HELLSPEC&&valid(o))HellspecCinderRing.observed(p,o.tool,o.pos,o.curioSource);
                if(o.applyHeldMiningEffects&&valid(o))ArtifactActions.mined(p,o.tool,o.kind,o.pos,o.state);
            }
        }
    }
    private static boolean valid(Observation o){
        var p=o.player;
        return p.isAlive()&&!p.isRemoved()&&!p.isSpectator()&&p.level().dimension()==o.dimension
                &&p.getMainHandItem()==o.tool&&!o.tool.isEmpty()
                &&ObservationWindow.fresh(p.server.getTickCount(),o.time)&&p.serverLevel().hasChunkAt(o.pos)
                &&(o.curioSource==null||!o.curioSource.expected().isEmpty()&&o.curioSource.active());
    }
    private static DropSnapshot dropSnapshot(ServerPlayer p,BlockPos pos) {
        var snapshot=new HashMap<UUID,ItemStack>();
        int[] inspected={0};
        p.serverLevel().getEntities().get(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
                new AABB(pos).inflate(2),drop->{
            if(++inspected[0]>=DROP_QUERY_LIMIT)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if(drop.isAlive())snapshot.put(drop.getUUID(),drop.getItem().copy());
            return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });
        return new DropSnapshot(snapshot,inspected[0]<DROP_QUERY_LIMIT);
    }
    private static void highlightNewOreDrops(ServerPlayer p,Observation o) {
        int[] inspected={0};
        p.serverLevel().getEntities().get(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
                new AABB(o.pos).inflate(2),drop->{
            if(++inspected[0]>DROP_QUERY_LIMIT)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            if(!drop.isAlive())return net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
            var previous=o.dropsBefore.get(drop.getUUID());var stack=drop.getItem();
            if(previous==null||stack.getCount()>previous.getCount()||!ItemStack.isSameItemSameTags(previous,stack)) {
                OreDropHighlight.mark(drop,p);
            }
            return inspected[0]>=DROP_QUERY_LIMIT?net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT
                    :net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
    private static void highlightNewSolarDrops(ServerPlayer p,Observation o){
        int[] inspected={0};
        p.serverLevel().getEntities().get(net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),new AABB(o.pos).inflate(2),drop->{
            if(++inspected[0]>DROP_QUERY_LIMIT)return net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT;
            var before=o.dropsBefore.get(drop.getUUID());var stack=drop.getItem();
            if(drop.isAlive()&&(before==null||stack.getCount()>before.getCount()||!ItemStack.isSameItemSameTags(before,stack)))drop.setGlowingTag(true);
            return inspected[0]>=DROP_QUERY_LIMIT?net.minecraft.util.AbortableIterationConsumer.Continuation.ABORT:net.minecraft.util.AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }
    public static void forget(ServerPlayer p) { ResonantFault.forget(p);ParadoxLattice.forget(p);PENDING.removeIf(o -> o.player.getUUID().equals(p.getUUID())); }
    public static void clear() { PENDING.clear();ResonantFault.clear();ParadoxLattice.clear(); }
}
