package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.artifact.RelicControl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Registers the mod's client key mappings on Forge's mod event bus. */
@Mod.EventBusSubscriber(modid="specialpickaxes",value=Dist.CLIENT,bus=Mod.EventBusSubscriber.Bus.MOD)
public final class RelicKeyRegistration {
    private RelicKeyRegistration() {}

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        for (var action : RelicControl.Action.values()) {
            if (action != RelicControl.Action.CONFIRM
                    && action != RelicControl.Action.PAUSE
                    && action != RelicControl.Action.SELECT
                    && action != RelicControl.Action.CANCEL) {
                event.register(RelicKeys.KEYS[action.ordinal()]);
            }
        }
        event.register(RelicKeys.MANUAL);
    }
}
