package mb.spoofax.cmd;

import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import mb.resource.ResourceRegistry;
import mb.resource.string.StringResourceRegistry;
import mb.spoofax.core.platform.ResourceRegistryScope;

@Module
public class SpoofaxCmdResourceRegistryModule {
    private final StringResourceRegistry stringResourceRegistry = new StringResourceRegistry();

    @Provides @ResourceRegistryScope StringResourceRegistry provideStringRegistry() {
        return stringResourceRegistry;
    }

    @Provides @ResourceRegistryScope @IntoSet ResourceRegistry provideStringRegistryIntoSet() {
        return stringResourceRegistry;
    }
}
