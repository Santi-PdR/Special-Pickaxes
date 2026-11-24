package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.*;
import java.util.*;
import net.minecraftforge.gametest.*;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;

@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class CuriosEffectGameTests {
    @GameTest(template="empty",batch="curios_effect_reform7") public static void equippedEffectsNeverStartMiningOrFlight(GameTestHelper h){
        var distinct=new HashSet<Map<MobEffect,Integer>>();
        for(var kind:ArtifactKind.playableValues()){
            var p=player(h,kind);var at=target(h,p);var source=p.getMainHandItem();var held=new ItemStack(Items.STICK);p.setItemInHand(InteractionHand.MAIN_HAND,held);var position=p.position();var motion=p.getDeltaMovement();
            h.assertTrue(CuriosEffectActive.activate(p,source,()->source),"effect accepts equipped "+kind.id);
            h.assertTrue(!p.getActiveEffects().isEmpty(),"activation grants effect "+kind.id);
            var actual=new HashMap<MobEffect,Integer>();
            for(var effect:p.getActiveEffects()){
                actual.put(effect.getEffect(),effect.getAmplifier());
                h.assertTrue(effect.getDuration()==200&&!effect.isVisible(),"bounded 10-second buff without particle spam "+kind.id);
            }
            h.assertTrue(actual.equals(expected(kind)),"exact source-specific effect levels "+kind.id+" "+actual);
            if(kind!=ArtifactKind.IRIDIUM&&kind!=ArtifactKind.SEAM_RIPPER)
                h.assertTrue(distinct.add(actual),"redesigned activation profile is not duplicated "+kind.id);
            h.assertTrue(!WorkQueue.busy(p)&&!IcarianFlight.active(p),"effect creates no block program or ascent "+kind.id);
            h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.STONE)&&source.getDamageValue()==0&&p.getMainHandItem()==held,"no mining, source wear or hand swap "+kind.id);
            h.assertTrue(p.position().equals(position)&&p.getDeltaMovement().equals(motion),"activation applies no movement "+kind.id);
            h.assertTrue(!CuriosEffectActive.activate(p,source,()->source),"bounded cooldown "+kind.id);
            MiningObservations.forget(p);
        }h.succeed();
    }
    private static Map<MobEffect,Integer> expected(ArtifactKind kind){
        return switch(kind){
            case IRIDIUM,SEAM_RIPPER -> Map.of(MobEffects.DIG_SPEED,0);
            case PALIMPSEST -> Map.of(MobEffects.DIG_SPEED,1);
            case CHOIR -> Map.of(MobEffects.DAMAGE_RESISTANCE,2);
            case EVENTIDE -> Map.of(MobEffects.NIGHT_VISION,0,MobEffects.WATER_BREATHING,0);
            case CRUCIBLE -> Map.of(MobEffects.DIG_SPEED,3,MobEffects.MOVEMENT_SPEED,1);
            case INTERREGNUM -> Map.of(MobEffects.ABSORPTION,3);
            case WORLDLOOM -> Map.of(MobEffects.REGENERATION,2);
            case ICARUS -> Map.of(MobEffects.JUMP,2,MobEffects.SLOW_FALLING,0);
            case AXIOM -> Map.of(MobEffects.INVISIBILITY,0);
            case EXODIUM -> Map.of(MobEffects.MOVEMENT_SPEED,2);
            case HELLSPEC -> Map.of(MobEffects.FIRE_RESISTANCE,0,MobEffects.DAMAGE_RESISTANCE,0);
            case WORLDBREAKER -> Map.of(MobEffects.ABSORPTION,1,MobEffects.DAMAGE_RESISTANCE,1);
            default -> Map.of();
        };
    }
    @GameTest(template="empty",batch="curios_copied_effect_profiles") public static void copiedEquippedBonusUsesSourceProfileWithoutChangingTerrain(GameTestHelper h){
        for(var kind:ArtifactKind.playableValues()){
            if(kind==ArtifactKind.WORLDBREAKER)continue;
            var p=player(h,ArtifactKind.WORLDBREAKER);var at=target(h,p);var source=p.getMainHandItem();
            source.getOrCreateTag().putString("copiedSkill",kind.id);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.STICK));
            h.assertTrue(CuriosEffectActive.activate(p,source,()->source),"copied effect activates "+kind.id);
            var actual=new HashMap<MobEffect,Integer>();for(var effect:p.getActiveEffects())actual.put(effect.getEffect(),effect.getAmplifier());
            h.assertTrue(actual.equals(expected(kind)),"copied effect retains source profile "+kind.id);
            h.assertTrue(!WorkQueue.busy(p)&&!IcarianFlight.active(p)&&h.getLevel().getBlockState(at).is(Blocks.STONE)&&source.getDamageValue()==0,"copy never mines or ascends "+kind.id);
            MiningObservations.forget(p);
        }h.succeed();
    }
    @GameTest(template="empty",batch="curios_external_effect_preservation") public static void activationDoesNotDowngradeStrongerExternalEffect(GameTestHelper h){
        var p=player(h,ArtifactKind.CHOIR);var source=p.getMainHandItem();
        p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,1200,4));
        h.assertTrue(CuriosEffectActive.activate(p,source,()->source),"activation accepts existing stronger effect");
        var effect=p.getEffect(MobEffects.DAMAGE_RESISTANCE);
        h.assertTrue(effect!=null&&effect.getAmplifier()==4&&effect.getDuration()==1200,"vanilla merge retains stronger external protection");
        MiningObservations.forget(p);h.succeed();
    }

    @GameTest(template="empty",batch="curios_partial_revocation_cooldown") public static void revokedFirstBonusCannotBeRepeatedWithoutCooldown(GameTestHelper h){
        var p=player(h,ArtifactKind.HELLSPEC);var source=p.getMainHandItem();
        var current=new java.util.concurrent.atomic.AtomicReference<ItemStack>(source);long now=ArtifactState.now(p);int[] calls={0};
        java.util.function.Consumer<net.minecraftforge.event.entity.living.MobEffectEvent.Added> revoke=e->{
            if(e.getEntity()==p&&e.getEffectInstance().getEffect()==MobEffects.FIRE_RESISTANCE){calls[0]++;current.set(ItemStack.EMPTY);}
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,false,net.minecraftforge.event.entity.living.MobEffectEvent.Added.class,revoke);
        try{
            h.assertTrue(!CuriosEffectActive.activate(p,source,current::get)&&calls[0]==1,"revoke during first bonus aborts remaining activation");
            h.assertTrue(p.hasEffect(MobEffects.FIRE_RESISTANCE)&&!p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"only already-applied first bonus survives, no stale second bonus");
            h.assertTrue(ArtifactState.of(p,ArtifactKind.HELLSPEC).getLong("curiosEffectReady")==now+200,"partial benefit reserves the source cooldown");
            current.set(source);
            h.assertTrue(!CuriosEffectActive.activate(p,source,current::get)&&calls[0]==1,"reequipping cannot repeat partial benefit for free");
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(revoke);}
        long time=h.getLevel().getGameTime();
        try{
            h.getLevel().getServer().getWorldData().overworldData().setGameTime(time+200);
            h.assertTrue(CuriosEffectActive.activate(p,source,current::get)&&p.hasEffect(MobEffects.DAMAGE_RESISTANCE),"cooldown expiry and reentry cleanup allow ordinary retry");
        }finally{h.getLevel().getServer().getWorldData().overworldData().setGameTime(time);MiningObservations.forget(p);}
        h.succeed();
    }
    @GameTest(template="empty",batch="curios_partial_copy_cooldown") public static void retaggedCopyKeepsOriginalSourceCooldownAfterFirstBonus(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var source=p.getMainHandItem();source.getOrCreateTag().putString("copiedSkill",ArtifactKind.ICARUS.id);
        long now=ArtifactState.now(p);int[] calls={0};
        java.util.function.Consumer<net.minecraftforge.event.entity.living.MobEffectEvent.Added> retag=e->{
            if(e.getEntity()==p&&e.getEffectInstance().getEffect()==MobEffects.JUMP){calls[0]++;source.getOrCreateTag().putString("copiedSkill",ArtifactKind.EVENTIDE.id);}
        };
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.NORMAL,false,net.minecraftforge.event.entity.living.MobEffectEvent.Added.class,retag);
        try{
            h.assertTrue(!CuriosEffectActive.activate(p,source,()->source)&&calls[0]==1,"retagged copy aborts after first effect callback");
            h.assertTrue(p.hasEffect(MobEffects.JUMP)&&!p.hasEffect(MobEffects.SLOW_FALLING),"stale copied profile grants no second effect");
            h.assertTrue(ArtifactState.of(p,ArtifactKind.ICARUS).getLong("curiosEffectReady")==now+200,"cooldown belongs to activated source, not replacement copy");
            source.getOrCreateTag().putString("copiedSkill",ArtifactKind.ICARUS.id);
            h.assertTrue(!CuriosEffectActive.activate(p,source,()->source)&&calls[0]==1,"restoring copy cannot bypass source cooldown");
        }finally{net.minecraftforge.common.MinecraftForge.EVENT_BUS.unregister(retag);MiningObservations.forget(p);}
        h.succeed();
    }

}
