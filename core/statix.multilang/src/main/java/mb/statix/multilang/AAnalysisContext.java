package mb.statix.multilang;

import org.immutables.value.Value;

import java.io.Serializable;
import java.util.Map;

@Value.Immutable
public abstract class AAnalysisContext implements Serializable {

    @Value.Parameter public abstract String contextId();
    @Value.Parameter public abstract Map<LanguageId, LanguageMetadata> languages();

    // TODO: Good hashing/equals for incrementality
}
