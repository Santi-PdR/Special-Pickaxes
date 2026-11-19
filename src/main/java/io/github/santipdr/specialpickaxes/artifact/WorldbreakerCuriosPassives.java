package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.function.Supplier;

/** Source-specific passive dispatch with a synchronous original-slot/source lease. */
public final class WorldbreakerCuriosPassives {
    private WorldbreakerCuriosPassives(){}
    private record Scope(ServerPlayer player,ItemStack stack,ArtifactKind source,Supplier<ItemStack> current){
        boolean active(){return current.get()==stack&&!stack.isEmpty()&&sourceKind(stack)==source;}
    }
    private static final ThreadLocal<Scope> SCOPE=new ThreadLocal<>();
    public static ArtifactKind sourceKind(ItemStack stack){
        if(stack.isEmpty()||!(stack.getItem() instanceof ArtifactItem item))return null;
        if(item.kind!=ArtifactKind.WORLDBREAKER||!stack.hasTag()||!stack.getTag().contains("copiedSkill"))return item.kind;
        try{var source=ArtifactKind.byId(stack.getTag().getString("copiedSkill"));return source.playable()&&source!=ArtifactKind.WORLDBREAKER?source:null;}
        catch(IllegalArgumentException invalid){return null;}
    }
    static boolean valid(ServerPlayer player){var scope=SCOPE.get();return scope==null||scope.player()!=player||scope.active();}
    private static void run(ServerPlayer p,ItemStack stack,Supplier<ItemStack> current,java.util.function.Consumer<ArtifactKind> action){
        var source=sourceKind(stack);if(source==null||SCOPE.get()!=null)return;
        var scope=new Scope(p,stack,source,current);if(!scope.active())return;
        SCOPE.set(scope);
        try{action.accept(source);}finally{
            SCOPE.remove();
            if(!scope.active())ArtifactPassives.onCuriosUnequip(p,source);
        }
    }
    public static void tick(ServerPlayer p,ItemStack stack,Supplier<ItemStack> current){run(p,stack,current,source->ArtifactPassives.tick(p,source));}
    public static void hurt(ServerPlayer p,ItemStack stack,Supplier<ItemStack> current,net.minecraftforge.event.entity.living.LivingHurtEvent event,boolean shared){
        run(p,stack,current,source->ArtifactPassives.onCuriosDamage(p,event,source,shared));
    }
}
