package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

/** Withdrawal-only vanilla chest screen. Display stacks are never transferred by vanilla clicks. */
public final class PickaxePouchMenu extends ChestMenu {
    private final ItemStack tool;
    private final SimpleContainer display;
    private int page;
    private PickaxePouchMenu(int id,Inventory inventory,ItemStack tool,SimpleContainer display){
        super(MenuType.GENERIC_9x3,id,inventory,display,3);this.tool=tool;this.display=display;refresh();
    }
    public static void open(ServerPlayer player,ItemStack tool){
        tool.getOrCreateTag().putBoolean("materialPouchEnabled",true);
        player.openMenu(new SimpleMenuProvider((id,inventory,p)->new PickaxePouchMenu(id,inventory,tool,new SimpleContainer(27)),Component.translatable("pouch.specialpickaxes.title")));
    }
    private void refresh(){
        int pages=Math.max(1,(PickaxePouch.types(tool)+25)/26);page=Math.min(page,pages-1);
        for(int i=0;i<26;i++)display.setItem(i,PickaxePouch.preview(tool,page*26+i));
        var next=new ItemStack(net.minecraft.world.item.Items.ARROW);
        next.setHoverName(Component.translatable("pouch.specialpickaxes.page",page+1,pages));display.setItem(26,next);broadcastChanges();
    }
    @Override public boolean stillValid(Player player){return player.isAlive()&&player.getMainHandItem()==tool&&!tool.isEmpty();}
    @Override public ItemStack quickMoveStack(Player player,int slot){return ItemStack.EMPTY;}
    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(!stillValid(player)||slot<0||slot>=27||button<0||button>1)return;
        if(type!=ClickType.PICKUP&&type!=ClickType.QUICK_MOVE)return;
        if(slot==26){page=(page+1)%Math.max(1,(PickaxePouch.types(tool)+25)/26);refresh();return;}
        var extracted=PickaxePouch.extract(tool,page*26+slot,button==1?1:64);
        if(!extracted.isEmpty()){
            player.getInventory().add(extracted);
            // Full inventory is not permission to drop or delete overflow.
            if(!extracted.isEmpty())PickaxePouch.insert(tool,extracted);
        }
        refresh();player.getInventory().setChanged();
    }
}
