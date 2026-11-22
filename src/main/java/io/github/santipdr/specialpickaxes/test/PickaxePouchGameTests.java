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
    @GameTest(template="empty",batch="pouch_capture_r7") public static void enabledPouchCapturesNativeAbilityLootBeforeEntityJoins(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.EVENTIDE);var at=SeismicReworkGameTests.target(h,p);var tool=p.getMainHandItem();
        tool.getOrCreateTag().putBoolean("materialPouchEnabled",true);
        h.assertTrue(WorldSafety.mine(p,tool,ArtifactKind.EVENTIDE,at,h.getLevel().getBlockState(at)),"native break succeeds");
        var out=PickaxePouch.extract(tool,0,64);
        h.assertTrue(out.is(Items.COBBLESTONE)&&out.getCount()==1,"native stone loot conserved inside enabled pouch");
        h.assertTrue(h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(2)).isEmpty(),"captured material never joins world as ticking item entity");
        SeismicReworkGameTests.finish(h,p);
    }

    @GameTest(template="empty",batch="pouch_menu_full_r7") public static void fullInventoryWithdrawalReturnsEverythingToPouch(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.EVENTIDE);var tool=p.getMainHandItem();
        for(int slot=0;slot<36;slot++)if(slot!=p.getInventory().selected)p.getInventory().setItem(slot,new ItemStack(Items.COBBLESTONE,64));
        h.assertTrue(PickaxePouch.insert(tool,new ItemStack(Items.DIAMOND,64)).isEmpty(),"fixture material stored");
        PickaxePouchMenu.open(p,tool);
        h.assertTrue(p.containerMenu instanceof PickaxePouchMenu,"server withdrawal menu opens");
        p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);
        h.assertTrue(PickaxePouch.extract(tool,0,64).getCount()==64,"full inventory loses no material during withdrawal");
        p.closeContainer();SeismicReworkGameTests.finish(h,p);
    }
    @GameTest(template="empty",batch="pouch_menu_extract_r7") public static void menuWithdrawalCannotDuplicateDisplayStacks(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.EVENTIDE);var tool=p.getMainHandItem();
        PickaxePouch.insert(tool,new ItemStack(Items.DIAMOND,17));PickaxePouchMenu.open(p,tool);
        p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,p);
        p.containerMenu.clicked(0,0,net.minecraft.world.inventory.ClickType.PICKUP,p);
        p.containerMenu.clicked(26,0,net.minecraft.world.inventory.ClickType.PICKUP,p);
        p.containerMenu.clicked(26,0,net.minecraft.world.inventory.ClickType.QUICK_MOVE,p);
        int total=0;for(int slot=0;slot<p.getInventory().getContainerSize();slot++)if(p.getInventory().getItem(slot).is(Items.DIAMOND))total+=p.getInventory().getItem(slot).getCount();
        h.assertTrue(total==17&&PickaxePouch.extract(tool,0,64).isEmpty(),"display and page arrow cannot be duplicated or taken");
        h.assertTrue(p.containerMenu.getCarried().isEmpty(),"read-only display leaves cursor empty");
        p.containerMenu.clicked(26,1,net.minecraft.world.inventory.ClickType.PICKUP,p);
        h.assertTrue(!tool.getTag().getBoolean("materialPouchEnabled"),"right click on navigation toggles capture off");
        p.closeContainer();PickaxePouchMenu.open(p,tool);
        h.assertTrue(!tool.getTag().getBoolean("materialPouchEnabled"),"reopening preserves explicit capture preference");
        p.closeContainer();SeismicReworkGameTests.finish(h,p);
    }

    @GameTest(template="empty",batch="pouch_paired_volume_r7") public static void sameNativeExcavationConservesLootWithAndWithoutPouch(GameTestHelper h){
        int[] counts=new int[2];
        for(int variant=0;variant<2;variant++){
            boolean enabled=variant==1;var p=SeismicReworkGameTests.player(h,ArtifactKind.WORLDBREAKER);var at=SeismicReworkGameTests.target(h,p);var tool=p.getMainHandItem();
            tool.getOrCreateTag().putBoolean("materialPouchEnabled",enabled);
            var cells=SignatureExcavation.footprint(at,net.minecraft.core.Direction.SOUTH,ArtifactKind.WORLDBREAKER,false);
            for(var cell:cells){h.getLevel().setBlock(cell,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
            try{
                h.assertTrue(SignatureExcavation.start(p,tool,ArtifactKind.WORLDBREAKER,false),"paired native signature starts, pouch="+enabled);
                SynchronousWorkDrain.drain(p,1000);
                h.assertTrue(!WorkQueue.busy(p)&&cells.stream().allMatch(cell->h.getLevel().getBlockState(cell).isAir()),"same full signature finishes in both variants");
                var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(12));
                int world=drops.stream().filter(d->d.getItem().is(Items.COBBLESTONE)).mapToInt(d->d.getItem().getCount()).sum();
                int stored=PickaxePouch.amount(tool,0);counts[variant]=drops.size();
                h.assertTrue(world+stored==cells.size(),"exact native stone-loot conservation, pouch="+enabled);
                h.assertTrue(enabled?world==0&&stored==cells.size():stored==0&&world==cells.size(),"capture changes storage location, not loot amount");
                com.mojang.logging.LogUtils.getLogger().info("POUCH_PAIRED_VOLUME enabled={} blocks={} world_items={} stored_items={} item_entities={}",enabled,cells.size(),world,stored,drops.size());
                drops.forEach(net.minecraft.world.entity.Entity::discard);
            }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        }
        h.assertTrue(counts[0]>0&&counts[1]==0,"enabled pouch eliminates item entities for the same supported material volume");h.succeed();
    }

    @GameTest(template="empty",batch="pouch_read_isolation") public static void previewAndWithdrawalCannotMutateStoredPrototype(GameTestHelper h){
        var tool=tool();PickaxePouch.insert(tool,new ItemStack(Items.DIAMOND,64));
        var before=tool.getTag().copy();var preview=PickaxePouch.preview(tool,0);
        preview.setCount(1);preview.getOrCreateTag().putString("foreign","changed");
        h.assertTrue(PickaxePouch.amount(tool,0)==64&&PickaxePouch.types(tool)==1&&tool.getTag().equals(before),"read APIs and mutable preview cannot rewrite storage");
        var extracted=PickaxePouch.extract(tool,0,1);extracted.getOrCreateTag().putString("foreign","changed");
        h.assertTrue(PickaxePouch.amount(tool,0)==63&&!PickaxePouch.preview(tool,0).hasTag(),"withdrawal cannot alias stored prototype");h.succeed();
    }

    @GameTest(template="empty",batch="pouch_enchanted_native") public static void pouchKeepsNativeSilkAndFortuneLoot(GameTestHelper h){
        for(boolean silk:new boolean[]{true,false}){
            var p=SeismicReworkGameTests.player(h,ArtifactKind.EVENTIDE);var at=SeismicReworkGameTests.target(h,p);var tool=p.getMainHandItem();
            tool.getOrCreateTag().putBoolean("materialPouchEnabled",true);
            tool.enchant(silk?net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH:net.minecraft.world.item.enchantment.Enchantments.BLOCK_FORTUNE,silk?1:3);
            h.getLevel().setBlock(at,net.minecraft.world.level.block.Blocks.DIAMOND_ORE.defaultBlockState(),2);
            h.assertTrue(WorldSafety.mine(p,tool,ArtifactKind.EVENTIDE,at,h.getLevel().getBlockState(at)),"enchanted native break succeeds");
            var out=PickaxePouch.extract(tool,0,64);
            h.assertTrue(silk?out.is(Items.DIAMOND_ORE)&&out.getCount()==1:out.is(Items.DIAMOND)&&out.getCount()>=1&&out.getCount()<=4,"capture retains native Silk/Fortune loot, not a replacement drop table");
            h.assertTrue(PickaxePouch.types(tool)==0&&h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(2)).isEmpty(),"one withdrawal exhausts captured loot without duplicate world drops");
            WorkQueue.cancel(p);MiningObservations.forget(p);
        }
        h.succeed();
    }

    @GameTest(template="empty",batch="pouch_saturated_native_r7") public static void saturatedDiversePouchLeavesExactNativeOverflowInWorld(GameTestHelper h){
        var p=SeismicReworkGameTests.player(h,ArtifactKind.WORLDBREAKER);var at=SeismicReworkGameTests.target(h,p);var tool=p.getMainHandItem();
        var materials=net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
                .filter(item->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft")&&item!=Items.COBBLESTONE)
                .filter(item->{var stack=new ItemStack(item);return !stack.isEmpty()&&stack.isStackable()&&!stack.hasTag()&&!stack.serializeNBT().contains("ForgeCaps");})
                .limit(PickaxePouch.MAX_TYPES).toList();
        h.assertTrue(materials.size()==64,"diverse vanilla material fixture available");
        for(var material:materials)h.assertTrue(PickaxePouch.insert(tool,new ItemStack(material,PickaxePouch.MAX_PER_TYPE)).isEmpty(),"fill each bounded material entry");
        tool.getOrCreateTag().putBoolean("materialPouchEnabled",true);var before=tool.getTag().getList("materialPouchV1",net.minecraft.nbt.Tag.TAG_COMPOUND).copy();
        for(int i=0;i<256;i++){
            var item=i%2==0?materials.get(63):Items.COBBLESTONE;var incoming=new ItemStack(item,64);
            h.assertTrue(ItemStack.matches(PickaxePouch.insert(tool,incoming),incoming),"full entry or type table returns exact independent remainder");
        }
        var aim=p.getLookAngle();var cells=SignatureExcavation.footprint(at,net.minecraft.core.Direction.getNearest(aim.x,aim.y,aim.z),ArtifactKind.WORLDBREAKER,false);
        for(var cell:cells){h.getLevel().setBlock(cell,net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(),2);PlayerPlacedBlocks.get(h.getLevel()).unmark(cell);}
        try{
            h.assertTrue(SignatureExcavation.start(p,tool,ArtifactKind.WORLDBREAKER,false),"full pouch does not block mining");SynchronousWorkDrain.drain(p,1000);
            var drops=h.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(at).inflate(12));
            int amount=drops.stream().filter(d->d.getItem().is(Items.COBBLESTONE)).mapToInt(d->d.getItem().getCount()).sum();
            h.assertTrue(!WorkQueue.busy(p)&&cells.stream().allMatch(cell->h.getLevel().getBlockState(cell).isAir())&&amount==cells.size(),"native overflow conserved in world without pausing");
            h.assertTrue(tool.getTag().getList("materialPouchV1",net.minecraft.nbt.Tag.TAG_COMPOUND).equals(before),"rejections and native overflow never rewrite saturated storage");
            for(int slot=0;slot<64;slot++)h.assertTrue(PickaxePouch.amount(tool,slot)==4096&&PickaxePouch.preview(tool,slot).getItem()==materials.get(slot),"every stored type and amount intact");
            com.mojang.logging.LogUtils.getLogger().info("POUCH_SATURATED types=64 stored_items=262144 rejected_stacks=256 native_overflow_items={}",amount);
            drops.forEach(net.minecraft.world.entity.Entity::discard);
        }finally{WorkQueue.cancel(p);MiningObservations.forget(p);}
        h.succeed();
    }

    @GameTest(template="empty",batch="pouch_many_type_match_r7") public static void matchingLastEntryPreservesOtherTypesAndLegacyIds(GameTestHelper h){
        var tool=tool();var materials=net.minecraft.core.registries.BuiltInRegistries.ITEM.stream()
                .filter(item->net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace().equals("minecraft"))
                .filter(item->{var stack=new ItemStack(item);return !stack.isEmpty()&&stack.isStackable()&&!stack.hasTag()&&!stack.serializeNBT().contains("ForgeCaps");})
                .limit(64).toList();
        h.assertTrue(materials.size()==64,"64 distinct ordinary material fixture");
        for(var material:materials)h.assertTrue(PickaxePouch.insert(tool,new ItemStack(material)).isEmpty(),"add distinct material");
        var entries=tool.getTag().getList("materialPouchV1",net.minecraft.nbt.Tag.TAG_COMPOUND);
        var encoded=entries.getCompound(63).getCompound("item");
        encoded.putString("id",net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(materials.get(63)).getPath());
        var before=entries.copy();var incoming=new ItemStack(materials.get(63),7);
        h.assertTrue(PickaxePouch.insert(tool,incoming).isEmpty()&&incoming.getCount()==7,"last entry merges even with full type table and unqualified legacy id");
        h.assertTrue(PickaxePouch.types(tool)==64&&PickaxePouch.amount(tool,63)==8,"merge changes only intended quantity, never appends another type");
        for(int slot=0;slot<63;slot++)h.assertTrue(tool.getTag().getList("materialPouchV1",net.minecraft.nbt.Tag.TAG_COMPOUND).getCompound(slot).equals(before.getCompound(slot)),"unrelated entry remains byte-equivalent "+slot);
        var out=PickaxePouch.extract(tool,63,64);
        h.assertTrue(out.getItem()==materials.get(63)&&out.getCount()==8&&PickaxePouch.types(tool)==63,"all merged material withdraws exactly once");
        h.succeed();
    }

}
