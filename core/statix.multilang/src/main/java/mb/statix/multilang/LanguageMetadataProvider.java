package mb.statix.multilang;

import mb.spoofax.core.language.LanguageComponent;

/**
 * Interface that should be implemented together with {@link LanguageComponent}
 */
public interface LanguageMetadataProvider {
    LanguageMetadata getLanguageMetadata();
}
