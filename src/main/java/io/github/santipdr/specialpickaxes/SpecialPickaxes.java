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
    public static final String ID="specialpickaxes";
    public static final Tier TIER=TierSortingRegistry.registerTier(new ForgeTier(4,32768,64F,12F,50,
        net.minecraft.tags.TagKey.create(Registries.BLOCK,new ResourceLocation(ID,"needs_artifact_tool")),() -> Ingredient.EMPTY),new ResourceLocation(ID,"artifact"),List.of(Tiers.NETHERITE),List.of());
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ID);
    public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(ForgeRegistries.MOB_EFFECTS,ID);
    public static final RegistryObject<MobEffect> DOMINION=EFFECTS.register("dominion",DominionEffect::new);
    public static final Map<ArtifactKind,RegistryObject<Item>> PICKS=new LinkedHashMap<>();
    static {
        for(var kind:ArtifactKind.playableValues()) PICKS.put(kind,ITEMS.register(kind.id,() -> new ArtifactItem(kind)));
        TABS.register("artifacts",() -> CreativeModeTab.builder().title(Component.translatable("itemGroup.specialpickaxes"))
            .icon(() -> new ItemStack(PICKS.get(ArtifactKind.PALIMPSEST).get()))
            .displayItems((parameters,output) -> PICKS.values().forEach(item -> output.accept(item.get()))).build());
    }
    public SpecialPickaxes() {
        io.github.santipdr.specialpickaxes.network.RelicNetwork.register();
        var bus=FMLJavaModLoadingContext.get().getModEventBus();ITEMS.register(bus);TABS.register(bus);EFFECTS.register(bus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER,ArtifactConfig.SPEC);
        var forge=MinecraftForge.EVENT_BUS;
        forge.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,this::protectPhysicalLimit);forge.addListener(this::tick);forge.addListener(this::logout);forge.addListener(this::clonePlayer);
        forge.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,this::trackPlayerPlacedBlocks);
        forge.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,this::schedulePlacedBlockCleanup);
        forge.addListener(this::curiosIridiumMine);
        forge.addListener(this::login);forge.addListener(this::speed);forge.addListener(this::attack);forge.addListener(this::hurt);
        forge.addListener(this::fall);
        forge.addListener(this::explosion);forge.addListener(this::dimension);forge.addListener(this::leftClick);forge.addListener(this::commands);forge.addListener(this::stopped);forge.addListener(this::missing);
    }
    private void protectPhysicalLimit(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(e.getPlayer() instanceof ServerPlayer p&&(WorldSafety.changedDuringBreakEvent(p,e.getPos())||p.getMainHandItem().getItem() instanceof ArtifactItem&&WorldSafety.barrier(p,e.getPos())))e.setCanceled(true);
    }
    private void trackPlayerPlacedBlocks(net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent e){
        if(!e.isCanceled()&&e.getEntity() instanceof net.minecraft.world.entity.player.Player
                &&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                &&(MiningDesigns.crucibleGeology(level.getBlockState(e.getPos()))||ArtifactOres.isOre(level.getBlockState(e.getPos()))))
            PlayerPlacedBlocks.get(level).mark(e.getPos());
    }
    private void schedulePlacedBlockCleanup(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(!e.isCanceled()&&e.getLevel() instanceof net.minecraft.server.level.ServerLevel level)
            PlayerPlacedBlocks.scheduleCleanup(level,e.getPos());
    }
    private void curiosIridiumMine(net.minecraftforge.event.level.BlockEvent.BreakEvent e){
        if(e.isCanceled()||WorkQueue.running()||!(e.getPlayer() instanceof ServerPlayer p)||!ArtifactOres.isOre(e.getState()))return;
        var equipped=CuriosCompat.find(p);
        if(equipped==null||!(equipped.stack().getItem() instanceof ArtifactItem pick)||pick.kind!=ArtifactKind.IRIDIUM)return;
        if(p.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.IRIDIUM)return;
        MiningObservations.capture(p,p.getMainHandItem(),ArtifactKind.IRIDIUM,e.getPos(),e.getState());
    }
    private void explosion(net.minecraftforge.event.level.ExplosionEvent.Detonate e){CompanionActions.protect(e.getLevel(),e.getAffectedBlocks());}
    private void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p){ArtifactState.clearAnchors(p,ArtifactKind.LODESTAR);ArtifactState.of(p,ArtifactKind.LODESTAR).remove("trail");CompanionActions.stop(p);ArtifactInteraction.clear(p);MiningObservations.forget(p);WorkQueue.cancel(p);DomainFields.stop(p);WorldloomSnare.forget(p);}}
    private void leftClick(net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock e){
        if(e.getEntity() instanceof ServerPlayer p && ArtifactInteraction.selecting(p)){
            if(e.getAction()==net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.START)RelicControl.corner(p,e.getPos());
            e.setCanceled(true);
        }
    }
    private void tick(TickEvent.ServerTickEvent e) {
        if(e.phase==TickEvent.Phase.END) {
            MiningObservations.flush();PlayerPlacedBlocks.tickCleanup();WorkQueue.tick();DomainFields.tick();CompanionActions.tick();WorldloomSnare.tick();
            for(var level:e.getServer().getAllLevels())for(var player:level.players())if(player.tickCount%10==0){
                var equipped=CuriosCompat.find(player);
                if(equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem pick)ArtifactPassives.tick(player,pick.kind);
            }
        }
    }
    private void stopped(ServerStoppedEvent e) { io.github.santipdr.specialpickaxes.network.RelicNetwork.clear(); CompanionActions.clear();ArtifactInteraction.clear();MiningObservations.clear();PlayerPlacedBlocks.clearPendingCleanup();WorkQueue.clear();DomainFields.clear();WorldloomSnare.clear(); }
    private void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        if(e.getEntity() instanceof ServerPlayer p) { io.github.santipdr.specialpickaxes.network.RelicNetwork.forget(p);CompanionActions.stop(p);ArtifactInteraction.clear(p);MiningObservations.forget(p);WorkQueue.cancel(p);DomainFields.stop(p);WorldloomSnare.forget(p); }
    }
    private void clonePlayer(PlayerEvent.Clone e) {
        var old=e.getOriginal().getPersistentData().getCompound(ArtifactState.ROOT);
        var clean=old.copy();
        for(var kind:ArtifactKind.values()) {
            var state=clean.getCompound(kind.id);state.remove("a");state.remove("b");state.putLong("ready",old.getCompound(kind.id).getLong("ready"));clean.put(kind.id,state);
        }
        e.getEntity().getPersistentData().put(ArtifactState.ROOT,clean);
        if(e.getOriginal() instanceof ServerPlayer p) { CompanionActions.stop(p);ArtifactInteraction.clear(p);MiningObservations.forget(p);WorkQueue.cancel(p);DomainFields.stop(p);WorldloomSnare.forget(p); }
    }
    private void login(PlayerEvent.PlayerLoggedInEvent e) {
        if(e.getEntity() instanceof ServerPlayer p) for(var kind:ArtifactKind.playableValues()) {
            ArtifactState.prune(p,kind);
            long ticks=ArtifactState.of(p,kind).getLong("ready")-ArtifactState.now(p);
            if(ticks>0) p.getCooldowns().addCooldown(PICKS.get(kind).get(),(int)Math.min(ticks,12000));
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
    }
    private void attack(LivingAttackEvent e) {
        if(DomainFields.frozen(e.getSource().getDirectEntity())) e.setCanceled(true);
    }
    private void hurt(LivingHurtEvent e){
        if(e.getEntity() instanceof ServerPlayer p){
            var equipped=CuriosCompat.find(p);
            if(equipped!=null&&equipped.stack().getItem() instanceof ArtifactItem pick)ArtifactPassives.onCuriosDamage(p,e,pick.kind);
        }
    }
    private void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&WorkQueue.protectsFall(p))e.setCanceled(true);}
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
