package io.github.santipdr.specialpickaxes.test;

import com.mojang.authlib.GameProfile;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.*;
import java.util.function.Consumer;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class PickaxeGameTests {
    private static ServerPlayer player(GameTestHelper h,ArtifactKind kind) {
        return player(h,kind,"ArtifactTest");
    }
    private static ServerPlayer player(GameTestHelper h,ArtifactKind kind,String name) {
        var p=new FakePlayer(h.getLevel(),new GameProfile(UUID.randomUUID(),name));
        p.connection=new ServerGamePacketListenerImpl(h.getLevel().getServer(),new Connection(PacketFlow.SERVERBOUND),p);
        p.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var at=h.absolutePos(new BlockPos(5,2,3));p.setPos(at.getX()+0.5,at.getY(),at.getZ()+0.5);
        if(kind==ArtifactKind.WORLDBREAKER)ArtifactState.of(p,kind).putInt("mode",0);
        p.setYRot(0);p.setXRot(0);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(kind).get()));return p;
    }
    private static BlockPos target(GameTestHelper h) {
        var pos=h.absolutePos(new BlockPos(5,3,7));h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());return pos;
    }
    private static void finish(GameTestHelper h,ServerPlayer p) { CompanionActions.stop(p);ArtifactInteraction.clear(p);WorkQueue.cancel(p);DomainFields.stop(p);h.succeed(); }
    private static void drain(ServerPlayer p) { for(int i=0;i<400 && WorkQueue.busy(p);i++) WorkQueue.tick(); }
    @GameTest(template="empty",batch="legacy_each_01") public static void registryNoRecipesAndAdmin(GameTestHelper h) {
        h.assertTrue(SpecialPickaxes.PICKS.size()==13,"thirteen playable pickaxes");var p=player(h,ArtifactKind.PALIMPSEST);p.getInventory().clearContent();
        var source=h.getLevel().getServer().createCommandSourceStack().withEntity(p).withPermission(2);
        for(var kind:ArtifactKind.playableValues()) {
            var item=SpecialPickaxes.PICKS.get(kind).get();h.assertTrue(item instanceof ArtifactItem,"artifact item class");
            var tool=new ItemStack(item);h.assertTrue(tool.is(net.minecraft.tags.ItemTags.PICKAXES),"recognized as a pickaxe: "+kind);
            h.assertTrue(tool.is(net.minecraft.tags.ItemTags.AXES)&&tool.is(net.minecraft.tags.ItemTags.SHOVELS),"multi-tool tags stay complete: "+kind);
            h.assertTrue(h.getLevel().getRecipeManager().getRecipes().stream().noneMatch(r -> r.getResultItem(h.getLevel().registryAccess()).is(item)),"no recipe outputs artifact");
            int result=h.getLevel().getServer().getCommands().performPrefixedCommand(source,"specialpickaxes grant @s "+kind.id);
            h.assertTrue(result==1 && p.getInventory().countItem(item)==1,"admin command actually grants "+kind.id);
        }
        boolean denied=!h.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("specialpickaxes").canUse(source.withPermission(0));
        h.assertTrue(denied && p.getInventory().countItem(SpecialPickaxes.PICKS.get(ArtifactKind.PALIMPSEST).get())==1,"non-admin cannot grant");
        finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_02") public static void activeControlsRequireHoldingTheMatchingPickaxe(GameTestHelper h) {
        for(var kind:ArtifactKind.playableValues()){
            var p=player(h,kind);var relic=p.getMainHandItem();int damage=relic.getDamageValue();
            p.getInventory().add(relic.copy());p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            for(var action:List.of(RelicControl.Action.ACTIVATE,RelicControl.Action.MODE,RelicControl.Action.ALT_SKILL))
                h.assertTrue(!RelicControl.execute(p,kind,action),"Curios/inventory relic cannot authorize active control: "+kind+" "+action);
            h.assertTrue(!WorkQueue.busy(p)&&!DomainFields.active(p)&&relic.getDamageValue()==damage,"denied controls start no work and spend no durability: "+kind);
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_03") public static void placedBlockProvenanceIsPrunedSafely(GameTestHelper h) {
        var removed=h.absolutePos(new BlockPos(5,3,7));h.getLevel().setBlockAndUpdate(removed,Blocks.STONE.defaultBlockState());
        var data=PlayerPlacedBlocks.get(h.getLevel());data.mark(removed);PlayerPlacedBlocks.scheduleCleanup(h.getLevel(),removed);
        h.getLevel().setBlockAndUpdate(removed,Blocks.AIR.defaultBlockState());PlayerPlacedBlocks.tickCleanup();
        h.assertTrue(!data.contains(removed),"removed placed-block provenance is released");
        var preserved=removed.east();h.getLevel().setBlockAndUpdate(preserved,Blocks.GRANITE.defaultBlockState());
        data.mark(preserved);PlayerPlacedBlocks.scheduleCleanup(h.getLevel(),preserved);PlayerPlacedBlocks.tickCleanup();
        h.assertTrue(data.contains(preserved),"unchanged/cancelled block keeps its protection");finish(h,player(h,ArtifactKind.PALIMPSEST));
    }
    @GameTest(template="empty",batch="legacy_each_04") public static void palimpsestMinesOnlyItsConnectedOreVein(GameTestHelper h) {
        var p=player(h,ArtifactKind.PALIMPSEST);var pos=target(h);
        for(var at:List.of(pos,pos.east(),pos.above()))h.getLevel().setBlockAndUpdate(at,Blocks.DIAMOND_ORE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos.east(2),Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
        var adjacentGold=pos.east(3);h.getLevel().setBlockAndUpdate(adjacentGold,Blocks.GOLD_ORE.defaultBlockState());
        var stone=pos.south();h.getLevel().setBlockAndUpdate(stone,Blocks.STONE.defaultBlockState());
        var distant=pos.east(8);h.getLevel().setBlockAndUpdate(distant,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(!ArtifactOres.veinFamilies(Blocks.DIAMOND_ORE.defaultBlockState()).stream().anyMatch(Blocks.GOLD_ORE.defaultBlockState()::is),"shared rate tags do not merge separate ore veins");
        h.assertTrue(!java.util.Objects.equals(ArtifactOres.registryOreFamily(Blocks.DIAMOND_ORE),ArtifactOres.registryOreFamily(Blocks.GOLD_ORE)),"registry ore families stay distinct");
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.PALIMPSEST,false),"vein echo activates");drain(p);
        for(var at:List.of(pos,pos.east(),pos.above(),pos.east(2)))h.assertTrue(h.getLevel().getBlockState(at).isAir(),"connected ore-family vein mined");
        h.assertTrue(h.getLevel().getBlockState(stone).is(Blocks.STONE),"stone is not part of the vein");
        h.assertTrue(h.getLevel().getBlockState(adjacentGold).is(Blocks.GOLD_ORE),"adjacent different ore family is preserved");
        h.assertTrue(h.getLevel().getBlockState(distant).is(Blocks.DIAMOND_ORE),"disconnected ore is preserved");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_05") public static void choirReplayMatchesState(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);p.setYRot(0);var origin=p.blockPosition().above().relative(Direction.SOUTH);
        for(int depth=0;depth<20;depth++)for(var pos:DirectionalProgram.section(origin,Direction.SOUTH,DirectionalProgram.Shape.RESONANT_TUNNEL,depth))h.getLevel().setBlockAndUpdate(pos,Blocks.STONE.defaultBlockState());
        var behind=p.blockPosition().relative(Direction.NORTH,2);h.getLevel().setBlockAndUpdate(behind,Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CHOIR,false),"forward resonance tunnel starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(origin).isAir()&&h.getLevel().getBlockState(origin.relative(Direction.SOUTH,19)).isAir(),"2x2 tunnel advances twenty blocks forward");
        h.assertTrue(h.getLevel().getBlockState(behind).is(Blocks.STONE),"tunnel leaves blocks behind untouched");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void eventideMiningNotCombat(GameTestHelper h) {
        var p=player(h,ArtifactKind.EVENTIDE);var center=target(h);
        h.getLevel().setBlockAndUpdate(center.above(),Blocks.DIAMOND_ORE.defaultBlockState());
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(center.getX()+2,center.getY(),center.getZ());h.getLevel().addFreshEntity(mob);
        mob.setDeltaMovement(Vec3.ZERO);float health=mob.getHealth();
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,false),"gravity field activates");DomainFields.tick();
        h.assertTrue(mob.getDeltaMovement().equals(Vec3.ZERO),"mining does not become entity combat");
        h.assertTrue(mob.getHealth()==health,"not a damage explosion");
        h.assertTrue(DomainFields.contains(p,center)&&p.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED),"field grants its mining effect");
        h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.STONE)&&h.getLevel().getBlockState(center.above()).is(Blocks.DIAMOND_ORE),"activation does not perform a large quarry");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void eventideCuriosCounterUsesGraviticTether(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.EVENTIDE);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.EVENTIDE);
        h.assertTrue(hit.getAmount()==9F,"gravity debt still trims the triggering hit by ten percent");
        h.assertTrue(attacker.hasEffect(SpecialPickaxes.GRAVITIC_TETHER.get()),"Curios retaliation applies its named snare");
        var tether=attacker.getEffect(SpecialPickaxes.GRAVITIC_TETHER.get());
        h.assertTrue(tether.getDuration()==70,"snare lasts three and a half seconds");
        attacker.setDeltaMovement(1,.25,0);SpecialPickaxes.GRAVITIC_TETHER.get().applyEffectTick(attacker,0);
        h.assertTrue(attacker.getDeltaMovement().x<.7&&attacker.getDeltaMovement().y==.25,"snare damps horizontal escape while preserving vertical movement");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_26") public static void eventideCuriosGraviticBalanceControlsFalls(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);wearer.setOnGround(false);
        ArtifactPassives.tick(wearer,ArtifactKind.EVENTIDE);
        var balance=SpecialPickaxes.GRAVITIC_BALANCE.get();
        h.assertTrue(wearer.hasEffect(balance)&&!wearer.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING),"Curios grants Eventide's named descent effect instead of generic Slow Falling");
        wearer.setDeltaMovement(0,-1,0);balance.applyEffectTick(wearer,0);
        h.assertTrue(Math.abs(wearer.getDeltaMovement().y+.24)<.001,"the passive caps a fast fall at a controlled descent speed");
        var fall=new net.minecraftforge.event.entity.living.LivingFallEvent(wearer,12F,1F);MinecraftForge.EVENT_BUS.post(fall);
        h.assertTrue(Math.abs(fall.getDamageMultiplier()-.25F)<.001,"Eventide reduces the landing damage multiplier by 75 percent");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.EVENTIDE);
        h.assertTrue(!wearer.hasEffect(balance),"the custom Curios passive clears when Eventide is unequipped");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_27") public static void icarusCuriosGlideBuildsSprintMomentumInAir(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);wearer.setOnGround(false);wearer.setSprinting(true);
        ArtifactPassives.tick(wearer,ArtifactKind.ICARUS);
        var glide=SpecialPickaxes.ICARIAN_MOMENTUM.get();
        h.assertTrue(wearer.hasEffect(glide)&&wearer.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING),"airborne Icarus Curios grants its glide effect and fall protection");
        wearer.setDeltaMovement(0,-.2,0);glide.applyEffectTick(wearer,0);var boosted=wearer.getDeltaMovement();
        h.assertTrue(boosted.horizontalDistanceSqr()>.0025&&Math.abs(boosted.y+.2)<.001,"sprint-gliding gains forward momentum without altering vertical control");
        wearer.setSprinting(false);wearer.setDeltaMovement(.1,-.2,0);glide.applyEffectTick(wearer,0);
        h.assertTrue(Math.abs(wearer.getDeltaMovement().x-.1)<.001&&Math.abs(wearer.getDeltaMovement().z)<.001,"the glide stops accelerating when sprint is released");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.ICARUS);
        h.assertTrue(!wearer.hasEffect(glide),"Icarian Momentum is removed when the Curios passive ends");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_28") public static void palimpsestCuriosCounterRestoresFoodInDawnPulses(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);wearer.getFoodData().setFoodLevel(10);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.PALIMPSEST);
        var reserve=SpecialPickaxes.DAWN_RESERVE.get();
        h.assertTrue(Math.abs(hit.getAmount()-9F)<.001F&&wearer.hasEffect(reserve),"Palimpsest still trims the hit and applies its named food reserve");
        h.assertTrue(wearer.getFoodData().getFoodLevel()==10&&wearer.getEffect(reserve).getDuration()==80,"food restoration is delayed into the four-second effect");
        h.assertTrue(reserve.isDurationEffectTick(80,0)&&!reserve.isDurationEffectTick(79,0),"the reserve pulses once per second");
        for(int i=0;i<4;i++)reserve.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getFoodData().getFoodLevel()==14,"four visible pulses restore four food points");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_29") public static void interregnumCuriosSharesAbsorptionOnlyWithNearbyAllies(GameTestHelper h){
        var wearer=player(h,ArtifactKind.INTERREGNUM);var ally=player(h,ArtifactKind.CHOIR,"StasisAlly");
        var armoredAlly=player(h,ArtifactKind.ICARUS,"StasisGuard");var enemy=player(h,ArtifactKind.WORLDLOOM,"StasisEnemy");
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("ward_"+wearer.getUUID().toString().substring(0,6));
        var hostile=scoreboard.addPlayerTeam("foe_"+enemy.getUUID().toString().substring(0,6));
        for(var teammate:List.of(wearer,ally,armoredAlly))scoreboard.addPlayerToTeam(teammate.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(enemy.getScoreboardName(),hostile);
        ally.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());armoredAlly.setPos(wearer.getX()+3,wearer.getY(),wearer.getZ());enemy.setPos(wearer.getX()+1,wearer.getY(),wearer.getZ());
        h.getLevel().addFreshEntity(ally);h.getLevel().addFreshEntity(armoredAlly);h.getLevel().addFreshEntity(enemy);
        armoredAlly.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,100,1,true,false,true));
        ArtifactPassives.tick(wearer,ArtifactKind.INTERREGNUM);
        var stasis=SpecialPickaxes.STASIS_WARD.get();
        h.assertTrue(wearer.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),"the Curios wearer keeps its own Stasis ward");
        h.assertTrue(wearer.hasEffect(stasis)&&ally.hasEffect(stasis)&&armoredAlly.hasEffect(stasis),"the custom visual ward reaches the wearer and nearby allies");
        h.assertTrue(Math.abs(ally.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)-.15D)<.001D,"Stasis Ward anchors allies against knockback");
        h.assertTrue(ally.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION)&&ally.getEffect(net.minecraft.world.effect.MobEffects.ABSORPTION).getAmplifier()==0,"nearby allied players receive Absorption I");
        h.assertTrue(armoredAlly.getEffect(net.minecraft.world.effect.MobEffects.ABSORPTION).getAmplifier()==1,"the aura does not downgrade a stronger allied effect");
        h.assertTrue(!enemy.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),"hostile players never receive the shared ward");
        h.assertTrue(!enemy.hasEffect(stasis),"hostile players never receive the custom ward");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.INTERREGNUM);
        h.assertTrue(!wearer.hasEffect(stasis),"the wearer loses the custom Curios ward when Stasis is unequipped");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_31") public static void seamRipperCuriosFraysHostileArmor(GameTestHelper h){
        var wearer=player(h,ArtifactKind.SEAM_RIPPER);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());
        attacker.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));h.getLevel().addFreshEntity(attacker);
        double armorBefore=attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.SEAM_RIPPER);
        var fray=SpecialPickaxes.SEAM_FRAY.get();
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F,"the Curios guard still trims the first hostile hit by 15 percent");
        h.assertTrue(attacker.hasEffect(fray)&&attacker.getEffect(fray).getDuration()==60,"the passive applies its named three-second custom effect");
        double armorAfter=attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR);
        h.assertTrue(armorBefore>0&&Math.abs(armorAfter/armorBefore-.75D)<.02D,"Seam Fray reduces armor by 25 percent for the effect duration");
        h.assertTrue(fray.isDurationEffectTick(60,0)&&!fray.isDurationEffectTick(59,0),"the scar particles pulse every six ticks");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_32") public static void worldloomSharesHeartwoodOnlyWithNearbyAllies(GameTestHelper h){
        var wearer=player(h,ArtifactKind.WORLDLOOM,"WorldloomBearer");
        var ally=player(h,ArtifactKind.CHOIR,"RootedAlly");var farAlly=player(h,ArtifactKind.ICARUS,"DistantAlly");
        var enemy=player(h,ArtifactKind.SEAM_RIPPER,"RootedEnemy");
        var scoreboard=h.getLevel().getScoreboard();var allies=scoreboard.addPlayerTeam("roots_"+wearer.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("foes_"+enemy.getUUID().toString().substring(0,6));
        for(var teammate:List.of(wearer,ally,farAlly))scoreboard.addPlayerToTeam(teammate.getScoreboardName(),allies);
        scoreboard.addPlayerToTeam(enemy.getScoreboardName(),foes);
        ally.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());
        farAlly.setPos(wearer.getX()+7,wearer.getY(),wearer.getZ());enemy.setPos(wearer.getX()+1,wearer.getY(),wearer.getZ());
        for(var other:List.of(ally,farAlly,enemy))h.getLevel().addFreshEntity(other);
        ally.setHealth(ally.getHealth()-4F);
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        h.assertTrue(wearer.hasEffect(renewal)&&!wearer.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),"the Curios wearer receives Heartwood Renewal instead of generic Regeneration");
        h.assertTrue(ally.hasEffect(renewal)&&ally.getEffect(renewal).getDuration()==40,"nearby allied miners receive the short custom renewal lease");
        h.assertTrue(!farAlly.hasEffect(renewal)&&!enemy.hasEffect(renewal),"the effect does not reach distant allies or enemies");
        float before=ally.getHealth();renewal.applyEffectTick(ally,0);
        h.assertTrue(Math.abs(ally.getHealth()-before-1F)<.001F,"the shared effect heals one health point per pulse");
        renewal.applyEffectTick(ally,0);h.assertTrue(Math.abs(ally.getHealth()-before-1F)<.001F,"refreshing the shared lease cannot accelerate its one-second heal");
        h.assertTrue(renewal.isDurationEffectTick(40,0)&&!renewal.isDurationEffectTick(39,0),"root healing remains spaced one second apart");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_23") public static void seamRipperMinesAnEasySameMaterialVein(GameTestHelper h) {
        var p=player(h,ArtifactKind.SEAM_RIPPER);var seed=target(h);var level=h.getLevel();
        var sameA=seed.east();var sameB=sameA.east();var mixed=seed.west();var ore=seed.below();var placed=seed.above();
        for(var pos:List.of(seed,sameA,sameB,mixed,ore,placed))PlayerPlacedBlocks.get(level).unmark(pos);
        level.setBlockAndUpdate(sameA,Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(sameB,Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(mixed,Blocks.GRANITE.defaultBlockState());level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        level.setBlockAndUpdate(placed,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).mark(placed);
        p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(seed));
        h.assertTrue(ArtifactInteraction.modeCount(ArtifactKind.SEAM_RIPPER)==1,"Seam R is a single, clear ability without obsolete A/B modes");
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER,false),"R starts a same-material vein with an empty offhand");
        for(int tick=0;tick<2000&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(level.getBlockState(seed).isAir()&&level.getBlockState(sameA).isAir()&&level.getBlockState(sameB).isAir(),"all connected natural stone is mined");
        h.assertTrue(level.getBlockState(mixed).is(Blocks.GRANITE)&&level.getBlockState(ore).is(Blocks.DIAMOND_ORE),"different geology and ores are preserved");
        h.assertTrue(level.getBlockState(placed).is(Blocks.STONE),"player-placed geology is preserved");
        PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_24") public static void seamRipperAlternateMinesTheVisibleFiveByFiveFace(GameTestHelper h) {
        var p=player(h,ArtifactKind.SEAM_RIPPER);var center=target(h);var level=h.getLevel();
        var east=center.east();var above=center.above();var ore=center.west();var placed=center.below();var outside=center.south();
        for(var pos:List.of(center,east,above,ore,placed,outside))PlayerPlacedBlocks.get(level).unmark(pos);
        level.setBlockAndUpdate(east,Blocks.TUFF.defaultBlockState());level.setBlockAndUpdate(above,Blocks.DIORITE.defaultBlockState());
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());level.setBlockAndUpdate(placed,Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(outside,Blocks.ANDESITE.defaultBlockState());PlayerPlacedBlocks.get(level).mark(placed);
        p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(center));
        h.assertTrue(ArtifactActions.useAlternate(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER),"X mines a visible geology face without offhand materials");
        for(int tick=0;tick<2000&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(level.getBlockState(center).isAir()&&level.getBlockState(east).isAir()&&level.getBlockState(above).isAir(),"natural geology on the 5x5 target face is mined");
        h.assertTrue(level.getBlockState(ore).is(Blocks.DIAMOND_ORE)&&level.getBlockState(placed).is(Blocks.STONE)&&level.getBlockState(outside).is(Blocks.ANDESITE),"ores, placed blocks and blocks beyond the face are preserved");
        PlayerPlacedBlocks.get(level).unmark(placed);finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_25") public static void choirCuriosCounterAppliesCustomFaultResonance(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),8F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.CHOIR);
        var resonance=SpecialPickaxes.FAULT_RESONANCE.get();
        h.assertTrue(attacker.hasEffect(resonance)&&attacker.getEffect(resonance).getDuration()==60,"Choir's Curios counter applies its custom three-second status");
        h.assertTrue(!attacker.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"the passive does not fall back to generic Slowness");
        attacker.tickCount=8;attacker.setDeltaMovement(1,.4,.5);resonance.applyEffectTick(attacker,0);
        var damped=attacker.getDeltaMovement();
        h.assertTrue(Math.abs(damped.x-.62)<.001&&Math.abs(damped.z-.31)<.001&&Math.abs(damped.y-.4)<.001,"Fault Resonance damps horizontal momentum while preserving vertical movement");
        h.assertTrue(resonance.isDurationEffectTick(60,0)&&!resonance.isDurationEffectTick(59,0),"resonance pulses every four ticks");
        var nearby=EntityType.ZOMBIE.create(h.getLevel());nearby.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());nearby.setOnGround(true);h.getLevel().addFreshEntity(nearby);
        h.assertTrue(ArtifactActions.useAlternate(wearer,wearer.getMainHandItem(),ArtifactKind.CHOIR),"Choir's held alternate remains usable");
        h.assertTrue(nearby.hasEffect(resonance)&&nearby.getEffect(resonance).getAmplifier()==1,"the active Fault Echo applies the stronger resonance pulse");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_22") public static void eventideMiningChargeCollapsesIntoDropPull(GameTestHelper h) {
        var p=player(h,ArtifactKind.EVENTIDE);var center=target(h);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.EVENTIDE,false),"gravity field activates");
        var drop=new ItemEntity(h.getLevel(),center.getX()+.5,center.getY()+.3,center.getZ()+.5,new ItemStack(Items.DIAMOND,3));
        drop.setDeltaMovement(Vec3.ZERO);h.getLevel().addFreshEntity(drop);var stone=Blocks.STONE.defaultBlockState();
        for(int i=0;i<23;i++)DomainFields.feed(p,p.getMainHandItem(),center,stone);
        h.assertTrue(drop.getDeltaMovement().lengthSqr()<1.0E-8,"the field waits until its full mining charge");
        DomainFields.feed(p,p.getMainHandItem(),center,stone);
        h.assertTrue(drop.getDeltaMovement().lengthSqr()>.1,"the 24th block triggers a gravity collapse that pulls loose drops");
        drop.setDeltaMovement(Vec3.ZERO);p.tickCount=5;DomainFields.tick();
        h.assertTrue(drop.getDeltaMovement().lengthSqr()>.1,"the collapse keeps guiding drops during its three-second pulse");
        h.assertTrue(DomainFields.contains(p,center),"the collapse preserves the active mining field");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void worldloomCuriosCounterPulsesHeartwoodRenewal(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.WORLDLOOM);wearer.setHealth(10F);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.WORLDLOOM);
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        h.assertTrue(wearer.hasEffect(renewal),"Worldloom's Curios counter grants its custom restorative effect");
        h.assertTrue(wearer.getEffect(renewal).getDuration()==60,"heartwood renewal lasts three seconds");
        h.assertTrue(renewal.isDurationEffectTick(60,0)&&!renewal.isDurationEffectTick(59,0),"healing is delivered in deliberate one-second pulses");
        renewal.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==11F,"a renewal pulse restores one health point");
        renewal.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==11F,"repeated effect refreshes cannot accelerate healing");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void hellspecSearingShellBlocksFireAndLava(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.HELLSPEC);ArtifactPassives.tick(wearer,ArtifactKind.HELLSPEC);
        var shell=SpecialPickaxes.SEARING_SHELL.get();
        h.assertTrue(wearer.hasEffect(shell)&&wearer.getEffect(shell).getDuration()==40,"Hellspec grants a two-second fire passive lease while held or equipped in Curios");
        h.assertTrue(wearer.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION).getDuration()==40,"night vision also uses the short passive lease");
        wearer.setSecondsOnFire(10);shell.applyEffectTick(wearer,0);
        h.assertTrue(!wearer.isOnFire(),"the shell clears active burning on its pulse");
        var fire=new net.minecraftforge.event.entity.living.LivingAttackEvent(wearer,h.getLevel().damageSources().inFire(),10F);
        var lava=new net.minecraftforge.event.entity.living.LivingAttackEvent(wearer,h.getLevel().damageSources().lava(),10F);
        var melee=new net.minecraftforge.event.entity.living.LivingAttackEvent(wearer,h.getLevel().damageSources().playerAttack(wearer),10F);
        h.assertTrue(MinecraftForge.EVENT_BUS.post(fire),"the shell cancels tagged fire damage");
        h.assertTrue(MinecraftForge.EVENT_BUS.post(lava),"the shell cancels lava damage");
        h.assertTrue(!MinecraftForge.EVENT_BUS.post(melee),"the shell does not cancel ordinary melee damage");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void hellspecCuriosShellEndsWhenUnequippedButSurvivesInHand(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.ICARUS);var shell=SpecialPickaxes.SEARING_SHELL.get();
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(shell,80,0,true,false,true));
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.HELLSPEC);
        h.assertTrue(!wearer.hasEffect(shell),"Curios-only fire immunity ends when Hellspec leaves its slot");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.HELLSPEC).get()));
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(shell,80,0,true,false,true));
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.HELLSPEC);
        h.assertTrue(wearer.hasEffect(shell),"held Hellspec keeps its own fire passive");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_36") public static void crucibleCuriosMantleIsCustomThermalWard(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CRUCIBLE);wearer.setSecondsOnFire(10);ArtifactPassives.tick(wearer,ArtifactKind.CRUCIBLE);
        var mantle=SpecialPickaxes.CRUCIBLE_MANTLE.get();
        h.assertTrue(wearer.hasEffect(mantle)&&wearer.getEffect(mantle).getDuration()==40,"Curios applies the named two-second Crucible Mantle lease in heat");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE),"Crucible uses its custom passive instead of a generic fire-resistance potion");
        mantle.applyEffectTick(wearer,0);h.assertTrue(!wearer.isOnFire(),"a mantle pulse clears active burning");
        var fire=new net.minecraftforge.event.entity.living.LivingAttackEvent(wearer,h.getLevel().damageSources().inFire(),10F);
        var lava=new net.minecraftforge.event.entity.living.LivingAttackEvent(wearer,h.getLevel().damageSources().lava(),10F);
        var melee=new net.minecraftforge.event.entity.living.LivingAttackEvent(wearer,h.getLevel().damageSources().playerAttack(wearer),10F);
        h.assertTrue(MinecraftForge.EVENT_BUS.post(fire)&&MinecraftForge.EVENT_BUS.post(lava),"the custom mantle cancels fire and lava damage");
        h.assertTrue(!MinecraftForge.EVENT_BUS.post(melee),"the thermal ward leaves melee damage unchanged");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.CRUCIBLE);
        h.assertTrue(!wearer.hasEffect(mantle),"the custom Curios lease clears immediately when the pick leaves its slot");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_37") public static void worldbreakerCuriosAegisAddsCustomArmorAndAnchor(GameTestHelper h){
        var wearer=player(h,ArtifactKind.WORLDBREAKER);ArtifactPassives.tick(wearer,ArtifactKind.WORLDBREAKER);
        var aegis=SpecialPickaxes.WORLDBREAKER_AEGIS.get();
        h.assertTrue(wearer.hasEffect(aegis)&&wearer.getEffect(aegis).getDuration()==40,"Curios grants the named Worldbreaker Aegis lease");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE),"the custom aegis replaces generic Resistance");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"the Curios passive grants no Strength");
        h.assertTrue(Math.abs(wearer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)-4D)<.01D,"the aegis adds four armor points");
        h.assertTrue(Math.abs(wearer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)-.3D)<.001D,"the aegis anchors the wearer against knockback");
        h.assertTrue(aegis.isDurationEffectTick(40,0)&&!aegis.isDurationEffectTick(39,0),"the slate orbit pulses once every ten ticks");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.WORLDBREAKER);
        h.assertTrue(!wearer.hasEffect(aegis),"the Curios ward clears when Worldbreaker is unequipped");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_38") public static void curiosStrataFocusReplacesHasteAndScalesMiningSpeed(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        ArtifactPassives.tick(wearer,ArtifactKind.PALIMPSEST);var focus=SpecialPickaxes.STRATA_FOCUS.get();
        h.assertTrue(wearer.hasEffect(focus)&&wearer.getEffect(focus).getAmplifier()==1,"Palimpsest grants the level-two custom mining passive");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED),"Strata Focus replaces generic Haste");
        var speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(wearer,Blocks.STONE.defaultBlockState(),64,target(h));
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(Math.abs(speed.getNewSpeed()-89.6F)<.01F,"level two applies the same 1.4x mining-speed factor as Haste II");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.PALIMPSEST);
        h.assertTrue(!wearer.hasEffect(focus),"Strata Focus clears when its last source is unequipped");
        ArtifactPassives.tick(wearer,ArtifactKind.CHOIR);
        h.assertTrue(wearer.hasEffect(focus)&&wearer.getEffect(focus).getAmplifier()==0,"Choir grants the level-one custom mining passive");
        speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(wearer,Blocks.STONE.defaultBlockState(),64,target(h));
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(Math.abs(speed.getNewSpeed()-76.8F)<.01F,"level one applies the 1.2x mining-speed factor");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"the passive never grants Strength");
        h.assertTrue(focus.isDurationEffectTick(40,0)&&!focus.isDurationEffectTick(39,0),"the custom aura animates on a ten-tick pulse");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.CHOIR);h.assertTrue(!wearer.hasEffect(focus),"the custom effect does not linger after Curios removal");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_40") public static void palimpsestCuriosDawnMendingPulsesAndClears(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);wearer.setHealth(10F);
        ArtifactPassives.tick(wearer,ArtifactKind.PALIMPSEST);var mending=SpecialPickaxes.DAWN_MENDING.get();
        h.assertTrue(wearer.hasEffect(mending)&&wearer.getEffect(mending).getDuration()==40,"Palimpsest grants its named Curios healing passive");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),"Dawn Mending replaces generic Regeneration");
        h.assertTrue(wearer.hasEffect(SpecialPickaxes.STRATA_FOCUS.get())&&wearer.getEffect(SpecialPickaxes.STRATA_FOCUS.get()).getAmplifier()==1,"Palimpsest keeps Strata Focus II while healing");
        mending.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==11F,"a dawn pulse heals one point");
        mending.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==11F,"effect refreshes cannot speed up the pulse");
        wearer.tickCount+=40;mending.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==12F,"a second pulse is available after two seconds");
        h.assertTrue(mending.isDurationEffectTick(40,0)&&!mending.isDurationEffectTick(39,0),"the potion effect uses a two-second pulse cadence");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.PALIMPSEST);
        h.assertTrue(!wearer.hasEffect(mending),"Dawn Mending clears when Palimpsest leaves Curios");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_41") public static void crucibleWardDeflectsFastHostileProjectile(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CRUCIBLE);var archer=EntityType.SKELETON.create(h.getLevel());
        archer.setPos(wearer.getX()+6,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(archer);
        var arrow=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),archer);
        arrow.setPos(wearer.getX()+3,wearer.getY()+1,wearer.getZ());arrow.setDeltaMovement(-3D,0D,0D);h.getLevel().addFreshEntity(arrow);
        h.assertTrue(ArtifactSkills.use(wearer,wearer.getMainHandItem(),ArtifactKind.CRUCIBLE),"the eight-second Crucible projectile ward starts");
        DomainFields.tick();
        h.assertTrue(arrow.getDeltaMovement().x>0D,"the ward redirects a fast hostile shot outward before impact");
        h.assertTrue(DomainFields.active(wearer),"the deflection does not consume or cancel the remaining ward");finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void axiomCuriosNullMarkSuppressesHealing(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.AXIOM);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.AXIOM);
        var mark=SpecialPickaxes.NULL_MARK.get();
        h.assertTrue(attacker.hasEffect(mark)&&attacker.getEffect(mark).getDuration()==60,"Axiom's Curios counter applies a three-second null mark");
        h.assertTrue(MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingHealEvent(attacker,4F)),"marked attacker cannot heal during the counter");
        h.assertTrue(!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingHealEvent(wearer,1F)),"the wearer and unmarked allies remain healable");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void hellBloomTelegraphMatchesItsThreeDimensionalMiningShape(GameTestHelper h) {
        h.assertTrue(HellspecBloomShape.contains(0,0,0)&&HellspecBloomShape.contains(4,0,0),"the core and all petal centers are mined");
        h.assertTrue(HellspecBloomShape.contains(0,2,0)&&HellspecBloomShape.contains(0,-2,0),"the bloom has two-block vertical depth");
        h.assertTrue(!HellspecBloomShape.contains(0,3,0)&&HellspecBloomShape.contains(7,0,0)&&!HellspecBloomShape.contains(8,0,0),"the excavation has firm vertical and radial edges");
        var outline=HellspecBloomShape.boundary();
        h.assertTrue(outline.size()>100&&outline.size()<400,"the full outline stays within its particle budget");
        for(var pos:outline)h.assertTrue(HellspecBloomShape.contains(pos.getX(),pos.getY(),pos.getZ()),"preview points belong to the mined volume");
        h.succeed();
    }

    @GameTest(template="empty",batch="legacy_each_07") public static void crucibleConservesBlocksAndLoot(GameTestHelper h) {
        var p=player(h,ArtifactKind.CRUCIBLE);var pos=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(pos);ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",7);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,false),"transmutation activates");drain(p);
        h.assertTrue(h.getLevel().getBlockState(pos).is(Blocks.OBSIDIAN),"curated rephase applied");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(0.8)).isEmpty(),"no extra drops from replacement");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_42") public static void crucibleRephasesCommonAndDeepGeologyButKeepsOresAndContainers(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var center=target(h);var level=h.getLevel();
        var convertible=new LinkedHashMap<BlockPos,Block>();
        convertible.put(center,Blocks.STONE);
        convertible.put(center.offset(1,0,0),Blocks.OBSIDIAN);
        convertible.put(center.offset(-1,0,0),Blocks.GRAVEL);
        convertible.put(center.offset(0,1,0),Blocks.DIORITE);
        convertible.put(center.offset(0,-1,0),Blocks.TUFF);
        convertible.put(center.offset(0,0,1),Blocks.POINTED_DRIPSTONE);
        for(var entry:convertible.entrySet()){
            level.setBlockAndUpdate(entry.getKey(),entry.getValue().defaultBlockState());
            PlayerPlacedBlocks.get(level).unmark(entry.getKey());
        }
        var chest=center.offset(2,0,0);var ore=center.offset(0,0,-1);
        level.setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        PlayerPlacedBlocks.get(level).unmark(ore);
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",8);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,false),"calcite rephase activates");drain(p);
        for(var entry:convertible.entrySet())h.assertTrue(level.getBlockState(entry.getKey()).is(Blocks.CALCITE),entry.getValue()+" rephases to calcite");
        h.assertTrue(level.getBlockState(chest).is(Blocks.CHEST)&&level.getBlockState(ore).is(Blocks.DIAMOND_ORE),"containers and ores are preserved");
        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(center).inflate(3)).isEmpty(),"rephasing never scatters drops");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_08") public static void interregnumStasisAndRelease(GameTestHelper h) {
        var p=player(h,ArtifactKind.INTERREGNUM);var center=target(h);
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(center.getX()+1.5,center.getY(),center.getZ()+0.5);h.getLevel().addFreshEntity(mob);
        mob.setDeltaMovement(0.15,0,0);Vec3 start=mob.position();
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,false),"stasis activation");DomainFields.tick();
        h.assertTrue(DomainFields.frozen(mob),"hostile enrolled in stasis");mob.setPos(start.add(0.3,0,0));DomainFields.tick();
        h.assertTrue(mob.position().distanceToSqr(start)<0.001 && mob.getDeltaMovement().lengthSqr()==0,"position and momentum held");
        h.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION)&&p.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier()==4&&p.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"domain grants Regeneration V and Night Vision");
        DomainFields.stop(p);h.assertTrue(!DomainFields.frozen(mob) && Math.abs(mob.getDeltaMovement().x-.15)<.00001,"original momentum restored without weaponizing release");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_30") public static void stasisBoundaryPulseTravelsTheSquareAndWraps(GameTestHelper h){
        var center=target(h);int radius=8;double side=radius*2D,perimeter=side*4D;
        var start=ArtifactFeedback.stasisBoundaryPoint(center,radius,0);
        var east=ArtifactFeedback.stasisBoundaryPoint(center,radius,side);
        var north=ArtifactFeedback.stasisBoundaryPoint(center,radius,side*2D);
        var wrapped=ArtifactFeedback.stasisBoundaryPoint(center,radius,perimeter+3D);
        h.assertTrue(Math.abs(start.x-(center.getX()-radius+.5D))<.001&&Math.abs(start.z-(center.getZ()-radius+.5D))<.001,"pulse starts on the near corner");
        h.assertTrue(Math.abs(east.x-(center.getX()+radius+.5D))<.001&&Math.abs(east.z-start.z)<.001,"pulse follows the first square edge");
        h.assertTrue(Math.abs(north.x-east.x)<.001&&Math.abs(north.z-(center.getZ()+radius+.5D))<.001,"pulse follows the second square edge");
        h.assertTrue(start.distanceToSqr(ArtifactFeedback.stasisBoundaryPoint(center,radius,perimeter))<.001,"pulse wraps cleanly around the domain");
        h.assertTrue(wrapped.x>=center.getX()-radius+.5D&&wrapped.x<=center.getX()+radius+.5D
                &&wrapped.z>=center.getZ()-radius+.5D&&wrapped.z<=center.getZ()+radius+.5D,"pulse remains on the marked boundary");
        h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_09") public static void stasisAuraFollowsOnlyItsOwner(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);ArtifactState.of(p,ArtifactKind.INTERREGNUM).putInt("mode",1);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,false),"personal aura activates without a target block");DomainFields.tick();
        h.assertTrue(DomainFields.contains(p,p.blockPosition())&&p.hasEffect(SpecialPickaxes.DOMINION.get())&&p.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"aura follows owner with personal effects");
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setPos(p.getX()+1,p.getY(),p.getZ());h.getLevel().addFreshEntity(mob);mob.setDeltaMovement(.1,0,0);DomainFields.tick();
        h.assertTrue(!DomainFields.frozen(mob),"personal aura does not freeze nearby mobs");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_10") public static void stasisDomainSupportsAlliedMiners(GameTestHelper h){
        var owner=player(h,ArtifactKind.INTERREGNUM);var ally=player(h,ArtifactKind.INTERREGNUM,"ArtifactAlly");var center=target(h);
        var team=h.getLevel().getScoreboard().addPlayerTeam("stasis_"+owner.getUUID().toString().substring(0,8));
        h.getLevel().getScoreboard().addPlayerToTeam(owner.getScoreboardName(),team);
        h.getLevel().getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        ally.setPos(center.getX()+1.5,center.getY(),center.getZ()+0.5);ally.getFoodData().setFoodLevel(0);
        h.getLevel().addFreshEntity(ally);
        h.assertTrue(DomainFields.start(owner,owner.getMainHandItem(),ArtifactKind.INTERREGNUM,center,8),"team stasis starts");
        DomainFields.tick();
        h.assertTrue(ally.hasEffect(SpecialPickaxes.DOMINION.get())&&ally.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"ally receives the domain identity effects");
        h.assertTrue(ally.getEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED).getAmplifier()==2
                &&ally.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier()==1,"ally receives Haste III and Regeneration II");
        h.assertTrue(ally.getFoodData().getFoodLevel()>0,"ally receives gradual food restoration");
        DomainFields.stop(owner);finish(h,owner);
    }
    @GameTest(template="empty",batch="legacy_each_11") public static void worldloomBoundedQuarryPreservesOres(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var center=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(center);p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(center));var stone=center.east();var ore=center.above();
        h.getLevel().setBlockAndUpdate(stone,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,false),"quarry activation");drain(p);
        h.assertTrue(h.getLevel().getBlockState(center).isAir()&&h.getLevel().getBlockState(stone).isAir(),"scheduled quarry mines the stone sphere");
        h.assertTrue(h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"quarry preserves ores");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_12") public static void icarusBoreAndSolidStop(GameTestHelper h) {
        var p=player(h,ArtifactKind.ICARUS);var wall=h.absolutePos(new BlockPos(5,2,5));
        for(int x=-1;x<=1;x++) for(int y=0;y<3;y++) h.getLevel().setBlockAndUpdate(wall.offset(x,y,0),Blocks.STONE.defaultBlockState());
        var barrier=wall.offset(0,0,2);h.getLevel().setBlockAndUpdate(barrier,Blocks.BEDROCK.defaultBlockState());
        h.getLevel().setBlockAndUpdate(barrier.above(),Blocks.BEDROCK.defaultBlockState());
        double old=p.getZ();h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.ICARUS,false),"kinetic bore activates");drain(p);
        h.assertTrue(p.getZ()>old && p.getZ()<barrier.getZ(),"advanced through excavation but not bedrock");
        h.assertTrue(h.getLevel().getBlockState(wall).isAir(),"bore excavated corridor");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_12") public static void icarianLiftReachesSkyThroughItsOwnShaft(GameTestHelper h) {
        var p=player(h,ArtifactKind.ICARUS);var base=p.blockPosition();
        for(int y=1;y<=6;y++)for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)h.getLevel().setBlockAndUpdate(base.offset(x,y,z),Blocks.STONE.defaultBlockState());
        h.assertTrue(RelicControl.execute(p,ArtifactKind.ICARUS,RelicControl.Action.ALT_SKILL),"Icarian Lift starts without a target block");
        h.assertTrue(WorkQueue.protectsFall(p),"fall damage is protected while the lift is running");drain(p);
        h.assertTrue(p.blockPosition().getY()>=base.getY()+5,"the lift carries its owner up the shaft toward open sky");
        for(int y=1;y<=6;y++)h.assertTrue(h.getLevel().getBlockState(base.above(y)).isAir(),"the central shaft is cleared through the roof");
        h.assertTrue(h.getLevel().getBlockState(base.above().east()).is(Blocks.STONE),"the lift never widens the shaft into the roof");
        h.assertTrue(!WorkQueue.busy(p)&&!WorkQueue.protectsFall(p),"movement protection ends with the lift job");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_12") public static void icarianLiftStopsAtProtectedBarrier(GameTestHelper h) {
        var p=player(h,ArtifactKind.ICARUS);var base=p.blockPosition();
        for(int y=1;y<=2;y++)h.getLevel().setBlockAndUpdate(base.above(y),Blocks.STONE.defaultBlockState());
        var protectedBlock=base.above(3);h.getLevel().setBlockAndUpdate(protectedBlock,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(h.getLevel()).mark(protectedBlock);
        h.assertTrue(RelicControl.execute(p,ArtifactKind.ICARUS,RelicControl.Action.ALT_SKILL),"Icarian Lift starts below a protected block");drain(p);
        h.assertTrue(h.getLevel().getBlockState(protectedBlock).is(Blocks.STONE),"the lift stops without breaking a player-protected block");
        h.assertTrue(p.blockPosition().getY()<=base.getY()+1,"the lift stops before pushing its owner through the barrier");
        h.assertTrue(!WorkQueue.busy(p),"barrier stop closes the lift cleanly");PlayerPlacedBlocks.get(h.getLevel()).unmark(protectedBlock);finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_13") public static void axiomPeelsConnectedMatrix(GameTestHelper h) {
        var p=player(h,ArtifactKind.AXIOM);var center=target(h);var matrix=center.offset(9,0,0);var ore=center.above();
        h.getLevel().setBlockAndUpdate(matrix,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false),"axiom activation");for(int tick=0;tick<2000&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(matrix).isAir(),"matrix removed");
        h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.STONE) && h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"hollow core and ore preserved");finish(h,p);
    }

    @GameTest(template="empty",batch="legacy_each_14") public static void protectionRollbackAndPayment(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var a=target(h);var b=a.offset(3,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.DEEPSLATE.defaultBlockState());
        Consumer<BlockEvent.EntityPlaceEvent> deny=e -> { if(e.getPos().equals(b)) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockEvent.EntityPlaceEvent.class,deny);
        try {
            h.assertTrue(!WorldSafety.exchange(p,p.getMainHandItem(),a,b,Blocks.STONE.defaultBlockState(),Blocks.DEEPSLATE.defaultBlockState()),"denied second placement rejects pair");
            h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE) && h.getLevel().getBlockState(b).is(Blocks.DEEPSLATE),"both states rolled back");
            h.getLevel().setBlockAndUpdate(b,Blocks.AIR.defaultBlockState());p.getInventory().add(new ItemStack(Items.STONE,2));
            h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,b,Blocks.STONE.defaultBlockState()),"placement cancellation honoured");
            h.assertTrue(p.getInventory().countItem(Items.STONE)==2 && h.getLevel().getBlockState(b).isAir(),"denial costs nothing and leaves air");
        } finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_15") public static void breakProtectionAndStalePlans(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);var pos=target(h);var tool=p.getMainHandItem();
        Consumer<BlockEvent.BreakEvent> deny=e -> { if(e.getPos().equals(pos)) e.setCanceled(true); };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockEvent.BreakEvent.class,deny);
        try { h.assertTrue(!WorldSafety.mine(p,tool,ArtifactKind.CHOIR,pos,Blocks.STONE.defaultBlockState()),"Forge break protection honoured"); }
        finally { MinecraftForge.EVENT_BUS.unregister(deny); }
        h.assertTrue(tool.getDamageValue()==0 && h.getLevel().getBlockState(pos).is(Blocks.STONE),"no denied drops/durability");
        var step=new WorkStep.Mine(pos,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());
        h.assertTrue(!step.apply(p,tool,ArtifactKind.CHOIR),"stale plan cannot mine changed state");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_16") public static void cooldownPersistenceAndMemoryBound(GameTestHelper h) {
        var p=player(h,ArtifactKind.INTERREGNUM);target(h);var tool=p.getMainHandItem();
        h.assertTrue(ArtifactActions.use(p,tool,ArtifactKind.INTERREGNUM,false),"first activation");
        h.assertTrue(!ArtifactActions.use(p,tool,ArtifactKind.INTERREGNUM,false),"replay blocked");
        h.assertTrue(tool.getDamageValue()==ArtifactConfig.COST.get(),"activation cost exact");
        var serialized=ArtifactState.root(p).copy();var other=player(h,ArtifactKind.INTERREGNUM);
        other.getPersistentData().put(ArtifactState.ROOT,serialized);
        h.assertTrue(!ArtifactActions.use(other,other.getMainHandItem(),ArtifactKind.INTERREGNUM,false),"serialized cooldown retained");
        var stoneState=Blocks.STONE.defaultBlockState();
        h.assertTrue(WorldSafety.inert(stoneState),"stone qualifies for bounded inert memory: key="+net.minecraftforge.registries.ForgeRegistries.BLOCKS.getKey(stoneState.getBlock())+", default="+(stoneState==stoneState.getBlock().defaultBlockState())+", fluid="+stoneState.getFluidState().isEmpty()+", bedrock="+stoneState.is(Blocks.BEDROCK)+", blockEntity="+stoneState.hasBlockEntity()+", ore="+ArtifactOres.isOre(stoneState)+", commonStone="+stoneState.is(net.minecraftforge.common.Tags.Blocks.STONE)+", dirt="+stoneState.is(net.minecraft.tags.BlockTags.DIRT)+", gravel="+stoneState.is(net.minecraftforge.common.Tags.Blocks.GRAVEL));
        ArtifactState.record(p,ArtifactKind.PALIMPSEST,new BlockPos(0,40,0),Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactState.of(p,ArtifactKind.PALIMPSEST).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND).size()==1,"memory record persists before capacity test");
        for(int i=1;i<ArtifactConfig.MEMORY.get()+10;i++) ArtifactState.record(p,ArtifactKind.PALIMPSEST,new BlockPos(i,40,0),Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactState.memories(p,ArtifactKind.PALIMPSEST).size()==ArtifactConfig.MEMORY.get(),"bounded memory expected="+ArtifactConfig.MEMORY.get()+" actual="+ArtifactState.memories(p,ArtifactKind.PALIMPSEST).size()+" stored="+ArtifactState.of(p,ArtifactKind.PALIMPSEST).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND).size());
        var snapshot=ArtifactState.root(p).copy();other.getPersistentData().put(ArtifactState.ROOT,snapshot);
        h.assertTrue(ArtifactState.memories(other,ArtifactKind.PALIMPSEST).size()==ArtifactConfig.MEMORY.get(),"memory roundtrip");
        WorkQueue.cancel(other);DomainFields.stop(other);finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_17") public static void schedulerBudgetsAndCancellation(GameTestHelper h) {
        var p=player(h,ArtifactKind.AXIOM);var steps=new ArrayList<WorkStep>();var count=new int[]{0};
        for(int i=0;i<100;i++) steps.add(new WorkStep() {
            public BlockPos pos(){return p.blockPosition();}
            public boolean apply(ServerPlayer actor,ItemStack stack,ArtifactKind kind){count[0]++;return false;}
        });
        h.assertTrue(WorkQueue.start(p,p.getMainHandItem(),ArtifactKind.AXIOM,steps),"job queued");WorkQueue.tick();
        h.assertTrue(count[0]>0 && count[0]<=ArtifactConfig.PER_PLAYER.get(),"attempts, including failures, are budgeted");
        h.assertTrue(WorkQueue.lastAttempts()<=ArtifactConfig.GLOBAL.get(),"global budget");
        int previous=count[0];p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WorkQueue.tick();
        h.assertTrue(count[0]==previous && !WorkQueue.busy(p),"switching tool cancels pending work");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_18") public static void unloadedChunkAndRestrictedBlocks(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var pos=new BlockPos(1000000,100,1000000);p.setPos(pos.getX()+0.5,pos.getY(),pos.getZ()+0.5);
        h.assertTrue(!h.getLevel().hasChunkAt(pos),"fixture chunk is unloaded");
        h.assertTrue(!WorldSafety.mine(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,Blocks.STONE.defaultBlockState()),"unloaded denied");
        h.assertTrue(!h.getLevel().hasChunkAt(pos),"operation did not load chunk");
        h.assertTrue(!WorldSafety.inert(Blocks.CHEST.defaultBlockState()) && !WorldSafety.inert(Blocks.DIAMOND_ORE.defaultBlockState())
            && !WorldSafety.inert(Blocks.BEDROCK.defaultBlockState()),"protected matter categories excluded");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_19") public static void silkFortuneLootAndDurability(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);var pos=target(h);var tool=p.getMainHandItem();tool.enchant(Enchantments.SILK_TOUCH,1);
        WorkQueue.start(p,tool,ArtifactKind.CHOIR,List.of(new WorkStep.Mine(pos,Blocks.STONE.defaultBlockState())));drain(p);
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(0.8));
        h.assertTrue(drops.stream().mapToInt(e -> e.getItem().is(Items.STONE)?e.getItem().getCount():0).sum()==1,"exact silk drop, not duplicated");
        h.assertTrue(tool.getDamageValue()==1,"normal per-block durability");
        tool=new ItemStack(tool.getItem());tool.enchant(Enchantments.BLOCK_FORTUNE,3);p.setItemInHand(InteractionHand.MAIN_HAND,tool);
        var ore=pos.offset(2,0,0);h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        WorkQueue.start(p,tool,ArtifactKind.CHOIR,List.of(new WorkStep.Mine(ore,Blocks.DIAMOND_ORE.defaultBlockState())));drain(p);
        int diamonds=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(ore).inflate(0.8)).stream().mapToInt(e -> e.getItem().is(Items.DIAMOND)?e.getItem().getCount():0).sum();
        h.assertTrue(diamonds>=1 && diamonds<=4,"Fortune loot within vanilla range");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_33") public static void iridiumCuriosLodeguardHardensAfterFirstHostileHit(GameTestHelper h){
        var wearer=player(h,ArtifactKind.IRIDIUM);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.IRIDIUM);
        var guard=SpecialPickaxes.LODEGUARD.get();var effect=wearer.getEffect(guard);
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F,"Lodeguard trims the first hit by 15 percent");
        h.assertTrue(effect!=null&&effect.getDuration()==80,"the custom crystal ward lasts four seconds");
        h.assertTrue(Math.abs(wearer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)-4D)<.01D,"the ward adds four armor points");
        var repeat=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,repeat,ArtifactKind.IRIDIUM);
        h.assertTrue(repeat.getAmount()==10F&&wearer.getEffect(guard).getDuration()==80,"the eight-second cooldown prevents repeated triggers and refresh spam");
        h.assertTrue(guard.isDurationEffectTick(80,0)&&!guard.isDurationEffectTick(79,0),"the ward's crystal effect pulses every ten ticks");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_19") public static void curiosIridiumHighlightsOnlyDropsCreatedByOreBreak(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR);var pos=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(pos);
        var ore=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(pos,ore);
        var unrelated=new ItemEntity(h.getLevel(),pos.getX()+.3,pos.getY()+.2,pos.getZ()+.3,new ItemStack(Items.ROTTEN_FLESH));
        h.getLevel().addFreshEntity(unrelated);var curioIridium=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.IRIDIUM).get());
        h.assertTrue(MiningObservations.captureCuriosIridium(p,curioIridium,pos,ore),"Curios Iridium observes an ore break while Choir is held");
        h.assertTrue(!MiningObservations.captureCuriosIridium(p,p.getMainHandItem(),pos,ore),"held Iridium does not create a duplicate Curios observation");
        h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        var drop=new ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY()+.25,pos.getZ()+.5,new ItemStack(Items.DIAMOND,2));h.getLevel().addFreshEntity(drop);
        MiningObservations.flush();
        h.assertTrue(drop.isCurrentlyGlowing()&&drop.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop"),"new ore loot gets Iridium's glow tag");
        h.assertTrue(drop.getPersistentData().hasUUID("specialpickaxesIridiumOwner")&&drop.getPersistentData().getUUID("specialpickaxesIridiumOwner").equals(p.getUUID()),"only the wearer owns the recalled drop");
        h.assertTrue(!OreDropHighlight.mark(drop,p),"a repeated observation does not replay the sparkle burst");
        h.assertTrue(!unrelated.isCurrentlyGlowing()&&!unrelated.getPersistentData().contains("specialpickaxesIridiumOreDrop"),"a nearby pre-existing item is not marked");
        h.assertTrue(p.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.CHOIR,"Curios ore tracking leaves the held pickaxe unchanged");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_20") public static void noUnpaidPlacementOrWallTeleport(GameTestHelper h) {
        var p=player(h,ArtifactKind.ICARUS);var pos=target(h);
        h.assertTrue(!WorldSafety.move(p,Vec3.atBottomCenterOf(pos)),"occupied destination rejected");
        h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,Blocks.STONE.defaultBlockState()),"occupied construction rejected");
        var air=pos.above();h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,air,Blocks.STONE.defaultBlockState()),"no free block without payment");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_21") public static void vanillaTiersRemainValid(GameTestHelper h) {
        h.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(Blocks.OBSIDIAN.defaultBlockState()),"artifact tier does not revoke vanilla diamond harvesting");
        h.assertTrue(new ItemStack(Items.NETHERITE_PICKAXE).isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),"netherite remains valid");
        h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_22") public static void changedPaymentCannotDuplicateMatter(GameTestHelper h) {
        var p=player(h,ArtifactKind.WORLDLOOM);var pos=target(h).above();
        p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.STONE,1));
        Consumer<BlockEvent.EntityPlaceEvent> change=e -> {
            if(e.getPos().equals(pos)) p.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,BlockEvent.EntityPlaceEvent.class,change);
        try {
            h.assertTrue(!WorldSafety.placePaid(p,p.getMainHandItem(),ArtifactKind.WORLDLOOM,pos,Blocks.STONE.defaultBlockState()),"invalidated payment rejects placement");
            h.assertTrue(h.getLevel().getBlockState(pos).isAir(),"unpaid matter rolled back");
        } finally { MinecraftForge.EVENT_BUS.unregister(change); }
        finish(h,p);
    }


    @GameTest(template="empty",batch="legacy_each_23") public static void dominionSpeedIsLocal(GameTestHelper h) {
        var p=player(h,ArtifactKind.INTERREGNUM);var center=target(h);
        DomainFields.start(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,center,4);
        p.setPos(center.getX()+0.5,center.getY(),center.getZ()-2);DomainFields.tick();
        var speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,Blocks.STONE.defaultBlockState(),64,center);
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(speed.getNewSpeed()==512,"domain grants actual eightfold speed");
        p.setPos(center.getX()+12,center.getY(),center.getZ());
        speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,Blocks.STONE.defaultBlockState(),64,center);
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(speed.getNewSpeed()==64,"no speed outside domain");finish(h,p);
    }



    @GameTest(template="empty",batch="legacy_each_24") public static void multiToolAndHighEnchantments(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var tool=p.getMainHandItem();
        for(var block:List.of(Blocks.STONE,Blocks.DEEPSLATE,Blocks.OAK_LOG,Blocks.OAK_PLANKS,Blocks.DIRT,Blocks.SAND,Blocks.GRAVEL,Blocks.CLAY,Blocks.SNOW_BLOCK)){
            h.assertTrue(tool.isCorrectToolForDrops(block.defaultBlockState()),"combined harvest tool: "+block);h.assertTrue(tool.getDestroySpeed(block.defaultBlockState())>=64,"combined mining speed");
        }
        for(int level:new int[]{100,255,1000,100000}){
            var levels=new net.minecraft.nbt.ListTag();var entry=new net.minecraft.nbt.CompoundTag();entry.putString("id","minecraft:efficiency");entry.putInt("lvl",level);levels.add(entry);tool.getOrCreateTag().put("Enchantments",levels);
            h.assertTrue(EnchantmentScaling.level(tool,Enchantments.BLOCK_EFFICIENCY)==level,"raw enchantment survives exactly");
            h.assertTrue(EnchantmentScaling.budget(tool)<=ArtifactConfig.ENCHANT_BUDGET.get(),"bounded throughput");
        }
        var fortune=new net.minecraft.nbt.CompoundTag();fortune.putString("id","minecraft:fortune");fortune.putInt("lvl",1000);tool.getEnchantmentTags().add(fortune);h.assertTrue(tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE)==1000,"Fortune 1000 not truncated to 255");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_25") public static void regionSelectionAnalysisAndPause(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",6);var a=target(h);var b=a.offset(2,0,0);PlayerPlacedBlocks.get(h.getLevel()).unmark(a);PlayerPlacedBlocks.get(h.getLevel()).unmark(b);h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R arms selection");
        h.assertTrue(p.getMainHandItem().getDestroySpeed(Blocks.STONE.defaultBlockState())<=1F,"mining speed drops while selecting corners");
        h.assertTrue(ArtifactInteraction.left(p,a,false)&&ArtifactInteraction.left(p,b,false),"select corners");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R analyzes selection");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE)&&WorkQueue.status(p).equals("paused"),"R pauses analysis");
        WorkQueue.tick();h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE),"paused operation untouched");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R resumes analysis");
        for(int tick=0;tick<10&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE)&&WorkQueue.status(p).equals("ready"),"analysis never mutates and requires confirmation");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R confirms analyzed operation");drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.BASALT),"confirmed operation transforms");ArtifactInteraction.clear(p);
        h.assertTrue(p.getMainHandItem().getDestroySpeed(Blocks.STONE.defaultBlockState())>1F,"normal mining speed returns after selection");finish(h,p);
    }


    @GameTest(template="empty",batch="legacy_each_26") public static void simultaneousLargeRegionsAreBounded(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var q=player(h,ArtifactKind.CRUCIBLE);var a=target(h);var v=new SelectionVolume(a,a.offset(49,19,49));
        h.assertTrue(v.size()==50000,"large selection");
        for(var owner:List.of(p,q))h.assertTrue(WorkQueue.startRegion(owner,owner.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState())),"stream does not materialize/truncate large volume");
        WorkQueue.tick();h.assertTrue(WorkQueue.lastAttempts()<=ArtifactConfig.GLOBAL.get(),"global fair budget");h.assertTrue(WorkQueue.remaining(p)>49000&&WorkQueue.remaining(q)>49000,"both cursors retained");
        WorkQueue.cancel(q);h.assertTrue(!WorkQueue.busy(q)&&WorkQueue.busy(p),"independent cancellation");p.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WorkQueue.tick();h.assertTrue(!WorkQueue.busy(p),"tool switch cancels large work");finish(h,p);
    }

    @GameTest(template="empty",batch="legacy_each_27") public static void regionLifecycleCancellation(GameTestHelper h){
        for(int reason=0;reason<3;reason++){
            var p=player(h,ArtifactKind.CRUCIBLE);var a=target(h);var v=new SelectionVolume(a,a.offset(10,10,10));
            WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState()));
            if(reason==0)MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(p));
            if(reason==1){p.setHealth(0);WorkQueue.tick();}
            if(reason==2)MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent(p,net.minecraft.world.level.Level.OVERWORLD,net.minecraft.world.level.Level.NETHER));
            h.assertTrue(!WorkQueue.busy(p),"logout/death/dimension cancellation "+reason);
        }h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_28") public static void unloadedRegionPausesWithoutLoading(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=new BlockPos(1000000,60,1000000);var v=new SelectionVolume(a,a);
        h.assertTrue(!h.getLevel().hasChunkAt(a),"fixture chunk absent");WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState()));
        WorkQueue.tick();h.assertTrue(WorkQueue.status(p).equals("paused")&&WorkQueue.remaining(p)==1&&!h.getLevel().hasChunkAt(a),"no implicit chunk load, cursor retained");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_29") public static void analyzedRegionDoesNotDestroyChangedBlocksOrMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var a=target(h);var b=a.offset(1,0,0);h.getLevel().setBlockAndUpdate(b,Blocks.CHEST.defaultBlockState());
        var v=new SelectionVolume(a,b);WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.CRUCIBLE,0,Blocks.BASALT.defaultBlockState()));
        WorkQueue.tick();h.getLevel().setBlockAndUpdate(a,Blocks.OBSIDIAN.defaultBlockState());WorkQueue.togglePause(p);drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.OBSIDIAN)&&h.getLevel().getBlockState(b).is(Blocks.CHEST),"analyzed state changed: skipped; machine untouched");finish(h,p);
    }


    @GameTest(template="empty",batch="legacy_each_30") public static void regionProtectionStillAppliesAfterAnalysis(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var a=target(h);var v=new SelectionVolume(a,a);
        WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new RegionWork(v,null,SelectionVolume.Transform.IDENTITY,ArtifactKind.WORLDBREAKER,0,Blocks.STONE.defaultBlockState()));WorkQueue.tick();
        Consumer<BlockEvent.BreakEvent> deny=e->{if(e.getPlayer()==p)e.setCanceled(true);};MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,deny);
        try{WorkQueue.togglePause(p);drain(p);h.assertTrue(h.getLevel().getBlockState(a).is(Blocks.STONE),"new protection still cancels after preparing");}finally{MinecraftForge.EVENT_BUS.unregister(deny);}finish(h,p);
    }

    @GameTest(template="empty",batch="legacy_each_31") public static void extremeEnchantmentGameplay(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var pos=target(h);var tool=p.getMainHandItem();var tags=new net.minecraft.nbt.ListTag();
        for(String id:List.of("efficiency","fortune","unbreaking","mending")){var t=new net.minecraft.nbt.CompoundTag();t.putString("id","minecraft:"+id);t.putInt("lvl",id.equals("efficiency")?Integer.MAX_VALUE:1000);tags.add(t);}tool.getOrCreateTag().put("Enchantments",tags);
        float speed=p.getDigSpeed(Blocks.STONE.defaultBlockState(),pos);h.assertTrue(Float.isFinite(speed)&&speed>0,"actual vanilla/Forge extreme Efficiency is finite and positive");
        h.getLevel().setBlockAndUpdate(pos,Blocks.DIAMOND_ORE.defaultBlockState());h.assertTrue(WorldSafety.mine(p,tool,ArtifactKind.WORLDBREAKER,pos,Blocks.DIAMOND_ORE.defaultBlockState()),"real Fortune 1000 harvesting");
        int diamonds=h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).stream().filter(e->e.getItem().is(Items.DIAMOND)).mapToInt(e->e.getItem().getCount()).sum();
        h.assertTrue(diamonds>0&&diamonds<=1002,"native high-Fortune loot remains bounded and nonnegative");
        tool.setDamageValue(100);new net.minecraft.world.entity.ExperienceOrb(h.getLevel(),p.getX(),p.getY(),p.getZ(),10).playerTouch(p);
        h.assertTrue(tool.getDamageValue()<100,"Mending 1000 repairs with actual XP pickup");finish(h,p);
    }

    @GameTest(template="empty",batch="legacy_each_32") public static void memoriesKeepDimensionsAndExpire(GameTestHelper h){
        var p=player(h,ArtifactKind.PALIMPSEST);var a=target(h);ArtifactState.record(p,ArtifactKind.PALIMPSEST,a,Blocks.STONE.defaultBlockState());
        var list=ArtifactState.of(p,ArtifactKind.PALIMPSEST).getList("memory",net.minecraft.nbt.Tag.TAG_COMPOUND);list.getCompound(0).putString("dim","minecraft:the_nether");
        ArtifactState.record(p,ArtifactKind.PALIMPSEST,a.offset(1,0,0),Blocks.STONE.defaultBlockState());
        h.assertTrue(list.size()==2,"cross-dimension record stays in bounded history");h.assertTrue(ArtifactState.memories(p,ArtifactKind.PALIMPSEST).size()==1,"only current-dimension memory can be restored");
        list.getCompound(0).putLong("epoch",System.currentTimeMillis()-(ArtifactConfig.MEMORY_TTL.get()+1L)*50);ArtifactState.prune(p,ArtifactKind.PALIMPSEST);
        h.assertTrue(list.size()==1,"expired record is pruned; remaining="+list.size());finish(h,p);
    }


    @GameTest(template="empty",batch="legacy_each_33") public static void onlyNecessaryModesUseCorners(GameTestHelper h){
        for(var k:ArtifactKind.playableValues())for(int m=0;m<ArtifactInteraction.modeCount(k);m++){
            boolean expected=k==ArtifactKind.CRUCIBLE||k==ArtifactKind.WORLDBREAKER&&m==5;
            h.assertTrue(ArtifactInteraction.regional(k,m)==expected,"regional contract "+k+" mode "+m);
        }h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_34") public static void worldbreakerRegionBreakSkipsFluidsAndMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",5);
        var a=target(h);var granite=a.east();var fluid=a.east(2);var chest=a.east(3);var b=a.east(4);
        h.getLevel().setBlockAndUpdate(granite,Blocks.GRANITE.defaultBlockState());h.getLevel().setBlockAndUpdate(fluid,Blocks.LAVA.defaultBlockState());
        h.getLevel().setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());h.getLevel().setBlockAndUpdate(b,Blocks.STONE.defaultBlockState());
        h.assertTrue(ArtifactInteraction.regional(ArtifactKind.WORLDBREAKER,5),"region mode uses corner selection");
        RelicControl.execute(p,ArtifactKind.WORLDBREAKER,RelicControl.Action.SELECT);RelicControl.corner(p,a);RelicControl.corner(p,b);WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready"),"selection is analyzed before execution");
        inputReady(p,ArtifactKind.WORLDBREAKER);h.assertTrue(RelicControl.execute(p,ArtifactKind.WORLDBREAKER,RelicControl.Action.ACTIVATE),"R confirms analyzed region");drain(p);
        h.assertTrue(h.getLevel().getBlockState(a).isAir()&&h.getLevel().getBlockState(granite).isAir()&&h.getLevel().getBlockState(b).isAir(),"minable region geology is harvested");
        h.assertTrue(h.getLevel().getBlockState(fluid).is(Blocks.LAVA)&&h.getLevel().getBlockState(chest).is(Blocks.CHEST),"fluid and container are preserved");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_35") public static void worldbreakerRunsCopiedMiningSkill(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var tool=p.getMainHandItem();var ore=target(h);
        h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());tool.getOrCreateTag().putString("copiedSkill",ArtifactKind.PALIMPSEST.id);
        h.assertTrue(ArtifactActions.primary(p,tool,ArtifactKind.WORLDBREAKER),"copied Palimpsest mining skill starts");drain(p);
        h.assertTrue(h.getLevel().getBlockState(ore).isAir(),"Worldbreaker executes the selected pick's connected-ore harvest");
        h.assertTrue(h.getLevel().getEntitiesOfClass(ItemEntity.class,new AABB(ore).inflate(1)).stream().anyMatch(e->e.getItem().is(Items.DIAMOND)),"copied ability preserves the source pick's native ore drops");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_36") public static void compactAndExpandedTooltipContracts(GameTestHelper h){
        for(var k:ArtifactKind.playableValues()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(k).get());var compact=new ArrayList<net.minecraft.network.chat.Component>();var expanded=new ArrayList<net.minecraft.network.chat.Component>();
            ArtifactTooltips.compact(tool,k,compact);ArtifactTooltips.expanded(tool,k,expanded);
            h.assertTrue(compact.size()==2&&expanded.size()>=9,"compact R/X lines and complete manual "+k);
            if(k==ArtifactKind.CRUCIBLE){var actions=ArtifactTooltips.actions(tool,k,7);h.assertTrue(!actions.contains(RelicControl.Action.SELECT)&&actions.contains(RelicControl.Action.ACTIVATE)&&!actions.contains(RelicControl.Action.CONFIRM)&&!actions.contains(RelicControl.Action.PAUSE),"R replaces separate selection, confirm, and pause keys");}
        }h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_37") public static void legacyEnergyDoesNotGateAbilities(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);target(h);var tool=p.getMainHandItem();var data=ArtifactState.of(p,ArtifactKind.INTERREGNUM);
        data.putInt("charge",0);tool.getOrCreateTag().putInt("artifactCharge",999);
        ((ArtifactItem)tool.getItem()).inventoryTick(tool,h.getLevel(),p,0,true);
        h.assertTrue(!data.contains("charge")&&!tool.getTag().contains("artifactCharge"),"legacy values removed");
        ArtifactInteraction.use(p,tool,ArtifactKind.INTERREGNUM,false);
        h.assertTrue(DomainFields.active(p)&&!ArtifactInteraction.selecting(p),"first use activates directly without stored resource");finish(h,p);
    }








    @GameTest(template="empty",batch="legacy_each_38") public static void allRelicsKeepTripleToolAndHighLevels(GameTestHelper h){
        for(var k:ArtifactKind.playableValues()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(k).get());
            for(var block:List.of(Blocks.STONE,Blocks.OAK_LOG,Blocks.DIRT))h.assertTrue(tool.isCorrectToolForDrops(block.defaultBlockState()),"triple tool "+k);
            for(int level:new int[]{100,255,1000,100000,Integer.MAX_VALUE}){
                var tags=new net.minecraft.nbt.ListTag();
                for(String id:List.of("efficiency","fortune","silk_touch","unbreaking","mending")){var t=new net.minecraft.nbt.CompoundTag();t.putString("id","minecraft:"+id);t.putInt("lvl",level);tags.add(t);}tool.getOrCreateTag().put("Enchantments",tags);
                var restored=ItemStack.of(tool.save(new net.minecraft.nbt.CompoundTag()));
                for(var enchant:List.of(Enchantments.BLOCK_EFFICIENCY,Enchantments.BLOCK_FORTUNE,Enchantments.SILK_TOUCH,Enchantments.UNBREAKING,Enchantments.MENDING)){
                    h.assertTrue(EnchantmentScaling.level(restored,enchant)==level,"raw NBT roundtrip "+level+" on "+k);
                    if(level<=1000)h.assertTrue(restored.getEnchantmentLevel(enchant)==level,"actual hook preserves "+level+" on "+k);
                }
            }
        }h.succeed();
    }

    private static void inputReady(ServerPlayer p,ArtifactKind k){ArtifactState.of(p,k).remove("inputTick");}
    @GameTest(template="empty",batch="legacy_each_39") public static void semanticControlsAreServerAuthoritative(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);target(h);
        h.assertTrue(!RelicControl.execute(p,ArtifactKind.ICARUS,RelicControl.Action.ACTIVATE),"stale tool packet rejected");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.INTERREGNUM,RelicControl.Action.ACTIVATE)&&DomainFields.active(p),"one intent activates without selection");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.INTERREGNUM,RelicControl.Action.CANCEL)&&!DomainFields.active(p),"cancel bypasses activation cooldown");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_40") public static void selectionAutomaticallyAnalyzesThenExplicitlyConfirms(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(at);PlayerPlacedBlocks.get(h.getLevel()).unmark(at.east());ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",7);p.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.OBSIDIAN));
        h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT),"selection key arms");
        RelicControl.corner(p,at);RelicControl.corner(p,at.east());for(int tick=0;tick<10&&!WorkQueue.status(p).equals("ready");tick++)WorkQueue.tick();
        h.assertTrue(WorkQueue.status(p).equals("ready")&&h.getLevel().getBlockState(at).is(Blocks.STONE),"last corner analyzes without mutation");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.ACTIVATE),"R confirms analyzed region");for(int tick=0;tick<10&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.OBSIDIAN),"confirmed region executes");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_41") public static void modeIsIndependentOfSecondaryAndIncompleteSelection(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT);RelicControl.corner(p,at);
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(!RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CONFIRM),"incomplete state cannot confirm");
        inputReady(p,ArtifactKind.CRUCIBLE);h.assertTrue(RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.MODE)&&!ArtifactInteraction.selecting(p),"mode cancels incompatible selection");h.assertTrue(ArtifactState.mode(p,ArtifactKind.CRUCIBLE)==1,"selected transform persists after leaving selection");
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.CANCEL);h.assertTrue(!ArtifactInteraction.selecting(p),"explicit cancel exits");finish(h,p);
    }


    @GameTest(template="empty",batch="legacy_each_42") public static void changingToDirectModeLeavesNoSelector(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var at=target(h);
        RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.SELECT);RelicControl.corner(p,at);
        inputReady(p,ArtifactKind.CRUCIBLE);RelicControl.execute(p,ArtifactKind.CRUCIBLE,RelicControl.Action.MODE);
        h.assertTrue(ArtifactState.mode(p,ArtifactKind.CRUCIBLE)==1&&!ArtifactInteraction.selecting(p),"direct carve cannot inherit an active corner selector");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_35") public static void worldloomRootsHoldHostilesThenReleaseCleanly(GameTestHelper h){
        var owner=player(h,ArtifactKind.WORLDLOOM);
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setNoAi(true);mob.setPos(owner.getX()+2,owner.getY(),owner.getZ());h.getLevel().addFreshEntity(mob);
        var anchor=mob.position();
        h.assertTrue(ArtifactActions.useAlternate(owner,owner.getMainHandItem(),ArtifactKind.WORLDLOOM),"Worldloom alternate catches a nearby hostile");
        mob.setPos(anchor.add(1,0,0));mob.setDeltaMovement(.8,.2,0);WorldloomSnare.tick();
        h.assertTrue(mob.position().distanceToSqr(anchor)<.001D&&mob.getDeltaMovement().lengthSqr()<.001D,"roots pull the hostile back and suppress escape momentum");
        WorldloomSnare.forget(owner);mob.setDeltaMovement(.4,0,0);WorldloomSnare.tick();
        h.assertTrue(Math.abs(mob.getDeltaMovement().x-.4D)<.001D,"cancel releases the target without leaving a movement lock");
        finish(h,owner);
    }
}
