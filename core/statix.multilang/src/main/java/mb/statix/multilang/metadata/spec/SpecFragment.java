package mb.statix.multilang.metadata.spec;

import mb.common.result.Result;
import mb.statix.multilang.metadata.SpecFragmentId;
import mb.statix.spec.Spec;
import org.immutables.value.Value;
import org.metaborg.util.functions.Function1;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A partial specification (for which dependent specifications are not resolved yet).
 * Usually, there is one SpecFragment per language (or interface).
 */
@Value.Immutable
public interface SpecFragment {

    @Value.Parameter SpecFragmentId id();

    @Value.Parameter Set<Module> modules();

    /**
     * Names of the modules that were resolved when the fragment was loaded, and should be supplied by other fragments.
     */
    @Value.Parameter Set<String> delayedModuleNames();

    default Set<String> providedModuleNames() {
        return modules().stream().map(Module::moduleName).collect(Collectors.toSet());
    }

    @Value.Check default void checkNotEmpty() {
        if(modules().isEmpty()) {
            throw new IllegalStateException("At least one module should be provided");
        }
    }

    default Stream<Map.Entry<String, String>> qualifiedLabels() {
        return modules().stream().flatMap(mod -> mod.qualifiedLabels(id().getId()));
    }

    default Result<Spec, SpecLoadException> load(Function1<String, String> renameFunc) {
        return modules()
            .stream()
            .map(module -> module.load(renameFunc))
            // Merge without overlapping names check
            .reduce(SpecUtils::mergeSpecs)
            // Cannot occur when check holds
            .orElse(Result.ofErr(new SpecLoadException("Bug: No modules in spec fragment")));
    }
}
