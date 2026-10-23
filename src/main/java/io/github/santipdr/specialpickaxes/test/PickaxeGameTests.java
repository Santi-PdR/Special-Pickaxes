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
    @GameTest(template="empty",batch="rework_each_84") public static void curiosAttunementHelixStaysTightAndDistinct(GameTestHelper h){
        var origin=new net.minecraft.world.phys.Vec3(3D,20D,-4D);var first=ArtifactFeedback.curiosAttunementPoint(origin,0,120L);
        boolean distinct=true;
        for(int i=0;i<12;i++){
            var point=ArtifactFeedback.curiosAttunementPoint(origin,i,120L);
            double radius=Math.sqrt(Math.pow(point.x-origin.x,2D)+Math.pow(point.z-origin.z,2D));
            h.assertTrue(radius>=.17D&&radius<=.47D&&point.y>=origin.y&&point.y<=origin.y+.92D,
                    "Curios attunement sample stays within its compact personal helix");
            if(i>0&&point.distanceToSqr(first)<1.0E-6D)distinct=false;
        }
        h.assertTrue(distinct,"all twelve equip cue samples occupy distinct positions");h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_74") public static void curiosPassiveLookupsAreEvenlySharded(GameTestHelper h){
        int[] buckets=new int[10];
        for(int i=0;i<1000;i++)buckets[SpecialPickaxes.curiosPassiveBucket(new UUID(0L,i))]++;
        h.assertTrue(Arrays.stream(buckets).allMatch(count->count==100),"Curios scans distribute evenly across all ten ticks");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_75") public static void cosmeticCuriosAuraRespectsParticleQualityCadence(GameTestHelper h){
        h.assertTrue(CurioAuraSchedule.due(40,Integer.MIN_VALUE),"the first nearby aura scan starts immediately");
        h.assertTrue(!CurioAuraSchedule.due(47,40)&&CurioAuraSchedule.due(48,40),"the visual scan refreshes every eight ticks");
        h.assertTrue(CurioAuraSchedule.due(1,200),"a fresh client level or wrapped tick counter refreshes immediately");
        h.assertTrue(CurioAuraSchedule.PARTICLE_INTERVAL_TICKS==5
                        &&CurioAuraSchedule.particleDue(40)&&!CurioAuraSchedule.particleDue(44)
                        &&CurioAuraSchedule.particleDue(45),
                "passive signatures emit smoothly at four updates per second");
        h.assertTrue(CurioAuraSchedule.particleDue(40,5)&&!CurioAuraSchedule.particleDue(45,10)
                        &&CurioAuraSchedule.particleDue(50,10)&&!CurioAuraSchedule.particleDue(65,20)
                        &&CurioAuraSchedule.particleDue(80,20),
                "the reduced and minimal particle settings use progressively slower aura cadences");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_85") public static void relicEffectsShareOneWholeBurstBudgetPerServerTick(GameTestHelper h){
        var budget=new RelicParticleBudget(256);
        h.assertTrue(budget.tryReserve(40,24)&&budget.tryReserve(40,232),"the global allowance accepts exact whole bursts up to its cap");
        h.assertTrue(!budget.tryReserve(40,16),"an exhausted tick never leaks extra particle packets");
        h.assertTrue(budget.tryReserve(41,24),"the particle allowance refreshes on the next server tick");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_95") public static void curiosAuraBudgetCountsEveryNearbyViewer(GameTestHelper h){
        long tick=Long.MAX_VALUE;
        h.assertTrue(ArtifactFeedback.reserveAuraParticles(tick,256,4),"the full four-player audience reserves 1024 particle instances");
        h.assertTrue(!ArtifactFeedback.reserveAuraParticles(tick,769,4),"an aura that would exceed the shared 4096 cap is skipped whole");
        h.assertTrue(ArtifactFeedback.reserveAuraParticles(tick,256,4),"later pulses fit while capacity remains");
        h.assertTrue(ArtifactFeedback.reserveAuraParticles(tick-1,1024,4),"the shared aura allowance refreshes next tick");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_96") public static void manualLayoutKeepsPanelsAndScrollRegionsDisjoint(GameTestHelper h){
        for(int width:new int[]{0,320,640,1920})for(int height:new int[]{0,100,200,540,1080}){
            for(boolean pickerEnabled:new boolean[]{false,true}){
                var layout=ManualScreenLayout.calculate(width,height,pickerEnabled);
                var panel=layout.panel();var content=layout.content();var picker=layout.picker();
                h.assertTrue(content.width()>=0&&content.height()>=0,"manual content has nonnegative dimensions");
                h.assertTrue(content.x()>=panel.x()&&content.right()<=panel.right()&&content.y()>=panel.y()&&content.bottom()<=panel.bottom(),"manual content stays inside its panel");
                h.assertTrue(layout.footerY()>=panel.y()&&layout.footerY()<=panel.bottom(),"manual footer stays inside its panel");
                if(layout.twoColumns()){
                    var first=layout.firstColumn();var second=layout.secondColumn();
                    h.assertTrue(first.x()>=content.x()&&first.right()<second.x()&&second.right()<=content.right(),"wide manual columns remain separated inside the content viewport");
                    h.assertTrue(first.y()==second.y()&&first.height()==second.height()&&first.bottom()<=content.bottom(),"manual columns share one aligned, clipped scroll viewport");
                }else h.assertTrue(layout.firstColumn().equals(content)&&layout.secondColumn().width()==0,"narrow manual uses one content column");
                if(layout.pickerVisible())h.assertTrue(picker.width()>0&&picker.height()>0&&picker.bottom()<=content.y()&&picker.right()<=panel.right(),"copy picker stays above, and separate from, scroll content");
            }
        }
        h.assertTrue(ManualScreenLayout.choiceIndexAt(1,0,24,13)==0&&ManualScreenLayout.choiceIndexAt(1,48,24,13)==2,"horizontal wheel scrolling maps visible pick icons to their source entries");
        h.assertTrue(ManualScreenLayout.choiceIndexAt(23,0,24,13)==-1&&ManualScreenLayout.choiceIndexAt(-1,0,24,13)==-1&&ManualScreenLayout.choiceIndexAt(1,0,24,2)==0,"cell gaps and out-of-range clicks cannot select a pick");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_96") public static void manualScrollbarDragMapsBothEndsAndKeepsThumbReachable(GameTestHelper h){
        h.assertTrue(ManualScrollModel.maxScroll(0,80)==0&&ManualScrollModel.thumbHeight(0,100)==0,
                "empty tracks and short content have no scroll range");
        for(int track:new int[]{1,8,24,80,300})for(int content:new int[]{track,track+1,track*2,track*20}){
            int max=ManualScrollModel.maxScroll(content,track),thumb=ManualScrollModel.thumbHeight(track,content);
            h.assertTrue(thumb>=1&&thumb<=track,"the scrollbar thumb always fits its viewport");
            h.assertTrue(ManualScrollModel.thumbOffset(track,content,0)==0,"the thumb starts at the top");
            h.assertTrue(ManualScrollModel.thumbOffset(track,content,max)==track-thumb,"the thumb reaches the bottom at maximum scroll");
            if(track>thumb)h.assertTrue(ManualScrollModel.scrollFromThumb(track,content,0,thumb/2)==0
                    &&ManualScrollModel.scrollFromThumb(track,content,track-thumb+thumb/2,thumb/2)==max,
                    "dragging the thumb to either edge maps exactly to the content limits");
            else h.assertTrue(ManualScrollModel.scrollFromThumb(track,content,0,thumb/2)==0,
                    "a thumb filling a tiny viewport stays at a valid scroll origin");
            int middle=ManualScrollModel.thumbOffset(track,content,max/2);
            int restored=ManualScrollModel.scrollFromThumb(track,content,middle,0);
            h.assertTrue(restored>=0&&restored<=max,"drag-derived scroll remains inside the valid content range");
        }
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_97") public static void magmaBrandStopsWhenItsOwnerLosesPermission(GameTestHelper h){
        var owner=player(h,ArtifactKind.HELLSPEC,"MagmaBrandOwner");h.getLevel().addFreshEntity(owner);
        var target=EntityType.ZOMBIE.create(h.getLevel());target.setPos(owner.getX()+2,owner.getY(),owner.getZ());h.getLevel().addFreshEntity(target);
        var brand=SpecialPickaxes.MAGMA_BRAND.get();float health=target.getHealth();
        h.assertTrue(MagmaBrandEffect.applyFrom(owner,target,80,0),"a permitted Hellspec owner applies the attributed brand");
        brand.applyEffectTick(target,0);
        h.assertTrue(target.getHealth()<health,"the first permitted Magma Brand pulse deals its custom damage");
        float protectedHealth=target.getHealth();owner.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        brand.applyEffectTick(target,0);
        h.assertTrue(target.getHealth()==protectedHealth&&!target.hasEffect(brand),"the brand dissipates without damage when its owner loses build/PvP permission");
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
        h.assertTrue(java.util.stream.IntStream.range(0,20).filter(ArtifactFeedback::resonantTunnelPulseDue).count()==10
                &&!ArtifactFeedback.resonantTunnelPulseDue(20),"the private tunnel ripple is capped at ten samples for the 20-block route");
        h.assertTrue(java.util.stream.IntStream.range(0,20).map(ArtifactFeedback::resonantTunnelParticleCount).sum()==42
                &&ArtifactFeedback.resonantTunnelParticleCount(20)==0,"the four-point tunnel outline stays targeted and below 42 particles per full bore");
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
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.STASIS_WARD.get(),100,0,true,false,true));
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        StasisWardEffect.answerHostileHit(wearer,hit);
        var tether=SpecialPickaxes.GRAVITIC_TETHER.get();
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F,"Stasis Reflex trims its triggering hostile hit by fifteen percent");
        h.assertTrue(attacker.hasEffect(tether)&&attacker.getEffect(tether).getDuration()==40&&attacker.getEffect(tether).getAmplifier()==1,"Stasis applies the stronger two-second Gravitic Tether");
        h.assertTrue(!attacker.hasEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN),"Stasis Reflex uses the custom tether instead of generic Slowness");
        attacker.setDeltaMovement(1,.25,0);tether.applyEffectTick(attacker,1);
        h.assertTrue(Math.abs(attacker.getDeltaMovement().x-.60)<.001&&Math.abs(attacker.getDeltaMovement().y-.25)<.001,"Stasis Tether damps horizontal escape without changing vertical movement");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_85") public static void sharedStasisWardAnswersHostileHitsForAllies(GameTestHelper h){
        var protectedAlly=player(h,ArtifactKind.CHOIR,"StasisProtectedAlly");
        protectedAlly.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.STASIS_WARD.get(),100,0,true,false,true));
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(protectedAlly.getX()+2,protectedAlly.getY(),protectedAlly.getZ());h.getLevel().addFreshEntity(attacker);
        var first=new net.minecraftforge.event.entity.living.LivingHurtEvent(protectedAlly,h.getLevel().damageSources().mobAttack(attacker),10F);
        h.assertTrue(StasisWardEffect.answerHostileHit(protectedAlly,first)&&Math.abs(first.getAmount()-8.5F)<.001F,
                "a Curios ally protected by Stasis receives the same fifteen-percent first-hit guard");
        h.assertTrue(attacker.hasEffect(SpecialPickaxes.GRAVITIC_TETHER.get()),"the shared ward returns a Gravitic Tether to the hostile attacker");
        var second=new net.minecraftforge.event.entity.living.LivingHurtEvent(protectedAlly,h.getLevel().damageSources().mobAttack(attacker),10F);
        h.assertTrue(!StasisWardEffect.answerHostileHit(protectedAlly,second)&&second.getAmount()==10F,
                "each protected ally has an independent eight-second reflex cooldown");
        finish(h,protectedAlly);
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
        wearer.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        ArtifactPassives.onCuriosUnequip(wearer,ArtifactKind.INTERREGNUM);current=wearer.getEffect(absorption);
        h.assertTrue(current!=null&&current.getDuration()==100&&!current.isAmbient(),"unequipping Stasis preserves the external replacement potion");
        var leaseOwner=player(h,ArtifactKind.INTERREGNUM,"StasisLeaseOwner");ArtifactPassives.tick(leaseOwner,ArtifactKind.INTERREGNUM);
        h.assertTrue(leaseOwner.hasEffect(absorption),"Stasis first grants its own absorption lease when no external potion exists");
        leaseOwner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
        ArtifactPassives.onCuriosUnequip(leaseOwner,ArtifactKind.INTERREGNUM);
        h.assertTrue(!leaseOwner.hasEffect(absorption),"unequipping Stasis immediately clears its owned absorption lease");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_48") public static void iridiumRecognizesForeignOresButNotBroadlyTaggedHostRock(GameTestHelper h){
        h.assertTrue(ArtifactOres.isOre(TestOres.NAMED_ORE.defaultBlockState()),"foreign conventional *_ore registry names qualify as ore");
        h.assertTrue(ArtifactOres.isOre(TestOres.TAGGED_ORE.defaultBlockState()),"foreign ore-family tags classify ores without ore in their registry name");
        var prismaticCluster=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("terramity","prismatic_crystal_cluster");
        var prismaticStorage=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("terramity","prismatic_crystal_block");
        h.assertTrue(ArtifactOres.isUnconventionalOre(prismaticCluster),"Terramity's loot-bearing Prismatic Crystal Cluster is protected as an ore despite its nonstandard registry name");
        h.assertTrue(!ArtifactOres.isUnconventionalOre(prismaticStorage),"crafted Prismatic Crystal storage blocks are not misclassified as ore deposits");
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
    @GameTest(template="empty",batch="rework_each_91") public static void eventideSharesGraviticBalanceWithThreeNearbyTeammates(GameTestHelper h){
        var owner=player(h,ArtifactKind.EVENTIDE,"EventideShareOwner");var level=h.getLevel();var center=owner.position();
        var allies=List.of(player(h,ArtifactKind.CHOIR,"GravityOne"),player(h,ArtifactKind.ICARUS,"GravityTwo"),
                player(h,ArtifactKind.WORLDLOOM,"GravityThree"),player(h,ArtifactKind.PALIMPSEST,"GravityFour"));
        var distant=player(h,ArtifactKind.AXIOM,"GravityDistant");var enemy=player(h,ArtifactKind.SEAM_RIPPER,"GravityEnemy");
        var scoreboard=level.getScoreboard();var team=scoreboard.addPlayerTeam("eventide_"+owner.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("gravfoes_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(owner.getScoreboardName(),team);
        for(var ally:allies)scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(distant.getScoreboardName(),team);scoreboard.addPlayerToTeam(enemy.getScoreboardName(),foes);
        for(int i=0;i<allies.size();i++)allies.get(i).setPos(center.x+i+1D,center.y,center.z);
        distant.setPos(center.x+7D,center.y,center.z);enemy.setPos(center.x+1D,center.y,center.z);
        var participants=new ArrayList<ServerPlayer>();participants.add(owner);participants.addAll(allies);participants.add(distant);participants.add(enemy);
        for(var participant:participants)level.addFreshEntity(participant);
        ArtifactPassives.tick(owner,ArtifactKind.EVENTIDE);var balance=SpecialPickaxes.GRAVITIC_BALANCE.get();
        h.assertTrue(owner.hasEffect(balance),"Eventide keeps its own Gravitic Balance");
        for(int i=0;i<3;i++)h.assertTrue(allies.get(i).hasEffect(balance),"one of the three nearest teammates receives controlled descent");
        h.assertTrue(!allies.get(3).hasEffect(balance)&&!distant.hasEffect(balance)&&!enemy.hasEffect(balance),
                "the shared gravity ward excludes the fourth teammate, distant players and enemies");
        allies.get(0).setDeltaMovement(0,-1D,0);balance.applyEffectTick(allies.get(0),0);
        h.assertTrue(Math.abs(allies.get(0).getDeltaMovement().y+.24D)<.001D,"a shared bearer receives the controlled-descent effect");
        var fall=new net.minecraftforge.event.entity.living.LivingFallEvent(allies.get(0),12F,1F);MinecraftForge.EVENT_BUS.post(fall);
        h.assertTrue(Math.abs(fall.getDamageMultiplier()-.25F)<.001F,"a shared bearer takes 75 percent less fall damage");finish(h,owner);
    }
    @GameTest(template="empty",batch="rework_each_92") public static void sharedCuriosAllyLinksFollowBoundedBowedArcs(GameTestHelper h){
        var start=new net.minecraft.world.phys.Vec3(-4D,2D,3D);var end=new net.minecraft.world.phys.Vec3(5D,5D,-2D);
        for(int sample=1;sample<=5;sample++){
            var point=CurioAuraGeometry.sharedAllyLinkPoint(start,end,sample,.34D);double t=sample/6D;
            var linear=start.lerp(end,t);
            h.assertTrue(Double.isFinite(point.x)&&Double.isFinite(point.y)&&Double.isFinite(point.z),"the shared gravity thread stays finite");
            h.assertTrue(point.distanceTo(linear)<.5D,"each gravity thread point stays within the compact bowed arc");
            if(sample==3)h.assertTrue(point.y>linear.y+.25D,"the middle of the ally link rises visibly above its straight path");
        }
        var worldloom=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.22D);
        var aegis=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.14D);
        var crucible=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.25D);
        var nullVeil=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.29D);
        var gravity=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.34D);
        var starfall=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.31D);
        var searing=CurioAuraGeometry.sharedAllyLinkPoint(start,end,3,.38D);
        h.assertTrue(aegis.y>start.lerp(end,.5D).y&&aegis.y<worldloom.y,
                "Worldbreaker's brief formation link stays lower than the natural-healing arc");
        h.assertTrue(nullVeil.y>worldloom.y&&nullVeil.y<gravity.y,
                "Axiom's null stitch has a distinct arc between vine renewal and gravity links");
        h.assertTrue(crucible.y>worldloom.y&&crucible.y<nullVeil.y,
                "Crucible's ember current has its own bounded arc between vine and null links");
        h.assertTrue(worldloom.y>start.lerp(end,.5D).y&&worldloom.y<gravity.y,
                "Worldloom's lower vine arc remains distinct from Eventide's higher gravity arc");
        h.assertTrue(starfall.y>nullVeil.y&&starfall.y<gravity.y&&searing.y>gravity.y,
                "Exodium's star stitch and Hellspec's ember arc have distinct Curios link silhouettes");
        var low=CurioAuraGeometry.sharedAllyLinkPoint(start,end,1,.34D);var clampedLow=CurioAuraGeometry.sharedAllyLinkPoint(start,end,0,.34D);
        var high=CurioAuraGeometry.sharedAllyLinkPoint(start,end,5,.34D);var clampedHigh=CurioAuraGeometry.sharedAllyLinkPoint(start,end,99,.34D);
        h.assertTrue(low.equals(clampedLow)&&high.equals(clampedHigh),"invalid particle sample indices clamp to the five bounded interior points");h.succeed();
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
        var incoming=new Vec3(.6D,-.8D,.3D);var braked=IcarianMomentumEffect.airBrake(incoming,true,false);
        h.assertTrue(Math.abs(braked.x-.408D)<1.0E-6D&&Math.abs(braked.z-.204D)<1.0E-6D&&braked.y==-.12D,
                "Sneak cuts horizontal drift and caps a fast fall for precise landing");
        h.assertTrue(IcarianMomentumEffect.airBrake(incoming,false,false).equals(incoming)
                        &&IcarianMomentumEffect.airBrake(incoming,true,true).equals(incoming),
                "the air brake only changes normal airborne movement");
        wearer.setXRot(-45F);wearer.setDeltaMovement(0,-.2,0);glide.applyEffectTick(wearer,0);var boosted=wearer.getDeltaMovement();
        h.assertTrue(boosted.horizontalDistanceSqr()>.0025&&boosted.y>-.2,"sprint-gliding gains forward momentum and updraft when the wearer looks up");
        wearer.setSprinting(false);wearer.setDeltaMovement(.1,-.2,0);glide.applyEffectTick(wearer,0);
        h.assertTrue(Math.abs(wearer.getDeltaMovement().x-.1)<.001&&Math.abs(wearer.getDeltaMovement().z)<.001,"the glide stops accelerating when sprint is released");
        wearer.setSprinting(true);wearer.setShiftKeyDown(true);wearer.setDeltaMovement(.6,-.8,.3);glide.applyEffectTick(wearer,0);
        var braking=wearer.getDeltaMovement();
        h.assertTrue(Math.abs(braking.x-.408)<.001D&&Math.abs(braking.z-.204)<.001D&&Math.abs(braking.y+.12)<.001D,
                "holding Sneak brakes horizontal momentum and slows a steep fall");
        wearer.setShiftKeyDown(false);wearer.setSprinting(false);
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
    @GameTest(template="empty",batch="rework_each_101") public static void icarusCuriosSharesControlledGlideWithTwoAirborneTeammates(GameTestHelper h){
        var wearer=player(h,ArtifactKind.ICARUS,"IcarianWindbearer");wearer.setPos(wearer.getX(),wearer.getY()+6D,wearer.getZ());wearer.setOnGround(false);
        var allies=List.of(player(h,ArtifactKind.CHOIR,"IcarianOne"),player(h,ArtifactKind.CHOIR,"IcarianTwo"),
                player(h,ArtifactKind.CHOIR,"IcarianThree"));
        var grounded=player(h,ArtifactKind.CHOIR,"IcarianGrounded");var enemy=player(h,ArtifactKind.CHOIR,"IcarianEnemy");
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("iw_"+wearer.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("iwe_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);
        for(var ally:allies)scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(grounded.getScoreboardName(),team);scoreboard.addPlayerToTeam(enemy.getScoreboardName(),foes);
        for(int i=0;i<allies.size();i++){allies.get(i).setPos(wearer.getX()+i+1D,wearer.getY(),wearer.getZ());allies.get(i).setOnGround(false);}
        grounded.setPos(wearer.getX()+1D,wearer.getY()-6D,wearer.getZ());grounded.setOnGround(true);
        enemy.setPos(wearer.getX()+1D,wearer.getY(),wearer.getZ());enemy.setOnGround(false);
        var participants=new ArrayList<ServerPlayer>();participants.add(wearer);participants.addAll(allies);participants.add(grounded);participants.add(enemy);
        for(var participant:participants){h.getLevel().addFreshEntity(participant);if(participant!=grounded)participant.setOnGround(false);}
        ArtifactPassives.tick(wearer,ArtifactKind.ICARUS);var glide=SpecialPickaxes.ICARIAN_MOMENTUM.get();
        h.assertTrue(wearer.hasEffect(glide),"Icarus keeps its own airborne Curios glide");
        h.assertTrue(allies.get(0).hasEffect(glide)&&allies.get(1).hasEffect(glide),"Icarus lends controlled glide to its two nearest airborne allies");
        h.assertTrue(!allies.get(2).hasEffect(glide)&&!grounded.hasEffect(glide)&&!enemy.hasEffect(glide),
                "the wind excludes the third ally, grounded players and enemies");
        var fall=new net.minecraftforge.event.entity.living.LivingFallEvent(allies.get(0),12F,1F);MinecraftForge.EVENT_BUS.post(fall);
        h.assertTrue(Math.abs(fall.getDamageMultiplier()-.5F)<.001F,"the shared wind halves an ally's fall damage");
        for(var participant:participants)finish(h,participant);
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
    @GameTest(template="empty",batch="rework_each_23") public static void seamRipperFollowsSameMaterialVeinsAcrossDiagonals(GameTestHelper h) {
        var p=player(h,ArtifactKind.SEAM_RIPPER);var seed=target(h);var level=h.getLevel();
        var trail=new ArrayList<BlockPos>();for(int i=0;i<8;i++)trail.add(seed.east(i));
        h.assertTrue(ArtifactFeedback.seamTrail(p,trail)==4,"a vein pulse draws at most four recent adjacent links");
        h.assertTrue(ArtifactFeedback.seamTrail(p,List.of(seed,seed.offset(10,0,0)))==0,"the private trail skips distant, disconnected breaks");
        var sameA=seed.east();var sameB=sameA.east();var diagonal=seed.offset(1,1,1);var mixed=seed.west();var ore=seed.below();var placed=seed.above();
        for(var pos:List.of(seed,sameA,sameB,diagonal,mixed,ore,placed))PlayerPlacedBlocks.get(level).unmark(pos);
        level.setBlockAndUpdate(sameA,Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(sameB,Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(diagonal,Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(mixed,Blocks.GRANITE.defaultBlockState());level.setBlockAndUpdate(ore,Blocks.DIAMOND_ORE.defaultBlockState());
        level.setBlockAndUpdate(placed,Blocks.STONE.defaultBlockState());PlayerPlacedBlocks.get(level).mark(placed);
        p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(seed));
        h.assertTrue(ArtifactInteraction.modeCount(ArtifactKind.SEAM_RIPPER)==1,"Seam R is a single, clear ability without obsolete A/B modes");
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.SEAM_RIPPER,false),"R starts a same-material vein with an empty offhand");
        for(int tick=0;tick<2000&&WorkQueue.busy(p);tick++)WorkQueue.tick();
        h.assertTrue(level.getBlockState(seed).isAir()&&level.getBlockState(sameA).isAir()&&level.getBlockState(sameB).isAir()
                        &&level.getBlockState(diagonal).isAir(),"connected natural stone, including a block touching only at a 3D corner, is mined");
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
    @GameTest(template="empty",batch="rework_each_63") public static void sharedWorldloomRenewalCountersOneNearbyHostileHit(GameTestHelper h){
        var ally=player(h,ArtifactKind.ICARUS,"HeartwoodAlly");var attacker=EntityType.ZOMBIE.create(h.getLevel());
        attacker.setPos(ally.getX()+2,ally.getY(),ally.getZ());h.getLevel().addFreshEntity(attacker);
        ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.HEARTWOOD_RENEWAL.get(),100,0));
        var first=new net.minecraftforge.event.entity.living.LivingHurtEvent(ally,h.getLevel().damageSources().mobAttack(attacker),10F);
        h.assertTrue(ArtifactPassives.onSharedHeartwoodGuard(ally,first),"a shared renewal grants its ally a defensive counter");
        h.assertTrue(Math.abs(first.getAmount()-9F)<.001F&&attacker.hasEffect(SpecialPickaxes.HEARTWOOD_GRASP.get()),
                "the ally softens one hostile hit and visibly roots its attacker");
        var rapid=new net.minecraftforge.event.entity.living.LivingHurtEvent(ally,h.getLevel().damageSources().mobAttack(attacker),10F);
        h.assertTrue(!ArtifactPassives.onSharedHeartwoodGuard(ally,rapid)&&Math.abs(rapid.getAmount()-10F)<.001F,
                "the ally cannot retrigger the counter during its eight-second cooldown");
        finish(h,ally);
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
    @GameTest(template="empty",batch="rework_each_97") public static void heldPickMaintainsItsNightVisionPassiveAndReleasesIt(GameTestHelper h){
        var wearer=player(h,ArtifactKind.PALIMPSEST);var nightVision=net.minecraft.world.effect.MobEffects.NIGHT_VISION;
        ArtifactPassives.syncNightVisionSource(wearer,true);
        h.assertTrue(wearer.hasEffect(nightVision)&&wearer.getEffect(nightVision).getDuration()==40,
                "holding a pick renews the advertised short Night Vision passive");
        ArtifactPassives.syncNightVisionSource(wearer,false);
        h.assertTrue(!wearer.hasEffect(nightVision),"putting away the last pick removes only its own Night Vision lease");
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(nightVision,6000,0,false,true,true));
        ArtifactPassives.syncNightVisionSource(wearer,true);ArtifactPassives.syncNightVisionSource(wearer,false);
        h.assertTrue(wearer.hasEffect(nightVision)&&wearer.getEffect(nightVision).getDuration()==6000,
                "a server-owned Night Vision effect survives held-pick changes");finish(h,wearer);
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
        var brand=SpecialPickaxes.MAGMA_BRAND.get();
        h.assertTrue(Math.abs(hit.getAmount()-8F)<.001F&&attacker.getRemainingFireTicks()>=60,"Cinder Retort reduces the first hostile hit by 20 percent and ignites its attacker");
        h.assertTrue(attacker.hasEffect(brand)&&attacker.getEffect(brand).getDuration()==40,"Cinder Retort adds a two-second custom Magma Brand");
        attacker.clearFire();var repeat=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,repeat,ArtifactKind.HELLSPEC);
        h.assertTrue(repeat.getAmount()==10F&&!attacker.isOnFire()&&attacker.getEffect(brand).getDuration()==40,"the eight-second cooldown prevents repeated reductions, ignitions and brand refreshes");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST),"the passive never grants Strength");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_69") public static void hellspecMagmaEruptionOnlyBrandsNearbyHostilesAndLeavesBlocksUntouched(GameTestHelper h){
        var wearer=player(h,ArtifactKind.HELLSPEC,"MagmaBearer");h.getLevel().addFreshEntity(wearer);var center=target(h);
        var enemy=EntityType.ZOMBIE.create(h.getLevel());enemy.setPos(center.getX()+1.5D,center.getY()+1,center.getZ()+.5D);h.getLevel().addFreshEntity(enemy);
        var farEnemy=EntityType.ZOMBIE.create(h.getLevel());farEnemy.setPos(center.getX()+.5D,center.getY()+1,center.getZ()+7.5D);h.getLevel().addFreshEntity(farEnemy);
        var ally=player(h,ArtifactKind.CHOIR,"MagmaAlly");ally.setPos(center.getX()+.5D,center.getY()+1,center.getZ()+2.5D);
        var scoreboard=h.getLevel().getScoreboard();var team=scoreboard.addPlayerTeam("magma_"+wearer.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);h.getLevel().addFreshEntity(ally);
        float health=enemy.getHealth();var floor=h.getLevel().getBlockState(center);
        h.assertTrue(ArtifactTechniques.magmaEruptionAt(wearer,center),"X erupts at the selected block when a hostile is in range");
        var brand=SpecialPickaxes.MAGMA_BRAND.get();
        h.assertTrue(enemy.getHealth()<=health-4.9F,"the eruption deals 5 direct damage");
        h.assertTrue(enemy.hasEffect(brand)&&enemy.getEffect(brand).getDuration()==80,"the eruption applies its four-second custom Magma Brand");
        h.assertTrue(enemy.isOnFire()&&enemy.getDeltaMovement().y>.8D&&enemy.hasImpulse,"the visible eruption lifts and ignites its target");
        h.assertTrue(ally.getHealth()==ally.getMaxHealth()&&!ally.hasEffect(brand),"allied players remain unharmed");
        h.assertTrue(!farEnemy.hasEffect(brand)&&farEnemy.getHealth()==farEnemy.getMaxHealth(),"the 5-block eruption radius is enforced");
        h.assertTrue(h.getLevel().getBlockState(center)==floor,"the effect does not alter the aimed block");
        float beforePulse=enemy.getHealth();brand.applyEffectTick(enemy,0);
        h.assertTrue(enemy.getHealth()<beforePulse,"Magma Brand deals its custom pulse separately from ordinary fire ticks");
        h.assertTrue(MagmaBrandEffect.brandParticles(enemy,0).stream().anyMatch(sample->sample.particle()==SpecialPickaxes.HELLSPEC_BRAND_FLARE.get()),"Magma Brand uses its registered animated Blockbench flare");
        h.assertTrue(brand.isDurationEffectTick(80,0)&&!brand.isDurationEffectTick(79,0),"Magma Brand uses a one-second effect cadence");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_83") public static void hellspecMagmaEruptionCapsTargets(GameTestHelper h){
        var wearer=player(h,ArtifactKind.HELLSPEC,"MagmaCapBearer");var center=target(h);var targets=new ArrayList<LivingEntity>();
        for(int i=0;i<10;i++){var enemy=EntityType.ZOMBIE.create(h.getLevel());enemy.setPos(center.getX()+.5D,center.getY()+1,center.getZ()+.5D);h.getLevel().addFreshEntity(enemy);targets.add(enemy);}
        h.assertTrue(ArtifactTechniques.magmaEruptionAt(wearer,center),"the eruption resolves with a crowded target cluster");
        long branded=targets.stream().filter(target->target.hasEffect(SpecialPickaxes.MAGMA_BRAND.get())).count();
        h.assertTrue(branded==8,"one eruption affects at most eight hostiles regardless of entity pile size");
        finish(h,wearer);
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
    @GameTest(template="empty",batch="rework_each_86") public static void hellspecHeatHaloKeepsItsCompactNineParticleShape(GameTestHelper h){
        var wearer=player(h,ArtifactKind.HELLSPEC);var halo=SearingShellEffect.heatHaloParticles(wearer,120);
        long flames=halo.stream().filter(sample->sample.particle()==net.minecraft.core.particles.ParticleTypes.FLAME).count();
        long lava=halo.stream().filter(sample->sample.particle()==net.minecraft.core.particles.ParticleTypes.LAVA).count();
        h.assertTrue(halo.size()==9&&flames==6&&lava==3,"the local thermal halo keeps six flame motes and three lava sparks");
        h.assertTrue(halo.stream().allMatch(sample->sample.position().distanceTo(wearer.position().add(0,wearer.getBbHeight()*.52D,0))<.42D),
                "all heat motes stay close to the wearer instead of expanding into a world-scale burst");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_89") public static void hellspecThermalMiningWindowGetsQuietVisibleHalo(GameTestHelper h){
        var wearer=player(h,ArtifactKind.HELLSPEC);var halo=SearingShellEffect.thermalMiningHaloParticles(wearer,140);
        long embers=halo.stream().filter(sample->sample.particle() instanceof net.minecraft.core.particles.DustParticleOptions).count();
        long flames=halo.stream().filter(sample->sample.particle()==net.minecraft.core.particles.ParticleTypes.SMALL_FLAME).count();
        var center=wearer.position().add(0,wearer.getBbHeight()*.56D,0);
        h.assertTrue(halo.size()==5&&embers==3&&flames==2,"the active heat-mining bonus uses a small, mixed ember halo");
        h.assertTrue(halo.stream().allMatch(sample->sample.position().distanceTo(center)<.4D),
                "the thermal cue stays tightly around its bearer instead of spreading through the mine");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_87") public static void hellspecCuriosShellAddsMiningTempoNearHeat(GameTestHelper h){
        var cold=player(h,ArtifactKind.HELLSPEC,"HellspecColdTest");
        cold.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.SEARING_SHELL.get(),100,0,true,false,true));
        h.assertTrue(SearingShellEffect.miningSpeedMultiplier(cold)==1D,"the shell gives no mining bonus away from nearby heat");

        var hot=player(h,ArtifactKind.HELLSPEC,"HellspecHotTest");
        hot.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.SEARING_SHELL.get(),100,0,true,false,true));
        h.getLevel().setBlockAndUpdate(hot.blockPosition().offset(2,0,0),Blocks.MAGMA_BLOCK.defaultBlockState());
        h.assertTrue(SearingShellEffect.nearbyHeat(hot),"magma within the compact scan activates the thermal passive");
        h.assertTrue(SearingShellEffect.miningSpeedMultiplier(hot)==SearingShellEffect.thermalMiningMultiplier()
                        &&SearingShellEffect.thermalMiningMultiplier()==1.35D,
                "nearby heat raises effective mining speed by 35 percent");
        var campfireBearer=player(h,ArtifactKind.HELLSPEC,"HellspecCampfireTest");
        campfireBearer.setPos(cold.getX()+10D,cold.getY(),cold.getZ());
        campfireBearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.SEARING_SHELL.get(),100,0,true,false,true));
        h.getLevel().setBlockAndUpdate(campfireBearer.blockPosition().offset(0,0,2),Blocks.CAMPFIRE.defaultBlockState());
        h.assertTrue(SearingShellEffect.miningSpeedMultiplier(campfireBearer)==1.35D,
                "campfires count as nearby heat sources for the mining passive");

        var cauldronBearer=player(h,ArtifactKind.HELLSPEC,"HellspecCauldronTest");
        cauldronBearer.setPos(cold.getX()+20D,cold.getY(),cold.getZ());
        cauldronBearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.SEARING_SHELL.get(),100,0,true,false,true));
        h.getLevel().setBlockAndUpdate(cauldronBearer.blockPosition().offset(0,0,2),Blocks.LAVA_CAULDRON.defaultBlockState());
        h.assertTrue(SearingShellEffect.miningSpeedMultiplier(cauldronBearer)==1.35D,
                "lava cauldrons count as nearby heat sources for the mining passive");
        finish(h,cold);finish(h,hot);finish(h,campfireBearer);finish(h,cauldronBearer);
    }
    @GameTest(template="empty",batch="rework_each_88") public static void hellspecCuriosSharesFireproofMiningShellWithAllies(GameTestHelper h){
        var owner=player(h,ArtifactKind.HELLSPEC,"HellspecAuraOwner");
        var ally=player(h,ArtifactKind.PALIMPSEST,"HellspecAuraAlly");
        var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("hellspec_"+owner.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(owner.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        ally.setPos(owner.getX()+3D,owner.getY(),owner.getZ());
        h.getLevel().addFreshEntity(owner);h.getLevel().addFreshEntity(ally);
        ArtifactPassives.tick(owner,ArtifactKind.HELLSPEC);
        var shell=SpecialPickaxes.SEARING_SHELL.get();
        h.assertTrue(owner.hasEffect(shell)&&ally.hasEffect(shell),"Hellspec shares its leased fireproof shell with a nearby teammate");
        h.getLevel().setBlockAndUpdate(ally.blockPosition().offset(0,0,2),Blocks.SOUL_CAMPFIRE.defaultBlockState());
        h.assertTrue(SearingShellEffect.miningSpeedMultiplier(ally)==1.35D,
                "the shared shell also grants its 35 percent mining tempo near a soul campfire");
        var lava=new net.minecraftforge.event.entity.living.LivingAttackEvent(ally,h.getLevel().damageSources().lava(),4F);
        h.assertTrue(MinecraftForge.EVENT_BUS.post(lava),"shared Searing Shell cancels lava damage for the ally");
        finish(h,owner);finish(h,ally);
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
        var wearer=player(h,ArtifactKind.CRUCIBLE,"CrucibleBearer");
        var ally=player(h,ArtifactKind.PALIMPSEST,"CrucibleAlly");var enemy=player(h,ArtifactKind.PALIMPSEST,"CrucibleEnemy");
        var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("crucible_"+wearer.getUUID().toString().substring(0,6));
        var hostile=scoreboard.addPlayerTeam("foe_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(enemy.getScoreboardName(),hostile);
        ally.setPos(wearer.getX()+3,wearer.getY(),wearer.getZ());enemy.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());
        h.getLevel().addFreshEntity(ally);h.getLevel().addFreshEntity(enemy);
        ArtifactPassives.tick(wearer,ArtifactKind.CRUCIBLE);
        var mantle=SpecialPickaxes.CRUCIBLE_MANTLE.get();
        h.assertTrue(wearer.hasEffect(mantle)&&wearer.getEffect(mantle).getDuration()==40,"Curios pre-leases the named two-second Crucible Mantle before the wearer enters heat");
        h.assertTrue(ally.hasEffect(mantle)&&!enemy.hasEffect(mantle),"Crucible shares its thermal ward with nearby teammates and never hostiles");
        h.assertTrue(!wearer.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE),"Crucible uses its custom passive instead of a generic fire-resistance potion");
        wearer.setSecondsOnFire(10);
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
    @GameTest(template="empty",batch="rework_each_81") public static void worldbreakerAegisImpactFormsADirectionalSlateShield(GameTestHelper h){
        var center=new Vec3(2D,8D,-3D);var towards=new Vec3(1D,0D,0D);var side=new Vec3(0D,0D,1D);
        var left=ArtifactFeedback.worldbreakerAegisShardPoint(center,towards,side,0);
        var middle=ArtifactFeedback.worldbreakerAegisShardPoint(center,towards,side,1);
        var right=ArtifactFeedback.worldbreakerAegisShardPoint(center,towards,side,2);
        var upper=ArtifactFeedback.worldbreakerAegisShardPoint(center,towards,side,5);
        h.assertTrue(left.x>.4D&&middle.x>.4D&&right.x>.4D,"the ward forms between its bearer and an attacker to the east");
        h.assertTrue(left.z<middle.z&&middle.z<right.z,"the three shards fan across the incoming side");
        h.assertTrue(upper.y>middle.y,"the shield has two visible vertical bands");h.succeed();
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
        // GameTest reuses structure coordinates; clear stale provenance from an earlier fixture.
        PlayerPlacedBlocks.get(wearer.serverLevel()).unmark(pos);
        var stasis=SpecialPickaxes.STASIS_RESONANCE.get();wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(stasis,40,0,true,false,true));
        h.assertTrue(StrataFocusEffect.observeBreak(wearer,pos),"an uncanceled Strata mining attempt is observed");
        h.assertTrue(StrataFocusEffect.flushObservedBreaks()==0
                &&StrataFocusEffect.miningTempoMultiplier(wearer)==1D
                &&!wearer.getPersistentData().contains("specialpickaxesStasisResonancePulseAtWorldTime"),
                "an unbroken or subsequently canceled block grants neither tempo nor a Stasis mining pulse");
        h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        h.assertTrue(StrataFocusEffect.observeBreak(wearer,pos,Blocks.STONE.defaultBlockState()),"a real break is queued with the state Forge reported before removal");
        h.assertTrue(StrataFocusEffect.flushObservedBreaks()==1
                &&Math.abs(StrataFocusEffect.miningTempoMultiplier(wearer)-1.08D)<.001D
                &&wearer.getPersistentData().contains("specialpickaxesStasisResonancePulseAtWorldTime"),
                "a confirmed break grants the first Strata charge and its Stasis pulse");
        var ore=Blocks.DIAMOND_ORE.defaultBlockState();h.getLevel().setBlockAndUpdate(pos,ore);
        h.assertTrue(StrataFocusEffect.observeBreak(wearer,pos,ore),"natural ores also contribute to Curios mining tempo");
        h.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        h.assertTrue(StrataFocusEffect.flushObservedBreaks()==1&&Math.abs(StrataFocusEffect.miningTempoMultiplier(wearer)-1.16D)<.001D,
                "confirmed ore mining builds on the first Strata tempo charge");
        var machine=Blocks.CHEST.defaultBlockState();
        h.assertTrue(!StrataFocusEffect.observeBreak(wearer,pos,machine),"block entities cannot feed the geology tempo");
        h.assertTrue(!StrataFocusEffect.observeBreak(wearer,pos,Blocks.WATER.defaultBlockState()),"fluids cannot feed the geology tempo");
        var placed=target(h);PlayerPlacedBlocks.get(wearer.serverLevel()).mark(placed);
        h.assertTrue(!StrataFocusEffect.observeBreak(wearer,placed),"player-placed geology stays outside the Curios mining streak");
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
        ArtifactFeedback.crucibleWardBoundary(wearer,10);ArtifactFeedback.crucibleWardOrbit(wearer,10);
        DomainFields.tick();
        h.assertTrue(arrow.getDeltaMovement().x>0D,"the ward redirects a fast hostile shot outward before impact");
        h.assertTrue(DomainFields.active(wearer),"the deflection does not consume or cancel the remaining ward");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_80") public static void starfallPredictionScansAreStaggeredAcrossPlayers(GameTestHelper h){
        for(int entityId=0;entityId<12;entityId++){
            int scans=0;
            for(int tick=0;tick<40;tick++)if(StarfallWardEffect.shouldPredictiveScan(tick,entityId))scans++;
            h.assertTrue(scans==20,"each bearer performs ten prediction scans per second, half the prior broadphase cost");
        }
        for(int tick=0;tick<40;tick++)
            h.assertTrue(StarfallWardEffect.shouldPredictiveScan(tick,1)!=StarfallWardEffect.shouldPredictiveScan(tick,2),
                    "adjacent entity IDs scan on opposite ticks instead of spiking the same server tick");
        for(int tick=0;tick<40;tick++)
            h.assertTrue(!StarfallWardEffect.shouldPredictiveScan(tick,1,100,101),
                    "a recharging ward skips projectile broadphase entirely");
        int rechargedScans=0;
        for(int tick=0;tick<40;tick++)if(StarfallWardEffect.shouldPredictiveScan(tick,1,100,100))rechargedScans++;
        h.assertTrue(rechargedScans==20,"predictive scans resume on the existing staggered cadence as soon as the ward is ready");
        h.succeed();
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
    @GameTest(template="empty",batch="rework_each_90") public static void exodiumSharedStarfallOrbitKeepsItsSmallLocalShape(GameTestHelper h){
        var wearer=player(h,ArtifactKind.EXODIUM);
        var charging=StarfallWardEffect.orbitParticles(wearer,120,.5D);
        var ready=StarfallWardEffect.orbitParticles(wearer,120,1D);
        h.assertTrue(charging.size()==4&&charging.stream().allMatch(sample->sample.particle() instanceof net.minecraft.core.particles.DustParticleOptions),
                "a recharging shared ward keeps its four-point violet orbit");
        h.assertTrue(ready.size()==5&&ready.stream().filter(sample->sample.particle()==net.minecraft.core.particles.ParticleTypes.END_ROD).count()==1,
                "a ready shared ward adds only its occasional center spark");
        var center=wearer.position().add(0,wearer.getBbHeight()*.56D,0);
        h.assertTrue(ready.stream().allMatch(sample->sample.position().distanceTo(center)<.5D),
                "team-visible Starfall motes stay tight around the bearer");
        finish(h,wearer);
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
        long flashUntil=allies.get(0).getPersistentData().getLong("specialpickaxesStarfallFlashAtWorldTime");
        h.assertTrue(flashUntil>ArtifactState.now(allies.get(0)),"shared projectile protection produces a visual cue");
        var rapidHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(allies.get(0),h.getLevel().damageSources().arrow(arrow,shooter),10F);
        h.assertTrue(ArtifactPassives.onSharedStarfallGuard(allies.get(0),rapidHit)
                &&allies.get(0).getPersistentData().getLong("specialpickaxesStarfallFlashAtWorldTime")==flashUntil,
                "rapid projectile hits remain protected without repeating the visual flash");
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
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F&&attacker.hasEffect(mark)&&attacker.getEffect(mark).getDuration()==60,
                "Axiom's Curios counter reduces its first hostile hit by 15 percent and applies a three-second null mark");
        var rapid=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,rapid,ArtifactKind.AXIOM);
        h.assertTrue(Math.abs(rapid.getAmount()-10F)<.001F,"Axiom's defensive counter respects its eight-second cooldown");
        h.assertTrue(MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingHealEvent(attacker,4F)),"marked attacker cannot heal during the counter");
        h.assertTrue(!MinecraftForge.EVENT_BUS.post(new net.minecraftforge.event.entity.living.LivingHealEvent(wearer,1F)),"the wearer and unmarked allies remain healable");
        finish(h,wearer);
    }
    @GameTest(template="empty",batch="legacy_each_06") public static void axiomCuriosNullVeilCleansesOneDebuffPerPulse(GameTestHelper h){
        var wearer=player(h,ArtifactKind.AXIOM,"AxiomBearer");
        var ally=player(h,ArtifactKind.PALIMPSEST,"AxiomAlly");var enemy=player(h,ArtifactKind.PALIMPSEST,"AxiomEnemy");
        var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("axiom_"+wearer.getUUID().toString().substring(0,6));
        var hostile=scoreboard.addPlayerTeam("foe_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(enemy.getScoreboardName(),hostile);
        ally.setPos(wearer.getX()+3,wearer.getY(),wearer.getZ());enemy.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());
        h.getLevel().addFreshEntity(ally);h.getLevel().addFreshEntity(enemy);
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.POISON,320,0));
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,300,0));
        wearer.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.HUNGER,40,0));
        ally.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.WITHER,320,0));
        ArtifactPassives.tick(wearer,ArtifactKind.AXIOM);
        h.assertTrue(wearer.hasEffect(SpecialPickaxes.NULL_VEIL.get()),"Axiom grants its custom passive veil while carried in Curios");
        h.assertTrue(ally.hasEffect(SpecialPickaxes.NULL_VEIL.get())&&!enemy.hasEffect(SpecialPickaxes.NULL_VEIL.get()),"Axiom shares the passive cleanse only with nearby teammates");
        var sharedAttacker=EntityType.ZOMBIE.create(h.getLevel());sharedAttacker.setPos(ally.getX()+2,ally.getY(),ally.getZ());h.getLevel().addFreshEntity(sharedAttacker);
        var sharedHit=new net.minecraftforge.event.entity.living.LivingHurtEvent(ally,h.getLevel().damageSources().mobAttack(sharedAttacker),10F);
        h.assertTrue(ArtifactPassives.onSharedNullVeilGuard(ally,sharedHit)&&Math.abs(sharedHit.getAmount()-8.5F)<.001F
                        &&sharedAttacker.hasEffect(SpecialPickaxes.NULL_MARK.get()),
                "each ally wearing the shared Null Veil gets an independent first-hostile-hit reduction and Null Mark counter");
        var sharedRapid=new net.minecraftforge.event.entity.living.LivingHurtEvent(ally,h.getLevel().damageSources().mobAttack(sharedAttacker),10F);
        h.assertTrue(!ArtifactPassives.onSharedNullVeilGuard(ally,sharedRapid)&&Math.abs(sharedRapid.getAmount()-10F)<.001F,
                "a shared Null Veil bearer's counter respects its own eight-second cooldown");
        NullVeilEffect.pulse(ally);
        h.assertTrue(!ally.hasEffect(MobEffects.WITHER),"a nearby ally receives Axiom's custom visible cleanse pulse");
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
    @GameTest(template="empty",batch="rework_each_102") public static void crucibleRecognizesAndRephasesThirdPartyGeology(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var pos=target(h);var level=h.getLevel();
        var gabbro=TestOres.GABBRO_ROCK.defaultBlockState();var mudstone=TestOres.MUDSTONE_ROCK.defaultBlockState();
        h.assertTrue(MiningDesigns.matrix(gabbro)&&MiningDesigns.crucibleGeology(gabbro)
                        &&MiningDesigns.matrix(mudstone)&&MiningDesigns.crucibleGeology(mudstone),
                "common modded igneous and sedimentary rock names count as geology");
        level.setBlockAndUpdate(pos,gabbro);PlayerPlacedBlocks.get(level).unmark(pos);
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",7);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,false),"Crucible can target third-party rock");drain(p);
        h.assertTrue(level.getBlockState(pos).is(Blocks.OBSIDIAN),"third-party rock rephases without drops into the selected output");
        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).isEmpty(),"modded geology rephase creates no drops");
        finish(h,p);
    }
    @GameTest(template="empty",batch="rework_each_103") public static void crucibleRecognizesPackStonesAndRejectsTerramityMachines(GameTestHelper h){
        var p=player(h,ArtifactKind.CRUCIBLE);var pos=target(h);var level=h.getLevel();
        var moonstone=TestOres.TERRAMITY_MOONSTONE.defaultBlockState();
        var fairystone=TestOres.TERRAMITY_FAIRYSTONE.defaultBlockState();
        var polishedIgneostone=TestOres.TERRAMITY_POLISHED_IGNEOSTONE.defaultBlockState();
        h.assertTrue(MiningDesigns.matrix(moonstone)&&MiningDesigns.crucibleGeology(moonstone)
                        &&MiningDesigns.matrix(fairystone)&&MiningDesigns.crucibleGeology(fairystone)
                        &&MiningDesigns.matrix(polishedIgneostone)&&MiningDesigns.crucibleGeology(polishedIgneostone),
                "Terramity moonstone, fairystone and polished igneostone count as geology");
        var lamp=TestOres.TERRAMITY_MOONSTONE_LAMP.defaultBlockState();
        var dispenser=TestOres.TERRAMITY_MOONSTONE_DISPENSER.defaultBlockState();
        h.assertTrue(!MiningDesigns.crucibleGeology(lamp)&&!MiningDesigns.crucibleGeology(dispenser),
                "functional Terramity lamp and poker-chip dispenser are not geology");
        level.setBlockAndUpdate(pos,moonstone);PlayerPlacedBlocks.get(level).unmark(pos);
        ArtifactState.of(p,ArtifactKind.CRUCIBLE).putInt("mode",7);
        h.assertTrue(ArtifactActions.use(p,p.getMainHandItem(),ArtifactKind.CRUCIBLE,false),"Crucible can target Terramity moonstone");drain(p);
        h.assertTrue(level.getBlockState(pos).is(Blocks.OBSIDIAN),"Terramity moonstone rephases to the selected material");
        h.assertTrue(level.getEntitiesOfClass(ItemEntity.class,new AABB(pos).inflate(1)).isEmpty(),"Terramity geology rephase creates no drops");
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
        var moving=StasisBoundaryGeometry.upperPerimeterPoint(center,radius,halfHeight,0D);
        h.assertTrue(moving.y==center.getY()+halfHeight+1D
                        &&Math.abs(moving.x-(center.getX()-radius+.5D))<.001D
                        &&Math.abs(moving.z-(center.getZ()-radius+.5D))<.001D,
                "the moving Stasis marker follows the square on its actual upper boundary");
        var movingEast=StasisBoundaryGeometry.upperPerimeterPoint(center,radius,halfHeight,radius*2D);
        h.assertTrue(movingEast.y==moving.y&&Math.abs(movingEast.x-(center.getX()+radius+.5D))<.001D,
                "the upper pulse stays on the top plane as it passes the east edge");
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
    @GameTest(template="empty",batch="rework_each_93") public static void stasisDomainPreservesExternalPotionEffects(GameTestHelper h){
        var owner=player(h,ArtifactKind.INTERREGNUM,"StasisLeaseOwner");var ally=player(h,ArtifactKind.CHOIR,"StasisLeaseAlly");
        var team=h.getLevel().getScoreboard().addPlayerTeam("stasislease_"+owner.getUUID().toString().substring(0,7));
        h.getLevel().getScoreboard().addPlayerToTeam(owner.getScoreboardName(),team);h.getLevel().getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        var center=target(h);ally.setPos(center.getX()+1.5D,center.getY(),center.getZ()+.5D);h.getLevel().addFreshEntity(ally);
        var externalRegeneration=new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION,200,0,false,true,true);
        var externalDominion=new net.minecraft.world.effect.MobEffectInstance(SpecialPickaxes.DOMINION.get(),200,1,false,true,true);
        ally.addEffect(externalRegeneration);owner.addEffect(externalDominion);
        h.assertTrue(DomainFields.start(owner,owner.getMainHandItem(),ArtifactKind.INTERREGNUM,center,8),"the Stasis domain starts around both players");
        DomainFields.tick();
        h.assertTrue(ally.getEffect(net.minecraft.world.effect.MobEffects.REGENERATION)==externalRegeneration
                        &&externalRegeneration.getDuration()==200&&externalRegeneration.getAmplifier()==0,
                "the ally's external regeneration remains untouched when Stasis offers a stronger tier");
        h.assertTrue(owner.getEffect(SpecialPickaxes.DOMINION.get())==externalDominion&&externalDominion.getDuration()==200,
                "the owner's external Dominion effect is not replaced by the domain lease");
        DomainFields.stop(owner);
        h.assertTrue(owner.getEffect(SpecialPickaxes.DOMINION.get())==externalDominion,
                "stopping Stasis clears only its own Dominion lease and preserves the external effect");
        finish(h,owner);
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
        var activeFall=new net.minecraftforge.event.entity.living.LivingFallEvent(p,24F,1F);MinecraftForge.EVENT_BUS.post(activeFall);
        h.assertTrue(WorkQueue.protectsFall(p)&&activeFall.isCanceled(),"Core Drill immediately cancels fall damage during descent");
        h.assertTrue(WorkQueue.togglePause(p),"Core Drill can pause without losing its safety guard");WorkQueue.tick();
        var pausedFall=new net.minecraftforge.event.entity.living.LivingFallEvent(p,24F,1F);MinecraftForge.EVENT_BUS.post(pausedFall);
        h.assertTrue(pausedFall.isCanceled()&&p.hasEffect(MobEffects.SLOW_FALLING),"fall safety remains active while the drill is paused");
        h.assertTrue(WorkQueue.togglePause(p),"Core Drill resumes with the same guard");drain(p);
        h.assertTrue(!WorkQueue.busy(p)&&WorkQueue.protectsFall(p),"protection remains after mining ends while the player is airborne");
        var afterDrillFall=new net.minecraftforge.event.entity.living.LivingFallEvent(p,24F,1F);MinecraftForge.EVENT_BUS.post(afterDrillFall);
        h.assertTrue(afterDrillFall.isCanceled()&&p.hasEffect(MobEffects.SLOW_FALLING),"fall safety continues after the drill finishes until the player lands");
        p.setOnGround(true);WorkQueue.tick();h.assertTrue(!WorkQueue.protectsFall(p),"landing releases the temporary fall guard");
        var landedFall=new net.minecraftforge.event.entity.living.LivingFallEvent(p,24F,1F);MinecraftForge.EVENT_BUS.post(landedFall);
        h.assertTrue(!landedFall.isCanceled(),"the temporary guard cannot cancel later falls after landing");finish(h,p);
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
        var wearer=player(h,ArtifactKind.IRIDIUM,"LodeguardBearer");
        var allies=List.of(player(h,ArtifactKind.CHOIR,"LodeguardOne"),player(h,ArtifactKind.CHOIR,"LodeguardTwo"),
                player(h,ArtifactKind.CHOIR,"LodeguardThree"),player(h,ArtifactKind.CHOIR,"LodeguardFour"));
        var distant=player(h,ArtifactKind.CHOIR,"LodeguardDistant");var teammateEnemy=player(h,ArtifactKind.CHOIR,"LodeguardEnemy");
        var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("lg_"+wearer.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("lge_"+teammateEnemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(wearer.getScoreboardName(),team);
        for(var ally:allies)scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(distant.getScoreboardName(),team);scoreboard.addPlayerToTeam(teammateEnemy.getScoreboardName(),foes);
        for(int i=0;i<allies.size();i++)allies.get(i).setPos(wearer.getX()+i+1D,wearer.getY(),wearer.getZ());
        distant.setPos(wearer.getX()+7D,wearer.getY(),wearer.getZ());teammateEnemy.setPos(wearer.getX()+1D,wearer.getY(),wearer.getZ());
        var participants=new ArrayList<ServerPlayer>();participants.add(wearer);participants.addAll(allies);
        participants.add(distant);participants.add(teammateEnemy);for(var participant:participants)h.getLevel().addFreshEntity(participant);
        var attacker=EntityType.ZOMBIE.create(h.getLevel());attacker.setPos(wearer.getX()+2,wearer.getY(),wearer.getZ());h.getLevel().addFreshEntity(attacker);
        var hit=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,hit,ArtifactKind.IRIDIUM);
        var guard=SpecialPickaxes.LODEGUARD.get();var effect=wearer.getEffect(guard);
        h.assertTrue(Math.abs(hit.getAmount()-8.5F)<.001F,"Lodeguard trims the first hit by 15 percent");
        h.assertTrue(effect!=null&&effect.getDuration()==80,"the custom crystal ward lasts four seconds");
        h.assertTrue(Math.abs(wearer.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)-4D)<.01D,"the ward adds four armor points");
        for(int i=0;i<3;i++)h.assertTrue(allies.get(i).hasEffect(guard),"Lodeguard shares the crystal ward with one of three nearest teammates");
        h.assertTrue(!allies.get(3).hasEffect(guard)&&!distant.hasEffect(guard)&&!teammateEnemy.hasEffect(guard),
                "the shared counter excludes the fourth teammate, distant players and enemies");
        var repeat=new net.minecraftforge.event.entity.living.LivingHurtEvent(wearer,h.getLevel().damageSources().mobAttack(attacker),10F);
        ArtifactPassives.onCuriosDamage(wearer,repeat,ArtifactKind.IRIDIUM);
        h.assertTrue(repeat.getAmount()==10F&&wearer.getEffect(guard).getDuration()==80,"the eight-second cooldown prevents repeated triggers and refresh spam");
        h.assertTrue(guard.isDurationEffectTick(80,0)&&!guard.isDurationEffectTick(79,0),"the ward's crystal effect pulses every ten ticks");
        h.assertTrue(SpecialPickaxes.IRIDIUM_LODE_SHARD.get()!=null,"the Curios crystal ward uses the registered animated Lode Shard particle");
        for(var participant:participants)finish(h,participant);
    }
    @GameTest(template="empty",batch="legacy_each_19") public static void curiosIridiumSharesLodeSurgeAndHighlightsOnlyOreDrops(GameTestHelper h) {
        var p=player(h,ArtifactKind.CHOIR,"LodeBearer");var ally=player(h,ArtifactKind.CHOIR,"LodeAlly");
        var enemy=player(h,ArtifactKind.CHOIR,"LodeEnemy");var scoreboard=h.getLevel().getScoreboard();
        var team=scoreboard.addPlayerTeam("lode_"+p.getUUID().toString().substring(0,6));
        var foes=scoreboard.addPlayerTeam("lfoe_"+enemy.getUUID().toString().substring(0,6));
        scoreboard.addPlayerToTeam(p.getScoreboardName(),team);scoreboard.addPlayerToTeam(ally.getScoreboardName(),team);
        scoreboard.addPlayerToTeam(enemy.getScoreboardName(),foes);
        ally.setPos(p.getX()+3,p.getY(),p.getZ());enemy.setPos(p.getX()+2,p.getY(),p.getZ());
        h.getLevel().addFreshEntity(ally);h.getLevel().addFreshEntity(enemy);
        var pos=target(h);PlayerPlacedBlocks.get(h.getLevel()).unmark(pos);
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
        h.assertTrue(ally.hasEffect(lodeSurge)&&ally.getEffect(lodeSurge).getAmplifier()==0&&!enemy.hasEffect(lodeSurge),
                "Curios shares the active ore rhythm with nearby teammates, never hostiles");
        h.assertTrue(drop.isCurrentlyGlowing()&&drop.getPersistentData().getBoolean("specialpickaxesIridiumOreDrop"),"new ore loot gets Iridium's glow tag");
        h.assertTrue(drop.getPersistentData().hasUUID("specialpickaxesIridiumOwner")&&drop.getPersistentData().getUUID("specialpickaxesIridiumOwner").equals(p.getUUID()),"only the wearer owns the recalled drop");
        h.assertTrue(!OreDropHighlight.mark(drop,p),"a repeated observation does not replay the animated Iridium shard");
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
    @GameTest(template="empty",batch="rework_each_60") public static void iridiumRecallUsesExtendedRangeAndOnlyPullsOwnedOreDrops(GameTestHelper h){
        var wearer=player(h,ArtifactKind.IRIDIUM,"IridiumRecall");var level=h.getLevel();var origin=wearer.position();
        var near=new ItemEntity(level,origin.x+10D,origin.y+.5D,origin.z,new ItemStack(Items.DIAMOND));
        near.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);near.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        var beyond=new ItemEntity(level,origin.x+50D,origin.y+.5D,origin.z,new ItemStack(Items.DIAMOND));
        beyond.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);beyond.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        var unrelated=new ItemEntity(level,origin.x+8D,origin.y+.5D,origin.z,new ItemStack(Items.ROTTEN_FLESH));
        unrelated.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
        unrelated.getPersistentData().putUUID("specialpickaxesIridiumOwner",UUID.randomUUID());
        var farOwned=new ItemEntity(level,origin.x+47D,origin.y+.5D,origin.z,new ItemStack(Items.DIAMOND));
        farOwned.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);farOwned.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        level.getChunkAt(farOwned.blockPosition());
        level.addFreshEntity(farOwned);
        var nearerDrops=new java.util.ArrayList<ItemEntity>();
        for(int i=0;i<95;i++){
            var item=new ItemEntity(level,origin.x+12D+(i%10)*.5D,origin.y+.5D,origin.z+(i/10)*2.5D,new ItemStack(Items.DIAMOND));
            item.getItem().getOrCreateTag().putInt("testDropIndex",i);
            item.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);item.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
            level.getChunkAt(item.blockPosition());
            level.addFreshEntity(item);nearerDrops.add(item);
        }
        level.getChunkAt(near.blockPosition());level.getChunkAt(unrelated.blockPosition());
        level.addFreshEntity(near);level.addFreshEntity(unrelated);
        var center=wearer.getEyePosition();
        h.assertTrue(ArtifactTechniques.IRIDIUM_RECALL_RADIUS==48&&ArtifactTechniques.isIridiumRecallTarget(wearer,near,center,48D),"the active Lode recall reaches owned drops from 48 blocks");
        h.assertTrue(ArtifactTechniques.isIridiumRecallTarget(wearer,farOwned,center,48D),"the last eligible drop is inside the active radius before the target cap is applied");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,beyond,center,48D),"an owned ore drop outside the extended range is excluded");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,unrelated,center,48D),"another player's ore drop is excluded");
        h.assertTrue(ArtifactSkills.use(wearer,wearer.getMainHandItem(),ArtifactKind.IRIDIUM),"Iridium recall activates without requiring a target block");
        h.assertTrue(near.getDeltaMovement().x<-.5D,"an owned ore drop in the active test zone accelerates to the wearer");
        h.assertTrue(!farOwned.isCurrentlyGlowing(),"the 96-item cap leaves the farthest eligible drop untouched");
        long recalledNearer=nearerDrops.stream().filter(ItemEntity::isCurrentlyGlowing).count();
        String unrecalled=nearerDrops.stream().filter(item->!item.isCurrentlyGlowing()).limit(3)
                .map(item->"alive="+item.isAlive()+", owner="+item.getPersistentData().getUUID("specialpickaxesIridiumOwner")
                        +", distance="+item.position().distanceToSqr(center)+", tag="+item.getItem().getTag())
                .collect(java.util.stream.Collectors.joining("; "));
        h.assertTrue(recalledNearer==nearerDrops.size(),"the 96-item cap selects all closer owned drops first (glowing="
                +recalledNearer+"/"+nearerDrops.size()+", missing="+unrecalled+")");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,beyond,wearer.getEyePosition(),48D),"the 48-block exclusion remains in force after recalling nearby loot");
        h.assertTrue(!ArtifactTechniques.isIridiumRecallTarget(wearer,unrelated,wearer.getEyePosition(),48D),"a nearby drop owned by someone else remains excluded after recall");finish(h,wearer);
    }
    @GameTest(template="empty",batch="rework_each_82") public static void iridiumPassiveGathersOnlyItsNearbyMinedOre(GameTestHelper h){
        var wearer=player(h,ArtifactKind.CHOIR,"IridiumLodeSense");var level=h.getLevel();var origin=wearer.position();
        var owned=new ItemEntity(level,origin.x+4D,origin.y+.3D,origin.z,new ItemStack(Items.DIAMOND));
        owned.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
        owned.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        var unmarked=new ItemEntity(level,origin.x+3D,origin.y+.3D,origin.z,new ItemStack(Items.EMERALD));
        var foreign=new ItemEntity(level,origin.x+2D,origin.y+.3D,origin.z,new ItemStack(Items.DIAMOND));
        foreign.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
        foreign.getPersistentData().putUUID("specialpickaxesIridiumOwner",UUID.randomUUID());
        var distant=new ItemEntity(level,origin.x+9D,origin.y+.3D,origin.z,new ItemStack(Items.DIAMOND));
        distant.getPersistentData().putBoolean("specialpickaxesIridiumOreDrop",true);
        distant.getPersistentData().putUUID("specialpickaxesIridiumOwner",wearer.getUUID());
        level.addFreshEntity(owned);level.addFreshEntity(unmarked);level.addFreshEntity(foreign);level.addFreshEntity(distant);
        Vec3 unmarkedMotion=unmarked.getDeltaMovement(),foreignMotion=foreign.getDeltaMovement(),distantMotion=distant.getDeltaMovement();
        ArtifactPassives.tick(wearer,ArtifactKind.IRIDIUM);
        h.assertTrue(owned.getDeltaMovement().x<-.2D&&owned.hasImpulse,"Iridium's passive draws its own glowing ore toward the wearer");
        Vec3 firstPull=owned.getDeltaMovement();
        h.assertTrue(OreDropHighlight.drawNearbyDrops(wearer)==0&&owned.getDeltaMovement().equals(firstPull),
                "duplicate passive calls in the same tick neither rescan nor stack extra pull force");
        h.assertTrue(unmarked.getDeltaMovement().equals(unmarkedMotion),"the passive leaves ordinary item drops untouched");
        h.assertTrue(foreign.getDeltaMovement().equals(foreignMotion),"the passive leaves another wearer's marked drops untouched");
        h.assertTrue(distant.getDeltaMovement().equals(distantMotion),"the passive leaves owned ore beyond eight blocks untouched");finish(h,wearer);
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
        h.assertTrue(ArtifactSkills.feedbackKind(copied,ArtifactKind.WORLDBREAKER)==ArtifactKind.CRUCIBLE,
                "Worldbreaker copies the selected pick's single audio and particle signature");
        copied.getOrCreateTag().putString("copiedSkill","invalid");
        h.assertTrue(ArtifactSkills.feedbackKind(copied,ArtifactKind.WORLDBREAKER)==ArtifactKind.WORLDBREAKER,
                "invalid copied state falls back to Worldbreaker's own feedback safely");
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
        var rival=player(h,ArtifactKind.WORLDLOOM,"SecondWorldloom");
        var mob=EntityType.ZOMBIE.create(h.getLevel());mob.setNoAi(true);mob.setPos(owner.getX()+2,owner.getY(),owner.getZ());h.getLevel().addFreshEntity(mob);
        var anchor=mob.position();
        h.assertTrue(ArtifactActions.useAlternate(owner,owner.getMainHandItem(),ArtifactKind.WORLDLOOM),"Worldloom alternate catches a nearby hostile");
        h.assertTrue(!WorldloomSnare.bind(rival,mob),"a second Worldloom cannot reset the first owner's four-second root windup");
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
    @GameTest(template="empty",batch="rework_each_94") public static void worldloomRootVisualBurstsRespectTheServerTickBudget(GameTestHelper h){
        long tick=Long.MAX_VALUE;
        h.assertTrue(WorldloomSnare.reserveVisualParticles(tick,8)&&WorldloomSnare.reserveVisualParticles(tick,248),
                "complete root signatures fit exactly within the 256-particle tick allowance");
        h.assertTrue(!WorldloomSnare.reserveVisualParticles(tick,1),"over-budget root visuals are skipped as whole bursts");
        h.assertTrue(WorldloomSnare.reserveVisualParticles(tick-1,24),"the visual allowance refreshes on the following tick");
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
    @GameTest(template="empty",batch="rework_each_98") public static void heartwoodGraspRootsStayAtTheTargetsFeet(GameTestHelper h){
        var feet=new Vec3(2D,5D,-3D);
        for(int i=0;i<4;i++){
            var point=HeartwoodGraspEffect.rootFootPoint(feet,1.1D,i);
            double height=point.y-feet.y,radius=Math.hypot(point.x-feet.x,point.z-feet.z);
            h.assertTrue(height>=.03D&&height<=.08D,"root particles remain grounded around the target's feet");
            h.assertTrue(radius>=.25D&&radius<=.33D,"the four roots make a close ring around the trapped target");
        }
        var first=HeartwoodGraspEffect.rootFootPoint(feet,1.1D,0);
        var opposite=HeartwoodGraspEffect.rootFootPoint(feet,1.1D,2);
        h.assertTrue(Math.abs((first.x+opposite.x)*.5D-feet.x)<.001D
                        &&Math.abs((first.z+opposite.z)*.5D-feet.z)<.001D,
                "the root ring uses opposing points instead of piling vines on one side");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_99") public static void mobAuraAudienceIsLocalCappedAndBudgetable(GameTestHelper h){
        var level=h.getLevel();var mob=EntityType.ZOMBIE.create(level);var anchor=h.absolutePos(new BlockPos(5,2,6));
        mob.setPos(anchor.getX()+.5D,anchor.getY(),anchor.getZ()+.5D);level.addFreshEntity(mob);
        var eligible=new ArrayList<ServerPlayer>();
        for(int i=0;i<9;i++){
            var viewer=player(h,ArtifactKind.PALIMPSEST,"AuraViewer"+i);viewer.setPos(mob.getX()+1+i,mob.getY(),mob.getZ());
            level.addFreshEntity(viewer);eligible.add(viewer);
        }
        var spectator=player(h,ArtifactKind.PALIMPSEST,"AuraSpectator");spectator.setPos(mob.getX()+.5D,mob.getY(),mob.getZ());
        spectator.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);level.addFreshEntity(spectator);
        var distant=player(h,ArtifactKind.PALIMPSEST,"AuraDistant");distant.setPos(mob.getX()+26D,mob.getY(),mob.getZ());level.addFreshEntity(distant);
        var audience=ArtifactFeedback.entityAuraAudience(level,mob);
        h.assertTrue(audience.size()==8,"one mob aura is delivered to at most eight nearby players");
        h.assertTrue(!audience.contains(spectator)&&!audience.contains(distant),"spectators and players beyond 24 blocks do not receive cosmetic pulses");
        for(int i=1;i<audience.size();i++)h.assertTrue(audience.get(i-1).distanceToSqr(mob.position())<=audience.get(i).distanceToSqr(mob.position()),
                "the capped aura audience chooses nearest viewers first");
        h.assertTrue(eligible.containsAll(audience),"only eligible nearby players appear in the mob aura audience");
        for(var viewer:eligible)viewer.discard();spectator.discard();distant.discard();mob.discard();h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_100") public static void hellspecThermalSourcesAreDatapackExtensible(GameTestHelper h){
        for(var block:List.of(Blocks.FIRE,Blocks.SOUL_FIRE,Blocks.MAGMA_BLOCK,Blocks.CAMPFIRE,Blocks.SOUL_CAMPFIRE,Blocks.LAVA_CAULDRON))
            h.assertTrue(SearingShellEffect.isThermalSource(block.defaultBlockState()),"default heat source stays tagged: "+block);
        h.assertTrue(SearingShellEffect.isThermalSource(Blocks.LAVA.defaultBlockState()),"lava fluids remain a thermal source independently of block tags");
        h.assertTrue(!SearingShellEffect.isThermalSource(Blocks.STONE.defaultBlockState()),"ordinary geology does not trigger the Hellspec thermal mining bonus");
        h.succeed();
    }
    @GameTest(template="empty",batch="rework_each_79") public static void heldAuraKeepsAValidOrbitWhenLookingVertically(GameTestHelper h){
        var upward=CurioAuraGeometry.handSide(new Vec3(0D,1D,0D));
        var downward=CurioAuraGeometry.handSide(new Vec3(0D,-1D,0D));
        var horizontal=CurioAuraGeometry.handSide(new Vec3(0D,0D,-1D));
        for(var side:List.of(upward,downward,horizontal))
            h.assertTrue(Double.isFinite(side.x)&&Double.isFinite(side.y)&&Double.isFinite(side.z)
                            &&Math.abs(side.lengthSqr()-1D)<1.0E-6D,
                    "held relic particle orbit uses a finite unit side vector at vertical and horizontal aim");
        h.assertTrue(Math.abs(upward.x-1D)<1.0E-6D&&Math.abs(downward.x-1D)<1.0E-6D,
                "vertical aim uses the deterministic fallback axis");
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
        for(int count:List.of(2,4,8)){
            var samples=new java.util.HashSet<Integer>();
            for(int i=0;i<count;i++)samples.add(CurioAuraGeometry.icarusFeatherSample(i,count));
            h.assertTrue(samples.size()==count&&samples.stream().anyMatch(sample->sample<4)
                            &&samples.stream().anyMatch(sample->sample>=4),
                    "Icarus keeps both wings visible at every particle quality");
        }
        var folded=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,3,false);
        var spread=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,3,true);
        h.assertTrue(Math.abs(folded.x-origin.x)<Math.abs(spread.x-origin.x),
                "Icarus' wings stay folded at rest and spread when soaring");
        var braking=CurioAuraGeometry.icarusFeatherPoint(origin,0D,0D,3,false,true);
        h.assertTrue(Math.abs(braking.x-origin.x)<Math.abs(folded.x-origin.x)&&braking.y<origin.y,
                "Icarus tucks its wings close and down while braking for a landing");
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
        h.assertTrue(audience==ArtifactFeedback.nearbyWardAudience(owner),"multiple aura pulses in one tick reuse the wearer's nearby-player scan");
        h.assertTrue(audience.size()==4&&audience.get(0)==owner,"ward auras include their bearer and cap observers at three");
        for(int i=0;i<3;i++)h.assertTrue(audience.contains(observers.get(i)),"the three nearest observers see the passive ward");
        h.assertTrue(!audience.contains(observers.get(3))&&!audience.contains(observers.get(4)),"observers outside the three nearest do not receive repeated aura packets");
        owner.setPos(owner.getX()+.25D,owner.getY(),owner.getZ());
        h.assertTrue(audience!=ArtifactFeedback.nearbyWardAudience(owner),"moving the bearer invalidates the audience cache immediately");
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
