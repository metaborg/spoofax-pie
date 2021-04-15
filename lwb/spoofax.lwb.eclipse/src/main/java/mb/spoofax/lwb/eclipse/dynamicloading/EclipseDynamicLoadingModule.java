package mb.spoofax.lwb.eclipse.dynamicloading;

import dagger.Binds;
import dagger.Module;
import mb.spoofax.lwb.dynamicloading.DefaultDynamicLanguageLoader;
import mb.spoofax.lwb.dynamicloading.DynamicLanguageLoader;
import mb.spoofax.lwb.dynamicloading.DynamicLoadingScope;

@Module
public interface EclipseDynamicLoadingModule {
    @Binds @DynamicLoadingScope DynamicLanguageLoader bindDynamicLanguageLoader(EclipseDynamicLanguageLoader impl);
}
