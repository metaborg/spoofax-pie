package mb.spoofax.lwb.dynamicloading;

import dagger.Binds;
import dagger.Module;

@Module
public interface DynamicLoadingModule {
    @Binds @DynamicLoadingScope DynamicLanguageLoader bindDynamicLanguageLoader(DefaultDynamicLanguageLoader impl);
}
