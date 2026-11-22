package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.minecraftforge.gametest.*;

@GameTestHolder(SpecialPickaxes.ID)
@PrefixGameTestTemplate(false)
public final class PickaxePouchGameTests {
    private static ItemStack tool(){return new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());}
    @GameTest(template="empty",batch="pouch_01") public static void pouchMergesMaterialsAndSurvivesSerialization(GameTestHelper h){
        var tool=tool();var loot=new ItemStack(Items.COBBLESTONE,64);
        for(int i=0;i<10;i++)h.assertTrue(PickaxePouch.insert(tool,loot).isEmpty(),"ordinary material accepted");
        h.assertTrue(loot.getCount()==64,"input loot is never mutated");
        var loaded=ItemStack.of(tool.save(new net.minecraft.nbt.CompoundTag()));int total=0;
        for(int i=0;i<10;i++){var out=PickaxePouch.extract(loaded,0,Integer.MAX_VALUE);h.assertTrue(out.is(Items.COBBLESTONE)&&out.getCount()==64,"withdrawal bounded to ordinary stack");total+=out.getCount();}
        h.assertTrue(total==640&&PickaxePouch.extract(loaded,0,64).isEmpty(),"exact conservation across save/load and exhaustion");h.succeed();
    }
    @GameTest(template="empty",batch="pouch_02") public static void pouchOverflowReturnsRemainderWithoutDeletingMaterials(GameTestHelper h){
        var tool=tool();for(int i=0;i<64;i++)h.assertTrue(PickaxePouch.insert(tool,new ItemStack(Items.DIAMOND,64)).isEmpty(),"fill bounded material entry");
        var excess=new ItemStack(Items.DIAMOND,17);var remainder=PickaxePouch.insert(tool,excess);
        h.assertTrue(remainder.getCount()==17&&excess.getCount()==17,"full entry returns complete excess");
        int total=0;for(int i=0;i<64;i++)total+=PickaxePouch.extract(tool,0,64).getCount();
        h.assertTrue(total==PickaxePouch.MAX_PER_TYPE&&PickaxePouch.extract(tool,0,64).isEmpty(),"capacity is bounded and stored amount conserved");h.succeed();
    }
    @GameTest(template="empty",batch="pouch_03") public static void pouchRejectsNestedAndTaggedItemsAndInvalidRequests(GameTestHelper h){
        var tool=tool();var tagged=new ItemStack(Items.DIAMOND,4);tagged.getOrCreateTag().putString("foreignData","preserve me");
        var remainder=PickaxePouch.insert(tool,tagged);
        h.assertTrue(ItemStack.matches(remainder,tagged),"foreign data returned unchanged");
        h.assertTrue(ItemStack.matches(PickaxePouch.insert(tool,tool.copy()),tool),"no nested artifact storage");
        h.assertTrue(PickaxePouch.insert(new ItemStack(Items.STICK),new ItemStack(Items.DIAMOND)).is(Items.DIAMOND),"non artifact owner rejected");
        h.assertTrue(PickaxePouch.extract(tool,-1,64).isEmpty()&&PickaxePouch.extract(tool,0,-1).isEmpty(),"invalid requests are inert");h.succeed();
    }
}
