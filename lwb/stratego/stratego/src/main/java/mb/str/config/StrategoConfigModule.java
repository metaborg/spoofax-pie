package mb.str.config;

import dagger.Module;
import dagger.Provides;
import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoScope;

import java.util.function.Function;

@Module
public class StrategoConfigModule {
    @Provides @StrategoScope
    public static Function<ResourcePath, StrategoAnalyzeConfig> provideDefaultAnalyzeConfigFunc() {
        return StrategoAnalyzeConfig::new;
    }
}
