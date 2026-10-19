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
import net.minecraft.world.effect.MobEffects;
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
    @GameTest(template="empty",batch="rework_each_74") public static void curiosPassiveLookupsAreEvenlySharded(GameTestHelper h){
        int[] buckets=new int[10];
        for(int i=0;i<1000;i++)buckets[SpecialPickaxes.curiosPassiveBucket(new UUID(0L,i))]++;
        h.assertTrue(Arrays.stream(buckets).allMatch(count->count==100),"Curios scans distribute evenly across all ten ticks");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_75") public static void cosmeticCuriosAuraScanUsesEightTickCadence(GameTestHelper h){
        h.assertTrue(CurioAuraSchedule.due(40,Integer.MIN_VALUE),"the first nearby aura scan starts immediately");
        h.assertTrue(!CurioAuraSchedule.due(47,40)&&CurioAuraSchedule.due(48,40),"the visual scan refreshes every eight ticks");
        h.assertTrue(CurioAuraSchedule.due(1,200),"a fresh client level or wrapped tick counter refreshes immediately");
        h.succeed();
    }

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
        h.assertTrue(attacker.getDeltaMovement().x<-.3D,"nearby attackers are tugged toward Eventide's wearer");
        var tether=attacker.getEffect(SpecialPickaxes.GRAVITIC_TETHER.get());
        h.assertTrue(tether.getDuration()==70,"snare lasts three and a half seconds");
        attacker.setDeltaMovement(1,.25,0);SpecialPickaxes.GRAVITIC_TETHER.get().applyEffectTick(attacker,0);
        h.assertTrue(attacker.getDeltaMovement().x<.7&&attacker.getDeltaMovement().y==.25,"snare damps horizontal escape while preserving vertical movement");
        var distantWearer=player(h,ArtifactKind.EVENTIDE,"EventideTestTwo");var distant=EntityType.SKELETON.create(h.getLevel());distant.setPos(distantWearer.getX()+14,distantWearer.getY(),distantWearer.getZ());h.getLevel().addFreshEntity(distant);
        var distantHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(distantWearer,h.getLevel().damageSources().mobAttack(distant),10F);
        ArtifactPassives.onCuriosDamage(distantWearer,distantHit,ArtifactKind.EVENTIDE);
        h.assertTrue(distant.getDeltaMovement().equals(Vec3.ZERO),"gravity debt does not yank a distant attacker across the field");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_63") public static void eventideHeldHitUsesGraviticTetherInsteadOfLevitation(GameTestHelper h){
        var wearer=player(h,ArtifactKind.EVENTIDE,"EventideStrike");var target=EntityType.ZOMBIE.create(h.getLevel());
        target.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(target);
        var tool=wearer.getMainHandItem();
        h.assertTrue(((ArtifactItem)tool.getItem()).hurtEnemy(tool,target,wearer),"Eventide's pickaxe can apply its melee signature");
        var tether=SpecialPickaxes.GRAVITIC_TETHER.get();
        h.assertTrue(target.hasEffect(tether)&&target.getEffect(tether).getAmplifier()==1
                &&target.getEffect(tether).getDuration()==80,"the hit applies four seconds of stronger custom Gravitic Tether");
        h.assertTrue(target.getDeltaMovement().x<-.4D&&!target.hasEffect(net.minecraft.world.effect.MobEffects.LEVITATION),"the hit visibly tugs its target without vanilla Levitation");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_47") public static void interregnumCuriosCounterUsesStrongerGraviticTether(GameTestHelper h){
        var wearer=player(h,ArtifactKind.INTERREGNUM);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.INTERREGNUM);
        var tether=SpecialPickaxes.GRAVITIC_TETHER.get();
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F,"Stasis Reflex trims its triggering hostile hit by fifteen percent");
        h.assertTrue(attacker.hasEffect(tether)&&attacker.getEffect(tether).getDuration()==40&&attacker.getEffect(tether).getAmplifier()==1,"Stasis applies the stronger two-second Gravitic Tether");
        h.assertTrue(!attacker.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Stasis Reflex uses the custom tether instead of generic Slowness");
        attacker.setDeltaMovement(1,.25,0);tether.applyEffectTick(attacker,1);
        h.assertTrue(Math.abs(attacker.getDeltaMovement().x-.60)<.001&&Math.abs(attacker.getDeltaMovement().y-.25)<.001,"Stasis Tether damps horizontal escape without changing vertical movement");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_52") public static void interregnumAbsorptionLeasePreservesExternalEffects(GameTestHelper h){
        var wearer=player(h,ArtifactKind.INTERREGNUM);var absorption=net.minecraft.world.effect.MobEffects.ABSORPTION;
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(absorption,200,1,false,true,true));
        ArtifactPassives.tick(wearer,ArtifactKind.INTERREGNUM);var current=wearer.getEffect(absorption);
        h.assertTrue(current!=null&&current.getDuration()==200&&current.getAmplifier()==1&&!current.isAmbient(),"Stasis does not replace a stronger server-owned Absorption potion");
        wearer.removeEffect(absorption);ArtifactPassives.tick(wearer,ArtifactKind.INTERREGNUM);current=wearer.getEffect(absorption);
        h.assertTrue(current!=null&&current.getDuration()==40&&current.getAmplifier()==0&&current.isAmbient()&&!current.isVisible(),"Stasis supplies and refreshes its own short Absorption lease when no external effect exists");
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(absorption,100,0,false,true,true));
        ArtifactPassives.tick(wearer,ArtifactKind.INTERREGNUM);current=wearer.getEffect(absorption);
        h.assertTrue(current!=null&&current.getDuration()==100&&current.getAmplifier()==0&&!current.isAmbient()&&current.isVisible(),"a later external Absorption potion takes ownership and is not refreshed by the Curios lease");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_48") public static void iridiumRecognizesForeignOresButNotBroadlyTaggedHostRock(GameTestHelper h){
        h.assertTrue(ArtifactOres.isOre(TestOres.NAMED_ORE.defaultBlockState()),"foreign conventional *_ore registry names qualify as ore");
        h.assertTrue(ArtifactOres.isOre(TestOres.TAGGED_ORE.defaultBlockState()),"foreign ore-family tags classify ores without ore in their registry name");
        var host=TestOres.HOST_ROCK.defaultBlockState();
        var aggregate=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("pickaxe_compat_test","ores/broad_aggregate"));
        var hostRockTag=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("pickaxe_compat_test","ore_bearing_ground/stone_hosts"));
        h.assertTrue(host.is(aggregate)&&host.is(hostRockTag),"fixture reproduces a broad mod ore aggregate that also contains host rock");
        h.assertTrue(!ArtifactOres.isOre(host),"ore_bearing_ground keeps modded host rock out of Iridium's ore abilities");
        finish(h,player(h,ArtifactKind.IRIDIUM));
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
    @GameTest(template="empty",batch="rework_each_26") public static void eventideBoundaryFlowShowsSelectedGravityPolarity(GameTestHelper h){
        for(int axis=0;axis<6;axis++){
            var inward=ArtifactFeedback.eventideFlowDirection(0,axis);var outward=ArtifactFeedback.eventideFlowDirection(1,axis);
            h.assertTrue(inward.dot(outward)<-.99D,"outward field particles reverse the inward direction at sphere axis "+axis);
        }
        var center=new BlockPos(13,26,-7);double radius=8D;
        for(int plane=0;plane<3;plane++)for(int sample=0;sample<24;sample++){
            var point=ArtifactFeedback.eventideSpherePoint(center,8,plane,Math.PI*2D*sample/24D);
            h.assertTrue(Math.abs(point.distanceTo(Vec3.atCenterOf(center))-radius)<1.0E-6D,"Eventide's visual wireframe stays on its true spherical radius");
        }
        finish(h,player(h,ArtifactKind.EVENTIDE));
    }
    @GameTest(template="empty",batch="rework_each_27") public static void icarusCuriosGlideBuildsSprintMomentumInAir(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);wearer.setOnGround(false);wearer.setSprinting(true);
        var trail=IcarianMomentumEffect.glideTrailForward(new Vec3(1,0,0),new Vec3(0,0,1));
        h.assertTrue(trail.x>.99D&&Math.abs(trail.z)<.001D,"the feather wake follows travel when the wearer looks sideways");
        var fallback=IcarianMomentumEffect.glideTrailForward(Vec3.ZERO,new Vec3(0,0,-1));
        h.assertTrue(fallback.z<-.99D,"a near-stationary glide uses facing direction for its wake");
        ArtifactPassives.tick(wearer,ArtifactKind.ICARUS);
        var glide=SpecialPickaxes.ICARIAN_MOMENTUM.get();
        h.assertTrue(wearer.hasEffect(glide)&&!wearer.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING),"airborne Icarus Curios grants its named glide effect without generic Slow Falling");
        Vec3 falling=new Vec3(.1D,-.2D,.2D);
        h.assertTrue(IcarianMomentumEffect.guidedLift(falling,new Vec3(0,.54D,0),true,false).equals(falling),"looking below the updraft threshold does not alter vertical movement");
        h.assertTrue(IcarianMomentumEffect.guidedLift(falling,new Vec3(0,.8D,0),false,false).equals(falling)
                        &&IcarianMomentumEffect.guidedLift(falling,new Vec3(0,.8D,0),true,true).equals(falling),
                "the updraft requires sprinting and does not interfere with Elytra flight");
        var lifted=IcarianMomentumEffect.guidedLift(falling,new Vec3(0,.8D,0),true,false);
        h.assertTrue(lifted.y>falling.y&&lifted.x==falling.x&&lifted.z==falling.z,"looking up while sprinting converts the glide into a controlled ascent");
        h.assertTrue(IcarianMomentumEffect.guidedLift(new Vec3(0,.18D,0),new Vec3(0,1D,0),true,false).y==.22D,
                "the updraft caps upward velocity instead of allowing unlimited flight speed");
        wearer.setXRot(-45F);wearer.setDeltaMovement(0,-.2,0);glide.applyEffectTick(wearer,0);var boosted=wearer.getDeltaMovement();
        h.assertTrue(boosted.horizontalDistanceSqr()>.0025&&boosted.y>-.2,"sprint-gliding gains forward momentum and updraft when the wearer looks up");
        wearer.setSprinting(false);wearer.setDeltaMovement(.1,-.2,0);glide.applyEffectTick(wearer,0);
        h.assertTrue(Math.abs(wearer.getDeltaMovement().x-.1)<.001&&Math.abs(wearer.getDeltaMovement().z)<.001,"the glide stops accelerating when sprint is released");
        wearer.setDeltaMovement(0,-1.2,0);glide.applyEffectTick(wearer,0);
        h.assertTrue(Math.abs(wearer.getDeltaMovement().y+.36)<.001,"Icarian Momentum caps a fast descent at a controlled speed");
        wearer.startFallFlying();wearer.setDeltaMovement(0,-1.2,0);glide.applyEffectTick(wearer,0);
        h.assertTrue(wearer.isFallFlying()&&Math.abs(wearer.getDeltaMovement().y+1.2)<.001,
                "Icarian Momentum preserves intentional Elytra dives instead of clamping their vertical speed");
        var fall=new net.minecraftforge.event.entity.living.LivingFallEvent(wearer,16F,1F);MinecraftForge.EVENT_BUS.post(fall);
        h.assertTrue(Math.abs(fall.getDamageMultiplier()-.5F)<.001,"Icarian Momentum cuts fall damage in half while active");
        wearer.setOnGround(true);ArtifactPassives.tick(wearer,ArtifactKind.ICARUS);
        h.assertTrue(!wearer.hasEffect(glide),"touchdown ends the airborne Curios passive promptly");
        wearer.setOnGround(false);ArtifactPassives.tick(wearer,ArtifactKind.ICARUS);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOW_FALLING,200,0,false,false,true));
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.ICARUS);
        h.assertTrue(!wearer.hasEffect(glide),"Icarian Momentum is removed when the Curios passive ends");
        h.assertTrue(wearer.hasEffect(net.minecraft.world.effect.MobEffects.SLOW_FALLING),"unequipping Icarus preserves Slow Falling supplied by another source");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_67") public static void icarusProtectedLandingConvertsIntoCooledWingRebound(GameTestHelper h){
        var wearer=player(h,ArtifactKind.ICARUS,"IcarusLanding");wearer.setYRot(0);wearer.setDeltaMovement(0,0,0);
        wearer.getPersistentData().putLong("specialpickaxesIcarianLandingReady",Long.MAX_VALUE);
        long worldTime=ArtifactState.now(wearer);wearer.tickCount=10_000;
        h.assertTrue(!IcarianMomentumEffect.convertLanding(wearer,4F),"short drops do not trigger a rebound");
        h.assertTrue(IcarianMomentumEffect.convertLanding(wearer,8F),"an eight-block protected landing becomes a forward glide kick");
        var first=wearer.getDeltaMovement();
        h.assertTrue(first.horizontalDistanceSqr()>.2D&&first.y>=.12D&&wearer.hasImpulse,"the landing adds forward and upward momentum");
        h.assertTrue(wearer.getPersistentData().getLong("specialpickaxesIcarianLandingReadyAtWorldTime")==worldTime+40,
                "the landing lockout uses persistent world time instead of the resettable entity clock");
        wearer.setDeltaMovement(0,0,0);wearer.tickCount=0;
        h.assertTrue(!IcarianMomentumEffect.convertLanding(wearer,12F)&&wearer.getDeltaMovement().horizontalDistanceSqr()==0D,
                "the two-second world-time cooldown prevents rebound chaining after a tick reset");
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
        h.assertTrue(ArtifactState.of(wearer,ArtifactKind.INTERREGNUM).getLong("stasisAuraScanAt")==ArtifactState.now(wearer)+20,"the Stasis ally search is throttled to once per twenty ticks");
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
    @GameTest(template="empty",batch="rework_each_66") public static void stasisWardEffectKeepsHalfSecondParticleCadence(GameTestHelper h){
        var ward=SpecialPickaxes.STASIS_WARD.get();
        h.assertTrue(ward.isDurationEffectTick(40,0)&&ward.isDurationEffectTick(30,0)
                &&!ward.isDurationEffectTick(39,0)&&!ward.isDurationEffectTick(29,0),
                "the animated Curios ward emits its three-fragment orbit only every ten ticks");
        finish(h,player(h,ArtifactKind.INTERREGNUM));
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
        ally.getFoodData().setFoodLevel(0);farAlly.getFoodData().setFoodLevel(0);enemy.getFoodData().setFoodLevel(0);
        for(var other:List.of(ally,farAlly,enemy))h.getLevel().addFreshEntity(other);
        ally.setHealth(ally.getHealth()-4F);
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        h.assertTrue(ArtifactState.of(wearer,ArtifactKind.WORLDLOOM).getLong("heartwoodAuraScanAt")==ArtifactState.now(wearer)+20,"the Heartwood ally search is throttled to once per twenty ticks");
        h.assertTrue(wearer.hasEffect(renewal)&&!wearer.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),"the Curios wearer receives Heartwood Renewal instead of generic Regeneration");
        h.assertTrue(ally.hasEffect(renewal)&&ally.getEffect(renewal).getDuration()==40,"nearby allied miners receive the short custom renewal lease");
        h.assertTrue(!farAlly.hasEffect(renewal)&&!enemy.hasEffect(renewal),"the effect does not reach distant allies or enemies");
        h.assertTrue(ally.getFoodData().getFoodLevel()==2,"Heartwood shares its first two-point food pulse with a nearby ally");
        h.assertTrue(farAlly.getFoodData().getFoodLevel()==0&&enemy.getFoodData().getFoodLevel()==0,"food support stays inside the allied aura");
        h.assertTrue(ArtifactState.of(ally,ArtifactKind.WORLDLOOM).getLong("rootSustainReady")==ArtifactState.now(ally)+100,
                "each supported ally gets an independent five-second food cooldown");
        float before=ally.getHealth();renewal.applyEffectTick(ally,0);
        h.assertTrue(Math.abs(ally.getHealth()-before-1F)<.001F,"the shared effect heals one health point per pulse");
        renewal.applyEffectTick(ally,0);h.assertTrue(Math.abs(ally.getHealth()-before-1F)<.001F,"refreshing the shared lease cannot accelerate its one-second heal");
        h.assertTrue(renewal.isDurationEffectTick(40,0)&&!renewal.isDurationEffectTick(39,0),"root healing remains spaced one second apart");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_54") public static void worldloomAllyAuraPreservesExternalHeartwoodEffect(GameTestHelper h){
        var wearer=player(h,ArtifactKind.WORLDLOOM,"HeartwoodBearer");var ally=player(h,ArtifactKind.CHOIR,"ProtectedAlly");
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("heartwood_"+wearer.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        ally.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(ally);
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(renewal,200,1,false,true,true));
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        var external=ally.getEffect(renewal);
        h.assertTrue(external!=null&&external.getDuration()==200&&external.getAmplifier()==1&&!external.isAmbient(),
                "Heartwood ally sharing does not replace a stronger effect supplied by the server");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_55") public static void curiosCombatEffectsPreserveExternalEffects(GameTestHelper h){
        var choir=player(h,ArtifactKind.CHOIR);var attacker=EntityType.ZOMBIE.create(h.getLevel());
        attacker.setPos(choir.getX()+2,choir.getY(),choir.getZ());h.getLevel().addFreshEntity(attacker);
        var resonance=SpecialPickaxes.FAULT_RESONANCE.get();
        attacker.addEffect(new net.minecraft.world.effect.MobEffectInstance(resonance,200,1,false,true,true));
        var choirHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(choir,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(choir,choirHit,ArtifactKind.CHOIR);
        var externalResonance=attacker.getEffect(resonance);
        h.assertTrue(externalResonance!=null&&externalResonance.getDuration()==200&&externalResonance.getAmplifier()==1&&!externalResonance.isAmbient(),
                "Choir's Curios counter does not replace an externally supplied stronger resonance");

        var worldbreaker=player(h,ArtifactKind.WORLDBREAKER);var secondAttacker=EntityType.ZOMBIE.create(h.getLevel());
        secondAttacker.setPos(worldbreaker.getX()+2,worldbreaker.getY(),worldbreaker.getZ());h.getLevel().addFreshEntity(secondAttacker);
        worldbreaker.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION,240,1,false,true,true));
        var breakerHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(worldbreaker,h.getLevel().damageSources().mobAttack(secondAttacker),10F);
        ArtifactPassives.onCuriosDamage(worldbreaker,breakerHit,ArtifactKind.WORLDBREAKER);
        var externalAbsorption=worldbreaker.getEffect(net.minecraft.world.effect.MobEffects.ABSORPTION);
        h.assertTrue(Math.abs(breakerHit.getAmount()-8F)<.001F,"Worldbreaker still reduces its triggering Curios hit");
        h.assertTrue(externalAbsorption!=null&&externalAbsorption.getDuration()==240&&externalAbsorption.getAmplifier()==1&&!externalAbsorption.isAmbient(),
                "Worldbreaker's Absorption II never shortens an externally supplied Absorption effect");
        finish(h,choir);
    }
    @GameTest(template="empty",batch="rework_each_23") public static void seamRipperMinesAnEasySameMaterialVein(GameTestHelper h) {
        var p=player(h,ArtifactKind.SEAM_RIPPER);var seed=target(h);var level=h.getLevel();
        var trail=new ArrayList<BlockPos>();for(int i=0;i<8;i++)trail.add(seed.east(i));
        h.assertTrue(ArtifactFeedback.seamTrail(p,trail)==4,"a vein pulse draws at most four recent adjacent links");
        h.assertTrue(ArtifactFeedback.seamTrail(p,List.of(seed,seed.offset(10,0,0)))==0,"the private trail skips distant, disconnected breaks");
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
        double normalSpeed=attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),8F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.CHOIR);
        var resonance=SpecialPickaxes.FAULT_RESONANCE.get();
        h.assertTrue(attacker.hasEffect(resonance)&&attacker.getEffect(resonance).getDuration()==60,"Choir's Curios counter applies its custom three-second status");
        h.assertTrue(Math.abs(attacker.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-normalSpeed*.75D)<.001D,"Fault Resonance reduces attacker movement speed by 25 percent");
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
    @GameTest(template="empty",batch="rework_each_25") public static void choirCuriosSharesMiningFocusOnlyWithNearbyTeammates(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR,"ChoirBearer");
        var ally=player(h,ArtifactKind.PALIMPSEST,"ChoirAlly");var enemy=player(h,ArtifactKind.PALIMPSEST,"ChoirEnemy");
        var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("choir_"+wearer.getUUID().toString().substring(0,6));
        var hostile=scoreboard.addPlayerTeam("foe_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(enemy.getScoreboardName(),hostile);
        ally.setPos(wearer.getX()+3,wearer.getY(),wearer.getZ());enemy.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());
        h.getLevel().addFreshEntity(ally);h.getLevel().addFreshEntity(enemy);
        ArtifactPassives.tick(wearer,ArtifactKind.CHOIR);
        var focus=SpecialPickaxes.STRATA_FOCUS.get();
        h.assertTrue(ally.hasEffect(focus)&&ally.getEffect(focus).getAmplifier()==0&&ally.getEffect(focus).getDuration()==40,
                "Choir lends its passive mining focus to a teammate within six blocks");
        h.assertTrue(!enemy.hasEffect(focus),"the mining rhythm does not leak to a nearby hostile player");
        var scanAt=ArtifactState.of(wearer,ArtifactKind.CHOIR).getLong("choirMiningAuraScanAt");
        ArtifactPassives.tick(wearer,ArtifactKind.CHOIR);
        h.assertTrue(ArtifactState.of(wearer,ArtifactKind.CHOIR).getLong("choirMiningAuraScanAt")==scanAt,
                "the nearby-player search is throttled to once every twenty ticks");
        var mined=h.absolutePos(new BlockPos(4,1,4));h.getLevel().setBlockAndUpdate(mined,Blocks.AIR.defaultBlockState());
        h.assertTrue(StrataFocusEffect.miningPulse(ally,mined)
                &&Math.abs(StrataFocusEffect.miningTempoMultiplier(ally)-1.08D)<.001D,
                "the ally builds a personal mining streak with the shared passive");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_62") public static void choirHeldHitUsesCustomFaultResonance(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR,"ChoirStrike");var target=EntityType.ZOMBIE.create(h.getLevel());
        target.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(target);
        var tool=wearer.getMainHandItem();
        h.assertTrue(((ArtifactItem)tool.getItem()).hurtEnemy(tool,target,wearer),"Choir's equipped pickaxe can trigger its strike effect");
        var resonance=SpecialPickaxes.FAULT_RESONANCE.get();
        h.assertTrue(target.hasEffect(resonance)&&target.getEffect(resonance).getAmplifier()==1
                &&target.getEffect(resonance).getDuration()==100,"the strike applies five seconds of stronger custom Fault Resonance");
        h.assertTrue(!target.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS)
                &&!target.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"the strike no longer falls back to generic Weakness and Slowness");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_64") public static void axiomHeldHitAppliesHealingNullMark(GameTestHelper h){
        var wearer=player(h,ArtifactKind.AXIOM,"AxiomStrike");var target=EntityType.ZOMBIE.create(h.getLevel());
        target.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(target);
        var tool=wearer.getMainHandItem();
        h.assertTrue(((ArtifactItem)tool.getItem()).hurtEnemy(tool,target,wearer),"Axiom's pickaxe can apply its melee signature");
        var mark=SpecialPickaxes.NULL_MARK.get();
        h.assertTrue(target.hasEffect(mark)&&target.getEffect(mark).getDuration()==100,"the strike applies five seconds of the custom healing lock");
        h.assertTrue(target.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING)&&!target.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS),"the hit keeps its visible mark without generic Weakness");
        h.assertTrue(MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingHealEvent(target,4F)),"Null Mark blocks recovery from potions or healing skills");
        h.assertTrue(!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingHealEvent(wearer,1F)),"the pickaxe wearer is not caught by its own mark");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_65") public static void interregnumHeldHitAppliesCustomStasisCrush(GameTestHelper h){
        var wearer=player(h,ArtifactKind.INTERREGNUM,"InterregnumStrike");var target=EntityType.ZOMBIE.create(h.getLevel());
        target.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(target);
        double speedBefore=target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        var tool=wearer.getMainHandItem();
        h.assertTrue(((ArtifactItem)tool.getItem()).hurtEnemy(tool,target,wearer),"Interregnum can apply its melee signature");
        var crush=SpecialPickaxes.STASIS_CRUSH.get();
        h.assertTrue(target.hasEffect(crush)&&target.getEffect(crush).getDuration()==160,"the hit applies eight seconds of custom Stasis Crush");
        h.assertTrue(target.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)<speedBefore*.36D
                &&!target.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN)
                &&!target.hasEffect(net.minecraft.world.effect.MobEffects.WEAKNESS),"Stasis Crush applies its strong movement cut without generic control effects");
        target.setDeltaMovement(1D,.25D,0D);target.tickCount=8;crush.applyEffectTick(target,0);
        h.assertTrue(Math.abs(target.getDeltaMovement().x-.38D)<.001D&&target.getDeltaMovement().y==.25D,"stasis damps horizontal escape while preserving vertical motion");
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
    @GameTest(template="empty",batch="rework_each_61") public static void eventideAlternateTethersLooseDropsToItsOwner(GameTestHelper h){
        var p=player(h,ArtifactKind.EVENTIDE,"EventideTether");var level=h.getLevel();
        var drop=new ItemEntity(level,p.getX()+5D,p.getY()+.5D,p.getZ(),new ItemStack(Items.AMETHYST_SHARD));
        drop.setDeltaMovement(Vec3.ZERO);level.addFreshEntity(drop);
        h.assertTrue(ArtifactSkills.use(p,p.getMainHandItem(),ArtifactKind.EVENTIDE),"Eventide's alternate catches nearby loose drops");
        h.assertTrue(drop.getDeltaMovement().x<-.5D,"the tether pulls its target toward the wearer");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_62") public static void eventideTetherUsesVisibleSpiralPath(GameTestHelper h){
        var from=new Vec3(0,0,0);var to=new Vec3(0,4,0);var middle=ArtifactFeedback.gravityTetherPoint(from,to,.5D);
        h.assertTrue(middle.distanceToSqr(from.lerp(to,.5D))>.02D&&middle.distanceToSqr(from.lerp(to,.5D))<.04D,
                "the gravity tether curls visibly around its pull path without drifting far from the target line");
        h.assertTrue(ArtifactFeedback.gravityTetherPoint(from,to,0D).distanceToSqr(from)<1.0E-8D
                        &&ArtifactFeedback.gravityTetherPoint(from,to,1D).distanceToSqr(to)<1.0E-8D,
                "the spiral converges cleanly at both tether endpoints");
        finish(h,player(h,ArtifactKind.EVENTIDE,"EventideArc"));
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void worldloomCuriosCounterPulsesHeartwoodRenewal(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.WORLDLOOM);wearer.setHealth(10F);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.WORLDLOOM);
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        var grasp=SpecialPickaxes.HEARTWOOD_GRASP.get();
        h.assertTrue(wearer.hasEffect(renewal),"Worldloom's Curios counter grants its custom restorative effect");
        h.assertTrue(Math.abs(hit.getAmount()-9F)<.001F&&attacker.hasEffect(grasp),"the counter softens one hostile hit and roots its attacker");
        h.assertTrue(wearer.getEffect(renewal).getDuration()==60,"heartwood renewal lasts three seconds");
        h.assertTrue(attacker.getEffect(grasp).getDuration()==60&&grasp.isDurationEffectTick(60,0),"Heartwood Grasp lasts three seconds and pulses while active");
        attacker.setDeltaMovement(1D,.25D,-.5D);grasp.applyEffectTick(attacker,0);
        h.assertTrue(Math.abs(attacker.getDeltaMovement().x-.58D)<.001D&&Math.abs(attacker.getDeltaMovement().z+.29D)<.001D
                &&Math.abs(attacker.getDeltaMovement().y-.25D)<.001D,"roots damp horizontal escape while preserving vertical movement");
        h.assertTrue(renewal.isDurationEffectTick(60,0)&&!renewal.isDurationEffectTick(59,0),"healing is delivered in deliberate one-second pulses");
        var pulseData=wearer.getPersistentData();long worldTime=wearer.level().getGameTime();
        pulseData.putLong("specialpickaxesHeartwoodPulseAt",Long.MAX_VALUE);
        wearer.tickCount=10_000;
        renewal.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==11F,"a renewal pulse restores one health point");
        h.assertTrue(pulseData.getLong("specialpickaxesHeartwoodPulseAtWorldTime")==worldTime+20,
                "the heal cooldown is persisted against world time, so reconnects or respawns cannot strand it");
        wearer.tickCount=0;renewal.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==11F,"repeated effect refreshes cannot accelerate healing after an entity tick reset");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_46") public static void worldloomCuriosRenewalEndsOnUnequipButPersistsWhenHeld(GameTestHelper h){
        var wearer=player(h,ArtifactKind.ICARUS);wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.WORLDLOOM);
        h.assertTrue(!wearer.hasEffect(renewal),"Worldloom's Curios healing lease ends when it leaves the slot");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"the shared night vision lease ends after removing the last pick source");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDLOOM).get()));
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,40,0,true,false,true));
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.WORLDLOOM);
        h.assertTrue(wearer.hasEffect(renewal),"held Worldloom keeps the matching healing passive");
        h.assertTrue(wearer.hasEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION),"another pick source preserves night vision");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_56",timeoutTicks=140) public static void worldloomCuriosFoodPulseIsSpacedFiveSeconds(GameTestHelper h){
        var wearer=player(h,ArtifactKind.WORLDLOOM);wearer.getFoodData().setFoodLevel(10);
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        h.assertTrue(wearer.getFoodData().getFoodLevel()==12,"Worldloom restores two food points on its first Curios pulse");
        ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
        h.assertTrue(wearer.getFoodData().getFoodLevel()==12,"repeated passive ticks cannot accelerate food restoration");
        h.runAfterDelay(100,()->{
            ArtifactPassives.tick(wearer,ArtifactKind.WORLDLOOM);
            h.assertTrue(wearer.getFoodData().getFoodLevel()==14,"the next food pulse arrives after five seconds");
            finish(h,wearer);
        });
    }
    @GameTest(template="empty",batch="rework_each_49") public static void pickaxeUnequipPreservesExternalNightVision(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION,6000,0,false,true,true));
        ArtifactPassives.tick(wearer,ArtifactKind.CHOIR);
        var external=wearer.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
        h.assertTrue(external!=null&&external.getDuration()==6000&&!external.isAmbient(),"a long server-owned Night Vision effect is not replaced by the pick passive");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.CHOIR);
        external=wearer.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION);
        h.assertTrue(external!=null&&external.getDuration()==6000&&!external.isAmbient(),"unequipping the last pick preserves Night Vision supplied elsewhere");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_50") public static void hellspecCuriosCinderRetortAnswersHostileHitsOncePerCooldown(GameTestHelper h){
        var wearer=player(h,ArtifactKind.HELLSPEC);var attacker=EntityType.ZOMBIE.create(h.getLevel());
        attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.HELLSPEC);
        h.assertTrue(Math.abs(hit.getAmount()-8F)<.001F&&attacker.getRemainingFireTicks()>=60,"Cinder Retort reduces the first hostile hit by 20 percent and ignites its attacker");
        attacker.clearFire();var repeat=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,repeat,ArtifactKind.HELLSPEC);
        h.assertTrue(repeat.getAmount()==10F&&!attacker.isOnFire(),"the eight-second cooldown prevents repeated reductions and ignitions");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"the passive never grants Strength");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_69") public static void hellspecMagmaWaveBrandsOnlyHostileTargets(GameTestHelper h){
        var wearer=player(h,ArtifactKind.HELLSPEC,"MagmaBearer");wearer.setYRot(0);
        var enemy=EntityType.ZOMBIE.create(h.getLevel());enemy.setPos(wearer.getX(),wearer.getY(),wearer.getZ()+4);h.getLevel().addFreshEntity(enemy);
        var ally=player(h,ArtifactKind.CHOIR,"MagmaAlly");ally.setPos(wearer.getX()+1,wearer.getY(),wearer.getZ()+4);
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("magma_"+wearer.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);h.getLevel().addFreshEntity(ally);
        float health=enemy.getHealth();
        h.assertTrue(ArtifactSkills.use(wearer,wearer.getMainHandItem(),ArtifactKind.HELLSPEC),"Magma Wave launches when a hostile target is in its forward fan");
        var brand=SpecialPickaxes.MAGMA_BRAND.get();
        h.assertTrue(enemy.getHealth()<=health-2.9F,"the wave deals its direct impact damage, subject to armor mitigation");
        h.assertTrue(enemy.hasEffect(brand),"the wave applies the custom Magma Brand");
        h.assertTrue(enemy.getEffect(brand).getDuration()>=59,"Magma Brand remains active for nearly its full three-second lease");
        h.assertTrue(enemy.isOnFire()&&enemy.getDeltaMovement().z>.5D&&enemy.hasImpulse,
                "the impact ignites and drives the target forward with the advancing wave");
        h.assertTrue(ally.getHealth()==ally.getMaxHealth()&&!ally.hasEffect(brand),"the wave ignores allied players");
        h.runAfterDelay(22,()->{
            float beforePulse=enemy.getHealth();brand.applyEffectTick(enemy,0);
            h.assertTrue(enemy.getHealth()<beforePulse,"Magma Brand deals its custom pulse separately from ordinary fire ticks");
            h.assertTrue(brand.isDurationEffectTick(60,0)&&!brand.isDurationEffectTick(59,0),"Magma Brand uses a one-second effect cadence");
            finish(h,wearer);
        });
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void hellspecSearingShellBlocksFireAndLava(GameTestHelper h) {
        var wearer=player(h,ArtifactKind.HELLSPEC);ArtifactPassives.tick(wearer,ArtifactKind.HELLSPEC);
        var shell=SpecialPickaxes.SEARING_SHELL.get();
        h.assertTrue(wearer.hasEffect(shell)&&wearer.getEffect(shell).getDuration()==40,"Hellspec grants a two-second fire passive lease while held or equipped in Curios");
        h.assertTrue(wearer.getEffect(net.minecraft.world.effect.MobEffects.NIGHT_VISION).getDuration()==40,"night vision also uses the short passive lease");
        h.assertTrue(shell.isDurationEffectTick(40,0)&&!shell.isDurationEffectTick(39,0),"the ember halo updates twice per second rather than every tick");
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
        ArtifactPassives.tick(wearer,ArtifactKind.HELLSPEC);
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.HELLSPEC);
        h.assertTrue(!wearer.hasEffect(shell),"Curios-only fire immunity ends when Hellspec leaves its slot");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.HELLSPEC).get()));
        ArtifactPassives.tick(wearer,ArtifactKind.HELLSPEC);
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
    @GameTest(template="empty",batch="rework_each_51") public static void crucibleCuriosCounterShearsAttackTempo(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CRUCIBLE);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),8F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.CRUCIBLE);
        var shear=SpecialPickaxes.PHASE_SHEAR.get();
        h.assertTrue(Math.abs(hit.getAmount()-6.8F)<.001F&&attacker.hasEffect(shear)&&attacker.getEffect(shear).getDuration()==40&&!attacker.isOnFire(),"Crucible's phase shift trims the first hit by 15% and applies its two-second Phase Shear without duplicating Hellspec's burn");
        var repeatHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),8F);
        ArtifactPassives.onCuriosDamage(wearer,repeatHit,ArtifactKind.CRUCIBLE);
        h.assertTrue(repeatHit.getAmount()==8F,"Crucible's 15% phase reduction and Phase Shear share one eight-second passive cooldown");
        var pvpTarget=player(h,ArtifactKind.PALIMPSEST,"PhaseShearTarget");
        double normalAttackSpeed=pvpTarget.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);
        double normalMovementSpeed=pvpTarget.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        pvpTarget.addEffect(new net.minecraft.world.effect.MobEffectInstance(shear,40,0,true,false,true));
        h.assertTrue(Math.abs(pvpTarget.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED)-normalAttackSpeed*.75D)<.001D,"Phase Shear slows player attack speed by 25 percent");
        h.assertTrue(Math.abs(pvpTarget.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED)-normalMovementSpeed*.85D)<.001D,"Phase Shear also slows escape movement by 15 percent");
        h.assertTrue(!pvpTarget.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"Phase Shear does not grant Strength");finish(h,wearer);
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
    @GameTest(template="empty",batch="rework_each_58") public static void worldbreakerCuriosAegisProtectsThreeNearestAllies(GameTestHelper h){
        var owner=player(h,ArtifactKind.WORLDBREAKER,"AegisBearer");
        var allies=List.of(player(h,ArtifactKind.CHOIR,"AegisOne"),player(h,ArtifactKind.ICARUS,"AegisTwo"),
                player(h,ArtifactKind.WORLDLOOM,"AegisThree"),player(h,ArtifactKind.PALIMPSEST,"AegisFour"));
        var distant=player(h,ArtifactKind.AXIOM,"AegisDistant");var enemy=player(h,ArtifactKind.SEAM_RIPPER,"AegisEnemy");
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("aegis_"+owner.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("foes_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(owner.getScoreboardName(),team);
        for(var ally:allies)scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(distant.getScoreboardName(),team);scoreboard.addPlayerToTeam(enemy.getScoreboardName(),foes);
        for(int i=0;i<allies.size();i++)allies.get(i).setPos(owner.getX()+i+1,owner.getY(),owner.getZ());
        distant.setPos(owner.getX()+6,owner.getY(),owner.getZ());enemy.setPos(owner.getX()+1,owner.getY(),owner.getZ());
        var participants=new ArrayList<ServerPlayer>();participants.add(owner);participants.addAll(allies);participants.add(distant);participants.add(enemy);
        for(var participant:participants)h.getLevel().addFreshEntity(participant);
        ArtifactPassives.tick(owner,ArtifactKind.WORLDBREAKER);var aegis=SpecialPickaxes.WORLDBREAKER_AEGIS.get();
        for(int i=0;i<3;i++)h.assertTrue(allies.get(i).hasEffect(aegis)
                &&Math.abs(allies.get(i).getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)-4D)<.01D,
                "one of the three closest allies receives the armor ward");
        h.assertTrue(!allies.get(3).hasEffect(aegis)&&!distant.hasEffect(aegis)&&!enemy.hasEffect(aegis),
                "the formation aura excludes the fourth, distant and hostile players");
        h.assertTrue(!allies.get(0).hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),
                "the owner's combat counter does not spread its absorption burst");finish(h,owner);
    }
    @GameTest(template="empty",batch="rework_each_38") public static void curiosStrataFocusReplacesHasteAndScalesMiningSpeed(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        ArtifactPassives.tick(wearer,ArtifactKind.PALIMPSEST);var focus=SpecialPickaxes.STRATA_FOCUS.get();
        h.assertTrue(wearer.hasEffect(focus)&&wearer.getEffect(focus).getAmplifier()==1,"Palimpsest grants the level-two custom mining passive");
        var minedBlock=target(h);var pulseData=wearer.getPersistentData();long worldTime=ArtifactState.now(wearer);
        pulseData.putLong("specialpickaxesStrataMiningPulseAt",Long.MAX_VALUE);wearer.tickCount=10_000;
        h.assertTrue(StrataFocusEffect.miningPulse(wearer,minedBlock),"Strata Focus flashes at a mined block");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"the escalating mining visuals never grant Strength");
        h.assertTrue(pulseData.getLong("specialpickaxesStrataMiningPulseAtWorldTime")==worldTime+6,
                "Strata's throttled mining animation is scheduled on world time");
        h.assertTrue(!StrataFocusEffect.miningPulse(wearer,minedBlock),"rapid mining cannot spam its feedback pulse");
        pulseData.putLong("specialpickaxesStrataMiningPulseAtWorldTime",worldTime-1);wearer.tickCount=0;
        h.assertTrue(StrataFocusEffect.miningPulse(wearer,minedBlock),"the mining pulse returns after its short throttle");
        h.assertTrue(Math.abs(StrataFocusEffect.miningTempoMultiplier(wearer)-1.24D)<.001D,
                "three consecutive mined blocks build the maximum 24% Strata tempo bonus");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED),"Strata Focus replaces generic Haste");
        var speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(wearer,Blocks.STONE.defaultBlockState(),64,target(h));
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(Math.abs(speed.getNewSpeed()-111.104F)<.03F,"Strata Focus II and the full tempo streak compound to a 1.736x mining-speed factor");
        pulseData.putLong("specialpickaxesStrataMiningStreakAt",worldTime-81);
        h.assertTrue(StrataFocusEffect.miningTempoMultiplier(wearer)==1D,"the mining tempo expires after four seconds without another block");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.PALIMPSEST);
        h.assertTrue(!wearer.hasEffect(focus),"Strata Focus clears when its last source is unequipped");
        ArtifactPassives.tick(wearer,ArtifactKind.CHOIR);
        h.assertTrue(wearer.hasEffect(focus)&&wearer.getEffect(focus).getAmplifier()==0,"Choir grants the level-one custom mining passive");
        speed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(wearer,Blocks.STONE.defaultBlockState(),64,target(h));
        MinecraftForge.EVENT_BUS.post(speed);h.assertTrue(Math.abs(speed.getNewSpeed()-76.8F)<.01F,"level one applies the 1.2x mining-speed factor after the streak expires");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"the passive never grants Strength");
        h.assertTrue(!focus.isDurationEffectTick(40,0)&&!focus.isDurationEffectTick(1,0),"Strata Focus does no periodic server-side aura work");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.CHOIR);h.assertTrue(!wearer.hasEffect(focus),"the custom effect does not linger after Curios removal");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_70") public static void curiosStrataTempoRequiresASuccessfulBreak(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);ArtifactPassives.tick(wearer,ArtifactKind.CHOIR);var pos=target(h);
        var stasis=SpecialPickaxes.STASIS_RESONANCE.get();wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(stasis,40,0,true,false,true));
        h.assertTrue(StrataFocusEffect.observeBreak(wearer,pos),"an uncanceled Strata mining attempt is observed");
        h.assertTrue(StrataFocusEffect.flushObservedBreaks()==0
                &&StrataFocusEffect.miningTempoMultiplier(wearer)==1D
                &&!wearer.getPersistentData().contains("specialpickaxesStasisResonancePulseAtWorldTime"),
                "an unbroken or subsequently canceled block grants neither tempo nor a Stasis mining pulse");
        h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        h.assertTrue(StrataFocusEffect.observeBreak(wearer,pos),"a real break is queued for end-of-tick confirmation");
        h.assertTrue(StrataFocusEffect.flushObservedBreaks()==1
                &&Math.abs(StrataFocusEffect.miningTempoMultiplier(wearer)-1.08D)<.001D
                &&wearer.getPersistentData().contains("specialpickaxesStasisResonancePulseAtWorldTime"),
                "a confirmed break grants the first Strata charge and its Stasis pulse");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_40") public static void palimpsestCuriosDawnMendingPulsesAndClears(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);wearer.setHealth(10F);
        long worldTime=ArtifactState.now(wearer);wearer.getPersistentData().putLong("specialpickaxesDawnMendingAt",Long.MAX_VALUE);wearer.tickCount=10_000;
        ArtifactPassives.tick(wearer,ArtifactKind.PALIMPSEST);var mending=SpecialPickaxes.DAWN_MENDING.get();
        h.assertTrue(wearer.hasEffect(mending)&&wearer.getEffect(mending).getDuration()==40,"Palimpsest grants its named Curios healing passive");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.REGENERATION),"Dawn Mending replaces generic Regeneration");
        h.assertTrue(wearer.hasEffect(SpecialPickaxes.STRATA_FOCUS.get())&&wearer.getEffect(SpecialPickaxes.STRATA_FOCUS.get()).getAmplifier()==1,"Palimpsest keeps Strata Focus II while healing");
        mending.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==11F,"a dawn pulse heals one point");
        h.assertTrue(wearer.getPersistentData().getLong("specialpickaxesDawnMendingAtWorldTime")==worldTime+40,
                "Dawn Mending's two-second healing cadence survives a player tick reset");
        mending.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==11F,"effect refreshes cannot speed up the pulse");
        wearer.getPersistentData().putLong("specialpickaxesDawnMendingAtWorldTime",worldTime-1);wearer.tickCount=0;
        mending.applyEffectTick(wearer,0);h.assertTrue(wearer.getHealth()==12F,"a second pulse is available when its world-time cooldown expires");
        h.assertTrue(mending.isDurationEffectTick(40,0)&&!mending.isDurationEffectTick(39,0),"the potion effect uses a two-second pulse cadence");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.PALIMPSEST);
        h.assertTrue(!wearer.hasEffect(mending),"Dawn Mending clears when Palimpsest leaves Curios");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_72") public static void dawnMendingKeepsItsPulseWhenAnotherEffectBlocksHealing(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);wearer.setHealth(10F);
        var mending=SpecialPickaxes.DAWN_MENDING.get();var data=wearer.getPersistentData();long now=ArtifactState.now(wearer);
        data.putLong("specialpickaxesDawnMendingAtWorldTime",now-1L);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.NULL_MARK.get(),100,0));
        mending.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==10F,"Null Mark blocks Dawn Mending without changing health");
        h.assertTrue(data.getLong("specialpickaxesDawnMendingAtWorldTime")==now-1L,"a vetoed heal does not consume Dawn Mending's cooldown");
        wearer.removeEffect(SpecialPickaxes.NULL_MARK.get());mending.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==11F&&data.getLong("specialpickaxesDawnMendingAtWorldTime")==now+40L,
                "the pulse heals and starts its cooldown as soon as healing is allowed");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_73") public static void heartwoodRenewalKeepsItsPulseWhenAnotherEffectBlocksHealing(GameTestHelper h){
        var wearer=player(h,ArtifactKind.WORLDLOOM);wearer.setHealth(10F);
        var renewal=SpecialPickaxes.HEARTWOOD_RENEWAL.get();var data=wearer.getPersistentData();long now=ArtifactState.now(wearer);
        data.putLong("specialpickaxesHeartwoodPulseAtWorldTime",now-1L);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.NULL_MARK.get(),100,0));
        renewal.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==10F,"Null Mark blocks Heartwood Renewal without changing health");
        h.assertTrue(data.getLong("specialpickaxesHeartwoodPulseAtWorldTime")==now-1L,"a vetoed heal does not consume Heartwood's pulse");
        wearer.removeEffect(SpecialPickaxes.NULL_MARK.get());renewal.applyEffectTick(wearer,0);
        h.assertTrue(wearer.getHealth()==11F&&data.getLong("specialpickaxesHeartwoodPulseAtWorldTime")==now+20L,
                "Heartwood heals and starts its one-second cadence once healing is allowed");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_53") public static void customCuriosLeasePreservesExternalEffectAndOnlyClearsItsOwn(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);var mending=SpecialPickaxes.DAWN_MENDING.get();
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(mending,20,0,false,true,true));
        ArtifactPassives.tick(wearer,ArtifactKind.PALIMPSEST);var external=wearer.getEffect(mending);
        h.assertTrue(external!=null&&external.getDuration()==20&&!external.isAmbient()&&external.isVisible(),"a short externally applied custom effect is not refreshed by the pick passive");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.PALIMPSEST);
        external=wearer.getEffect(mending);
        h.assertTrue(external!=null&&external.getDuration()==20&&!external.isAmbient(),"unequipping Curios preserves an external custom effect");
        wearer.removeEffect(mending);ArtifactPassives.tick(wearer,ArtifactKind.PALIMPSEST);var owned=wearer.getEffect(mending);
        h.assertTrue(owned!=null&&owned.getDuration()==40&&owned.isAmbient()&&!owned.isVisible(),"the pick still grants its own recognizable effect lease");
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.PALIMPSEST);
        h.assertTrue(!wearer.hasEffect(mending),"the effect lease is removed once its last pick source is unequipped");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_41") public static void crucibleWardDeflectsFastHostileProjectile(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CRUCIBLE);var archer=EntityType.SKELETON.create(h.getLevel());
        archer.setPos(wearer.getX()+6,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(archer);
        var arrow=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),archer);
        arrow.setPos(wearer.getX()+3,wearer.getY()+1,wearer.getZ());arrow.setDeltaMovement(-3D,0D,0D);h.getLevel().addFreshEntity(arrow);
        h.assertTrue(ArtifactSkills.use(wearer,wearer.getMainHandItem(),ArtifactKind.CRUCIBLE),"the eight-second Crucible projectile ward starts");
        ArtifactFeedback.crucibleWardOrbit(wearer,10);
        DomainFields.tick();
        h.assertTrue(arrow.getDeltaMovement().x>0D,"the ward redirects a fast hostile shot outward before impact");
        h.assertTrue(DomainFields.active(wearer),"the deflection does not consume or cancel the remaining ward");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_43") public static void exodiumCuriosStarfallWardSoftensProjectileDamage(GameTestHelper h){
        var wearer=player(h,ArtifactKind.EXODIUM);ArtifactPassives.tick(wearer,ArtifactKind.EXODIUM);
        var ward=SpecialPickaxes.STARFALL_WARD.get();
        h.assertTrue(wearer.hasEffect(ward),"Exodium Curios leases the named Starfall Ward effect");
        wearer.tickCount=1;
        var shooter=EntityType.SKELETON.create(h.getLevel());shooter.setPos(wearer.getX()+5,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(shooter);
        var impactShot=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        impactShot.setPos(wearer.getX()+1,wearer.getY()+.9D,wearer.getZ());h.getLevel().addFreshEntity(impactShot);
        var impactEvent=new net.minecraftforge.event.entity.ProjectileImpactEvent(impactShot,
                new net.minecraft.world.phys.EntityHitResult(wearer,wearer.position()));
        h.assertTrue(StarfallWardEffect.interceptImpact(impactEvent)
                        &&impactEvent.getImpactResult()==net.minecraftforge.event.entity.ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE
                        &&!impactShot.isAlive(),
                "Starfall cancels and catches a hostile projectile at exact player impact");
        ArtifactState.of(wearer,ArtifactKind.EXODIUM).putLong("starfallCatchReady",ArtifactState.now(wearer));
        var outgoing=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        outgoing.setPos(wearer.getX()-3,wearer.getY()+.9D,wearer.getZ());outgoing.setDeltaMovement(-1D,0D,0D);h.getLevel().addFreshEntity(outgoing);
        h.assertTrue(!StarfallWardEffect.tryCatchIncoming(wearer,h.getLevel())&&outgoing.isAlive(),"Starfall leaves projectiles travelling away from the wearer untouched");
        var incoming=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        incoming.setPos(wearer.getX()+9,wearer.getY()+.9D,wearer.getZ());incoming.setDeltaMovement(-16D,0D,0D);h.getLevel().addFreshEntity(incoming);
        h.assertTrue(StarfallWardEffect.tryCatchIncoming(wearer,h.getLevel())&&!incoming.isAlive(),"Starfall catches a high-speed hostile shot up to ten blocks along the wearer's path");
        var followup=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        followup.setPos(wearer.getX()+3,wearer.getY()+.9D,wearer.getZ());followup.setDeltaMovement(-1D,0D,0D);h.getLevel().addFreshEntity(followup);
        h.assertTrue(!StarfallWardEffect.tryCatchIncoming(wearer,h.getLevel())&&followup.isAlive(),"the 1.5-second catch cooldown prevents projectile spam");
        h.assertTrue(ArtifactState.of(wearer,ArtifactKind.EXODIUM).getLong("starfallCatchReady")==ArtifactState.now(wearer)+30,
                "a successful catch starts a thirty-tick recharge");
        var arrow=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        var projectileHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().arrow(arrow,shooter),12F);
        ArtifactPassives.onCuriosDamage(wearer,projectileHit,ArtifactKind.EXODIUM);
        h.assertTrue(Math.abs(projectileHit.getAmount()-9F)<.001F,"a shot that bypasses the catch still deals 25 percent less damage");
        var flashData=wearer.getPersistentData();long worldTime=ArtifactState.now(wearer);
        flashData.putLong("specialpickaxesStarfallFlashAt",Long.MAX_VALUE);StarfallWardEffect.projectileImpact(wearer);
        h.assertTrue(flashData.getLong("specialpickaxesStarfallFlashAtWorldTime")==worldTime+5,"the first projectile catch flashes even when it lands between five-tick boundaries");
        wearer.tickCount=0;StarfallWardEffect.projectileImpact(wearer);
        h.assertTrue(flashData.getLong("specialpickaxesStarfallFlashAtWorldTime")==worldTime+5,"repeated hits inside five ticks share one visual pulse after an entity tick reset");
        flashData.putLong("specialpickaxesStarfallFlashAtWorldTime",worldTime-1);StarfallWardEffect.projectileImpact(wearer);
        h.assertTrue(flashData.getLong("specialpickaxesStarfallFlashAtWorldTime")==worldTime+5,"the ward flashes again once its world-time visual cooldown expires");
        h.assertTrue(ward.isDurationEffectTick(39,0)&&ward.isDurationEffectTick(38,0)&&ward.isDurationEffectTick(37,0),"the ward checks trajectories every tick while its visual orbit stays separately throttled");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.EXODIUM);
        h.assertTrue(!wearer.hasEffect(ward),"Starfall Ward clears when Exodium leaves Curios and the hand");
        var unwardedHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().arrow(arrow,shooter),12F);
        ArtifactPassives.onCuriosDamage(wearer,unwardedHit,ArtifactKind.EXODIUM);
        h.assertTrue(unwardedHit.getAmount()==12F,"the projectile guard is not retained after unequipping");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_71",timeoutTicks=80) public static void exodiumStarfallCatchRechargesAfterThirtyTicks(GameTestHelper h){
        h.assertTrue(StarfallWardEffect.chargeProgress(100,130)==0D&&StarfallWardEffect.chargeProgress(115,130)==.5D
                &&StarfallWardEffect.chargeProgress(130,130)==1D,"the Starfall orbit visibly refills with its thirty-tick catch cooldown");
        var wearer=player(h,ArtifactKind.EXODIUM);var shooter=EntityType.SKELETON.create(h.getLevel());
        shooter.setPos(wearer.getX()+6,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(shooter);
        var first=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        first.setPos(wearer.getX()+9,wearer.getY()+.9D,wearer.getZ());first.setDeltaMovement(-1D,0D,0D);h.getLevel().addFreshEntity(first);
        h.assertTrue(StarfallWardEffect.tryCatchIncoming(wearer,h.getLevel())&&!first.isAlive(),"Starfall catches the first incoming shot");
        h.runAfterDelay(29,()->{
            var early=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
            early.setPos(wearer.getX()+9,wearer.getY()+.9D,wearer.getZ());early.setDeltaMovement(-1D,0D,0D);h.getLevel().addFreshEntity(early);
            h.assertTrue(!StarfallWardEffect.tryCatchIncoming(wearer,h.getLevel())&&early.isAlive(),"the ward remains charged until the full thirty ticks elapse");
            early.discard();
            h.runAfterDelay(1,()->{
                var ready=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
                ready.setPos(wearer.getX()+9,wearer.getY()+.9D,wearer.getZ());ready.setDeltaMovement(-1D,0D,0D);h.getLevel().addFreshEntity(ready);
                h.assertTrue(StarfallWardEffect.tryCatchIncoming(wearer,h.getLevel())&&!ready.isAlive(),"Starfall catches again as soon as the thirty-tick recharge completes");finish(h,wearer);
            });
        });
    }
    @GameTest(template="empty",batch="rework_each_59") public static void exodiumSharesStarfallGuardWithThreeNearbyAllies(GameTestHelper h){
        var owner=player(h,ArtifactKind.EXODIUM,"StarfallBearer");
        var allies=List.of(player(h,ArtifactKind.CHOIR,"StarfallOne"),player(h,ArtifactKind.ICARUS,"StarfallTwo"),
                player(h,ArtifactKind.WORLDLOOM,"StarfallThree"),player(h,ArtifactKind.PALIMPSEST,"StarfallFour"));
        var distant=player(h,ArtifactKind.AXIOM,"StarfallDistant");var enemy=player(h,ArtifactKind.SEAM_RIPPER,"StarfallEnemy");
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("starfall_"+owner.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("stfoes_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(owner.getScoreboardName(),team);
        for(var ally:allies)scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(distant.getScoreboardName(),team);scoreboard.addPlayerToTeam(enemy.getScoreboardName(),foes);
        for(int i=0;i<allies.size();i++)allies.get(i).setPos(owner.getX()+i+1,owner.getY(),owner.getZ());
        distant.setPos(owner.getX()+7,owner.getY(),owner.getZ());enemy.setPos(owner.getX()+1,owner.getY(),owner.getZ());
        var participants=new ArrayList<ServerPlayer>();participants.add(owner);participants.addAll(allies);participants.add(distant);participants.add(enemy);
        for(var participant:participants)h.getLevel().addFreshEntity(participant);
        ArtifactPassives.tick(owner,ArtifactKind.EXODIUM);var guard=SpecialPickaxes.STARFALL_GUARD.get();
        for(int i=0;i<3;i++)h.assertTrue(allies.get(i).hasEffect(guard),"one of the three closest teammates receives the projectile guard");
        h.assertTrue(!allies.get(3).hasEffect(guard)&&!distant.hasEffect(guard)&&!enemy.hasEffect(guard),
                "the aura excludes the fourth teammate, distant players and enemies");
        var shooter=EntityType.SKELETON.create(h.getLevel());h.getLevel().addFreshEntity(shooter);
        var arrow=new net.minecraft.world.entity.projectile.Arrow(h.getLevel(),shooter);
        var projectileHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(allies.get(0),h.getLevel().damageSources().arrow(arrow,shooter),10F);
        h.assertTrue(ArtifactPassives.onSharedStarfallGuard(allies.get(0),projectileHit),"the shared ward applies with no Curios relic equipped");
        h.assertTrue(Math.abs(projectileHit.getAmount()-7.5F)<.001F,"the shared passive reduces projectile damage by 25 percent");
        var meleeHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(allies.get(0),h.getLevel().damageSources().mobAttack(shooter),10F);
        h.assertTrue(!ArtifactPassives.onSharedStarfallGuard(allies.get(0),meleeHit),"the shared ward ignores melee damage");
        h.assertTrue(meleeHit.getAmount()==10F,"Starfall Guard does not reduce melee damage");
        var wearer=player(h,ArtifactKind.EXODIUM);wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(guard,40,0));
        ArtifactPassives.tick(wearer,ArtifactKind.EXODIUM);
        var oneReduction=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().arrow(arrow,shooter),10F);
        boolean wearerGuard=ArtifactPassives.onSharedStarfallGuard(wearer,oneReduction);
        ArtifactPassives.onCuriosDamage(wearer,oneReduction,ArtifactKind.EXODIUM,wearerGuard);
        h.assertTrue(Math.abs(oneReduction.getAmount()-7.5F)<.001F,"overlapping wearer and team wards never stack their projectile reduction");
        h.assertTrue(guard.isDurationEffectTick(40,0)&&!guard.isDurationEffectTick(39,0),"the shared effect uses a low-cost two-particle pulse every ten ticks");
        finish(h,owner);
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
    @GameTest(template="empty",batch="legacy_each_06") public static void axiomCuriosNullVeilCleansesOneDebuffPerPulse(GameTestHelper h){
        var wearer=player(h,ArtifactKind.AXIOM);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON,320,0));
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,300,0));
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.HUNGER,40,0));
        ArtifactPassives.tick(wearer,ArtifactKind.AXIOM);
        h.assertTrue(wearer.hasEffect(SpecialPickaxes.NULL_VEIL.get()),"Axiom grants its custom passive veil while carried in Curios");
        NullVeilEffect.pulse(wearer);
        h.assertTrue(!wearer.hasEffect(MobEffects.POISON)&&wearer.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)&&wearer.hasEffect(MobEffects.HUNGER),"lethal poison is prioritized over shorter control and hunger effects");
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON,200,0));NullVeilEffect.pulse(wearer);
        h.assertTrue(wearer.hasEffect(MobEffects.POISON),"the veil cannot cleanse another debuff before its eight-second cooldown");
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.AXIOM);
        h.assertTrue(!wearer.hasEffect(SpecialPickaxes.NULL_VEIL.get()),"the custom passive expires when Axiom leaves Curios and the hand");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void choirCuriosFaultCounterGuardsAndStaggers(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR);var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.CHOIR);
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F,"Choir's Curios counter reduces its triggering hostile hit by 15 percent");
        h.assertTrue(attacker.hasEffect(SpecialPickaxes.FAULT_RESONANCE.get()),"the counter staggers its attacker with the custom resonance effect");
        h.assertTrue(ArtifactState.of(wearer,ArtifactKind.CHOIR).getLong("curiosPassiveReady")==ArtifactState.now(wearer)+160,"defensive and retaliatory effects share one eight-second cooldown");
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
        convertible.put(center.offset(2,0,0),Blocks.POLISHED_GRANITE);
        convertible.put(center.offset(-2,0,0),Blocks.CUT_SANDSTONE);
        convertible.put(center.offset(0,0,2),Blocks.CLAY);
        for(var entry:convertible.entrySet()){
            level.setBlockAndUpdate(entry.getKey(),entry.getValue().defaultBlockState());
            PlayerPlacedBlocks.get(level).unmark(entry.getKey());
        }
        var chest=center.offset(4,0,0);var ore=center.offset(0,0,-2);var placed=center.offset(0,2,0);
        var bedrock=center.offset(0,-2,0);var fluid=center.offset(0,0,-3);
        level.setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
        level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        level.setBlockAndUpdate(placed,Blocks.CALCITE.defaultBlockState());PlayerPlacedBlocks.get(level).mark(placed);
        level.setBlockAndUpdate(bedrock,Blocks.BEDROCK.defaultBlockState());
        level.setBlockAndUpdate(fluid,Blocks.WATER.defaultBlockState());
        PlayerPlacedBlocks.get(level).unmark(ore);
        h.assertTrue(MiningDesigns.matrix(Blocks.POLISHED_GRANITE.defaultBlockState())
                &&MiningDesigns.matrix(Blocks.CUT_SANDSTONE.defaultBlockState())
                &&MiningDesigns.matrix(Blocks.CLAY.defaultBlockState()),"shared mining designs recognize polished rock, sandstone and clay as geology");
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",8);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,false),"calcite rephase activates");drain(p);
        for(var entry:convertible.entrySet())h.assertTrue(level.getBlockState(entry.getKey()).is(Blocks.CALCITE),entry.getValue()+" rephases to calcite");
        h.assertTrue(level.getBlockState(chest).is(Blocks.CHEST)&&level.getBlockState(ore).is(Blocks.DIAMOND_ORE),"containers and ores are preserved");
        h.assertTrue(level.getBlockState(placed).is(Blocks.CALCITE)&&level.getBlockState(bedrock).is(Blocks.BEDROCK)&&level.getBlockState(fluid).is(Blocks.WATER),"player-placed geology, bedrock and fluids are preserved");
        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(center).inflate(5)).isEmpty(),"rephasing never scatters drops");
        PlayerPlacedBlocks.get(level).unmark(placed);
        finish(h,p);
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
    @GameTest(template="empty",batch="rework_each_78") public static void stasisUpperMarkersMatchTheFourDomainCorners(GameTestHelper h){
        var center=new BlockPos(3,10,-4);int radius=8,halfHeight=6;var corners=new HashSet<Vec3>();
        for(int i=0;i<4;i++){
            var point=StasisBoundaryGeometry.upperCorner(center,radius,halfHeight,i);corners.add(point);
            h.assertTrue(point.y==center.getY()+halfHeight+1D,"corner markers sit on the domain's top edge");
            h.assertTrue(Math.abs(point.x-(center.getX()+.5D))==radius+.5D
                            &&Math.abs(point.z-(center.getZ()+.5D))==radius+.5D,
                    "each marker sits at a horizontal corner of the bounded field");
        }
        h.assertTrue(corners.size()==4,"all four upper corners get a distinct marker position");
        h.assertTrue(StasisBoundaryGeometry.upperCorner(center,radius,halfHeight,4)
                        .equals(StasisBoundaryGeometry.upperCorner(center,radius,halfHeight,0)),
                "corner indices wrap deterministically");
        var midpoints=new HashSet<Vec3>();
        for(int edge=0;edge<4;edge++){
            var point=StasisBoundaryGeometry.upperEdgeMidpoint(center,radius,halfHeight,edge);midpoints.add(point);
            h.assertTrue(point.y==center.getY()+halfHeight+1D,"edge ticks sit on the upper domain boundary");
            h.assertTrue((edge%2==0?Math.abs(point.z-(center.getZ()+.5D))==radius+.5D
                            :Math.abs(point.x-(center.getX()+.5D))==radius+.5D),
                    "each tick sits at the midpoint of one distinct horizontal edge");
        }
        h.assertTrue(midpoints.size()==4,"all four upper edges get their own marker");
        h.assertTrue(StasisBoundaryGeometry.upperEdgeMidpoint(center,radius,halfHeight,0).equals(
                        StasisBoundaryGeometry.upperEdgeMidpoint(center,radius,halfHeight,4)),
                "edge indices wrap deterministically");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_30") public static void movingStasisDomainReplacesItsOldBoundary(GameTestHelper h){
        var p=player(h,ArtifactKind.INTERREGNUM);var center=target(h);var oldEdge=center.west(8);var destination=center.east(5);
        h.assertTrue(DomainFields.start(p,p.getMainHandItem(),ArtifactKind.INTERREGNUM,center,8),"Stasis domain starts at its original center");
        h.assertTrue(DomainFields.contains(p,oldEdge),"the original field includes its outer edge");
        h.assertTrue(DomainFields.relocate(p,destination),"the active domain relocates");
        h.assertTrue(DomainFields.contains(p,destination)&&!DomainFields.contains(p,oldEdge),"the live influence follows the new center and drops the previous boundary");
        DomainFields.stop(p);finish(h,p);
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
        h.assertTrue(ally.hasEffect(SpecialPickaxes.STASIS_WARD.get())
                &&Math.abs(ally.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)-.15D)<.001D,
                "Stasis Ward shares its custom 15% knockback resistance with allied miners");
        h.assertTrue(ally.hasEffect(SpecialPickaxes.STASIS_RESONANCE.get())&&!ally.hasEffect(net.minecraft.world.effect.MobEffects.DIG_SPEED)
                &&ally.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION).getAmplifier()==1,"ally receives custom Stasis Resonance and Regeneration II instead of generic Haste");
        ally.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        var miningSpeed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(ally,Blocks.STONE.defaultBlockState(),64,target(h));
        MinecraftForge.EVENT_BUS.post(miningSpeed);
        h.assertTrue(Math.abs(miningSpeed.getNewSpeed()-102.4F)<.01F,"custom Stasis Resonance grants the same 1.6x mining speed with an ordinary correct pick");
        var miningFlash=target(h);
        var pulseData=ally.getPersistentData();long worldTime=ArtifactState.now(ally);
        pulseData.putLong("specialpickaxesStasisResonancePulseAt",Long.MAX_VALUE);ally.tickCount=10_000;
        h.assertTrue(StasisResonanceEffect.miningPulse(ally,miningFlash),"breaking a block creates a personal Stasis mining flash");
        h.assertTrue(pulseData.getLong("specialpickaxesStasisResonancePulseAtWorldTime")==worldTime+5,
                "the teammate mining animation uses the same persistent world clock");
        h.assertTrue(!StasisResonanceEffect.miningPulse(ally,miningFlash),"the visual pulse is throttled against particle spam");
        pulseData.putLong("specialpickaxesStasisResonancePulseAtWorldTime",worldTime-1);ally.tickCount=0;
        h.assertTrue(StasisResonanceEffect.miningPulse(ally,miningFlash),"the mining flash returns after its five-tick cooldown");
        h.assertTrue(!SpecialPickaxes.STASIS_RESONANCE.get().isDurationEffectTick(40,0),"the custom potion performs no idle tick animation work");
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
    @GameTest(template="empty",batch="legacy_each_12") public static void coreDrillFallProtectionContinuesUntilLanding(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);ArtifactState.of(p,ArtifactKind.WORLDBREAKER).putInt("mode",3);p.setOnGround(false);
        var origin=p.blockPosition().below();h.getLevel().setBlockAndUpdate(origin,Blocks.BEDROCK.defaultBlockState());
        h.assertTrue(WorkQueue.startRegion(p,p.getMainHandItem(),ArtifactKind.WORLDBREAKER,new DirectionalProgram(origin,Direction.DOWN,DirectionalProgram.Shape.CORE_DRILL)),"Core Drill job starts");
        h.assertTrue(WorkQueue.protectsFall(p),"Core Drill immediately protects its descent");drain(p);
        h.assertTrue(!WorkQueue.busy(p)&&WorkQueue.protectsFall(p),"protection remains after mining ends while the player is airborne");
        h.assertTrue(p.hasEffect(MobEffects.SLOW_FALLING),"Slow Falling remains refreshed for the descent");
        p.setOnGround(true);WorkQueue.tick();h.assertTrue(!WorkQueue.protectsFall(p),"landing releases the temporary fall guard");finish(h,p);
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
    @GameTest(template="empty",batch="rework_each_44") public static void icarianLiftStopsBeforeLavaAndLeavesItUntouched(GameTestHelper h){
        var p=player(h,ArtifactKind.ICARUS);var base=p.blockPosition();
        h.getLevel().setBlockAndUpdate(base.above(),Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(base.above(2),Blocks.STONE.defaultBlockState());
        var lava=base.above(3);h.getLevel().setBlockAndUpdate(lava,Blocks.LAVA.defaultBlockState());
        h.assertTrue(RelicControl.execute(p,ArtifactKind.ICARUS,RelicControl.Action.ALT_SKILL),"Icarian Lift starts below a lava barrier");drain(p);
        h.assertTrue(h.getLevel().getBlockState(lava).is(Blocks.LAVA),"the lift does not mine, drain or replace lava");
        h.assertTrue(p.blockPosition().getY()<=base.getY()+1,"the lift stops before moving the player through lava");
        h.assertTrue(!WorkQueue.busy(p),"the lava stop ends the queue and movement protection");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_45") public static void icarianLiftStopsBeforeForeignForgeFluid(GameTestHelper h){
        var p=player(h,ArtifactKind.ICARUS);var base=p.blockPosition();
        h.getLevel().setBlockAndUpdate(base.above(),Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(base.above(2),Blocks.STONE.defaultBlockState());
        var foreignFluid=base.above(3);h.getLevel().setBlockAndUpdate(foreignFluid,TestFluids.BLOCK.defaultBlockState());
        h.assertTrue(TestFluids.BLOCK.defaultBlockState().getFluidState().getFluidType()==TestFluids.TYPE,"fixture is a registered non-vanilla Forge fluid");
        h.assertTrue(RelicControl.execute(p,ArtifactKind.ICARUS,RelicControl.Action.ALT_SKILL),"Icarian Lift starts below a foreign fluid");drain(p);
        h.assertTrue(h.getLevel().getBlockState(foreignFluid).is(TestFluids.BLOCK),"the lift leaves a modded fluid such as diesel untouched");
        h.assertTrue(p.blockPosition().getY()<=base.getY()+1,"the lift stops before moving through the foreign fluid");
        h.assertTrue(!WorkQueue.busy(p),"the foreign-fluid stop ends the queue cleanly");finish(h,p);
    }
    @GameTest(template="empty",batch="legacy_each_13") public static void axiomPeelsConnectedMatrix(GameTestHelper h) {
        var p=player(h,ArtifactKind.AXIOM);var center=target(h);var matrix=center.offset(9,0,0);var ore=center.above();
        // GameTest reuses world coordinates; remove any stale provenance from an earlier fixture.
        var placed=PlayerPlacedBlocks.get(p.serverLevel());placed.unmark(center);placed.unmark(matrix);placed.unmark(ore);
        h.getLevel().setBlockAndUpdate(matrix,Blocks.STONE.defaultBlockState());h.getLevel().setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        var rayTarget=ArtifactActions.target(p);var seed=h.getLevel().getBlockState(center);
        boolean allowed=WorldSafety.allowed(p,ArtifactKind.AXIOM,center),harvestable=WorldSafety.harvestable(p,p.getMainHandItem(),center);
        boolean activated=ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.AXIOM,false);
        h.assertTrue(activated,"axiom activation (ray="+rayTarget+", center="+center+", allowed="+allowed+", matrix="+MiningDesigns.matrix(seed)+", ore="+ArtifactOres.isOre(seed)+", placed="+PlayerPlacedBlocks.get(p.serverLevel()).contains(center)+", harvestable="+harvestable+")");for(int tick=0;tick<2000&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(h.getLevel().getBlockState(matrix).isAir(),"matrix removed");
        h.assertTrue(h.getLevel().getBlockState(center).is(Blocks.STONE) && h.getLevel().getBlockState(ore).is(Blocks.DIAMOND_ORE),"hollow core and ore preserved");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_80") public static void axiomNullcutFeedbackFlowsTowardItsPreservedCore(GameTestHelper h){
        var shell=new net.minecraft.core.BlockPos(6,8,-2);var core=new net.minecraft.core.BlockPos(1,2,3);
        var inward=ArtifactFeedback.axiomNullcutDirection(shell,core);
        var toward=Vec3.atCenterOf(core).subtract(Vec3.atCenterOf(shell));
        h.assertTrue(Math.abs(inward.lengthSqr()-1D)<1.0E-6D&&inward.dot(toward)>0D,
                "Nullcut's sculk streak points from the mined shell toward the preserved core");
        h.assertTrue(ArtifactFeedback.axiomNullcutDirection(core,core).equals(new Vec3(0,1,0)),
                "the visual pulse remains finite if its source coincides with the core");
        h.succeed();
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
        ArtifactPassives.tick(p,ArtifactKind.IRIDIUM);
        var lodeSurge=SpecialPickaxes.LODE_SURGE.get();
        var ore=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(pos,ore);
        var unrelated=new ItemEntity(h.getLevel(),pos.getX()+.3,pos.getY()+.2,pos.getZ()+.3,new ItemStack(Items.ROTTEN_FLESH));
        h.getLevel().addFreshEntity(unrelated);var curioIridium=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.IRIDIUM).get());
        h.assertTrue(MiningObservations.captureCuriosIridium(p,curioIridium,pos,ore),"Curios Iridium observes an ore break while Choir is held");
        h.assertTrue(!MiningObservations.captureCuriosIridium(p,p.getMainHandItem(),pos,ore),"held Iridium does not create a duplicate Curios observation");
        h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        var drop=new ItemEntity(h.getLevel(),pos.getX()+.5,pos.getY()+.25,pos.getZ()+.5,new ItemStack(Items.DIAMOND,2));h.getLevel().addFreshEntity(drop);
        MiningObservations.flush();
        h.assertTrue(p.hasEffect(lodeSurge)&&p.getEffect(lodeSurge).getAmplifier()==0&&p.getEffect(lodeSurge).getDuration()==80,
                "the first Curios ore break starts a four-second Lode Surge");
        h.assertTrue(drop.isCurrentlyGlowing()&&drop.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop"),"new ore loot gets Iridium's glow tag");
        h.assertTrue(drop.getPersistentData().hasUUID("specialpickaxesIridiumOwner")&&drop.getPersistentData().getUUID("specialpickaxesIridiumOwner").equals(p.getUUID()),"only the wearer owns the recalled drop");
        h.assertTrue(!OreDropHighlight.mark(drop,p),"a repeated observation does not replay the sparkle burst");
        h.assertTrue(!unrelated.isCurrentlyGlowing()&&!unrelated.getPersistentData().contains("specialpickaxesIridiumOreDrop"),"a nearby pre-existing item is not marked");
        var moddedPos=pos.east(3);var moddedOre=TestOres.TAGGED_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(moddedPos,moddedOre);
        h.assertTrue(MiningObservations.captureCuriosIridium(p,curioIridium,moddedPos,moddedOre),"Iridium observes a foreign ore identified only by its modded ore-family tag");
        h.getLevel().setBlockAndUpdate(moddedPos,Blocks.AIR.defaultBlockState());
        var moddedDrop=new ItemEntity(h.getLevel(),moddedPos.getX()+.5,moddedPos.getY()+.25,moddedPos.getZ()+.5,new ItemStack(Items.EMERALD));h.getLevel().addFreshEntity(moddedDrop);MiningObservations.flush();
        h.assertTrue(p.getEffect(lodeSurge).getAmplifier()==1,"a second foreign ore break builds the second speed charge");
        h.assertTrue(moddedDrop.isCurrentlyGlowing()&&moddedDrop.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop"),"tagged foreign ore drops receive the same Iridium highlight");
        var queuedPos=pos.east(10);var queuedOre=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(queuedPos,queuedOre);
        var capturedDuringJob=new boolean[1];java.util.function.Consumer<net.minecraftforge.event.level.BlockEvent.BreakEvent> observeQueuedOre=event->{
            if(event.getPlayer()==p&&event.getPos().equals(queuedPos))capturedDuringJob[0]=WorkQueue.running()
                    &&MiningObservations.captureCuriosIridium(p,curioIridium,queuedPos,queuedOre);
        };
        MinecraftForge.EVENT_BUS.addListener(observeQueuedOre);
        try{h.assertTrue(WorkQueue.start(p,p.getMainHandItem(),ArtifactKind.CHOIR,List.of(new WorkStep.Mine(queuedPos,queuedOre))),"Choir queues an ore break while Curios Iridium is equipped");WorkQueue.tick();}
        finally{MinecraftForge.EVENT_BUS.unregister(observeQueuedOre);}
        h.assertTrue(capturedDuringJob[0]&&h.getLevel().getBlockState(queuedPos).isAir(),"Curios Iridium observes ore breaks inside another pickaxe's queued ability");
        StrataFocusEffect.flushObservedBreaks();
        var queuedDrop=new ItemEntity(h.getLevel(),queuedPos.getX()+.5,queuedPos.getY()+.25,queuedPos.getZ()+.5,new ItemStack(Items.EMERALD));h.getLevel().addFreshEntity(queuedDrop);MiningObservations.flush();
        h.assertTrue(p.getEffect(lodeSurge).getAmplifier()==2,"a queued third ore break reaches but cannot exceed three charges");
        var miningSpeed=new net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed(p,Blocks.STONE.defaultBlockState(),64F,pos);
        MinecraftForge.EVENT_BUS.post(miningSpeed);
        h.assertTrue(Math.abs(miningSpeed.getNewSpeed()-131.60448F)<.03F,
                "three Lode Surge charges and the first Strata tempo charge compound on Strata Focus II");
        h.assertTrue(lodeSurge.isDurationEffectTick(72,2)&&lodeSurge.isDurationEffectTick(68,2)
                        &&!lodeSurge.isDurationEffectTick(71,2),"the peak nourishment checks every four ticks without increasing the twelve-tick orbit cadence");
        p.getFoodData().setFoodLevel(10);lodeSurge.applyEffectTick(p,2);
        h.assertTrue(p.getFoodData().getFoodLevel()==11,"a full Curios Lode Surge restores one food point");
        lodeSurge.applyEffectTick(p,2);
        h.assertTrue(p.getFoodData().getFoodLevel()==11,"peak nourishment cannot pulse again during the same tick");
        lodeSurge.applyEffectTick(p,2);
        h.assertTrue(queuedDrop.isCurrentlyGlowing()&&queuedDrop.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop"),"queued ability ore drops receive the Curios highlight without held-tool mining effects");
        h.assertTrue(p.getMainHandItem().getItem() instanceof ArtifactItem held&&held.kind==ArtifactKind.CHOIR,"Curios ore tracking leaves the held pickaxe unchanged");finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_60") public static void iridiumRecallReachesThirtyTwoBlocksAndOnlyPullsOwnedOreDrops(GameTestHelper h){
        var wearer=player(h,ArtifactKind.IRIDIUM,"IridiumRecall");var level=h.getLevel();var origin=wearer.position();
        var near=new ItemEntity(level,origin.x+10D,origin.y+.5D,origin.z,new ItemStack(Items.DIAMOND));
        near.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);near.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        var beyond=new ItemEntity(level,origin.x+34D,origin.y+.5D,origin.z,new ItemStack(Items.DIAMOND));
        beyond.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);beyond.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        var unrelated=new ItemEntity(level,origin.x+8D,origin.y+.5D,origin.z,new ItemStack(Items.ROTTEN_FLESH));
        unrelated.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
        unrelated.getPersistentData().putUUID("specialpickaxesIridiumOwner",UUID.randomUUID());
        level.addFreshEntity(near);level.addFreshEntity(unrelated);
        var center=wearer.getEyePosition();
        h.assertTrue(ArtifactTechniques.isIridiumRecallTarget(wearer,near,center,32D),"an owned ore drop inside 32 blocks is eligible");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,beyond,center,32D),"an owned ore drop outside 32 blocks is excluded");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,unrelated,center,32D),"another player's ore drop is excluded");
        h.assertTrue(ArtifactSkills.use(wearer,wearer.getMainHandItem(),ArtifactKind.IRIDIUM),"Iridium recall activates without requiring a target block");
        h.assertTrue(near.getDeltaMovement().x<-.5D,"an owned ore drop in the active test zone accelerates to the wearer");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,beyond,wearer.getEyePosition(),32D),"the 32-block exclusion remains in force after recalling nearby loot");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,unrelated,wearer.getEyePosition(),32D),"a nearby drop owned by someone else remains excluded after recall");finish(h,wearer);
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
    @GameTest(template="empty",batch="rework_each_68") public static void everyArtifactOutclassesExodiumAndKeepsFastMultiToolHarvest(GameTestHelper h){
        h.assertTrue(SpecialPickaxes.TIER.getLevel()>4,"the artifact tier exceeds Terramity Exodium's tier IV");
        for(var kind:ArtifactKind.playableValues()){
            var tool=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());
            h.assertTrue(tool.isCorrectToolForDrops(Blocks.DEEPSLATE.defaultBlockState())
                    &&tool.isCorrectToolForDrops(Blocks.DIAMOND_ORE.defaultBlockState()),kind.id+" harvests stone and ore at tier V");
            h.assertTrue(tool.getDestroySpeed(Blocks.DEEPSLATE.defaultBlockState())>=64F,kind.id+" keeps OP mining speed");
        }
        finish(h,player(h,ArtifactKind.WORLDBREAKER));
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
        }
        var copied=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get());
        copied.getOrCreateTag().putString("copiedSkill",ArtifactKind.CRUCIBLE.id);copied.getOrCreateTag().putInt("copiedMode",8);
        var copiedLines=new ArrayList<net.minecraft.network.chat.Component>();ArtifactTooltips.compact(copied,ArtifactKind.WORLDBREAKER,copiedLines);
        h.assertTrue(copiedLines.size()==3&&ArtifactTooltips.copyModeKey(copied).equals("calcite"),"Worldbreaker tooltip identifies the copied Crucible and its current Calcite mode");
        h.succeed();
    }
    @GameTest(template="empty",batch="legacy_each_36") public static void pickaxeSoundsUseFiveBlockPlayerCenteredRadius(GameTestHelper h){
        var player=Vec3.ZERO;var forwardedSound=player.add(0,1.6D,2D);
        h.assertTrue(ArtifactConfig.SOUND_RADIUS.get()==5&&ArtifactConfig.SOUND_COOLDOWN.get()>=20,
                "sound configuration cannot exceed five blocks or play more often than once per second");
        h.assertTrue(ArtifactFeedback.withinSoundRadius(player,new Vec3(0,0,5),5D),"a listener at five blocks remains in range");
        h.assertTrue(!ArtifactFeedback.withinSoundRadius(player,new Vec3(0,0,6),5D)
                &&forwardedSound.distanceTo(new Vec3(0,0,6))<5D,"the forward sound offset cannot extend the five-block listener radius");
        h.succeed();
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
    @GameTest(template="empty",batch="rework_each_35",timeoutTicks=120) public static void worldloomRootsHoldHostilesThenReleaseCleanly(GameTestHelper h){
        var owner=player(h,ArtifactKind.WORLDLOOM);
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setNoAi(true);mob.setPos(owner.getX()+2,owner.getY(),owner.getZ());h.getLevel().addFreshEntity(mob);
        var anchor=mob.position();
        h.assertTrue(ArtifactActions.useAlternate(owner,owner.getMainHandItem(),ArtifactKind.WORLDLOOM),"Worldloom alternate catches a nearby hostile");
        mob.setPos(anchor.add(1,0,0));mob.setDeltaMovement(.8,.2,0);WorldloomSnare.tick();
        h.assertTrue(mob.position().distanceToSqr(anchor)>.5D&&mob.getDeltaMovement().lengthSqr()>.001D,"roots mark the target without immediately freezing it");
        h.runAfterDelay(60,()->{
            WorldloomSnare.tick();
            h.assertTrue(mob.getDeltaMovement().lengthSqr()>.001D,"the target can still move after three seconds of telegraph");
            h.runAfterDelay(20,()->{
                WorldloomSnare.tick();
                var caughtPosition=mob.position();mob.setPos(caughtPosition.add(1,0,0));mob.setDeltaMovement(.8,.2,0);WorldloomSnare.tick();
                h.assertTrue(mob.position().distanceToSqr(caughtPosition)<.001D&&mob.getDeltaMovement().lengthSqr()<.001D,"closing roots pin the hostile after four seconds for four seconds");
                WorldloomSnare.forget(owner);mob.setDeltaMovement(.4,0,0);WorldloomSnare.tick();
                h.assertTrue(Math.abs(mob.getDeltaMovement().x-.4D)<.001D,"cancel releases the target without leaving a movement lock");
                finish(h,owner);
            });
        });
    }
    @GameTest(template="empty",batch="rework_each_57") public static void worldloomRootVisualPulsesAreEvenlyStaggered(GameTestHelper h){
        int[] pulsesByTick=new int[10];
        for(int entityId=0;entityId<40;entityId++)for(int tick=0;tick<10;tick++)
            if(WorldloomSnare.visualPulseAt(tick,entityId))pulsesByTick[tick]++;
        for(int tick=0;tick<pulsesByTick.length;tick++)
            h.assertTrue(pulsesByTick[tick]==4,"40 simultaneous roots spread into four visual pulses per tick; tick "+tick);
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_57") public static void worldloomCatchVinesFormFourColumnsAroundTheTarget(GameTestHelper h){
        var anchor=new net.minecraft.world.phys.Vec3(4.25D,12D,-3.5D);
        for(int i=0;i<4;i++){
            var point=WorldloomSnare.catchVinePoint(anchor,i);
            h.assertTrue(Math.abs(point.y-(anchor.y+.58D))<.001D,"catch vine rises from the target's feet");
            h.assertTrue(Math.abs(point.distanceToSqr(anchor)-(.34D*.34D+.58D*.58D))<.001D,
                    "four catch columns keep a consistent radius and height");
        }
        var first=WorldloomSnare.catchVinePoint(anchor,0);var opposite=WorldloomSnare.catchVinePoint(anchor,2);
        h.assertTrue(Math.abs(first.x-opposite.x)>.67D&&Math.abs(first.z-opposite.z)<.001D,
                "opposing vines visibly enclose the target instead of stacking at one point");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_76") public static void worldloomCuriosAuraClimbsAsACompactVineSpiral(GameTestHelper h){
        var origin=new net.minecraft.world.phys.Vec3(2D,5D,-3D);double lowest=Double.MAX_VALUE,highest=Double.MIN_VALUE;
        for(int i=0;i<4;i++){
            var point=CurioAuraGeometry.worldloomVinePoint(origin,1.2D,i);double horizontal=Math.hypot(point.x-origin.x,point.z-origin.z);
            lowest=Math.min(lowest,point.y-origin.y);highest=Math.max(highest,point.y-origin.y);
            h.assertTrue(horizontal>=.12D&&horizontal<=.29D,"Worldloom's Curios leaves stay close to the wearer");
        }
        h.assertTrue(highest-lowest>.45D&&highest-lowest<.9D,"the passive reads as a rising spiral rather than a flat orbit");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_78") public static void icarusCuriosAuraFormsMirroredAnimatedWings(GameTestHelper h){
        var origin=new Vec3(2D,5D,-3D);double widest=0D;
        for(int feather=0;feather<4;feather++){
            var left=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,feather);
            var right=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,feather+4);
            h.assertTrue(Math.abs((left.x-origin.x)+(right.x-origin.x))<1.0E-6D
                            &&Math.abs(left.z-right.z)<1.0E-6D&&Math.abs(left.y-right.y)<1.0E-6D,
                    "left and right Icarus feathers mirror across the wearer");
            h.assertTrue(left.z<origin.z,"Icarus feathers trail behind the wearer's facing direction");
            double span=Math.abs(left.x-origin.x);
            h.assertTrue(span>widest,"outer feathers widen the wing silhouette");widest=span;
        }
        var flapped=CurioAuraGeometry.icarusFeatherPoint(origin,0D,Math.PI/2D,0);
        h.assertTrue(Math.abs(flapped.y-CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,0).y)>.03D,
                "the Icarus wing points change height through the flap cycle");
        h.assertTrue(CurioAuraGeometry.icarusFeatherPoint(origin,Math.PI/2D,0D,0).x>origin.x,
                "the wings rotate with the wearer's facing direction");
        var folded=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,3,false);
        var spread=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,3,true);
        h.assertTrue(Math.abs(folded.x-origin.x)<Math.abs(spread.x-origin.x),
                "Icarus' wings stay folded at rest and spread when soaring");
        var foldedFlap=CurioAuraGeometry.icarusFeatherPoint(origin,0D,Math.PI/2D,2,false);
        var soaringFlap=CurioAuraGeometry.icarusFeatherPoint(origin,0D,Math.PI/2D,2,true);
        h.assertTrue(Math.abs(soaringFlap.y-CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,2,true).y)>
                        Math.abs(foldedFlap.y-CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,2,false).y),
                "the soaring wing animation has a stronger flap than the folded idle aura");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_77") public static void curiosWardAuraTargetsOnlyThreeNearbyObservers(GameTestHelper h){
        var owner=player(h,ArtifactKind.INTERREGNUM,"WardOwner");var level=h.getLevel();var observers=new ArrayList<ServerPlayer>();
        for(int i=1;i<=5;i++){
            var observer=player(h,ArtifactKind.WORLDBREAKER,"WardViewer"+i);
            observer.setPos(owner.getX()+i,owner.getY(),owner.getZ());level.addFreshEntity(observer);observers.add(observer);
        }
        var audience=ArtifactFeedback.nearbyWardAudience(owner);
        h.assertTrue(audience.size()==4&&audience.get(0)==owner,"ward auras include their bearer and cap observers at three");
        for(int i=0;i<3;i++)h.assertTrue(audience.contains(observers.get(i)),"the three nearest observers see the passive ward");
        h.assertTrue(!audience.contains(observers.get(3))&&!audience.contains(observers.get(4)),"observers outside the three nearest do not receive repeated aura packets");
        finish(h,owner);
    }
    @GameTest(template="empty",batch="rework_each_78") public static void iridiumOrefallHonorsConfiguredScanBudgetWithExtremeEfficiency(GameTestHelper h){
        var wearer=player(h,ArtifactKind.IRIDIUM,"BudgetIridium");var tool=wearer.getMainHandItem();var center=target(h);
        var enchantments=new net.minecraft.nbt.ListTag();var efficiency=new net.minecraft.nbt.CompoundTag();
        efficiency.putString("id","minecraft:efficiency");efficiency.putInt("lvl",100000);enchantments.add(efficiency);tool.getOrCreateTag().put("Enchantments",enchantments);
        int configured=ArtifactConfig.ORE_SCAN_BUDGET.get();ArtifactConfig.ORE_SCAN_BUDGET.set(5);
        try{
            var program=new OrefallProgram(center);
            h.assertTrue(program.attemptsPerTick(tool)==5,"scan workload follows the server budget instead of scaling with Efficiency");
            h.assertTrue(WorkQueue.startRegion(wearer,tool,ArtifactKind.IRIDIUM,program),"Iridium starts its bounded Orefall scan");
            WorkQueue.tick();
            h.assertTrue(WorkQueue.lastAttempts()==5,"one server tick inspects no more than five scan positions");
        }finally{ArtifactConfig.ORE_SCAN_BUDGET.set(configured);finish(h,wearer);}
    }
    @GameTest(template="empty",batch="rework_each_79") public static void iridiumOrefallMinesNearbyVanillaAndForeignOresButStopsAtBedrock(GameTestHelper h){
        var wearer=player(h,ArtifactKind.IRIDIUM,"OrefallTester");var level=h.getLevel();var center=target(h);var foreign=center.east();var barrier=center.above();
        PlayerPlacedBlocks.get(level).unmark(center);PlayerPlacedBlocks.get(level).unmark(foreign);PlayerPlacedBlocks.get(level).unmark(barrier);
        level.setBlockAndUpdate(center,Blocks.DIAMOND_ORE.defaultBlockState());
        level.setBlockAndUpdate(foreign,TestOres.NAMED_ORE.defaultBlockState());
        level.setBlockAndUpdate(barrier,Blocks.BEDROCK.defaultBlockState());
        int configured=ArtifactConfig.ORE_SCAN_BUDGET.get();ArtifactConfig.ORE_SCAN_BUDGET.set(32);
        try{
            h.assertTrue(WorkQueue.startRegion(wearer,wearer.getMainHandItem(),ArtifactKind.IRIDIUM,new OrefallProgram(center)),
                    "Iridium starts a center-out ore scan");
            WorkQueue.tick();
            h.assertTrue(level.getBlockState(center).isAir()&&level.getBlockState(foreign).isAir(),
                    "one bounded pulse harvests nearby vanilla and registry-named modded ores");
            h.assertTrue(level.getBlockState(barrier).is(Blocks.BEDROCK)&&WorkQueue.succeeded(wearer)==2,
                    "Orefall ignores bedrock while counting only successful ore breaks");
        }finally{ArtifactConfig.ORE_SCAN_BUDGET.set(configured);finish(h,wearer);}
    }
}
