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
    private CuriosCompat() {}

    private static Class<?> loadApi(){
        try{return Class.forName(API,false,CuriosCompat.class.getClassLoader());}
        catch(ClassNotFoundException|LinkageError ignored){return null;}
    }

    public static Equipped find(LivingEntity entity) {
        try {
            Class<?> api = API_CLASS;if(api==null)return null;
            Object lazy = api.getMethod("getCuriosInventory", LivingEntity.class).invoke(null, entity);
            Object handler = lazy == null ? null : ((net.minecraftforge.common.util.LazyOptional<?>) lazy).orElse(null);
            if (handler == null) return null;
            Class<?> handlerApi = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler", false, CuriosCompat.class.getClassLoader());
            Object stacksOptional = handlerApi.getMethod("getStacksHandler", String.class).invoke(handler, "pickaxe");
            Object stacksHandler = optionalValue(stacksOptional);
            if (stacksHandler == null) return null;
            Class<?> stacksApi = Class.forName("top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler", false, CuriosCompat.class.getClassLoader());
            Object stacks = stacksApi.getMethod("getStacks").invoke(stacksHandler);
            Class<?> dynamicApi = Class.forName("top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler", false, CuriosCompat.class.getClassLoader());
            int count = (int) dynamicApi.getMethod("getSlots").invoke(stacks);
            Method get = dynamicApi.getMethod("getStackInSlot", int.class);
            for (int slot = 0; slot < count; slot++) {
                Object value = get.invoke(stacks, slot);
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
