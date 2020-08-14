package mb.statix.multilang.metadata;

import mb.spoofax.core.language.LanguageComponent;
import mb.statix.multilang.metadata.spec.SpecConfig;

import java.util.Map;

/**
 * Interface that should be implemented together with {@link LanguageComponent}
 */
public interface LanguageMetadataProvider {
    LanguageMetadata getLanguageMetadata();

    Map<SpecFragmentId, SpecConfig> getSpecConfigs();
}
