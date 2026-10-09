package io.github.santipdr.specialpickaxes.artifact;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import java.lang.reflect.Method;
import java.util.Optional;

/** Optional Curios bridge. Reflection keeps the mod loadable when Curios is absent. */
public final class CuriosCompat {
    public record Equipped(ItemStack stack, int slot) {}
    private static final String API = "top.theillusivec4.curios.api.CuriosApi";
    private static final Class<?> API_CLASS = loadApi();
    private record Access(Method inventory,Method stacksHandler,Method stacks,Method slots,Method stackInSlot) {}
    private record Cached(long tick,Equipped equipped) {}
    private static final Access ACCESS = resolveAccess();
    /** Curios is queried by inventory ticks, combat events and the manual; cache repeated calls in one tick. */
    private static final java.util.Map<LivingEntity,Cached> FIND_CACHE=java.util.Collections.synchronizedMap(new java.util.WeakHashMap<>());
    private CuriosCompat() {}

    private static Class<?> loadApi(){
        try{return Class.forName(API,false,CuriosCompat.class.getClassLoader());}
        catch(ClassNotFoundException|LinkageError ignored){return null;}
    }

    private static Access resolveAccess(){
        if(API_CLASS==null)return null;
        try{
            ClassLoader loader=CuriosCompat.class.getClassLoader();
            Class<?> handler=Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler",false,loader);
            Class<?> stacksHandler=Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler",false,loader);
            Class<?> dynamicStacks=Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler",false,loader);
            return new Access(API_CLASS.getMethod("getCuriosInventory",LivingEntity.class),
                    handler.getMethod("getStacksHandler",String.class),stacksHandler.getMethod("getStacks"),
                    dynamicStacks.getMethod("getSlots"),dynamicStacks.getMethod("getStackInSlot",int.class));
        }catch(ReflectiveOperationException|LinkageError ignored){return null;}
    }

    public static Equipped find(LivingEntity entity) {
        Access methods=ACCESS;if(methods==null)return null;
        long tick=entity.tickCount;
        Cached cached=FIND_CACHE.get(entity);
        if(cached!=null&&cached.tick()==tick)return cached.equipped();
        Equipped equipped=findUncached(entity,methods);
        FIND_CACHE.put(entity,new Cached(tick,equipped));
        return equipped;
    }

    private static Equipped findUncached(LivingEntity entity,Access methods) {
        try {
            Object lazy = methods.inventory.invoke(null, entity);
            Object handler = lazy == null ? null : ((net.minecraftforge.common.util.LazyOptional<?>) lazy).orElse(null);
            if (handler == null) return null;
            Object stacksOptional = methods.stacksHandler.invoke(handler, "pickaxe");
            Object stacksHandler = optionalValue(stacksOptional);
            if (stacksHandler == null) return null;
            Object stacks = methods.stacks.invoke(stacksHandler);
            int count = (int) methods.slots.invoke(stacks);
            for (int slot = 0; slot < count; slot++) {
                Object value = methods.stackInSlot.invoke(stacks, slot);
                if (value instanceof ItemStack stack && stack.getItem() instanceof ArtifactItem) return new Equipped(stack, slot);
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // Curios is optional. Missing API or a changed slot simply disables this bridge.
        }
        return null;
    }

    private static Object optionalValue(Object optional) throws ReflectiveOperationException {
        if (optional instanceof Optional<?> value) return value.orElse(null);
        if (optional == null) return null;
        return optional.getClass().getMethod("orElse", Object.class).invoke(optional, new Object[]{null});
    }
}
