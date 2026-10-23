package io.github.santipdr.specialpickaxes.client;

import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only particle factories; kept off the dedicated server class path. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SpecialParticleRenderers {
    private SpecialParticleRenderers(){}

    @SubscribeEvent public static void register(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(SpecialPickaxes.STASIS_SHARD.get(),StasisShardParticle.Provider::new);
    }
}
