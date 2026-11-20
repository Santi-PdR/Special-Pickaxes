package io.github.santipdr.specialpickaxes.test;

import com.electronwill.nightconfig.core.CommentedConfig;
import io.github.santipdr.specialpickaxes.SpecialPickaxes;
import io.github.santipdr.specialpickaxes.artifact.ArtifactConfig;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Disposable validation overrides must not race NightConfig's live file watcher. Excluded from release JAR. */
@Mod.EventBusSubscriber(modid=SpecialPickaxes.ID)
public final class ValidationConfigIsolation {
    private ValidationConfigIsolation(){}
    @SubscribeEvent public static void started(ServerStartedEvent event){
        if(!Boolean.getBoolean("specialpickaxes.testConfig")&&!Boolean.getBoolean("specialpickaxes.clientSmoke"))return;
        var config=CommentedConfig.inMemory();
        ArtifactConfig.SPEC.correct(config);
        ArtifactConfig.SPEC.setConfig(config);
    }
}
