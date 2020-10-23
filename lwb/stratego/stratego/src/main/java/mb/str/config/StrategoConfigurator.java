package mb.str.config;

import mb.resource.hierarchical.ResourcePath;
import mb.str.StrategoScope;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.function.Function;

@StrategoScope
public class StrategoConfigurator {
    private final HashMap<ResourcePath, StrategoAnalyzeConfig> analyzeConfigs = new HashMap<>();
    private final Function<ResourcePath, StrategoAnalyzeConfig> defaultAnalyzeConfigFunc;

    @Inject public StrategoConfigurator(
        Function<ResourcePath, StrategoAnalyzeConfig> defaultAnalyzeConfigFunc
    ) {
        this.defaultAnalyzeConfigFunc = defaultAnalyzeConfigFunc;
    }

    public void setAnalyzeConfig(ResourcePath rootDirectory, StrategoAnalyzeConfig config) {
        final @Nullable StrategoAnalyzeConfig existingConfig = analyzeConfigs.get(rootDirectory);
        if(existingConfig != null && !existingConfig.equals(config)) {
            throw new RuntimeException("Cannot change analyze config once it is set, until we implement change detection for it in the PIE tasks");
        }
        analyzeConfigs.put(rootDirectory, config);
    }

    public StrategoAnalyzeConfig getAnalyzeConfig(ResourcePath rootDirectory) {
        @Nullable StrategoAnalyzeConfig config = analyzeConfigs.get(rootDirectory);
        if(config == null) {
            config = defaultAnalyzeConfigFunc.apply(rootDirectory);
            analyzeConfigs.put(rootDirectory, config);
        }
        return config;
    }
}
