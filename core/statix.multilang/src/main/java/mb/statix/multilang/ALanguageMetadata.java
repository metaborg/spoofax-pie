package mb.statix.multilang;

import mb.pie.api.Function;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.spec.Spec;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;

import java.util.HashSet;

@Value.Immutable
public abstract class ALanguageMetadata {
    @Value.Parameter public abstract LanguageId languageId();
    @Value.Parameter public abstract Function<ResourcePath, HashSet<ResourceKey>> resourcesSupplier(); // Use HashSet because should be serializable
    @Value.Parameter public abstract Function<ResourceKey, IStrategoTerm> astFunction();
    @Value.Parameter public abstract Function<IStrategoTerm, IStrategoTerm> postTransform();
    @Value.Parameter public abstract Spec statixSpec();
    @Value.Parameter public abstract String fileConstraint();
    @Value.Parameter public abstract String projectConstraint();
}
