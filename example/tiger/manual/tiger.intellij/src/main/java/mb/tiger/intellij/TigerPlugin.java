package mb.tiger.intellij;

import com.intellij.openapi.extensions.PluginId;
import mb.spoofax.intellij.SpoofaxPlugin;
import mb.tiger.spoofax.TigerModule;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The Tiger language plugin.
 */
public final class TigerPlugin {
    private static @Nullable TigerIntellijComponent component;

    public static TigerIntellijComponent getComponent() {
        if(component == null) {
            throw new RuntimeException("Cannot access TigerComponent; TigerPlugin has not been started yet, or has been stopped");
        }
        return component;
    }

    public static void init() {
        component = DaggerTigerIntellijComponent
            .builder()
            .spoofaxIntellijComponent(SpoofaxPlugin.getComponent())
            .tigerModule(new TigerModule())
            .tigerIntellijModule(new TigerIntellijModule())
            .build();
    }

    /**
     * Gets the Plugin ID of this plugin.
     *
     * @return The {@link PluginId}.
     */
    public static PluginId getId() {
        return PluginId.getId("tiger");
    }
}
