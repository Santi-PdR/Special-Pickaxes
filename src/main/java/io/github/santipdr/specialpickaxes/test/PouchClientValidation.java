package io.github.santipdr.specialpickaxes.test;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;

/** Requires physical sneak/right-click and Escape, not a synthetic menu-open call. */
final class PouchClientValidation {
    static boolean tick(ServerPlayer p){
        var data=p.getPersistentData();if(data.getBoolean("pouchClientDone"))return true;
        long now=ArtifactState.now(p);
        if(!data.contains("pouchClientStarted")){
            WorkQueue.cancel(p);IcarianFlight.stop(p);
            var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());
            source.getOrCreateTag().putUUID("controlIdentity",java.util.UUID.randomUUID());
            PickaxePouch.insert(source,new ItemStack(Items.DIAMOND,17));
            p.setItemInHand(InteractionHand.MAIN_HAND,source);p.connection.teleport(200.5,100,200.5,0,-90);
            data.putLong("pouchClientStarted",now);p.sendSystemMessage(Component.literal("POUCH_MENU_READY"));return false;
        }
        if(now-data.getLong("pouchClientStarted")>300)throw new IllegalStateException("Physical pouch interaction timed out");
        if(!data.getBoolean("pouchClientOpened")){
            if(!(p.containerMenu instanceof PickaxePouchMenu))return false;
            if(!p.getMainHandItem().getOrCreateTag().getBoolean("materialPouchEnabled")||PickaxePouch.preview(p.getMainHandItem(),0).getCount()!=17)
                throw new IllegalStateException("Pouch menu did not preserve material fixture or enable capture");
            data.putBoolean("pouchClientOpened",true);p.sendSystemMessage(Component.literal("POUCH_MENU_OPEN"));return false;
        }
        if(p.containerMenu instanceof PickaxePouchMenu)return false;
        data.putBoolean("pouchClientDone",true);p.sendSystemMessage(Component.literal("POUCH_MENU_OK"));return true;
    }
}
