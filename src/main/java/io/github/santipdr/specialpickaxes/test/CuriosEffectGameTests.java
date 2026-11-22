package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.*;
import static io.github.santipdr.specialpickaxes.test.SeismicReworkGameTests.*;

@GameTestHolder("specialpickaxes")
@PrefixGameTestTemplate(false)
public final class CuriosEffectGameTests {
    @GameTest(template="empty",batch="curios_effect_reform7") public static void equippedEffectsNeverStartMiningOrFlight(GameTestHelper h){
        for(var kind:ArtifactKind.playableValues()){
            var p=player(h,kind);var at=target(h,p);var source=p.getMainHandItem();var held=new ItemStack(Items.STICK);p.setItemInHand(InteractionHand.MAIN_HAND,held);var position=p.position();var motion=p.getDeltaMovement();
            h.assertTrue(CuriosEffectActive.activate(p,source,()->source),"effect accepts equipped "+kind.id);
            h.assertTrue(!p.getActiveEffects().isEmpty(),"activation grants effect "+kind.id);
            h.assertTrue(!WorkQueue.busy(p)&&!IcarianFlight.active(p),"effect creates no block program or ascent "+kind.id);
            h.assertTrue(h.getLevel().getBlockState(at).is(Blocks.STONE)&&source.getDamageValue()==0&&p.getMainHandItem()==held,"no mining, source wear or hand swap "+kind.id);
            h.assertTrue(p.position().equals(position)&&p.getDeltaMovement().equals(motion),"activation applies no movement "+kind.id);
            h.assertTrue(!CuriosEffectActive.activate(p,source,()->source),"bounded cooldown "+kind.id);
            MiningObservations.forget(p);
        }h.succeed();
    }
}
