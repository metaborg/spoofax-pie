package mb.statix.multilang.metadata.spec;

import mb.resource.hierarchical.HierarchicalResource;
import mb.statix.multilang.metadata.SpecFragmentId;
import org.immutables.value.Value;
import org.spoofax.interpreter.terms.ITermFactory;

import java.util.Set;

@Value.Immutable
public interface SpecConfig {

    @Value.Parameter HierarchicalResource rootPackage();

    @Value.Parameter Set<SpecFragmentId> dependencies();

    @Value.Parameter Set<String> rootModules();

    @Value.Parameter ITermFactory termFactory();

    @Value.Check default void rootModulesNonEmpty() {
        if(rootModules().isEmpty()) {
            throw new IllegalStateException("At least one root module must be given to the loader");
        }
    }
}
