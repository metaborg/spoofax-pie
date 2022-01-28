package mb.spoofax.lwb.eclipse.dynamicloading;

import dagger.Binds;
import dagger.Module;
import mb.spoofax.lwb.dynamicloading.component.DynamicComponentLoader;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingScope;

@Module
public interface EclipseDynamicLoadingModule {
    @Binds @DynamicLoadingScope DynamicComponentLoader bindDynamicLanguageLoader(EclipseDynamicLanguageLoader impl);
}
