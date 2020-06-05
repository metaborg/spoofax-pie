package mb.statix.common.context;

import mb.pie.api.Function;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.statix.spec.Spec;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashSet;

@Value.Immutable
@Value.Style(typeImmutable = "A*")
public interface LanguageMetadata {
    String languageId();
    Supplier<HashSet<ResourceKey>> resourcesSupplier(); // Use HashSet because should be serializable
    Function<ResourceKey, IStrategoTerm> astFunction();
    Spec statixSpec();
    String fileConstraint();
    String projectConstraint();

    static ALanguageMetadata.Builder builder() {
        return ALanguageMetadata.builder();
    }
}
