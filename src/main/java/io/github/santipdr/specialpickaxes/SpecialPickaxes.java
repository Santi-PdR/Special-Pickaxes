package io.github.santipdr.specialpickaxes;

import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.*;
import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import java.util.*;

@Mod(SpecialPickaxes.ID)
public final class SpecialPickaxes {
    private int passiveTickClock;
    private final Map<UUID,ArtifactKind> curiosPassives=new HashMap<>();
    public static final String ID="specialpickaxes";
    public static final Tier TIER=TierSortingRegistry.registerTier(new ForgeTier(4,32768,64F,12F,50,
        net.minecraft.tags.TagKey.create(Registries.BLOCK,new ResourceLocation(ID,"needs_artifact_tool")),() -> Ingredient.EMPTY),new ResourceLocation(ID,"artifact"),List.of(Tiers.NETHERITE),List.of());
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ID);
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,ID);
    public static final RegistryObject<MobEffect> DOMINION=EFFECTS.register("dominion",DominionEffect::new);
    public static final RegistryObject<MobEffect> GRAVITIC_TETHER=EFFECTS.register("gravitic_tether",GraviticTetherEffect::new);
    public static final RegistryObject<MobEffect> HEARTWOOD_RENEWAL=EFFECTS.register("heartwood_renewal",HeartwoodRenewalEffect::new);
    public static final RegistryObject<MobEffect> HEARTWOOD_GRASP=EFFECTS.register("heartwood_grasp",HeartwoodGraspEffect::new);
    public static final RegistryObject<MobEffect> SEARING_SHELL=EFFECTS.register("searing_shell",SearingShellEffect::new);
    public static final RegistryObject<MobEffect> NULL_MARK=EFFECTS.register("null_mark",NullMarkEffect::new);
    public static final RegistryObject<MobEffect> NULL_VEIL=EFFECTS.register("null_veil",NullVeilEffect::new);
    public static final RegistryObject<MobEffect> FAULT_RESONANCE=EFFECTS.register("fault_resonance",FaultResonanceEffect::new);
    public static final RegistryObject<MobEffect> GRAVITIC_BALANCE=EFFECTS.register("gravitic_balance",GraviticBalanceEffect::new);
    public static final RegistryObject<MobEffect> ICARIAN_MOMENTUM=EFFECTS.register("icarian_momentum",IcarianMomentumEffect::new);
    public static final RegistryObject<MobEffect> DAWN_RESERVE=EFFECTS.register("dawn_reserve",DawnReserveEffect::new);
    public static final RegistryObject<MobEffect> SEAM_FRAY=EFFECTS.register("seam_fray",SeamFrayEffect::new);
    public static final RegistryObject<MobEffect> LODEGUARD=EFFECTS.register("lodeguard",LodeguardEffect::new);
    public static final RegistryObject<MobEffect> STARFALL_WARD=EFFECTS.register("starfall_ward",StarfallWardEffect::new);
    public static final RegistryObject<MobEffect> STARFALL_GUARD=EFFECTS.register("starfall_guard",StarfallGuardEffect::new);
    public static final RegistryObject<MobEffect> CRUCIBLE_MANTLE=EFFECTS.register("crucible_mantle",CrucibleMantleEffect::new);
    public static final RegistryObject<MobEffect> PHASE_SHEAR=EFFECTS.register("phase_shear",PhaseShearEffect::new);
    public static final RegistryObject<MobEffect> WORLDBREAKER_AEGIS=EFFECTS.register("worldbreaker_aegis",WorldbreakerAegisEffect::new);
    public static final RegistryObject<MobEffect> STRATA_FOCUS=EFFECTS.register("strata_focus",StrataFocusEffect::new);
    public static final RegistryObject<MobEffect> STASIS_WARD=EFFECTS.register("stasis_ward",StasisWardEffect::new);
    public static final RegistryObject<MobEffect> STASIS_CRUSH=EFFECTS.register("stasis_crush",StasisCrushEffect::new);
    public static final RegistryObject<MobEffect> STASIS_RESONANCE=EFFECTS.register("stasis_resonance",StasisResonanceEffect::new);
    public static final RegistryObject<MobEffect> DAWN_MENDING=EFFECTS.register("dawn_mending",DawnMendingEffect::new);
    public static final Map<ArtifactKind,RegistryObject<Item>> PICKS=new LinkedHashMap<>();
    static {
        for(var kind:ArtifactKind.playableValues()) PICKS.put(kind,ITEMS.register(kind.id,() -> new ArtifactItem(kind)));
        TABS.register("artifacts",() -> CreativeModeTab.builder().title(Component.translatable("itemGroup.specialpickaxes"))
            .icon(() -> new ItemStack(PICKS.get(ArtifactKind.PALIMPSEST).get()))
            .displayItems((parameters,output) -> PICKS.values().forEach(item -> output.accept(item.get()))).build());
    }
    public SpecialPickaxes() {
        OrefallProgram.warmup();
        io.github.santipdr.specialpickaxes.network.RelicNetwork.register();
        var bus=FMLJavaModLoadingContext.get().getModEventBus();ITEMS.register(bus);TABS.register(bus);EFFECTS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,ArtifactConfig.SPEC);
        var forge=MinecraftForge.EVENT_BUS;
        forge.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,this::protectPhysicalLimit);forge.addListener(this::tick);forge.addListener(this::logout);forge.addListener(this::clonePlayer);
        forge.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,this::trackPlayerPlacedBlocks);
        forge.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,this::schedulePlacedBlockCleanup);
        forge.addListener(this::tagsUpdated);
        forge.addListener(this::curiosMiningFeedback);
        forge.addListener(this::entityJoin);
        forge.addListener(this::login);forge.addListener(this::speed);forge.addListener(this::attack);forge.addListener(this::hurt);forge.addListener(this::heal);
        forge.addListener(this::fall);
        forge.addListener(this::explosion);forge.addListener(this::dimension);forge.addListener(this::leftClick);forge.addListener(this::commands);forge.addListener(this::stopped);forge.addListener(this::missing);
    }
    private void protectPhysicalLimit(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(e.getPlayer() instanceof ServerPlayer p&&(WorldSafety.changedDuringBreakEvent(p,e.getPos())||p.getMainHandItem().getItem() instanceof ArtifactItem&&WorldSafety.barrier(p,e.getPos())))e.setCanceled(true);
    }
    private void trackPlayerPlacedBlocks(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e){
        if(!e.isCanceled()&&e.getEntity() instanceof net.minecraft.world.entity.player.Player
                &&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                &&level.getFluidState(e.getPos()).isEmpty()){
            var state=level.getBlockState(e.getPos());
            if(!state.hasBlockEntity()&&ArtifactTools.effective(state))PlayerPlacedBlocks.get(level).mark(e.getPos());
        }
    }
    private void schedulePlacedBlockCleanup(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(!e.isCanceled()&&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
            PlayerPlacedBlocks.scheduleCleanup(level,e.getPos());
    }
    private void tagsUpdated(net.minecraftforge.event.TagsUpdatedEvent e){ArtifactOres.clearCache();MiningDesigns.clearCache();}
    private void curiosMiningFeedback(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(e.isCanceled()||!(e.getPlayer() instanceof ServerPlayer p))return;
        StrataFocusEffect.miningPulse(p,e.getPos());
        StasisResonanceEffect.miningPulse(p,e.getPos());
        if(!ArtifactOres.isOre(e.getState()))return;
        var equipped=CuriosCompat.find(p);
        if(equipped!=null)MiningObservations.captureCuriosIridium(p,equipped.stack(),e.getPos(),e.getState());
    }
    private void entityJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent e){
        if(e.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity&&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
            WorldSafety.tagIridiumDrop(e.getEntity(),level);
    }
    private void explosion(net.minecraftforge.event.level.ExplosionEvent.Detonate e){CompanionActions.protect(e.getLevel(),e.getAffectedBlocks());}
    private void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){ArtifactState.clearAnchors(p,ArtifactKind.LODESTAR);ArtifactState.of(p,ArtifactKind.LODESTAR).remove("trail");CompanionActions.stop(p);ArtifactInteraction.clear(p);MiningObservations.forget(p);WorkQueue.cancel(p);WorkQueue.forget(p);DomainFields.stop(p);WorldloomSnare.forget(p);}}
    private void leftClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock e){
        if(e.getEntity() instanceof ServerPlayer p && ArtifactInteraction.selecting(p)){
            if(e.getAction()==net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START)RelicControl.corner(p,e.getPos());
            e.setCanceled(true);
        }
    }
    private void tick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            MiningObservations.flush();PlayerPlacedBlocks.tickCleanup();WorkQueue.tick();DomainFields.tick();CompanionActions.tick();WorldloomSnare.tick();
            if(++passiveTickClock>=10){
                passiveTickClock=0;
                for(var level:e.getServer().getAllLevels())for(var player:level.players()){
                    var equipped=CuriosCompat.find(player);
                    ArtifactKind current=equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem pick?pick.kind:null;
                    ArtifactKind previous=curiosPassives.put(player.getUUID(),current);
                    if(previous!=null&&previous!=current)ArtifactPassives.onCuriosUnequip(player,previous);
                    if(current!=null)ArtifactPassives.tick(player,current);
                }
            }
        }
    }
    private void stopped(ServerStoppedEvent e) { passiveTickClock=0;curiosPassives.clear();io.github.santipdr.specialpickaxes.network.RelicNetwork.clear();ArtifactFeedback.clearSounds();CompanionActions.clear();ArtifactInteraction.clear();MiningObservations.clear();PlayerPlacedBlocks.clearPendingCleanup();WorkQueue.clear();DomainFields.clear();WorldloomSnare.clear(); }
    private void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer p) { curiosPassives.remove(p.getUUID());io.github.santipdr.specialpickaxes.network.RelicNetwork.forget(p);ArtifactFeedback.forget(p);CompanionActions.stop(p);ArtifactInteraction.clear(p);MiningObservations.forget(p);WorkQueue.cancel(p);WorkQueue.forget(p);DomainFields.stop(p);WorldloomSnare.forget(p); }
    }
    private void clonePlayer(PlayerEvent.Clone e) {
        var old=e.getOriginal().getPersistentData().getCompound(ArtifactState.ROOT);
        var clean=old.copy();
        for(var kind:ArtifactKind.values()) {
            var state=clean.getCompound(kind.id);state.remove("a");state.remove("b");state.putLong("ready",old.getCompound(kind.id).getLong("ready"));clean.put(kind.id,state);
        }
        e.getEntity().getPersistentData().put(ArtifactState.ROOT,clean);
        if(e.getOriginal() instanceof ServerPlayer p) { CompanionActions.stop(p);ArtifactInteraction.clear(p);MiningObservations.forget(p);WorkQueue.cancel(p);WorkQueue.forget(p);DomainFields.stop(p);WorldloomSnare.forget(p); }
    }
    private void login(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getEntity() instanceof ServerPlayer p) for(var kind:ArtifactKind.playableValues()) {
            ArtifactState.prune(p,kind);
            var state=ArtifactState.of(p,kind);long now=ArtifactState.now(p);
            long ticks=Math.max(state.getLong("ready"),state.getLong("alternateReady"))-now;
            if(ticks>0) p.getCooldowns().addCooldown(PICKS.get(kind).get(),(int)Math.min(ticks,Integer.MAX_VALUE));
        }
    }
    private void speed(PlayerEvent.BreakSpeed e) {
        var p=e.getEntity();
        if(p instanceof ServerPlayer server&&p.getMainHandItem().getItem() instanceof ArtifactItem&&ArtifactInteraction.selecting(server)){
            e.setNewSpeed(0F);return;
        }
        if(p.getMainHandItem().getItem() instanceof ArtifactItem && ArtifactTools.effective(e.getState())) {
            int raw=EnchantmentScaling.level(p.getMainHandItem(),net.minecraft.world.item.enchantment.Enchantments.BLOCK_EFFICIENCY);
            if(raw>46340){double nativeBase=64D+46340D*46340D+1;double environment=Math.max(0,e.getNewSpeed())/nativeBase;e.setNewSpeed(EnchantmentScaling.finiteSpeed((64D+(double)raw*raw+1)*environment));}
        }
        if(p.getMainHandItem().getItem() instanceof ArtifactItem item && item.kind==ArtifactKind.INTERREGNUM
                && p.hasEffect(DOMINION.get()) && p.getMainHandItem().isCorrectToolForDrops(e.getState())
                && e.getNewSpeed()>0 && (!(p instanceof ServerPlayer server) || DomainFields.contains(server,p.blockPosition())))
            e.setNewSpeed(EnchantmentScaling.finiteSpeed((double)e.getNewSpeed()*8));
        var focus=p.getEffect(STRATA_FOCUS.get());
        if(focus!=null&&e.getNewSpeed()>0){
            double multiplier=1D+.2D*(Math.min(1,focus.getAmplifier())+1D);
            e.setNewSpeed(EnchantmentScaling.finiteSpeed((double)e.getNewSpeed()*multiplier));
        }
        if(p.hasEffect(STASIS_RESONANCE.get())&&e.getNewSpeed()>0&&p.getMainHandItem().isCorrectToolForDrops(e.getState()))
            e.setNewSpeed(EnchantmentScaling.finiteSpeed((double)e.getNewSpeed()*1.6D));
    }
    private void attack(LivingAttackEvent e) {
        if(DomainFields.frozen(e.getSource().getDirectEntity())) e.setCanceled(true);
        if(!e.isCanceled()&&(e.getEntity().hasEffect(SEARING_SHELL.get())||e.getEntity().hasEffect(CRUCIBLE_MANTLE.get()))
                &&e.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE))e.setCanceled(true);
    }
    private void hurt(LivingHurtEvent e){
        if(e.getEntity() instanceof ServerPlayer p){
            boolean sharedStarfallGuard=ArtifactPassives.onSharedStarfallGuard(p,e);
            var equipped=CuriosCompat.find(p);
            if(equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem pick)
                ArtifactPassives.onCuriosDamage(p,e,pick.kind,sharedStarfallGuard);
        }
    }
    private void heal(net.minecraftforge.event.entity.living.LivingHealEvent e){if(e.getEntity().hasEffect(NULL_MARK.get()))e.setCanceled(true);}
    private void fall(LivingFallEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        if(WorkQueue.protectsFall(p)){e.setCanceled(true);return;}
        if(p.hasEffect(GRAVITIC_BALANCE.get())){
            e.setDamageMultiplier(e.getDamageMultiplier()*.25F);
            ArtifactFeedback.burst(p,ArtifactKind.EVENTIDE,p.blockPosition(),4);
        }
        if(p.hasEffect(ICARIAN_MOMENTUM.get())){
            e.setDamageMultiplier(e.getDamageMultiplier()*.5F);
            ArtifactFeedback.burst(p,ArtifactKind.ICARUS,p.blockPosition(),4);
        }
    }
    private void commands(RegisterCommandsEvent e) {
        e.getDispatcher().register(Commands.literal("specialpickaxes").requires(source -> source.hasPermission(2))
            .then(Commands.literal("grant").then(Commands.argument("player",EntityArgument.player())
            .then(Commands.argument("artifact",StringArgumentType.word())
                .suggests((context,builder) -> SharedSuggestionProvider.suggest(Arrays.stream(ArtifactKind.playableValues()).map(k -> k.id),builder))
                .executes(context -> {
                    String id=StringArgumentType.getString(context,"artifact");ArtifactKind kind;
                    try { kind=ArtifactKind.byId(id); } catch(IllegalArgumentException invalid) {
                        context.getSource().sendFailure(Component.translatable("message.specialpickaxes.unknown"));return 0;
                    }
                    var player=EntityArgument.getPlayer(context,"player");var stack=new ItemStack(PICKS.get(kind).get());
                    player.getInventory().add(stack);if(!stack.isEmpty()) player.drop(stack,false);
                    context.getSource().sendSuccess(() -> Component.translatable("message.specialpickaxes.granted",id,player.getName()),true);
                    return 1;
                })))))
            ;
    }
    private void missing(MissingMappingsEvent e) {
        for(var mapping:e.getMappings(ForgeRegistries.Keys.MOB_EFFECTS,ID))
            if(mapping.getKey().getPath().equals("overdrive")) mapping.remap(DOMINION.get());
        String[] old={"overdrive","excavator","vein_miner","inferno","magnetic","scanner","storm","void","ender","explosive"};
        ArtifactKind[] kinds={ArtifactKind.ICARUS,ArtifactKind.WORLDLOOM,ArtifactKind.CHOIR,ArtifactKind.CRUCIBLE,
            ArtifactKind.EVENTIDE,ArtifactKind.AXIOM,ArtifactKind.INTERREGNUM,ArtifactKind.PALIMPSEST,ArtifactKind.MERIDIAN,ArtifactKind.ATLAS};
        for(var mapping:e.getMappings(ForgeRegistries.Keys.ITEMS,ID)) for(int i=0;i<old.length;i++)
            if(mapping.getKey().getPath().equals(old[i])) {if(kinds[i].playable())mapping.remap(PICKS.get(kinds[i]).get());else mapping.ignore();}
    }
    private static final class DominionEffect extends MobEffect { private DominionEffect() { super(MobEffectCategory.BENEFICIAL,0x8ae5ff); } }
}
