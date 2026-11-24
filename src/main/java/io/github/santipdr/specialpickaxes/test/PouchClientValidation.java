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
        try{return tickWithFiles(p);}catch(java.io.IOException error){throw new IllegalStateException("Pouch client handshake failed",error);}
    }
    private static java.nio.file.Path flag(String name){return java.nio.file.Path.of("client-pouch-"+name+".flag");}
    private static boolean tickWithFiles(ServerPlayer p)throws java.io.IOException{
        var data=p.getPersistentData();if(data.getBoolean("pouchClientDone"))return true;
        long now=ArtifactState.now(p);
        if(!data.contains("pouchClientStarted")){
            WorkQueue.cancel(p);IcarianFlight.stop(p);
            for(var name:new String[]{"initial-captured","live-request","live-ack"})java.nio.file.Files.deleteIfExists(flag(name));
            var source=new ItemStack(SpecialPickaxes.PICKS.get(ArtifactKind.EVENTIDE).get());
            source.getOrCreateTag().putUUID("controlIdentity",java.util.UUID.randomUUID());
            PickaxePouch.insert(source,new ItemStack(Items.DIAMOND,17));
            p.setItemInHand(InteractionHand.MAIN_HAND,source);p.connection.teleport(200.5,100,200.5,0,-90);
            data.putLong("pouchClientStarted",now);p.sendSystemMessage(Component.literal("POUCH_MENU_READY"));return false;
        }
        if(now-data.getLong("pouchClientStarted")>600)throw new IllegalStateException("Physical pouch interaction timed out");
        if(!data.getBoolean("pouchClientOpened")){
            if(!(p.containerMenu instanceof PickaxePouchMenu))return false;
            if(!p.getMainHandItem().getOrCreateTag().getBoolean("materialPouchEnabled")||PickaxePouch.preview(p.getMainHandItem(),0).getCount()!=17)
                throw new IllegalStateException("Pouch menu did not preserve material fixture or enable capture");
            data.putBoolean("pouchClientOpened",true);p.sendSystemMessage(Component.literal("POUCH_MENU_OPEN"));return false;
        }
        if(!data.getBoolean("pouchClientUpdated")){
            if(!(p.containerMenu instanceof PickaxePouchMenu))throw new IllegalStateException("Pouch closed before live-update verification");
            if(!data.getBoolean("pouchClientUpdateSent")){
                if(!java.nio.file.Files.exists(flag("initial-captured")))return false;
                var source=p.getMainHandItem();
                if(!PickaxePouch.insert(source,new ItemStack(Items.DIAMOND,3)).isEmpty()
                        ||!PickaxePouch.insert(source,new ItemStack(Items.GOLD_INGOT,5)).isEmpty())
                    throw new IllegalStateException("Pouch live fixture failed storage insertion");
                java.nio.file.Files.writeString(flag("live-request"),"20,5");data.putBoolean("pouchClientUpdateSent",true);
            }
            if(!java.nio.file.Files.exists(flag("live-ack")))return false;
            if(!java.nio.file.Files.readString(flag("live-ack")).equals("20,5"))throw new IllegalStateException("Invalid pouch render acknowledgement");
            data.putBoolean("pouchClientUpdated",true);p.sendSystemMessage(Component.literal("POUCH_MENU_UPDATED"));return false;
        }
        if(p.containerMenu instanceof PickaxePouchMenu)return false;
        if(PickaxePouch.amount(p.getMainHandItem(),0)!=20||PickaxePouch.amount(p.getMainHandItem(),1)!=5||!p.containerMenu.getCarried().isEmpty())
            throw new IllegalStateException("Pouch live update changed stored quantities or cursor");
        for(var name:new String[]{"initial-captured","live-request","live-ack"})java.nio.file.Files.deleteIfExists(flag(name));
        data.putBoolean("pouchClientDone",true);p.sendSystemMessage(Component.literal("POUCH_MENU_OK"));return true;
    }
}
