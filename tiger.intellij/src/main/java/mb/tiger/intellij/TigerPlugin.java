package mb.tiger.intellij;

import mb.spoofax.intellij.SpoofaxPlugin;
import mb.tiger.spoofax.DaggerTigerComponent;
import mb.tiger.spoofax.TigerModule;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TigerPlugin {
    private static @Nullable TigerIntellijComponent component;

    public static TigerIntellijComponent getComponent() {
        if(component == null) {
            throw new RuntimeException(
                "Cannot access TigerComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    public static void init() throws Exception {
        component = DaggerTigerIntellijComponent
            .builder()
            .platformComponent(SpoofaxPlugin.getComponent())
            .tigerModule(TigerModule.fromClassLoaderResources())
            .tigerIntellijModule(new TigerIntellijModule())
            .build();
    }
}
