package io.github.santipdr.specialpickaxes.test;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import io.github.santipdr.specialpickaxes.ability.AbilityUseEvent;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;
@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class WorldbreakerPassiveCopyGameTests {
    private static ItemStack copy(ArtifactKind kind){var stack=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get());stack.getOrCreateTag().putString("copiedSkill",kind.id);return stack;}
    @GameTest(template="empty",batch="worldbreaker_passive_01") public static void copiedSearingShellReplacesNativeAegisAndCanBeRemoved(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var stack=copy(ArtifactKind.HELLSPEC);var slot=new AtomicReference<ItemStack>(stack);
        WorldbreakerCuriosPassives.tick(p,stack,slot::get);
        h.assertTrue(p.hasEffect(SpecialPickaxes.SEARING_SHELL.get())&&!p.hasEffect(SpecialPickaxes.WORLDBREAKER_AEGIS.get()),"source shell granted instead of stacking native aegis");
        slot.set(ItemStack.EMPTY);ArtifactPassives.onCuriosUnequip(p,ArtifactKind.HELLSPEC);
        h.assertTrue(!p.hasEffect(SpecialPickaxes.SEARING_SHELL.get()),"owned copied shell removed on unequip");finish(h,p);
    }
    @GameTest(template="empty",batch="worldbreaker_passive_02") public static void callbackRevocationRemovesOwnedCopiedEffect(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var stack=copy(ArtifactKind.HELLSPEC);var slot=new AtomicReference<ItemStack>(stack);int[] calls={0};
        Consumer<MobEffectEvent.Added> revoke=e->{if(e.getEntity()==p&&e.getEffectInstance().getEffect()==SpecialPickaxes.SEARING_SHELL.get()){calls[0]++;slot.set(ItemStack.EMPTY);}};
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,MobEffectEvent.Added.class,revoke);
        try{WorldbreakerCuriosPassives.tick(p,stack,slot::get);h.assertTrue(calls[0]==1&&!p.hasEffect(SpecialPickaxes.SEARING_SHELL.get()),"source revoked inside effect callback cannot retain owned shell");}
        finally{MinecraftForge.EVENT_BUS.unregister(revoke);finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_passive_03") public static void copiedCombatReactionUsesSourceAndRejectsChangedCopy(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var stack=copy(ArtifactKind.HELLSPEC);var slot=new AtomicReference<ItemStack>(stack);var enemy=EntityType.ZOMBIE.create(h.getLevel());enemy.setPos(p.position().add(2,0,0));h.getLevel().addFreshEntity(enemy);
        var event=new LivingHurtEvent(p,h.getLevel().damageSources().mobAttack(enemy),10F);
        Consumer<AbilityUseEvent> change=e->{if(e.player==p)stack.getOrCreateTag().putString("copiedSkill",ArtifactKind.AXIOM.id);};MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL,false,AbilityUseEvent.class,change);
        try{WorldbreakerCuriosPassives.hurt(p,stack,slot::get,event,false);h.assertTrue(event.getAmount()==10F&&!enemy.isOnFire()&&ArtifactState.of(p,ArtifactKind.HELLSPEC).getLong("curiosPassiveReady")==0,"changed source cannot retaliate or spend old cooldown");}
        finally{MinecraftForge.EVENT_BUS.unregister(change);enemy.discard();finish(h,p);}
    }
    @GameTest(template="empty",batch="worldbreaker_passive_04") public static void passiveResolverPreservesNativePicksAndRejectsRecursiveCopies(GameTestHelper h){
        for(var kind:ArtifactKind.playableValues()){
            var nativeStack=new ItemStack(SpecialPickaxes.PICKS.get(kind).get());h.assertTrue(WorldbreakerCuriosPassives.sourceKind(nativeStack)==kind,"native source unchanged");
            if(kind!=ArtifactKind.WORLDBREAKER)h.assertTrue(WorldbreakerCuriosPassives.sourceKind(copy(kind))==kind,"copied passive source resolves");
        }
        h.assertTrue(WorldbreakerCuriosPassives.sourceKind(copy(ArtifactKind.WORLDBREAKER))==null,"self copy rejected");var invalid=copy(ArtifactKind.AXIOM);invalid.getOrCreateTag().putString("copiedSkill","invalid");h.assertTrue(WorldbreakerCuriosPassives.sourceKind(invalid)==null,"invalid copy rejected");h.succeed();
    }
    @GameTest(template="empty",batch="worldbreaker_passive_05") public static void liveCopiedCombatUsesInfernalReactionWithoutNativeAbsorption(GameTestHelper h){
        var p=player(h,ArtifactKind.WORLDBREAKER);var stack=copy(ArtifactKind.HELLSPEC);var enemy=EntityType.ZOMBIE.create(h.getLevel());enemy.setPos(p.position().add(2,0,0));h.getLevel().addFreshEntity(enemy);
        var event=new LivingHurtEvent(p,h.getLevel().damageSources().mobAttack(enemy),10F);
        try{WorldbreakerCuriosPassives.hurt(p,stack,()->stack,event,false);h.assertTrue(event.getAmount()==8F&&enemy.isOnFire()&&!p.hasEffect(net.minecraft.world.effect.MobEffects.ABSORPTION),"infernal source reaction replaces native absorption reaction");}
        finally{enemy.discard();finish(h,p);}
    }
}
