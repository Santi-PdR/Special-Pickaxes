package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.phys.Vec3;

/** Real optional Curios inventory + physical G key. No synthetic packets or activation calls. */
final class EquippedClientValidation {
    private static final BlockPos TARGET=new BlockPos(200,101,204),PROBE=TARGET.east(3);
    private EquippedClientValidation(){}
    private static void equip(ServerPlayer p,ItemStack stack){
        try{
            var api=Class.forName("top.theillusivec4.curios.api.CuriosApi");
            Object lazy=api.getMethod("getCuriosInventory",net.minecraft.world.entity.LivingEntity.class).invoke(null,p);
            Object inventory=((net.minecraftforge.common.util.LazyOptional<?>)lazy).orElse(null);
            if(inventory==null)throw new IllegalStateException("Curios player inventory missing");
            var handlerClass=Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
            Object optional=handlerClass.getMethod("getStacksHandler",String.class).invoke(inventory,"pickaxe");
            Object handler=((java.util.Optional<?>)optional).orElseThrow();
            var stacksHandler=Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler");
            Object stacks=stacksHandler.getMethod("getStacks").invoke(handler);
            var dynamic=Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler");
            if((Integer)dynamic.getMethod("getSlots").invoke(stacks)<1)throw new IllegalStateException("Curios pickaxe slot missing");
            dynamic.getMethod("setStackInSlot",int.class,ItemStack.class).invoke(stacks,0,stack);
        }catch(ReflectiveOperationException error){throw new IllegalStateException("Cannot populate real Curios validation slot",error);}
    }
    static boolean tick(ServerPlayer p){
        var data=p.getPersistentData();if(data.getBoolean("curioKeyDone"))return true;
        int now=p.server.getTickCount();
        if(!data.contains("curioKeyStarted")){
            WorkQueue.cancel(p);DomainFields.stop(p);IcarianFlight.stop(p);
            for(var cell:BlockPos.betweenClosed(TARGET.offset(-5,-5,-5),TARGET.offset(5,5,5))){p.serverLevel().setBlock(cell,Blocks.AIR.defaultBlockState(),2);PlayerPlacedBlocks.get(p.serverLevel()).unmark(cell);}
            p.serverLevel().setBlockAndUpdate(new BlockPos(200,99,200),Blocks.BEDROCK.defaultBlockState());
            p.serverLevel().setBlockAndUpdate(TARGET,Blocks.STONE.defaultBlockState());p.serverLevel().setBlockAndUpdate(PROBE,Blocks.STONE.defaultBlockState());
            p.connection.teleport(200.5,100,200.5,0,0);p.lookAt(EntityAnchorArgument.Anchor.EYES,Vec3.atCenterOf(TARGET));
            p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.SURVIVAL);p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Items.DIAMOND_PICKAXE));
            var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.WORLDBREAKER).get());source.getOrCreateTag().putUUID("controlIdentity",java.util.UUID.randomUUID());equip(p,source);
            ArtifactState.of(p,ArtifactKind.WORLDBREAKER).remove("curiosActiveReady");data.putInt("curioKeyStarted",now);return false;
        }
        int age=now-data.getInt("curioKeyStarted");
        if(age>400)throw new IllegalStateException("Physical equipped G key did not finish in time");
        if(age>=20&&!data.getBoolean("curioKeyReady")){
            var found=CuriosCompat.findFresh(p);if(found==null)throw new IllegalStateException("Real Curios lookup failed after slot setup");
            data.putBoolean("curioKeyReady",true);p.sendSystemMessage(Component.literal("CURIO_KEY_READY"));
        }
        if(ArtifactState.of(p,ArtifactKind.WORLDBREAKER).getLong("curiosActiveReady")==0||WorkQueue.busy(p))return false;
        var found=CuriosCompat.findFresh(p);
        if(found==null||found.stack().getDamageValue()!=8||!p.getMainHandItem().is(Items.DIAMOND_PICKAXE)
                ||!p.serverLevel().getBlockState(PROBE).isAir()||!p.serverLevel().getBlockState(TARGET).is(Blocks.STONE))
            throw new IllegalStateException("Equipped G failed source cost, held tool, core or mining probe assertion");
        equip(p,ItemStack.EMPTY);ArtifactPassives.onCuriosUnequip(p,ArtifactKind.WORLDBREAKER);
        data.putBoolean("curioKeyDone",true);p.gameMode.changeGameModeForPlayer(net.minecraft.world.level.GameType.CREATIVE);
        p.sendSystemMessage(Component.literal("CURIO_KEY_OK"));return true;
    }
}
