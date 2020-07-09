package mb.statix.multilang;

import mb.common.option.Option;
import mb.pie.api.Function;
import mb.pie.api.Pie;
import mb.pie.api.Supplier;
import mb.resource.ResourceKey;
import mb.resource.hierarchical.ResourcePath;
import mb.statix.multilang.spec.SpecBuilder;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.HashSet;

@Value.Immutable
public interface LanguageMetadata {
    @Value.Parameter LanguageId languageId();

    @Value.Parameter
    Function<ResourcePath, HashSet<ResourceKey>> resourcesSupplier(); // Use HashSet because should be serializable

    @Value.Parameter Function<ResourceKey, Option<IStrategoTerm>> astFunction();

    @Value.Parameter Function<Supplier<Option<IStrategoTerm>>, Option<IStrategoTerm>> postTransform();

    @Value.Parameter SpecBuilder statixSpec();

    @Value.Parameter String fileConstraint();

    @Value.Parameter String projectConstraint();

    @Value.Parameter Pie languagePie();

    @Value.Parameter ITermFactory termFactory();
}
