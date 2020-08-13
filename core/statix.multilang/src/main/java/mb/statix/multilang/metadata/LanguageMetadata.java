package mb.statix.multilang.metadata;

import mb.common.result.Result;
import mb.pie.api.Function;
import mb.pie.api.Pie;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.HashSet;

@Value.Immutable
public interface LanguageMetadata {
    @Value.Parameter LanguageId languageId();

    // Use HashSet because should be serializable
    @Value.Parameter Function<ResourcePath, HashSet<ResourceKey>> resourcesSupplier();

    @Value.Parameter Function<ResourceKey, Result<IStrategoTerm, ?>> astFunction();

    @Value.Parameter Function<Supplier<? extends Result<IStrategoTerm, ?>>, Result<IStrategoTerm, ?>> postTransform();

    @Value.Parameter String fileConstraint();

    @Value.Parameter String projectConstraint();

    @Value.Parameter Pie languagePie();

    @Value.Parameter ITermFactory termFactory();
}
