package mb.statix.multilang.metadata;

import mb.spoofax.core.language.LanguageComponent;
import mb.statix.multilang.metadata.spec.SpecConfig;

import java.util.Map;

/**
 * Interface that should be implemented together with {@link LanguageComponent}
 */
public interface LanguageMetadataProvider {
    LanguageMetadata getLanguageMetadata();

    // Provides config of own spec + spec of all dependencies
    // Dependencies are included so that it is still possible to register them without generating plugins for each
    // It is provided via a component so that the term factory it depends on can be injected properly
    Map<SpecFragmentId, SpecConfig> getSpecConfigs();
}
